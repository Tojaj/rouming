package com.tojaj.android.rouming;

import android.app.Application;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.tojaj.android.rouming.R;

public class RoumingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Set default shared preference values if it never been done before
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        
        // Set options for image displaying
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory(true)
	        .cacheOnDisc(true)
	        .showStubImage(R.drawable.nacitani)
			.showImageForEmptyUri(R.drawable.prazdne_url)
			.showImageOnFail(R.drawable.chyba)
	        .displayer(new FadeInBitmapDisplayer(1500))
	        .build();
        
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        	.defaultDisplayImageOptions(defaultOptions)
			.threadPriority(Thread.NORM_PRIORITY - 1)
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new RouminguvHashCodeFileNameGenerator())
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.discCacheFileCount(100)
            .build();
        ImageLoader.getInstance().init(config);
    }
    
    private class RouminguvHashCodeFileNameGenerator implements FileNameGenerator {
    	@Override
    	public String generate(String imageUri) {
    		// Preserve the suffix of the img file in the filename
    		String name;

    		int i = imageUri.lastIndexOf('.');
    		if (i != -1)
    			name = String.valueOf(imageUri.hashCode()) + imageUri.substring(i);
    		else
    			name = String.valueOf(imageUri.hashCode());
 
    		return name;
		}
	}
}

