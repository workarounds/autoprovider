package in.workarounds.autoprovider.compiler.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.AnnotatedProvider;
import in.workarounds.autoprovider.compiler.AnnotatedTable;
import in.workarounds.autoprovider.compiler.ProviderProcessor;
import in.workarounds.autoprovider.compiler.utils.ClassUtils;
import in.workarounds.autoprovider.compiler.utils.StringUtils;

/**
 * Created by mouli on 10/29/15.
 */
public class SQLiteOpenHelperGenerator {

    public static final String mName = "AutoSQLiteOpenHelper";

    private final String nTag = "TAG";
    private final String nDatabaseFileName = "DATABASE_FILE_NAME";
    private final String nDatabaseVersion = "DATABASE_VERSION";
    private final String nInstance = "mInstance";
    private final String nContext = "mContext";
    private final String nGetInstance = "getInstance";
    private final String nNewInstance = "newInstance";
    private final String nNewInstancePreHoneyComb = "newInstancePreHoneyComb";
    private final String nNewInstancePostHoneyComb = "newInstancePostHoneyComb";
    private final String nOnCreate = "onCreate";
    private final String nOnUpgrade = "onUpgrade";

    private final FieldSpec TAG;
    private final FieldSpec DATABASE_FILE_NAME;
    private final FieldSpec DATABASE_VERSION;
    private final FieldSpec INSTANCE;
    private final FieldSpec CONTEXT;

    private final MethodSpec ON_CREATE;
    private final MethodSpec ON_UPGRADE;
    private final MethodSpec CONSTRUCTOR1;
    private final MethodSpec CONSTRUCTOR2;
    private final MethodSpec GET_INSTANCE;
    private final MethodSpec NEW_INSTANCE;
    private final MethodSpec NEW_INSTANCE_PRE_HONEYCOMB;
    private final MethodSpec NEW_INSTANCE_POST_HONEYCOMB;


    public SQLiteOpenHelperGenerator(AnnotatedProvider annotatedProvider, List<AnnotatedTable> annotatedTableList) {
        TAG = FieldSpec.builder(String.class, nTag)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.class.getSimpleName()", mName)
                .build();

        DATABASE_FILE_NAME = FieldSpec.builder(String.class, nDatabaseFileName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", annotatedProvider.getDatabaseFileName())
                .build();

        DATABASE_VERSION = FieldSpec.builder(int.class, nDatabaseVersion)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", annotatedProvider.getDatabaseVersion())
                .build();

        INSTANCE = FieldSpec.builder(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName), nInstance)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();

        CONTEXT = FieldSpec.builder(ClassUtils.CONTEXT, nContext)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        GET_INSTANCE = MethodSpec.methodBuilder(nGetInstance)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassUtils.CONTEXT, "context")
                .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName))
                .beginControlFlow("if ($L == null) ", nInstance)
                .addStatement("$L = $L(context.getApplicationContext())", nInstance, nNewInstance)
                .endControlFlow()
                .addStatement("return $L", nInstance)
                .build();

        NEW_INSTANCE = MethodSpec.methodBuilder(nNewInstance)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ClassUtils.CONTEXT, "context")
                .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName))
                .beginControlFlow("if ($T.VERSION.SDK_INT < $T.VERSION_CODES.HONEYCOMB)", ClassUtils.BUILD, ClassUtils.BUILD)
                .addStatement("return $L(context)", nNewInstancePreHoneyComb)
                .endControlFlow()
                .addStatement("return $L(context)", nNewInstancePostHoneyComb)
                .build();

        NEW_INSTANCE_PRE_HONEYCOMB = MethodSpec.methodBuilder(nNewInstancePreHoneyComb)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ClassUtils.CONTEXT, "context")
                .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName))
                .addStatement("return new $T(context)", ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName))
                .build();

        NEW_INSTANCE_POST_HONEYCOMB = MethodSpec.methodBuilder(nNewInstancePostHoneyComb)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addAnnotation(AnnotationSpec.builder(ClassUtils.TARGET_API)
                        .addMember("value", "$T.VERSION_CODES.HONEYCOMB", ClassUtils.BUILD)
                        .build())
                .addParameter(ClassUtils.CONTEXT, "context")
                .returns(ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName))
                .addStatement("return new $T(context, new $T())",
                        ClassName.get(ProviderProcessor.OUTPUT_PACKAGE, mName),
                        ClassUtils.DEFAULT_DATABASE_ERROR_HANDLER)
                .build();

        CONSTRUCTOR1 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassUtils.CONTEXT, "context")
                .addStatement("super(context, $L, null, $L)", nDatabaseFileName, nDatabaseVersion)
                .addStatement("$L = context", nContext)
                .build();

        String paramErrorHandler = "errorHandler";
        CONSTRUCTOR2 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(ClassUtils.TARGET_API)
                        .addMember("value", "$T.VERSION_CODES.HONEYCOMB", ClassUtils.BUILD)
                        .build())
                .addParameter(ClassUtils.CONTEXT, "context")
                .addParameter(ClassUtils.DATABASE_ERROR_HANDLER, paramErrorHandler)
                .addStatement("super(context, $L, null, $L, $L)", nDatabaseFileName, nDatabaseVersion, paramErrorHandler)
                .addStatement("$L = context", nContext)
                .build();

        String paramDb = "db";
        ON_CREATE = MethodSpec.methodBuilder(nOnCreate)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.SQLITE_DATABASE, paramDb)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .beginControlFlow("if ($T.DEBUG)", ClassName.get(annotatedProvider.getPackageName(), "BuildConfig"))
                .addStatement("$T.d($L, $S)", ClassUtils.LOG, nTag, nOnCreate)
                .endControlFlow()
//                .addStatement("$L.onPreCreate($L, $L)", nOpenHelperCallbacks, nContext, paramDb)
                .addCode(getDatabaseCreateStatements(annotatedTableList, paramDb))
//                .addStatement("$L.onPostCreate($L, $L)", nOpenHelperCallbacks, nContext, paramDb)
                .build();

        String paramOldVersion = "oldVersion";
        String paramNewVersion = "newVersion";
        ON_UPGRADE = MethodSpec.methodBuilder(nOnUpgrade)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassUtils.SQLITE_DATABASE, paramDb)
                .addParameter(int.class, paramOldVersion)
                .addParameter(int.class, paramNewVersion)
                .build();
    }

    private CodeBlock getDatabaseCreateStatements(List<AnnotatedTable> tables, String paramDb) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for(AnnotatedTable table : tables) {
            builder.addStatement("$L.execSQL($L.$L)", paramDb, table.getTableName(), TableGenerator.mSQLCreate);
        }
        return builder.build();
    }

    public JavaFile generateSQLiteOpenHelper(String outputPackage, String outputName) {
        TypeSpec outputFile = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputFile).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassUtils.SQLITE_OPEN_HELPER);

        builder.addField(TAG);
        builder.addField(DATABASE_FILE_NAME);
        builder.addField(DATABASE_VERSION);
        builder.addField(INSTANCE);
        builder.addField(CONTEXT);

        builder.addMethod(CONSTRUCTOR1);
        builder.addMethod(CONSTRUCTOR2);
        builder.addMethod(GET_INSTANCE);
        builder.addMethod(NEW_INSTANCE);
        builder.addMethod(NEW_INSTANCE_PRE_HONEYCOMB);
        builder.addMethod(NEW_INSTANCE_POST_HONEYCOMB);
        builder.addMethod(ON_CREATE);
        builder.addMethod(ON_UPGRADE);

        return builder.build();
    }

}
