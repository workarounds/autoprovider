package in.workarounds.autoprovider.compiler.generator;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.AnnotatedColumn;
import in.workarounds.autoprovider.compiler.AnnotatedTable;
import in.workarounds.autoprovider.compiler.ProviderProcessor;
import in.workarounds.autoprovider.compiler.utils.ClassUtils;
import in.workarounds.autoprovider.compiler.utils.StringUtils;
import in.workarounds.autoprovider.compiler.utils.TypeMatcher;

/**
 * Created by mouli on 11/2/15.
 */
public class SelectorGenerator {

    private static final String PREFIX_ORDER_BY = "orderBy";

    private static final String SUFFIX_NOT = "Not";
    private static final String SUFFIX_LIKE = "Like";
    private static final String SUFFIX_CONTAINS = "Contains";
    private static final String SUFFIX_STARTS_WITH = "StartsWith";
    private static final String SUFFIX_ENDS_WITH = "EndsWith";
    private static final String SUFFIX_GT = "Gt";
    private static final String SUFFIX_GT_EQ = "GtEq";
    private static final String SUFFIX_LT = "Lt";
    private static final String SUFFIX_LT_EQ = "LtEq";

    private static final String nBaseUri = "baseUri";
    private static final String nQuery = "query";
    
    private final MethodSpec BASE_URI;
    private final MethodSpec QUERY1;
    private final MethodSpec QUERY2;
    private final MethodSpec QUERY3;
    private final MethodSpec QUERY4;

    private List<MethodSpec> SELECTION_METHODS = new ArrayList<>();

    public SelectorGenerator(AnnotatedTable annotatedTable) {
        
        BASE_URI = MethodSpec.methodBuilder(nBaseUri)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .returns(ClassUtils.URI)
                .addStatement("return $L.$L", StringUtils.toCamelCase(annotatedTable.getTableName()), TableGenerator.mContentUri)
                .build();

        String paramContentResolver = "contentResolver";
        String paramProjection = "projection";
        String paramCursor = "cursor";
        ClassName MODEL_CURSOR = ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getCursorName());
        QUERY1 = MethodSpec.methodBuilder(nQuery)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTENT_RESOLVER, paramContentResolver)
                .addParameter(ArrayTypeName.of(String.class), paramProjection)
                .returns(MODEL_CURSOR)
                .addStatement("$T $L = $L.query(uri(), $L, sel(), args(), order())", ClassUtils.CURSOR, paramCursor,
                        paramContentResolver, paramProjection)
                .beginControlFlow("if ($L == null)", paramCursor)
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return new $L($L)", MODEL_CURSOR,
                        paramCursor)
                .build();

        QUERY2 = MethodSpec.methodBuilder(nQuery)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTENT_RESOLVER, paramContentResolver)
                .returns(MODEL_CURSOR)
                .addStatement("return $L($L, null)", nQuery, paramContentResolver)
                .build();

        String paramContext = "context";
        QUERY3 = MethodSpec.methodBuilder(nQuery)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTEXT, paramContext)
                .addParameter(ArrayTypeName.of(String.class), paramProjection)
                .returns(MODEL_CURSOR)
                .addStatement("return $L($L.getContentResolver(), $L)", nQuery, paramContext, paramProjection)
                .build();

        QUERY4 = MethodSpec.methodBuilder(nQuery)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtils.CONTEXT, paramContext)
                .returns(MODEL_CURSOR)
                .addStatement("return $L($L, null)", nQuery, paramContext)
                .build();

        String paramValue = "value";
        ClassName TABLE = ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, StringUtils.toCamelCase(annotatedTable.getTableName()));
        for(AnnotatedColumn column : annotatedTable.getColumns()) {
            if(column.getTypeInDb()== TypeMatcher.SQLiteType.TEXT) {
                System.out.println("########### " + "equals should have been called");
                MethodSpec EQUALS = MethodSpec.methodBuilder(column.getColumnName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addEquals($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();
                System.out.println("########### " + "equals was called");

                MethodSpec NOT_EQUALS = MethodSpec.methodBuilder(column.getColumnName() + SUFFIX_NOT)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addNotEquals($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec LIKE = MethodSpec.methodBuilder(column.getColumnName() + SUFFIX_LIKE)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addLike($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec CONTAINS = MethodSpec.methodBuilder(column.getColumnName() + SUFFIX_CONTAINS)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addContains($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec STARTS_WITH = MethodSpec.methodBuilder(column.getColumnName() + SUFFIX_STARTS_WITH)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addStartsWith($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec ENDS_WITH = MethodSpec.methodBuilder(column.getColumnName() + SUFFIX_ENDS_WITH)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String[].class, paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addEndsWith($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                SELECTION_METHODS.add(EQUALS);
                SELECTION_METHODS.add(NOT_EQUALS);
                SELECTION_METHODS.add(LIKE);
                SELECTION_METHODS.add(CONTAINS);
                SELECTION_METHODS.add(STARTS_WITH);
                SELECTION_METHODS.add(ENDS_WITH);
            } else if (column.getTypeInDb()==TypeMatcher.SQLiteType.INTEGER || column.getTypeInDb()==TypeMatcher.SQLiteType.REAL) {
                System.out.println("########### " + "equals should have been called");
                MethodSpec EQUALS = MethodSpec.methodBuilder(column.getColumnName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ArrayTypeName.of(ClassName.get(column.getTypeInObject())), paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addEquals($T.$L, toObjectArray($L))", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();
                System.out.println("########### " + "equals has been called");

                MethodSpec NOT_EQUALS = MethodSpec.methodBuilder(String.format("%s%s", column.getColumnName(), SUFFIX_NOT))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ArrayTypeName.of(ClassName.get(column.getTypeInObject())), paramValue)
                        .varargs()
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addNotEquals($T.$L, toObjectArray($L))", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec GREATER_THAN = MethodSpec.methodBuilder(String.format("%s%s", column.getColumnName(), SUFFIX_GT))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(column.getTypeInObject()), paramValue)
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addGreaterThan($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec GREATER_THAN_OR_EQUALS = MethodSpec.methodBuilder(String.format("%s%s", column.getColumnName(), SUFFIX_GT_EQ))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(column.getTypeInObject()), paramValue)
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addGreaterThanOrEquals($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec LESS_THAN = MethodSpec.methodBuilder(String.format("%s%s", column.getColumnName(), SUFFIX_LT))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(column.getTypeInObject()), paramValue)
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addLessThan($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                MethodSpec LESS_THAN_OR_EQUALS = MethodSpec.methodBuilder(String.format("%s%s", column.getColumnName(), SUFFIX_LT_EQ))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(column.getTypeInObject()), paramValue)
                        .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                        .addStatement("addLessThanOrEquals($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramValue)
                        .addStatement("return this")
                        .build();

                SELECTION_METHODS.add(EQUALS);
                SELECTION_METHODS.add(NOT_EQUALS);
                SELECTION_METHODS.add(GREATER_THAN);
                SELECTION_METHODS.add(GREATER_THAN_OR_EQUALS);
                SELECTION_METHODS.add(LESS_THAN);
                SELECTION_METHODS.add(LESS_THAN_OR_EQUALS);
            }

            String paramDesc = "desc";
            MethodSpec ORDER_BY1 = MethodSpec.methodBuilder(PREFIX_ORDER_BY + StringUtils.toCamelCase(column.getColumnName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(boolean.class, paramDesc)
                    .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                    .addStatement("orderBy($T.$L, $L)", TABLE, column.getColumnName().toUpperCase(), paramDesc)
                    .addStatement("return this")
                    .build();

            MethodSpec ORDER_BY2 = MethodSpec.methodBuilder(PREFIX_ORDER_BY + StringUtils.toCamelCase(column.getColumnName()))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTable.getSelectorName()))
                    .addStatement("orderBy($T.$L, false)", TABLE, column.getColumnName().toUpperCase())
                    .addStatement("return this")
                    .build();

            SELECTION_METHODS.add(ORDER_BY1);
            SELECTION_METHODS.add(ORDER_BY2);
        }
    }
    
    public JavaFile generateSelection(String outputPackage, String outputName) {
        TypeSpec outputSelection = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputSelection).build();
    }

    public TypeSpec buildClass(String name) {
        ClassName className = ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, name);
        TypeName superClass = ParameterizedTypeName.get(ClassUtils.ABSTRACT_SELECTOR, className);
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .superclass(superClass)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(BASE_URI)
                .addMethod(QUERY1)
                .addMethod(QUERY2)
                .addMethod(QUERY3)
                .addMethod(QUERY4)
                .addMethods(SELECTION_METHODS);

        return builder.build();
    }
}
