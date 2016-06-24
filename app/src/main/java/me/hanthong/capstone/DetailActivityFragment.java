package me.hanthong.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final static String LOG_TAG = "DetailFragment";

    private static final int DETAIL_LOADER = 2;
    private ImageView mNewsImage;
    private TextView mTitleText;
    private TextView mBodyText;
    private long mNewsID;
    private Uri mNewsLink;

    String[] PROJECTION = {
            NewsColumns._ID,
            NewsColumns.TITLE,
            NewsColumns.DESCRIPTION,
            NewsColumns.DATE,
            NewsColumns.LINK,
            NewsColumns.PHOTO,
            NewsColumns.FAV
    };

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Intent intent = activity.getIntent();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNewsImage = (ImageView) view.findViewById(R.id.detailNews_image);
        mTitleText = (TextView) view.findViewById(R.id.detailnews_title);
        mBodyText = (TextView)view.findViewById(R.id.detailNews_bodytext);
        mNewsID =  Long.valueOf(intent.getStringExtra("news_id"));


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //getLoaderManager().initLoader(0,null,null);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NewsProvider.Lists.withId(mNewsID),PROJECTION,null,null,null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        Log.d("Data",String.valueOf(data.getCount()));

        String titleText = data.getString(data.getColumnIndex(NewsColumns.TITLE));

        mTitleText.setText(titleText);
        mBodyText.setText(Html.fromHtml(data.getString(
                data.getColumnIndex(NewsColumns.DESCRIPTION))
        ));

        Glide.with(getActivity())
                .load(data.getString(data.getColumnIndex(NewsColumns.PHOTO)))
                .error(R.drawable.hold)
                .crossFade()
                .centerCrop()
                .into(mNewsImage);
        mNewsLink = Uri.parse(data.getString(data.getColumnIndex(NewsColumns.LINK)));
    }
}
