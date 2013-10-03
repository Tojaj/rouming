package com.tojaj.android.rouming;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.tojaj.android.rouming.provider.RoumingContract;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.CursorAdapter;

public class PicturesFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    static final String TAG = "PicturesFragment";
    private static final int PICTURES_LOADER = 0;

    OnPictureClickedListener listener;
    RoumingPicturesCursorAdapter mAdapter;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    /*
     * Interface(s) required by this activity
     */

    public static interface OnPictureClickedListener {
        public void OnPictureClicked(String local_uri, String origin_uri,
                String name);
    }

    public static PicturesFragment newInstance() {
        PicturesFragment f = new PicturesFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    /*
     * Fragment default callbacks
     */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnPictureClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPictureClickedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Set own cursor and own adapter
        mAdapter = new RoumingPicturesCursorAdapter(getActivity(), null);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(PICTURES_LOADER, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mAdapter.getCursor();
        if (c == null)
            return;

        c.moveToPosition(position);

        String local_uri = null;
        String origin_uri = c.getString(c
                .getColumnIndex(RoumingContract.Pictures.PICTURE_URL));
        String name = c.getString(c
                .getColumnIndex(RoumingContract.Pictures.NAME));

        if (origin_uri != null) {
            DiscCacheAware discCache = imageLoader.getDiscCache();
            File image = DiscCacheUtil.findInCache(origin_uri, discCache);
            if (image != null)
                local_uri = Uri.fromFile(image).toString();
        }

        listener.OnPictureClicked(local_uri, origin_uri, name);
    }

    /*
     * Custom cursor adapter with view recycling
     */

    public class RoumingPicturesCursorAdapter extends CursorAdapter {

        Context context;

        private class ViewHolder {
            public TextView textViewName;
            public ImageView imageView;
        }

        public RoumingPicturesCursorAdapter(Context context, Cursor c) {
            super(context, c, false);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.obrazek_item, parent, false);

                holder = new ViewHolder();
                holder.textViewName = (TextView) view
                        .findViewById(R.id.textViewName);
                holder.imageView = (ImageView) view
                        .findViewById(R.id.imageView);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final Cursor cursor = (Cursor) getItem(position);

            holder.textViewName.setText(cursor.getString(cursor
                    .getColumnIndex(RoumingContract.Pictures.NAME)));
            imageLoader.displayImage(cursor.getString(cursor
                    .getColumnIndex(RoumingContract.Pictures.PICTURE_URL)),
                    holder.imageView);

            /*
             * imageLoader.loadImage(rouming_picture_href.pic_url.toString(),
             * new SimpleImageLoadingListener() {
             * 
             * @Override public void onLoadingComplete(String imageUri, View
             * view, Bitmap loadedImage) { // Do whatever you want with Bitmap
             * holder.imageView.setImageBitmap(loadedImage); } });
             */

            return view;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }
    }

    /*
     * Loader interface implementation
     */

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        Log.d(TAG, "Loader created");
        switch (loaderID) {
        case PICTURES_LOADER:
            // Return a new CursorLoader
            String[] projection = { RoumingContract.Pictures._ID,
                    RoumingContract.Pictures.TIME,
                    RoumingContract.Pictures.NAME,
                    RoumingContract.Pictures.DETAIL_URL,
                    RoumingContract.Pictures.PICTURE_URL,
                    RoumingContract.Pictures.SIZE,
                    RoumingContract.Pictures.LIKES,
                    RoumingContract.Pictures.DISLIKES,
                    RoumingContract.Pictures.COMMENTS };

            return new CursorLoader(getActivity(),
                    RoumingContract.Pictures.CONTENT_URI, // Table to query
                    projection, // Projection to return
                    null, // No selection clause
                    null, // No selection arguments
                    null // Default sort order
            );
        default:
            // An invalid id was passed in
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        /*
         * Moves the query results into the adapter, causing the ListView
         * fronting this adapter to re-display
         */
        Log.d(TAG, "LOADER Finished");
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Clears out the adapter's reference to the Cursor. This prevents
         * memory leaks.
         */
        Log.d(TAG, "LOADER Reseted");
        mAdapter.changeCursor(null);
    }

}
