package in.workarounds.autoprovider.compiler.utils;

/**
 * Created by madki on 08/10/15.
 */
public class TypeMatcher {

    public static SQLiteType getSQLiteType(Class<?> variableType) {
        SQLiteType sqliteType = null;

        if(variableType.equals(Float.class)
                || variableType.equals(Double.class)) {
            sqliteType = SQLiteType.REAL;
        } else if(variableType.equals(Integer.class)
                || variableType.equals(Long.class)
                || variableType.equals(Boolean.class)) {
            sqliteType = SQLiteType.INTEGER;
        } else if(variableType.equals(String.class)) {
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
