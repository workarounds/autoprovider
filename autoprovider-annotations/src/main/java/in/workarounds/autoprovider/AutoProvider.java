package in.workarounds.autoprovider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by madki on 08/10/15.
 */
@Target(TYPE) @Retention(CLASS)
public @interface AutoProvider {
    String authority();
    String providerName() default "";
    String packageName() default "";
    String databaseName();
    int databaseVersion() default 1;
}
