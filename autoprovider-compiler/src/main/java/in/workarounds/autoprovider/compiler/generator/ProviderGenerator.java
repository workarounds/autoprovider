package in.workarounds.autoprovider.compiler.generator;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.AnnotatedProvider;
import in.workarounds.autoprovider.compiler.AnnotatedTable;
import in.workarounds.autoprovider.compiler.ProviderProcessor;
import in.workarounds.autoprovider.compiler.utils.ClassUtils;

/**
 * Created by mouli on 10/29/15.
 */
public class ProviderGenerator {
    private static final String TYPE_CURSOR_ITEM_STRING = "vnd.android.cursor.item/";
    private static final String TYPE_CURSOR_DIR_STRING = "vnd.android.cursor.dir/";

    private final String mTag = "TAG";
    private final String mDebug = "DEBUG";
    private final String mTypeCursorItem = "TYPE_CURSOR_ITEM";
    private final String mTypeCursorDir = "TYPE_CURSOR_DIR";
    private final String mAuthority = "AUTHORITY";
    public static final String mContentBaseUri = "CONTENT_BASE_URI";
    private final String mUriMatcher = "URI_MATCHER";
    private final String mUriTypePrefix = "URI_TYPE";
    private final String mCreateSqliteOpenHelper = "createSqLiteOpenHelper";
    private final String mHasDebug = "hasDebug";
    private final String mGetType = "getType";
    private final String mInsert = "insert";
    private final String mBulkInsert = "bulkInsert";
    private final String mUpdate = "update";
    private final String mDelete = "delete";
    private final String mQuery = "query";
    private final String mGetQueryParams = "getQueryParams";

    private final FieldSpec TAG;
    private final FieldSpec DEBUG;
    private final FieldSpec TYPE_CURSOR_ITEM;
    private final FieldSpec TYPE_CURSOR_DIR;
    private final FieldSpec AUTHORITY;
    private final FieldSpec CONTENT_URI_BASE;
    private final FieldSpec URI_MATCHER;

    private final MethodSpec CREATE_SQLITE_OPEN_HELPER;
    private final MethodSpec HAS_DEBUG;
    private final MethodSpec GET_TYPE;
    private final MethodSpec INSERT;
    private final MethodSpec BULK_INSERT;
    private final MethodSpec DELETE;
    private final MethodSpec UPDATE;
    private final MethodSpec QUERY;
    private final MethodSpec GET_QUERY_PARAMS;

    private final List<FieldSpec> URI_TYPE_FIELDS = new ArrayList<>();
    private final List<CodeBlock> URI_MATCHER_STATEMENTS = new ArrayList<>();

    public ProviderGenerator(AnnotatedProvider annotatedProvider, List<AnnotatedTable> annotatedTables) {
        TAG = FieldSpec.builder(String.class, mTag)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.class.getSimpleName()", annotatedProvider.getProviderName())
                .build();

        DEBUG = FieldSpec.builder(TypeName.BOOLEAN, mDebug)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.BuildConfig.DEBUG", annotatedProvider.getPackageName())
                .build();

        TYPE_CURSOR_ITEM = FieldSpec.builder(String.class, mTypeCursorItem)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", TYPE_CURSOR_ITEM_STRING)
                .build();

        TYPE_CURSOR_DIR = FieldSpec.builder(String.class, mTypeCursorDir)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", TYPE_CURSOR_DIR_STRING)
                .build();

        AUTHORITY = FieldSpec.builder(String.class, mAuthority)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", annotatedProvider.getAuthority())
                .build();

        CONTENT_URI_BASE = FieldSpec.builder(String.class, mContentBaseUri)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S + $L", "content://", mAuthority)
                .build();

        for(int i=0;i<annotatedTables.size();i++) {
            String fieldName = String.format("%s_%s", mUriTypePrefix, annotatedTables.get(i).getTableName().toUpperCase());
            FieldSpec fieldSpec = FieldSpec.builder(int.class, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", i)
                    .build();
            URI_TYPE_FIELDS.add(fieldSpec);
            CodeBlock codeBlock = CodeBlock.builder()
                    .addStatement("$L.addURI($L, $T.$L, $L)",
                            mUriMatcher,
                            mAuthority,
                            ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTables.get(i).getTableName()),
                            TableGenerator.mTableName,
                            fieldName)
                    .build();
            URI_MATCHER_STATEMENTS.add(codeBlock);
        }

        for(int i=0;i<annotatedTables.size();i++) {
            String fieldName = String.format("%s_%s_ID", mUriTypePrefix, annotatedTables.get(i).getTableName().toUpperCase());
            FieldSpec fieldSpec = FieldSpec.builder(int.class, fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", i + annotatedTables.size())
                    .build();
            URI_TYPE_FIELDS.add(fieldSpec);
            CodeBlock codeBlock = CodeBlock.builder()
                    .addStatement("$L.addURI($L, $T.$L + \"/#\", $L)",
                            mUriMatcher,
                            mAuthority,
                            ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, annotatedTables.get(i).getTableName()),
                            TableGenerator.mTableName,
                            fieldName)
                    .build();
            URI_MATCHER_STATEMENTS.add(codeBlock);
        }

        URI_MATCHER = FieldSpec.builder(ClassUtils.URI_MATCHER, mUriMatcher)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T($T.NO_MATCH)", ClassUtils.URI_MATCHER, ClassUtils.URI_MATCHER)
                .build();

        CREATE_SQLITE_OPEN_HELPER = MethodSpec.methodBuilder(mCreateSqliteOpenHelper)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .returns(ClassUtils.SQLITE_OPEN_HELPER)
                .addStatement("return $T.getInstance(getContext())",
                        ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, SQLiteOpenHelperGenerator.mName))
                .build();

        HAS_DEBUG = MethodSpec.methodBuilder(mHasDebug)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .returns(boolean.class)
                .addStatement("return $L", mDebug)
                .build();

        String paramMatch = "match";
        String paramUri = "uri";
        GET_TYPE = MethodSpec.methodBuilder(mGetType)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .returns(String.class)
                .addStatement("$T $L = $L.match($L)", int.class, paramMatch, mUriMatcher, paramUri)
                .beginControlFlow("switch ($L)", paramMatch)
                .addCode(getUriTypeCases(annotatedTables))
                .endControlFlow()
                .addStatement("return null")
                .build();

        String paramContentValues = "values";
        INSERT = MethodSpec.methodBuilder(mInsert)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(ClassUtils.CONTENT_VALUES, paramContentValues)
                .returns(ClassUtils.URI)
                .beginControlFlow("if($L)", mDebug)
                .addStatement("$T.d($L, \"insert uri=\" + $L + \" values=\" + $L)", ClassUtils.LOG, mTag, paramUri, paramContentValues)
                .endControlFlow()
                .addStatement("return super.$L($L, $L)", mInsert, paramUri, paramContentValues)
                .build();

        BULK_INSERT = MethodSpec.methodBuilder(mBulkInsert)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(ArrayTypeName.of(ClassUtils.CONTENT_VALUES), paramContentValues)
                .returns(int.class)
                .beginControlFlow("if($L)", mDebug)
                .addStatement("$T.d($L, \"bulkInsert uri=\" + $L + \" values.length=\" + $L.length)", ClassUtils.LOG, mTag, paramUri, paramContentValues)
                .endControlFlow()
                .addStatement("return super.$L($L, $L)", mBulkInsert, paramUri, paramContentValues)
                .build();

        String paramSelection = "selection";
        String paramSelectionArgs = "selectionArgs";
        UPDATE = MethodSpec.methodBuilder(mUpdate)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(ClassUtils.CONTENT_VALUES, paramContentValues)
                .addParameter(String.class, paramSelection)
                .addParameter(ArrayTypeName.of(String.class), paramSelectionArgs)
                .returns(int.class)
                .beginControlFlow("if($L)", mDebug)
                .addStatement("$T.d($L, \"update uri=\" + $L + \" values=\" + $L + \" selection=\" + $L + \" selectionArgs=\" + $T.toString($L))",
                        ClassUtils.LOG, mTag, paramUri, paramContentValues, paramSelection, Arrays.class, paramSelectionArgs)
                .endControlFlow()
                .addStatement("return super.$L($L, $L, $L, $L)", mUpdate, paramUri, paramContentValues, paramSelection, paramSelectionArgs)
                .build();

        DELETE = MethodSpec.methodBuilder(mDelete)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(String.class, paramSelection)
                .addParameter(ArrayTypeName.of(String.class), paramSelectionArgs)
                .returns(int.class)
                .beginControlFlow("if($L)", mDebug)
                .addStatement("$T.d($L, \"delete uri=\" + $L + \" selection=\" + $L + \" selectionArgs=\" + $T.toString($L))",
                        ClassUtils.LOG, mTag, paramUri, paramSelection, Arrays.class, paramSelectionArgs)
                .endControlFlow()
                .addStatement("return super.$L($L, $L, $L)", mDelete, paramUri, paramSelection, paramSelectionArgs)
                .build();

        String paramProjection = "projection";
        String paramSortOrder = "sortOrder";
        QUERY = MethodSpec.methodBuilder(mQuery)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(ArrayTypeName.of(String.class), paramProjection)
                .addParameter(String.class, paramSelection)
                .addParameter(ArrayTypeName.of(String.class), paramSelectionArgs)
                .addParameter(String.class, paramSortOrder)
                .returns(ClassUtils.CURSOR)
                .beginControlFlow("if($L)", mDebug)
                .addStatement("$T.d($L, \"query uri=\" + $L + \" selection=\" + $L + \" selectionArgs=\" + $T.toString($L) + \" sortOrder=\" + $L + " +
                                "\" groupBy=\" + $L.getQueryParameter($L) + \" having=\" +  $L.getQueryParameter($L) + \" limit=\" + $L.getQueryParameter($L))",
                        ClassUtils.LOG, mTag, paramUri, paramSelection, Arrays.class, paramSelectionArgs, paramSortOrder, paramUri, "QUERY_GROUP_BY", paramUri, "QUERY_HAVING", paramUri, "QUERY_LIMIT")
                .endControlFlow()
                .addStatement("return super.$L($L, $L, $L, $L, $L)", mQuery, paramUri, paramProjection, paramSelection, paramSelectionArgs, paramSortOrder)
                .build();

        String paramQueryParams = "res";
        String paramId = "id";
        String paramMatchedId = "matchedId";
        GET_QUERY_PARAMS = MethodSpec.methodBuilder(mGetQueryParams)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.URI, paramUri)
                .addParameter(String.class, paramSelection)
                .addParameter(ArrayTypeName.of(String.class), paramSelectionArgs)
                .returns(ClassUtils.QUERY_PARAMS)
                .addStatement("$T res = new $T()", ClassUtils.QUERY_PARAMS, ClassUtils.QUERY_PARAMS)
                .addStatement("$T $L = null", String.class, paramId)
                .addStatement("$T $L = $L.match($L)", int.class, paramMatchedId, mUriMatcher, paramUri)
                .beginControlFlow("switch ($L)", paramMatchedId)
                .addCode(getQueryParamsCases(annotatedTables, paramQueryParams, paramUri))
                .endControlFlow()
                .beginControlFlow("switch ($L)", paramMatchedId)
                .addCode(getQueryIdCases(annotatedTables, paramId, paramUri))
                .endControlFlow()
                .beginControlFlow("if ($L != null)", paramId)
                .beginControlFlow("if ($L != null)", paramSelection)
                .addStatement("$L.selection = $L.table + $S + $L.idColumn + $S + $L + $S + $L + $S",
                        paramQueryParams,
                        paramQueryParams,
                        ".",
                        paramQueryParams,
                        "=",
                        paramId,
                        " and (",
                        paramSelection,
                        ")")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$L.selection = $L.table + $S + $L.idColumn + $S + $L",
                        paramQueryParams,
                        paramQueryParams,
                        ".",
                        paramQueryParams,
                        "=",
                        paramId)
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$L.selection = $L", paramQueryParams, paramSelection)
                .endControlFlow()
                .addStatement("return $L", paramQueryParams)
                .build();
    }

    private CodeBlock getQueryIdCases(List<AnnotatedTable> tables, String paramId, String paramUri) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for(AnnotatedTable table: tables) {
            String name = String.format("%s_%s_ID", mUriTypePrefix, table.getTableName().toUpperCase());
            builder.add("case $L:\n", name);
        }
        builder.addStatement("      $L = $L.getLastPathSegment()", paramId, paramUri);
        return builder.build();
    }

    private CodeBlock getQueryParamsCases(List<AnnotatedTable> tables, String paramQueryParams, String paramUri) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for(AnnotatedTable table: tables) {
            String name1 = String.format("%s_%s", mUriTypePrefix, table.getTableName().toUpperCase());
            String name2 = String.format("%s_%s_ID", mUriTypePrefix, table.getTableName().toUpperCase());
            builder.add("case $L:\n", name1);
            builder.add("case $L:\n", name2);
            builder.addStatement("      $L.table = $L.$L", paramQueryParams,
                    table.getTableName(), TableGenerator.mTableName);
            builder.addStatement("      $L.idColumn = $L.$L", paramQueryParams,
                    table.getTableName(), table.getPrimaryColumn().getColumnName().toUpperCase());
            builder.addStatement("      $L.tablesWithJoins = $L.$L", paramQueryParams,
                    table.getTableName(), TableGenerator.mTableName);
            builder.addStatement("      $L.orderBy = $L.$L", paramQueryParams,
                    table.getTableName(), TableGenerator.mDefaultOrder);
            builder.addStatement("break");
        }
        builder.add("default:\n");
        builder.addStatement("      throw new $T($S + $L + $S)", IllegalArgumentException.class, "The uri'", paramUri, "' is not supported by this ContentProvider");
        return builder.build();
    }

    private CodeBlock getUriTypeCases(List<AnnotatedTable> tables) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for(AnnotatedTable table: tables) {
            String name1 = String.format("%s_%s", mUriTypePrefix, table.getTableName().toUpperCase());
            String name2 = String.format("%s_%s_ID", mUriTypePrefix, table.getTableName().toUpperCase());
            builder.add("case $L:\n", name1);
            builder.addStatement("      return $L + $T.$L", mTypeCursorDir,
                    ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, table.getTableName()),
                    TableGenerator.mTableName);
            builder.add("case $L:\n", name2);
            builder.addStatement("      return $L + $T.$L", mTypeCursorItem,
                    ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, table.getTableName()),
                    TableGenerator.mTableName);
        }
        builder.indent();
        return builder.build();
    }

    public JavaFile generateProvider(String outputPackage, String outputName) {
        TypeSpec outputTable = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputTable).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassUtils.BASE_PROVIDER);

        builder.addField(TAG);
        builder.addField(DEBUG);
        builder.addField(TYPE_CURSOR_DIR);
        builder.addField(TYPE_CURSOR_ITEM);
        builder.addField(AUTHORITY);
        builder.addField(CONTENT_URI_BASE);
        builder.addField(URI_MATCHER);

        for(FieldSpec field: URI_TYPE_FIELDS) {
            builder.addField(field);
        }

        CodeBlock.Builder uriMatcherStatementsBuilder = CodeBlock.builder();

        for(CodeBlock codeBlock: URI_MATCHER_STATEMENTS) {
            uriMatcherStatementsBuilder.add(codeBlock);
        }
        builder.addStaticBlock(uriMatcherStatementsBuilder.build());

        builder.addMethod(CREATE_SQLITE_OPEN_HELPER);
        builder.addMethod(HAS_DEBUG);
        builder.addMethod(GET_TYPE);
        builder.addMethod(INSERT);
        builder.addMethod(BULK_INSERT);
        builder.addMethod(UPDATE);
        builder.addMethod(DELETE);
        builder.addMethod(QUERY);
        builder.addMethod(GET_QUERY_PARAMS);

        return builder.build();
    }

}
