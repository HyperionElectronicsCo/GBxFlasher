package com.mycompany.gbxflasher;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;

public final class UriUtils {


    public static final int CONTENT_SIZE_INVALID = -1;

    /**
     * @param context context
     * @param contentUri content Uri, i.e, of the scheme <code>content://</code>
     * @return The Display name and size for content. In case of non-determination, display name
     * would be null and content size would be {@link #CONTENT_SIZE_INVALID}
     */
    @NonNull
    public static DisplayNameAndSize getDisplayNameSize(@NonNull Context context, @NonNull Uri contentUri){

        final String scheme = contentUri.getScheme();
        if(scheme == null || !scheme.equals(ContentResolver.SCHEME_CONTENT)){
            throw new RuntimeException("Only scheme content:// is accepted");
        }

        final DisplayNameAndSize displayNameAndSize = new DisplayNameAndSize();
        displayNameAndSize.size = CONTENT_SIZE_INVALID;

        String[] projection = new String[]{MediaStore.Images.Media.DATA, OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {

                // Try extracting content size

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    displayNameAndSize.size = cursor.getLong(sizeIndex);
                }

                // Try extracting display name
                String name = null;

                // Strategy: The column name is NOT guaranteed to be indexed by DISPLAY_NAME
                // so, we try two methods
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }

                if (nameIndex == -1 || name == null) {
                    nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex);
                    }
                }
                displayNameAndSize.displayName = name;
            }
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
        }

        // We tried querying the ContentResolver...didn't work out
        // Try extracting the last path segment
        if(displayNameAndSize.displayName == null){
            displayNameAndSize.displayName = contentUri.getLastPathSegment();
        }

        return displayNameAndSize;
    }
}
