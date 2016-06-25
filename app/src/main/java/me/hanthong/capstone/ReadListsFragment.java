package me.hanthong.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import me.hanthong.capstone.data.NewsColumns;
import me.hanthong.capstone.data.NewsProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReadListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int READ_LOADER = 0;
    protected RecyclerView mRecyclerView;
    protected TextView mEmptyView;
    protected MyNewsRecyclerViewAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    String[] PROJECTION = {
            NewsColumns._ID,
            NewsColumns.TITLE,
            NewsColumns.DATE,
            NewsColumns.LINK,
            NewsColumns.PHOTO,
            NewsColumns.FAV
    };


    public ReadListsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_lists, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setHasOptionsMenu(true);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);

        mEmptyView = (TextView) view.findViewById(R.id.show_net_text);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyNewsRecyclerViewAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mEmptyView.setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "open readlist");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        LoaderManager LoaderManager = getLoaderManager();
        LoaderManager.initLoader(READ_LOADER, null, this);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            getActivity().startActivity(intent);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "setting");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_read_lists, menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String order  = NewsColumns.DATE+" DESC";
        String select = NewsColumns.FAV + " = ?";
        return new CursorLoader(getActivity(),
                NewsProvider.Lists.LISTS,
                PROJECTION,
                select,
                new String[]{"1"},
                order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        if(data.getCount()==0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
