package com.example.tigerdownload.utils;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class ValidFileName {

    public static String fileName(String url){
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            System.out.println("fileName "+filename);
            // if the filename contains special characters, we don't consider it valid for our matching purposes:
            if (!filename.isEmpty() &&
                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
                return filename;
            }
        }

        return "";
    }
}
