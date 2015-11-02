package in.workarounds.autoprovider.compiler;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import in.workarounds.autoprovider.AndroidId;
import in.workarounds.autoprovider.AutoIncrement;
import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.NotNull;
import in.workarounds.autoprovider.PrimaryKey;
import in.workarounds.autoprovider.compiler.utils.StringUtils;
import in.workarounds.autoprovider.compiler.utils.TypeMatcher;

/**
 * Created by madki on 08/10/15.
 */
public class AnnotatedColumn {
    private static final String ERROR_MSG_PRIMARY_KEY_NOT_LONG = "Primary key should be of type java.util.Long";
    private static final String ERROR_MSG_ANDROID_ID_NOT_LONG = "Android id should be of type java.util.Long";

    private String columnName;
    private TypeMirror typeInObject;
    private TypeMatcher.SQLiteType typeInDb;
    private boolean primaryKey;
    private boolean notNull;
    private boolean autoIncrement;

    public AnnotatedColumn(Element columnElement, Elements elementUtils) throws IllegalArgumentException {
        if(isValidColumn(columnElement)) {
            columnName = getColumnName(columnElement);
            typeInObject = columnElement.asType();
            typeInDb = TypeMatcher.getSQLiteType(typeInObject);

            AndroidId androidIdAnn = columnElement.getAnnotation(AndroidId.class);
            if(androidIdAnn!=null) {
                if(!typeInObject.toString().equals(Long.class.getCanonicalName())) {
                    throw new IllegalArgumentException(ERROR_MSG_ANDROID_ID_NOT_LONG);
                }
                primaryKey = true;
                autoIncrement = true;
                notNull = true;
            }

            PrimaryKey primaryKeyAnn = columnElement.getAnnotation(PrimaryKey.class);
            if(primaryKeyAnn != null) {
                if(!typeInObject.toString().equals(Long.class.getCanonicalName())) {
                    throw new IllegalArgumentException(ERROR_MSG_PRIMARY_KEY_NOT_LONG);
                }
                primaryKey = true;
            }

            AutoIncrement autoIncrementAnn = columnElement.getAnnotation(AutoIncrement.class);
            if(autoIncrementAnn != null) {
                autoIncrement = true;
            }

            NotNull notNullAnn = columnElement.getAnnotation(NotNull.class);
            if(notNullAnn != null) {
                notNull = true;
            }

        }
    }

    private boolean isValidColumn(Element element) throws IllegalArgumentException {
        Set<Modifier> modifiers = element.getModifiers();
        if(modifiers.contains(Modifier.PUBLIC)
                && !modifiers.contains(Modifier.STATIC)
                && !modifiers.contains(Modifier.FINAL)) {
            return true;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "The field %s annotated with @%s should be public, non-static and non-final",
                            element.getSimpleName(),
                            Column.class.getSimpleName()
                    ));
        }
    }

    private String getColumnName(Element columnElement) {
        Column columnAnn = columnElement.getAnnotation(Column.class);
        String columnName = columnAnn.value();
        if(columnName.trim().isEmpty()) {
            columnName = StringUtils.toSnakeCase(columnElement.getSimpleName().toString());
        }
        return columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public TypeMirror getTypeInObject() {
        return typeInObject;
    }

    public TypeMatcher.SQLiteType getTypeInDb() {
        return typeInDb;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
}
