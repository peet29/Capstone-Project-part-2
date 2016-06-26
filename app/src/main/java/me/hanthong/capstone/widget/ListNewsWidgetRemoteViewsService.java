package me.hanthong.capstone.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import me.hanthong.capstone.R;
import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;

/**
 * Created by peet29 on 25/6/2559.
 */
public class ListNewsWidgetRemoteViewsService extends RemoteViewsService {
    String[] PROJECTION = {
            NewsColumns._ID,
            NewsColumns.TITLE,
            NewsColumns.DATE,
            NewsColumns.LINK,
            NewsColumns.PHOTO,
            NewsColumns.FAV
    };


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                String order = NewsColumns.DATE + " DESC";
                data = getContentResolver().query(NewsProvider.Lists.LISTS,
                        PROJECTION,
                        null,
                        null,
                        order);
                Log.d("Widget data", data + "");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                String newsTitle = data.getString(data.getColumnIndex(NewsColumns.TITLE));
                String newsFav = data.getString(data.getColumnIndex(NewsColumns.FAV));
                String newsID = data.getString(data.getColumnIndex(NewsColumns._ID));
                final RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);


                views.setTextViewText(R.id.news_title, newsTitle);


                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("news_id", newsID);
                fillInIntent.putExtra("news_fav", newsFav);
                views.setOnClickFillInIntent(R.id.news_list, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
