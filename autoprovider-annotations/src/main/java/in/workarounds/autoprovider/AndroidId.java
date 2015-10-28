package in.workarounds.autoprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by mouli on 10/28/15.
 *
 * Annotates a field as PrimaryKey and AutoIncrement
 */
@Retention(CLASS) @Target(TYPE)
public @interface AndroidId {
}
