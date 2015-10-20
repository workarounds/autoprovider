package in.workarounds.autoprovider.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.org.apache.bcel.internal.classfile.Code;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileDataSource;
import javax.lang.model.element.Modifier;

import in.workarounds.autoprovider.compiler.utils.TypeMatcher;
import jdk.nashorn.internal.runtime.regexp.joni.ast.AnchorNode;

import static in.workarounds.autoprovider.compiler.utils.TypeMatcher.*;

/**
 * Created by mouli on 10/20/15.
 */
public class TableGenerator {

    public static final String NOT_NULL = "NOT NULL";
    public static final String AUTO_INCREMENT = "AUTO INCREMENT";
    public static final String PRIMARY_KEY = "PRIMARY KEY";
    public static final String SQL_TYPE_INTEGER = "INTEGER";
    public static final String SQL_TYPE_TEXT = "TEXT";
    public static final String SQL_TYPE_REAL = "REAL";
    public static final String SQL_TYPE_BLOB = "BLOB";

    private final String mTableName = "TABLE_NAME";
    private final String mSQLCreate = "SQL_CREATE";
    private final String mAllColumns = "ALL_COLUMNS";
    private final String mSQLInsert = "SQL_INSERT";
    private final String mSQLDrop = "SQL_DROP";
    private final String mStatementPrefix = "WHERE_";
    private final String mStatementSuffix = "_EQUALS";
    private final String mStatementValue = "=?";

    private final FieldSpec TABLE_NAME;
    private List<FieldSpec> columnFields = new ArrayList<>();
    private List<FieldSpec> queryFields = new ArrayList<>();
    private final FieldSpec ALL_COLUMNS;
    private final FieldSpec SQL_INSERT;
    private final FieldSpec SQL_CREATE;
    private final FieldSpec SQL_DROP;

    public TableGenerator(AnnotatedTable annotatedTable) {

        TABLE_NAME = FieldSpec.builder(String.class, mTableName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", annotatedTable.getTableName()).build();

        for(AnnotatedColumn annotatedColumn : annotatedTable.getColumns()) {
            FieldSpec column = FieldSpec.builder(String.class, annotatedColumn.getColumnName().toUpperCase())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", annotatedColumn.getColumnName()).build();
            FieldSpec query = FieldSpec.builder(String.class,
                    mStatementPrefix+annotatedColumn.getColumnName().toUpperCase()+mStatementSuffix)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", mStatementValue).build();
            columnFields.add(column);
            queryFields.add(query);
        }

        SQL_INSERT = FieldSpec.builder(String.class, mSQLInsert)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", getSQLInsertStatement(annotatedTable).toString())
                .build();

        ALL_COLUMNS = FieldSpec.builder(ArrayTypeName.of(String.class), mAllColumns)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(getAllColumnsValue(annotatedTable.getColumns()).toString())
                .build();

        SQL_DROP = FieldSpec.builder(String.class, mSQLDrop)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", getSQLDropStatement(annotatedTable.getTableName()))
                .build();

        SQL_CREATE = FieldSpec.builder(String.class, mSQLCreate)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", getSQLCreateStatement(annotatedTable).toString())
                .build();

    }

    private CodeBlock getAllColumnsValue(List<AnnotatedColumn> annotatedColumns) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("new $T {", ArrayTypeName.of(String.class));
        for(int i=0;i<annotatedColumns.size();i++) {
            AnnotatedColumn annotatedColumn = annotatedColumns.get(i);
            builder.add(" $L", annotatedColumn.getColumnName().toUpperCase());
            if(i!=annotatedColumns.size()-1) {
                builder.add(",");
            }
        }
        builder.add("}");
        return builder.build();
    }

    private CodeBlock getSQLDropStatement(String tableName) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("DROP TABLE IF EXISTS $L", tableName);
        return builder.build();
    }

    private CodeBlock getSQLInsertStatement(AnnotatedTable annotatedTable) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("INSERT INTO $L (", annotatedTable.getTableName());
        List<AnnotatedColumn> annotatedColumnList = annotatedTable.getColumns();
        for(int i=0; i<annotatedColumnList.size(); i++) {
            AnnotatedColumn annotatedColumn = annotatedColumnList.get(i);
            builder.add(" $L", annotatedColumn.getColumnName());
            if(i!=annotatedColumnList.size()-1) {
                builder.add(",");
            }
        }
        builder.add(" ) VALUES ( ");
        for(int i=0; i<annotatedColumnList.size(); i++) {
            builder.add(" ?");
            if(i!=annotatedColumnList.size()-1) {
                builder.add(",");
            }
        }
        builder.add(" )");
        return builder.build();
    }

    private CodeBlock getSQLCreateStatement(AnnotatedTable annotatedTable) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("CREATE TABLE $L (", annotatedTable.getTableName());
        List<AnnotatedColumn> annotatedColumns = annotatedTable.getColumns();
        for(int i=0;i<annotatedColumns.size();i++) {
            AnnotatedColumn annotatedColumn = annotatedColumns.get(i);
            builder.add(" $L", annotatedColumn.getColumnName());
            SQLiteType type = annotatedColumn.getTypeInDb();
            if(type==SQLiteType.INTEGER) {
                builder.add(" $L", SQL_TYPE_INTEGER);
            } else if (type==SQLiteType.TEXT) {
                builder.add(" $L", SQL_TYPE_TEXT);
            } else if (type==SQLiteType.REAL) {
                builder.add(" $L", SQL_TYPE_REAL);
            } else if (type==SQLiteType.BLOB) {
                builder.add(" $L", SQL_TYPE_BLOB);
            }
            if(annotatedColumn.isNotNull()) {
                builder.add(" $L", NOT_NULL);
            }
            if(annotatedColumn.isPrimaryKey()) {
                builder.add(" $L", PRIMARY_KEY);
            }
            if(annotatedColumn.isAutoIncrement()) {
                builder.add(" $L", AUTO_INCREMENT);
            }
            if(i!=annotatedColumns.size()-1) {
                builder.add(",");
            }
        }
        builder.add(" )");
        return builder.build();
    }

    public JavaFile generateTable(String outputPackage, String outputName) {
        TypeSpec outputTable = buildClass(outputName);
        return JavaFile.builder(outputPackage, outputTable).build();
    }

    public TypeSpec buildClass(String name) {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addField(TABLE_NAME);

        columnFields.forEach(builder::addField);

        builder.addField(ALL_COLUMNS);
        builder.addField(SQL_CREATE);
        builder.addField(SQL_INSERT);
        builder.addField(SQL_DROP);

        queryFields.forEach(builder::addField);

        return builder.build();
    }

}
