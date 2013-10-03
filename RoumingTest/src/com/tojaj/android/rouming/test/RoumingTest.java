package com.tojaj.android.rouming.test;

import java.net.URL;
import java.util.Date;

import android.test.AndroidTestCase;
import com.tojaj.android.rouming.Rouming;

public class RoumingTest extends AndroidTestCase {

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
}
