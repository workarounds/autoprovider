package in.workarounds.autoprovider.compiler.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by mouli on 10/22/15.
 */
public interface ClassUtils {

    ClassName CURSOR = ClassName.get("android.database", "Cursor");
    ClassName NONNULL = ClassName.get("android.support.annotation", "NonNull");
    ClassName ABSTRACT_CURSOR = ClassName.get("in.workarounds.autoprovider", "AbstractCursor");

}
