package in.workarounds.autoprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public abstract class AbstractValues {
    protected final ContentValues mContentValues = new ContentValues();

    /**
     * Returns the {@code uri} argument to pass to the {@code ContentResolver} methods.
     */
    public abstract Uri uri();

    /**
     * Returns the {@code ContentValues} wrapped by this object.
     */
    public ContentValues values() {
        return mContentValues;
    }

    /**
     * Inserts a row into a table using the values stored by this object.
     *
     * @param contentResolver The content resolver to use.
     */
    public Uri insert(ContentResolver contentResolver) {
        return contentResolver.insert(uri(), values());
    }

    /**
     * Inserts a row into a table using the values stored by this object.
     *
     * @param context The context to use.
     */
    public Uri insert(Context context) {
        return context.getContentResolver().insert(uri(), values());
    }
}