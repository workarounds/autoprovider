package in.workarounds.autoprovider.compiler.utils;

import javax.lang.model.type.TypeMirror;

/**
 * Created by madki on 08/10/15.
 */
public class TypeMatcher {

    public static SQLiteType getSQLiteType(TypeMirror variableType) {
        SQLiteType sqliteType = null;
        if(variableType.toString().equals(Float.class.getCanonicalName())
                || variableType.toString().equals(Double.class.getCanonicalName())) {
            sqliteType = SQLiteType.REAL;
        } else if(variableType.toString().equals(Integer.class.getCanonicalName())
                || variableType.toString().equals(Long.class.getCanonicalName())
                || variableType.toString().equals(Boolean.class.getCanonicalName())) {
            sqliteType = SQLiteType.INTEGER;
        } else if(variableType.toString().equals(String.class.getCanonicalName())) {
            sqliteType = SQLiteType.TEXT;
        } else {
            sqliteType = SQLiteType.BLOB;
        }

        return sqliteType;
    }

    public enum SQLiteType {
        INTEGER,
        TEXT,
        REAL,
        BLOB
    }
}
