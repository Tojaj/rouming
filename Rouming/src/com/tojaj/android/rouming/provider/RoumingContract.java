package com.tojaj.android.rouming.provider;

import android.graphics.Picture;
import android.net.Uri;
import android.provider.BaseColumns;

public class RoumingContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RoumingContract() {
    }

    interface MetadataColumns {
        String LAST_UPDATE = "last_update";
    }

    interface PicturesColumns {
        String TIME = "time";
        String NAME = "name";
        String DETAIL_URL = "detail_url";
        String PICTURE_URL = "picture_url";
        String SIZE = "size";
        String LIKES = "likes";
        String DISLIKES = "dislikes";
        String COMMENTS = "comments";
    }

    interface JokesColumns {
        String TIME = "time";
        String NAME = "name";
        String TEXT = "text";
        String CATEGORY = "category";
        String GRADE = "grade";
    }

    public static final String CONTENT_AUTHORITY = "com.tojaj.android.rouming.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    public static final String PATH_METADATA = "metadata";
    public static final String PATH_PICTURES = "pictures";
    public static final String PATH_JOKES = "jokes";

    public static class Metadata implements MetadataColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_METADATA).build();

        public static final String[] PROJECTION = { BaseColumns._ID,
                MetadataColumns.LAST_UPDATE, };

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.tojaj.rouming.metadata";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.tojaj.rouming.metadata";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = MetadataColumns.LAST_UPDATE
                + " DESC";
    }

    public static class Pictures implements PicturesColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PICTURES).build();

        public static final String[] PROJECTION = {
                BaseColumns._ID,
                PicturesColumns.TIME,
                PicturesColumns.NAME,
                PicturesColumns.DETAIL_URL,
                PicturesColumns.PICTURE_URL,
                PicturesColumns.SIZE,
                PicturesColumns.LIKES,
                PicturesColumns.DISLIKES,
                PicturesColumns.COMMENTS
        };

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.tojaj.rouming.picture";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.tojaj.rouming.picture";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = PicturesColumns.TIME
                + " DESC";

        /** Build {@link Uri} for requested picture {@link #_ID}. */
        public static Uri buildPictureUri(String pictureId) {
            return CONTENT_URI.buildUpon().appendPath(pictureId).build();
        }

        /** Read picture {@link #_ID} from {@link Picture} {@link Uri}. */
        public static String getPictureId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Jokes implements JokesColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_JOKES).build();

        public static final String[] PROJECTION = {
                BaseColumns._ID,
                JokesColumns.TIME,
                JokesColumns.NAME,
                JokesColumns.TEXT,
                JokesColumns.CATEGORY,
                JokesColumns.GRADE
        };

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.tojaj.rouming.joke";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.tojaj.rouming.joke";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = PicturesColumns.TIME
                + " DESC";

        /** Build {@link Uri} for requested joke {@link #_ID}. */
        public static Uri buildJokeUri(String jokeId) {
            return CONTENT_URI.buildUpon().appendPath(jokeId).build();
        }

        /** Read joke {@link #_ID} from {@link Joke} {@link Uri}. */
        public static String getJokeId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
