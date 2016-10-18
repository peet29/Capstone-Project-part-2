package me.hanthong.capstone.sync;

/**
 * Created by peet29 on 21/6/2559.
 */

import android.accounts.Account;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSEnclosure;
import com.einmalfel.earl.RSSFeed;
import com.einmalfel.earl.RSSItem;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.DataFormatException;

import me.hanthong.capstone.DetailActivity;
import me.hanthong.capstone.R;
import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;
import me.hanthong.capstone.widget.ListNewsWidgetProvider;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final static String LOG_TAG = "SyncAdapter";
    // Global variables
    // Define a variable to contain a content resolver instance
    private final ContentResolver mContentResolver;

    private final static String PREFNAME = "SyncPref";
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();

    }

    private void downloadRss(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        ParsRss(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Log.d("Feed","can't get Rss");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void ParsRss(String url)
    {
        try {
            InputStream inputStream = new ByteArrayInputStream(url.getBytes(StandardCharsets.UTF_8));
            Feed feed = EarlParser.parseOrThrow(inputStream, 0);
            Log.i("Feed", "Processing feed: " + feed.getTitle());

            ArrayList<ContentValues> cvArray = new ArrayList<>();

            if (RSSFeed.class.isInstance(feed)) {
                RSSFeed rssFeed = (RSSFeed) feed;
                for (RSSItem item : rssFeed.items) {
                    String photoLink;
                    //Log.i("Feed", "Item title: " + (title == null ? "N/A" : title));

                    //Date date = new Date(item.pubDate.getTime());
                    //SimpleDateFormat sdf = new SimpleDateFormat("d LLL yyyy  HH:mm", Locale.getDefault());
                    RSSEnclosure enclosure;
                    if(!item.enclosures.isEmpty()) {
                        enclosure = item.enclosures.get(0);
                        photoLink = enclosure.getLink();
                    }else{
                        photoLink = "";
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NewsColumns.TITLE, item.getTitle());
                    contentValues.put(NewsColumns.LINK, item.getLink());
                    contentValues.put(NewsColumns.DESCRIPTION, item.getDescription());
                    contentValues.put(NewsColumns.FAV, 0);
                    contentValues.put(NewsColumns.DATE, Long.toString(item.pubDate.getTime()));
                    contentValues.put(NewsColumns.PHOTO, photoLink);

                    String select = "("+NewsColumns.TITLE+ " = ? )";
                    Cursor check = mContentResolver.query(NewsProvider.Lists.LISTS,new String[]{ NewsColumns.TITLE},
                            select,new String[]{item.getTitle()},null,null);
                    check.moveToFirst();
                    if(check.getCount() > 0) {
                        int columIndex = check.getColumnIndex(NewsColumns.TITLE);
                        if (item.getTitle().compareTo(check.getString(columIndex)) == 1 ) {
                            cvArray.add(contentValues);
                        }
                    }else{
                        cvArray.add(contentValues);
                    }
                    check.close();
                }
            }
            ContentValues[] cc = new ContentValues[cvArray.size()];
            cvArray.toArray(cc);

            mContentResolver.bulkInsert(NewsProvider.Lists.LISTS, cc);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if(cc.length>0 && prefs.getBoolean(getContext().getString(R.string.pref_notification_key),true) ) {
                checkNotifications();
                upDateWidget();
            }
            deleteOldData();

            //Cursor c =  mContentResolver.query(NewsProvider.Lists.LISTS,new String[]{ NewsColumns._ID},null,null,null);
            Log.d("Provider data", Integer.toString(cc.length));
            //c.close();



        }catch (MalformedURLException e) {
            Log.d("Url","error");
        }catch (IOException e){
            Log.d("IO","ERROR");
        }catch (XmlPullParserException e)
        {
            Log.d("XML","error");
        }catch (DataFormatException e){
            Log.d("Date","Error");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /*
         * Specify the code you want to run in the sync adapter. The entire
         * sync adapter runs in a background thread, so you don't have to set
         * up your own background processing.
         */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */

        Log.d(LOG_TAG, "syncWork");
        // Instantiate the RequestQueue.
        String urlBangkokPost = getContext().getResources().getString(R.string.rss_bangkokpost);
        String urlThaiPbs = getContext().getResources().getString(R.string.rss_thaipbs);
        String urlRrachatai = getContext().getResources().getString(R.string.rss_prachatai);
        String urlNation = getContext().getResources().getString(R.string.rss_nation);

        downloadRss(urlBangkokPost);
        downloadRss(urlThaiPbs);
        downloadRss(urlRrachatai);
        downloadRss(urlNation);
    }

    private void checkNotifications()
    {
        String order  = NewsColumns.DATE+" DESC";
        Cursor c =  mContentResolver.query(NewsProvider.Lists.LISTS,new String[]{ NewsColumns._ID,NewsColumns.TITLE,NewsColumns.DATE},null,null,order);
        c.moveToFirst();
        //get news title from SharedPreferences
        SharedPreferences notifications = getContext().getSharedPreferences(PREFNAME,Context.MODE_PRIVATE);
        String newsTitle = notifications.getString(getContext().getResources().getString(R.string.natition_key),"");
        Long date = notifications.getLong(getContext().getResources().getString(R.string.date_nattion_key),0);

        if(newsTitle.length()>1){
            if (newsTitle.compareTo(c.getString(c.getColumnIndex(NewsColumns.TITLE))) == 0 ) {
                Long cDate = Long.valueOf(c.getString(c.getColumnIndex(NewsColumns.DATE)));
                if(date < cDate) {
                    showNotifications();
                    SharedPreferences.Editor editor = notifications.edit();
                    editor.putString(getContext().getResources().getString(R.string.natition_key), c.getString(c.getColumnIndex(NewsColumns.TITLE))).commit();
                    editor.putLong(getContext().getResources().getString(R.string.date_nattion_key),cDate).commit();
                }
            }
        }else{
            Long cDate = Long.valueOf(c.getString(c.getColumnIndex(NewsColumns.DATE)));
            showNotifications();
            SharedPreferences.Editor editor = notifications.edit();
            editor.putString(getContext().getResources().getString(R.string.natition_key), c.getString(c.getColumnIndex(NewsColumns.TITLE))).commit();
            editor.putLong(getContext().getResources().getString(R.string.date_nattion_key),cDate).commit();
        }


        c.close();
    }

    private void showNotifications()
    {
        String order  = NewsColumns.DATE+" DESC";
        Cursor c =  mContentResolver.query(NewsProvider.Lists.LISTS,new String[]{ NewsColumns._ID,NewsColumns.TITLE,NewsColumns.DATE},null,null,order);
        c.moveToFirst();
        Intent resultIntent = new Intent(getContext(), DetailActivity.class);
        resultIntent.putExtra("news_id",c.getString(c.getColumnIndex(NewsColumns._ID)));
        resultIntent.putExtra("news_fav","0");

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        Date date = new Date(Long.valueOf(c.getString(c.getColumnIndex(NewsColumns.DATE))));
        SimpleDateFormat sdf = new SimpleDateFormat("d LLL yyyy  HH:mm", Locale.getDefault());
        String dateText = sdf.format(date);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentTitle(c.getString(c.getColumnIndex(NewsColumns.TITLE)))
                        .setContentText(dateText);

        builder.setContentIntent(resultPendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(1, builder.build());
        c.close();
    }

    private void deleteOldData()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String order  = NewsColumns.DATE+" ASC";
        String select = NewsColumns.FAV + " = ?";
        Cursor c =  mContentResolver.query(NewsProvider.Lists.LISTS,
                new String[]{ NewsColumns._ID,NewsColumns.FAV},
                select,
                new String[]{"0"},
                order);
        int maxItem = Integer.valueOf(prefs.getString(getContext().getString(R.string.pref_maxitem_key),getContext().getString(R.string.pref_maxitem_default)));
        int deletNum = 0;
        if(c.getCount() >0 && c.getCount() > maxItem) {
            deletNum = c.getCount() - maxItem;
        }
        int[] deleID;
        if(deletNum != 0)
        {
            c.moveToFirst();
            deleID = new int[deletNum];
            for(int i = 0;i < deletNum;i++)
            {
                deleID[i] = Integer.valueOf(c.getString(c.getColumnIndex(NewsColumns._ID)));
                c.moveToNext();
            }
            for(int i = 0;i < deletNum;i++) {
                mContentResolver.delete(NewsProvider.Lists.withId((long)deleID[i]),null,null);
            }
        }
        c.close();
    }

    private void upDateWidget()
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(getContext(), ListNewsWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
}