package in.workarounds.autoprovider.compiler.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by mouli on 10/22/15.
 */
public class ClassUtils {

    public static final ClassName CURSOR = ClassName.get("android.database", "Cursor", new String[]{});
    public static final ClassName NONNULL = ClassName.get("android.support.annotation", "NonNull", new String[]{});

}
