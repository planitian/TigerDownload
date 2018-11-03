package com.example.tigerdownload;

import android.support.v4.content.MimeTypeFilter;
import android.webkit.MimeTypeMap;

import org.junit.Test;

import okhttp3.MediaType;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {


        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String ss=mimeTypeMap.getExtensionFromMimeType("text/plain");
        System.out.println(ss);

    }
}