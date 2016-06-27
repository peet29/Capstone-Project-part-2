package me.hanthong.capstone;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;

import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FirebaseAnalytics mFirebaseAnalytics;

    final static String LOG_TAG = "DetailFragment";

    private static final int DETAIL_LOADER = 2;
    private ImageView mNewsImage;
    private TextView mTitleText;
    private TextView mBodyText;
    private long mNewsID;
    private Uri mNewsLink;
    private ShareActionProvider mShareActionProvider;
    private FloatingActionButton mFab;
    private int mFav;


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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Intent intent = activity.getIntent();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setHasOptionsMenu(true);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mNewsImage = (ImageView) view.findViewById(R.id.detailNews_image);
        mTitleText = (TextView) view.findViewById(R.id.detailnews_title);
        mBodyText = (TextView) view.findViewById(R.id.detailNews_bodytext);
        mNewsID = Long.valueOf(intent.getStringExtra("news_id"));
        mFav = Integer.valueOf(intent.getStringExtra("news_fav"));

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        if(mFav == 1) {
            mFab.setImageResource(R.drawable.ic_turned_in_white_24dp);
        }else{
            mFab.setImageResource(R.drawable.ic_turned_in_not_white_24dp);
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String snackeText;
                if(mFav == 1) {
                    mFab.setImageResource(R.drawable.ic_turned_in_not_white_24dp);
                    snackeText = getString(R.string.snackbar_remove_news);
                    mFav = 0;
                }else{
                    mFab.setImageResource(R.drawable.ic_turned_in_white_24dp);
                    mFav = 1;
                    snackeText = getString(R.string.snackbar_save_news);
                }
                saveOrRemoveReadlist();
                Snackbar.make(view, snackeText, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

            }
        });

        Button button = (Button) view.findViewById(R.id.readmore_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.build().launchUrl(getActivity(), mNewsLink);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "app detail");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        return view;
    }

    private void saveOrRemoveReadlist()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NewsColumns.FAV, mFav);
        getActivity().getContentResolver().update(NewsProvider.Lists.withId(mNewsID),contentValues,null,null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            getActivity().startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

      inflater.inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private Intent createShareForecastIntent() {
        if(mNewsLink != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mNewsLink.toString());
            return shareIntent;
        }else{
            return null;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NewsProvider.Lists.withId(mNewsID), PROJECTION, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        Log.d("Data", String.valueOf(data.getCount()));

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
