package com.tojaj.android.rouming.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import android.test.AndroidTestCase;
import com.tojaj.android.rouming.Rouming;
import com.tojaj.android.rouming.Rouming.RoumingPictureHref;

public class RoumingTest extends AndroidTestCase {

    static final String ROUMING_HTML_PATH = "assets/rouming.htm";
    static final String ROUMING_JOKES_HTML_PATH = "assets/rouming_jokes.htm";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() {
    }

    public void testStringCleanup() {
        String res;

        res = Rouming.stringCleanup("  foo  ");
        assertEquals("foo", res);

        res = Rouming.stringCleanup("f&nbsp;o&nbsp;o");
        assertEquals("f o o", res);
    }
    
    public void testToLong() {
        long res;

        res = Rouming.toLong("foo");
        assertEquals(0, res);

        res = Rouming.toLong("5");
        assertEquals(5, res);
        
        res = Rouming.toLong("6a");
        assertEquals(0, res);
        
        res = Rouming.toLong("0xA");
        assertEquals(10, res);
    }
    
    public void testToSizeInKb() {
        long res;
        
        res = Rouming.toSizeInKb("123kB");
        assertEquals(123, res);
        
        res = Rouming.toSizeInKb("123FooByte");
        assertEquals(-1, res);
    }
    
    public void testToUrl() {
        URL res;
        
        res = Rouming.toUrl("http://foo.bar");
        assertNotNull(res);
        assertEquals("http://foo.bar", res.toString());
        
        res = Rouming.toUrl("foobar");
        assertNull(res);
    }
    
    public void testToPicUrl() {
        URL res;
        
        res = Rouming.toPicUrl("http://www.rouming.cz/roumingShow.php?file=test.jpg");
        assertNotNull(res);
        assertEquals(Rouming.RoumingConf.ROUMING_IMG_URL + "test.jpg", res.toString());
 
        res = Rouming.toPicUrl("foobar.jpg");
        assertNotNull(res);
        assertEquals(Rouming.RoumingConf.ROUMING_IMG_URL + "foobar.jpg", res.toString());
    }
    
    public void testToUnixTime() {
        long res;
        
        res = Rouming.toUnixTime("foobar");
        assertEquals(0L, res);
        
        res = Rouming.toUnixTime("3.5.2013");
        assertEquals(1367539200000L, res);
    }
    
    private String getResourceContent(String path) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);        
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder content = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return content.toString();
    }
    
    public void testParseRoumingHtml() {
        String content = getResourceContent(ROUMING_HTML_PATH);
        ArrayList<RoumingPictureHref> hrefs = Rouming.parseRoumingHtml(content);

        assertEquals(119, hrefs.size());
        
        assertEquals("application for employment", hrefs.get(0).name);
        assertEquals("http://www.rouming.cz/roumingShow.php?file=application_for_employment.jpg", hrefs.get(0).url);
        assertEquals("20:45", hrefs.get(0).time);
        assertEquals(7, hrefs.get(0).comments);
        assertEquals(14, hrefs.get(0).likes);
        assertEquals(4, hrefs.get(0).dislikes);
        assertEquals(60, hrefs.get(0).size);

        assertEquals("humanized", hrefs.get(118).name);
    }
}
