package in.workarounds.autoprovider.compiler.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import in.workarounds.autoprovider.compiler.AnnotatedColumn;
import in.workarounds.autoprovider.compiler.AnnotatedTable;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

import static in.workarounds.autoprovider.compiler.utils.ClassUtils.ABSTRACT_CURSOR;
import static in.workarounds.autoprovider.compiler.utils.ClassUtils.CURSOR;
import static in.workarounds.autoprovider.compiler.utils.ClassUtils.NONNULL;

/**
 * Created by mouli on 10/22/15.
 */
public class CursorGenerator {

    private final String mGetObject = "getObject";

    private final String ERROR_MESSAGE_NULL = "The value of '%s' in the database was null, which is not allowed according to the model definition";

    private final MethodSpec CONSTRUCTOR;
    List<MethodSpec> methods = new ArrayList<>();
    private final MethodSpec GET_OBJECT;

    public CursorGenerator(AnnotatedTable annotatedTable) {

        CONSTRUCTOR = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CURSOR, "cursor")
                .addStatement("super(cursor)")
                .build();

        for(AnnotatedColumn annotatedColumn: annotatedTable.getColumns()) {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(String.format("get%s",
                    StringUtils.toCamelCase(annotatedColumn.getColumnName())))
                    .addAnnotation(NONNULL)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(annotatedColumn.getTypeInObject()))
                    .addCode(getColumnInitializer(annotatedTable.getTableName(), annotatedColumn));

            if(annotatedColumn.isNotNull()) {
                String nullMessage = String.format(ERROR_MESSAGE_NULL, annotatedColumn.getColumnName());
                builder.beginControlFlow("if( res == null) ")
                        .addStatement("throw new NullPointerException($S)", nullMessage)
                        .endControlFlow();
            }
            builder.addStatement("return res");
            methods.add(builder.build());
        }

        GET_OBJECT = MethodSpec.methodBuilder(mGetObject)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(annotatedTable.getAnnotatedClassElement().asType()))
                .addCode(getObjectCode(annotatedTable))
                .build();

    }

    private CodeBlock getColumnInitializer(String tableName, AnnotatedColumn annotatedColumn) {
        String typeInObject = annotatedColumn.getTypeInObject().toString();
        String splits[] = typeInObject.split("\\.");
        String simpleTypeName = splits[splits.length-1];
        CodeBlock codeBlock = CodeBlock.builder()
                .add("$T res = get$LOrNull($L.$L);\n", annotatedColumn.getTypeInObject(),
                        StringUtils.toCamelCase(simpleTypeName), tableName, annotatedColumn.getColumnName().toUpperCase())
                .build();
        return codeBlock;
    }

    private CodeBlock getObjectCode(AnnotatedTable annotatedTable) {
        TypeMirror object = annotatedTable.getAnnotatedClassElement().asType();
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("$T item = new $T();\n", object, object);
        for(AnnotatedColumn annotatedColumn: annotatedTable.getColumns()) {
            String columnName = annotatedColumn.getColumnName();
            builder.add("item.$L = get$L();\n", columnName, StringUtils.toCamelCase(columnName));
        }
        builder.add("return item;\n");
        return builder.build();
    }

    public JavaFile generateCursor(String outputPackage, String outputName) {
        TypeSpec outputTable = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputTable).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ABSTRACT_CURSOR);

        builder.addMethod(CONSTRUCTOR);
        builder.addMethods(methods);
        builder.addMethod(GET_OBJECT);

        return builder.build();
    }
}
