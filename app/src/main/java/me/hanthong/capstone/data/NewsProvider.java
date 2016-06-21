package me.hanthong.capstone.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by peet29 on 17/6/2559.
 */
@ContentProvider(authority = NewsProvider.AUTHORITY, database = NewsDatabase.class)
public final class NewsProvider {

    public static final String AUTHORITY = "me.hanthong.capstone";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String LISTS = "lists";
        String NOTES = "notes";
        String FROM_LIST = "fromList";
    }

    @TableEndpoint(table = NewsDatabase.LISTS)
    public static class Lists {

        @ContentUri(
                path = "lists",
                type = "vnd.android.cursor.dir/list",
                defaultSort = NewsColumns.TITLE + " ASC")
        public static final Uri LISTS = Uri.parse("content://" + AUTHORITY + "/lists");

        @InexactContentUri(
                path = Path.LISTS + "/#",
                name = "LIST_ID",
                type = "vnd.android.cursor.item/list",
                whereColumn = NewsColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.parse("content://" + AUTHORITY + "/lists/" + id);
        }
    }
}
