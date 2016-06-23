package me.hanthong.capstone.sync;

/**
 * Created by peet29 on 21/6/2559.
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSEnclosure;
import com.einmalfel.earl.RSSFeed;
import com.einmalfel.earl.RSSItem;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    final static String LOG_TAG = "SyncAdapter";
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

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
        String url = "http://englishnews.thaipbs.or.th/feed/";

        try {
            InputStream inputStream = new URL(url).openConnection().getInputStream();
            Feed feed = EarlParser.parseOrThrow(inputStream, 0);
            Log.i("Feed", "Processing feed: " + feed.getTitle());

             ArrayList<ContentValues> cvArray = new ArrayList<>();

            if (RSSFeed.class.isInstance(feed)) {
                RSSFeed rssFeed = (RSSFeed) feed;
                for (RSSItem item : rssFeed.items) {
                    String title = item.getTitle();
                    Log.i("Feed", "Item title: " + (title == null ? "N/A" : title));

                    RSSEnclosure enclosure = item.enclosures.get(0);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NewsColumns.TITLE, item.getTitle());
                    contentValues.put(NewsColumns.LINK, item.getLink());
                    contentValues.put(NewsColumns.DESCRIPTION, item.getDescription());
                    contentValues.put(NewsColumns.FAV, 0);
                    contentValues.put(NewsColumns.DATE, Long.toString(item.pubDate.getTime()));
                    contentValues.put(NewsColumns.PHOTO, enclosure.getLink());

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
            Cursor c =  mContentResolver.query(NewsProvider.Lists.LISTS,new String[]{ NewsColumns._ID},null,null,null);
            Log.d("Provider data", Integer.toString(c.getCount()));
            c.close();



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
}