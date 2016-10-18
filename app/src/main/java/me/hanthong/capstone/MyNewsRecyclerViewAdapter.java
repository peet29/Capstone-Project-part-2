package me.hanthong.capstone;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.hanthong.capstone.data.NewsColumns;


public class MyNewsRecyclerViewAdapter extends RecyclerView.Adapter<MyNewsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private Cursor mCursor;
    private Context mContext;


    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView newsTitle;
        public final TextView newsDate;
        public final ImageView newsImage;
        public String newsID;
        public String Fav;

        public ViewHolder(View v) {
            super(v);
            final Context itemContext = v.getContext();
            final String intentKey = "news_id";
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");

                    Intent intent = new Intent(itemContext, DetailActivity.class);
                    intent.putExtra(intentKey,newsID);
                    intent.putExtra("news_fav",Fav);
                    Log.d(TAG,"NewsID "+newsID);
                    itemContext.startActivity(intent);

                }
            });
            newsTitle = (TextView) v.findViewById(R.id.news_title);
            newsDate = (TextView) v.findViewById(R.id.news_date);
            newsImage = (ImageView) v.findViewById(R.id.news_image);
        }

        public TextView getNewsTitle() {
            return newsTitle;
        }

        public TextView getNewsDate() {
            return newsDate;
        }

        public ImageView getNewsImage() {
            return newsImage;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)


    public MyNewsRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_news, viewGroup, false);

        v.setFocusable(true);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Log.d(TAG, "Element " + position + " set.");
        mCursor.moveToPosition(position);
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getNewsTitle().setText(mCursor.getString(mCursor.getColumnIndex(NewsColumns.TITLE)));
        Date date = new Date(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(NewsColumns.DATE))));
        SimpleDateFormat sdf = new SimpleDateFormat("d LLL yyyy  HH:mm", Locale.getDefault());
        viewHolder.getNewsDate().setText(sdf.format(date));
        Glide.with(mContext)
                .load(mCursor.getString(mCursor.getColumnIndex(NewsColumns.PHOTO)))
                .error(R.drawable.hold)
                .crossFade()
                .centerCrop()
                .into(viewHolder.getNewsImage());

        viewHolder.newsID = mCursor.getString(mCursor.getColumnIndex(NewsColumns._ID));
        viewHolder.Fav = mCursor.getString(mCursor.getColumnIndex(NewsColumns.FAV));

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public Cursor getCursor(){return mCursor;}

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
