package com.tojaj.android.rouming;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

//TODO: Naming convention

public class Rouming {

    static final String TAG = "ROUMING";

    public final class RoumingConf {
        public static final String ROUMING_URL = "http://www.rouming.cz/";
        public static final String ROUMING_IMG_URL = "http://cdn.roumen.cz/kecy/";
    }

    public static class RoumingPictureHref {
        public String time;
        public long unixtime;
        public long likes;
        public long dislikes;
        public long comments;
        public long size;
        public String name;
        public URL url;
        public URL pic_url;
    }

    // Helpers

    public static String stringCleanup(String str) {
        return str.replace("&nbsp;", " ").trim();
    }

    public static long toLong(String str) {
        try {
            return Long.decode(str);
        } catch (NumberFormatException e) {
            Log.d(TAG, "Cannot decode: \"" + str + "\" to long");
            return 0;
        }
    }

    public static long toSizeInKb(String size) {
        if (size.endsWith("kB")) {
            return toLong(size.replace("kB", ""));
        } else {
            return -1;
        }
    }

    public static URL toUrl(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Cannot parse URL: " + str);
            e.printStackTrace();
            return null;
        }
    }

    public static URL toPicUrl(String str) {
        String splited[] = str.split("file=");

        String img_url = RoumingConf.ROUMING_IMG_URL
                + splited[splited.length - 1];

        try {
            return new URL(img_url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Cannot parse URL: " + img_url);
            e.printStackTrace();
            return null;
        }
    }

    public static long toUnixTime(String str) {
        long date_as_milliseconds;
        SimpleDateFormat sdf;
        final Locale locale = new Locale("cs", "cz");

        if (str.matches("[0-9]{1,2}:[0-9]{1,2}")) {
            sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm", locale);
            String current_day = new SimpleDateFormat("dd.MM.yyyy", locale)
                    .format(new Date());
            str = current_day + "-" + str;
        } else {
            sdf = new SimpleDateFormat("dd.MM.yyyy", locale);
        }

        try {
            date_as_milliseconds = sdf.parse(str).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Cannot convert date string \"" + str
                    + "\" to milliseconds");
            date_as_milliseconds = 0;
        }

        return date_as_milliseconds;
    }

    // Main Logic

    public static ArrayList<RoumingPictureHref> getRoumingPictureHrefs(
            Context context) {
        String content = getRoumingPageContent(context);
        ArrayList<RoumingPictureHref> list = parseRoumingHtml(content);
        return list;
    }

    public static String getUrl(URL url, Context context) {
        String content = null;

        // Network is available, download current version
        InputStream stream = null;
        int len = 128000;

        try {
            HttpURLConnection conn;
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            if (response != 200) {
                Log.e(TAG, "Bad response: " + response);
                return null;
            }

            stream = conn.getInputStream();

            // Convert the InputStream into a string
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            content = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException raised during downloading: " + e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG,
                            "IOException raised during closing stream: "
                                    + e.toString());
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return content;
    }

    public static String getRoumingPageContent(Context context) {
        String content = null;

        URL url;
        try {
            url = new URL(RoumingConf.ROUMING_URL);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Cannot parse URL: " + RoumingConf.ROUMING_URL);
            return null;
        }

        content = getUrl(url, context);

        return content;
    }

    public static ArrayList<RoumingPictureHref> parseRoumingHtml(String content) {

        ArrayList<RoumingPictureHref> list = new ArrayList<RoumingPictureHref>();

        // Get the main page
        if (content == null)
            return list;

        // Parse content
        Pattern p = Pattern.compile(
            "\\s*<td\\s?(?:\\s*[^>]*)>([0-9.:-]+)</\\s*td>(?:\\s*\n?)*"
                    + "\\s*<td\\s?(?:\\s*[^>]*)>.*?<a\\s+href=\"roumingComments.php[^>]*>\\s*([0-9]*)\\s*</\\s*a>\\s*</\\s*td>(?:\\s*\n?)*"
                    + "\\s*<td\\s?(?:\\s*[^>]*)>[^<]*<font[^>]*>\\s*([0-9]+)\\s*</\\s*font>\\s*</\\s*td>(?:\\s*\n?)*"
                    + "<td>\\s*/\\s*</\\s*td>(?:\\s*\n?)*"
                    + "\\s*<td\\s?(?:\\s*[^>]*)>[^<]*<font[^>]*>\\s*([0-9]+)\\s*</\\s*font>[^<]*</\\s*td>(?:\\s*\n?)*"
                    + "\\s*<td\\s?(?:\\s*[^>]*)>[^0-9]*([0-9]+\\s*kB)[^<]*</\\s*td>(?:\\s*\n?)*"
                    + "\\s*<td\\s?(?:\\s*[^>]*)>.*?<a.*?href=\"([^\"]*)\"[^>]*>(.*?)</\\s*a>\\s*</\\s*td>",
            Pattern.MULTILINE + Pattern.CASE_INSENSITIVE
                    + Pattern.DOTALL + Pattern.COMMENTS);

        Matcher m = p.matcher(content);

        while (m.find()) { // Find each match in turn; String can't do this.
            String p_time = m.group(1);
            String p_comments = m.group(2);
            String p_likes = m.group(3);
            String p_dislikes = m.group(4);
            String p_sizekb = m.group(5);
            String p_url = m.group(6);
            String p_name = m.group(7);

            Log.d(TAG, "Item:" +
                       "\nName: " + p_name +
                       "\nURL: " + p_url +
                       "\nTime: " + p_time +
                       "\nComments: " + p_comments +
                       "\nLikes: " + p_likes +
                       "\nDislikes: " + p_dislikes +
                       "\nSize: " + p_sizekb);

            RoumingPictureHref pic_href = new RoumingPictureHref();
            pic_href.name = stringCleanup(p_name);
            pic_href.time = stringCleanup(p_time);
            pic_href.unixtime = toUnixTime(stringCleanup(p_time));
            pic_href.comments = toLong(stringCleanup(p_comments));
            pic_href.likes = toLong(stringCleanup(p_likes));
            pic_href.dislikes = toLong(stringCleanup(p_dislikes));
            pic_href.size = toSizeInKb(stringCleanup(p_sizekb));
            pic_href.url = toUrl(stringCleanup(p_url));
            pic_href.pic_url = toPicUrl(stringCleanup(p_url));

            list.add(pic_href);
        }

        return list;
    }
}
