package in.workarounds.autoprovider.compiler.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.AnnotatedColumn;
import in.workarounds.autoprovider.compiler.AnnotatedTable;
import in.workarounds.autoprovider.compiler.ProviderProcessor;
import in.workarounds.autoprovider.compiler.utils.ClassUtils;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

/**
 * Created by mouli on 11/6/15.
 */
public class ValuesGenerator {

    private final String PREFIX_PUT = "put";
    private final String SUFFIX_NULL = "Null";

    //This variable name is of the global variable of ContentValues in AbstractValues.
    //Change this whenever the former is changed
    private final String nContentValues = "mContentValues";

    private final String nUri = "uri";
    private final String nUpdate = "update";
    private final String nPutObject = "putObject";

    private final MethodSpec URI;
    private final MethodSpec UPDATE1;
    private final MethodSpec UPDATE2;
    private final MethodSpec PUT_OBJECT;

    private final List<MethodSpec> PUT_METHODS = new ArrayList<>();
    public ValuesGenerator(AnnotatedTable annotatedTable) {

        URI = MethodSpec.methodBuilder(nUri)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassUtils.URI)
                .addStatement("return $L.$L", annotatedTable.getTableName(), TableGenerator.mContentUri)
                .build();

        String paramContentResolver = "contentResolver";
        String paramContext = "context";
        String paramWhere = "where";
        ClassName classSelection = ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName());
        UPDATE1 = MethodSpec.methodBuilder(nUpdate)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTENT_RESOLVER, paramContentResolver)
                .addParameter(ParameterSpec.builder(
                        classSelection,
                        paramWhere).addAnnotation(ClassUtils.NULLABLE).build())
                .returns(int.class)
                .addStatement("return $L.update(uri(), values(), $L == null ? null : $L.sel(), $L == null ? null : $L.args())",
                        paramContentResolver,
                        paramWhere,
                        paramWhere,
                        paramWhere,
                        paramWhere)
                .build();

        UPDATE2 = MethodSpec.methodBuilder(nUpdate)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTEXT, paramContext)
                .addParameter(ParameterSpec.builder(
                        classSelection,
                        paramWhere).addAnnotation(ClassUtils.NULLABLE).build())
                .returns(int.class)
                .addStatement("return $L.getContentResolver().update(uri(), values(), $L == null ? null : $L.sel(), $L == null ? null : $L.args())",
                        paramContext,
                        paramWhere,
                        paramWhere,
                        paramWhere,
                        paramWhere)
                .build();

        String paramValue = "value";
        ClassName classContentValues = ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getValuesName());
        for(AnnotatedColumn column : annotatedTable.getColumns()) {
            MethodSpec.Builder putMethodBuilder = MethodSpec.methodBuilder(PREFIX_PUT + StringUtils.toCamelCase(column.getColumnName()))
                    .addModifiers(Modifier.PUBLIC);

            boolean isNotNull = column.isNotNull();
            putMethodBuilder.addParameter(ParameterSpec.builder(ClassName.get(column.getTypeInObject()), paramValue)
                    .addAnnotation(isNotNull ? ClassUtils.NONNULL : ClassUtils.NULLABLE).build())
                    .returns(classContentValues);

            if(isNotNull) {
                putMethodBuilder.beginControlFlow("if ($L == null) ", paramValue)
                        .addStatement("throw new $T($S)", IllegalStateException.class,
                                String.format("%s must not be null", column.getColumnName()))
                        .endControlFlow();
            }
            putMethodBuilder.addStatement("$L.put($L.$L, $L)", nContentValues, annotatedTable.getTableName(),
                    column.getColumnName().toUpperCase(), paramValue)
                    .addStatement("return this");
            PUT_METHODS.add(putMethodBuilder.build());

            if(!isNotNull) {
                MethodSpec.Builder nullMethodBuilder = MethodSpec.
                        methodBuilder(PREFIX_PUT+StringUtils.toCamelCase(column.getColumnName())+SUFFIX_NULL)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(classContentValues)
                        .addStatement("$L.putNull($L.$L)", nContentValues, annotatedTable.getTableName(),
                                column.getColumnName().toUpperCase())
                        .addStatement("return this");

                PUT_METHODS.add(nullMethodBuilder.build());
            }
        }

        String paramObject = "object";
        PUT_OBJECT = MethodSpec.methodBuilder(nPutObject)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(annotatedTable.getAnnotatedClassElement().asType()), paramObject)
                .returns(classContentValues)
                .addCode(getPutObjectCode(annotatedTable, paramObject))
                .addStatement("return this")
                .build();
    }

    private CodeBlock getPutObjectCode(AnnotatedTable annotatedTable, String paramObject) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for(AnnotatedColumn column: annotatedTable.getColumns()) {
            builder.addStatement("put$L($L.$L)", StringUtils.toCamelCase(column.getColumnName()),
                    paramObject, column.getColumnName());
        }
        return builder.build();
    }

    public JavaFile generateValues(String outputPackage, String outputName) {
        TypeSpec outputValues = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputValues).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassUtils.ABSTRACT_VALUES);

        builder.addMethod(URI);
        builder.addMethod(UPDATE1);
        builder.addMethod(UPDATE2);
        builder.addMethods(PUT_METHODS);
        builder.addMethod(PUT_OBJECT);

        return builder.build();
    }

}
