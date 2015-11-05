package in.workarounds.autoprovider.compiler.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by mouli on 10/22/15.
 */
public interface ClassUtils {

    ClassName URI = ClassName.get("android.net", "Uri");
    ClassName URI_MATCHER = ClassName.get("android.content", "UriMatcher");
    ClassName CURSOR = ClassName.get("android.database", "Cursor");
    ClassName NONNULL = ClassName.get("android.support.annotation", "NonNull");
    ClassName CONTEXT = ClassName.get("android.content", "Context");
    ClassName SQLITE_DATABASE = ClassName.get("android.database.sqlite", "SQLiteDatabase");
    ClassName SQLITE_OPEN_HELPER = ClassName.get("android.database.sqlite", "SQLiteOpenHelper");
    ClassName DATABASE_ERROR_HANDLER = ClassName.get("android.database", "DatabaseErrorHandler");
    ClassName DEFAULT_DATABASE_ERROR_HANDLER = ClassName.get("android.database", "DefaultDatabaseErrorHandler");
    ClassName BUILD = ClassName.get("android.os", "Build");
    ClassName TARGET_API = ClassName.get("android.annotation", "TargetApi");
    ClassName LOG = ClassName.get("android.util", "Log");
    ClassName CONTENT_VALUES = ClassName.get("android.content", "ContentValues");
    ClassName CONTENT_RESOLVER = ClassName.get("android.content", "ContentResolver");
    ClassName NULLABLE = ClassName.get("android.support.annotation", "Nullable");

    ClassName ABSTRACT_CURSOR = ClassName.get("in.workarounds.autoprovider", "AbstractCursor");
    ClassName BASE_PROVIDER = ClassName.get("in.workarounds.autoprovider", "BaseProvider");
    ClassName QUERY_PARAMS = ClassName.get("in.workarounds.autoprovider.BaseProvider", "QueryParams");
    ClassName ABSTRACT_SELECTOR = ClassName.get("in.workarounds.autoprovider", "AbstractSelector");
    ClassName ABSTRACT_VALUES = ClassName.get("in.workarounds.autoprovider", "AbstractValues");
}
