package com.tojaj.android.rouming;

import uk.co.senab.photoview.PhotoView;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import com.tojaj.android.rouming.R;

public class PictureFragment extends Fragment {

    // Arguments
    public static final String ARGUMENT_LOCAL_URI = "local_uri";
    public static final String ARGUMENT_ORIGIN_URI = "origin_uri";
    public static final String ARGUMENT_NAME = "name";

    private static final String TAG = "Picture fragment";

    private PhotoView photoView;
    private Intent mShareIntent;
    private ShareActionProvider mShareActionProvider;
    private Bitmap mBitmap;
    private String mName;

    public static PictureFragment newInstance(String local_uri,
            String origin_uri, String name) {
        PictureFragment f = new PictureFragment();

        Bundle args = new Bundle();
        args.putString(ARGUMENT_LOCAL_URI, local_uri);
        args.putString(ARGUMENT_ORIGIN_URI, origin_uri);
        args.putString(ARGUMENT_NAME, name);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Report intention to participate in populating the options menu
        setHasOptionsMenu(true);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        Bundle arguments = getArguments();

        // origin_uri contain origin URL of the image,
        // which is used to for the share share intent
        String origin_uri_str = arguments.getString(ARGUMENT_ORIGIN_URI);
        if (origin_uri_str != null) {
            Uri origin_uri = Uri.parse(origin_uri_str);
            mShareIntent = new Intent(Intent.ACTION_SEND);
            mShareIntent.setType("text/plain");
            mShareIntent.putExtra(Intent.EXTRA_TEXT, origin_uri.toString());
        }

        // Load picture into memory
        String local_uri_str = arguments.getString(ARGUMENT_LOCAL_URI);
        Uri local_uri = Uri.parse(local_uri_str);
        if (local_uri != null) {
            Log.d(TAG, "No local_uri");
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inPurgeable = true;
            bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;

            String str = local_uri.getPath();
            mBitmap = BitmapFactory.decodeFile(str, bmOptions);
        }

        // Set the name as the activity title in the action bar
        mName = arguments.getString(ARGUMENT_NAME, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_picture, container,
                false);

        photoView = (PhotoView) view.findViewById(R.id.photoView);
        photoView.setImageBitmap(mBitmap);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        ActionBar ab = null;

        if (activity != null)
            ab = activity.getActionBar();
        if (ab != null)
            ab.setTitle(mName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.picture, menu);
        // Prepare share share action option (icon)
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(mShareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
