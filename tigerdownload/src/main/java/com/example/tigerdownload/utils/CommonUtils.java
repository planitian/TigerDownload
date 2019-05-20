package com.example.tigerdownload.utils;

import android.os.Handler;

import java.io.File;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public class CommonUtils {

    /**
     * 持有主线程的引用
     */
    private static Handler mainHandler = new Handler();

    public static Handler getMainHandler() {
        return mainHandler;
    }

    /**
     * 读取baseurl
     *
     * @param url
     * @return
     */
    public static String getBasUrl(String url) {
        String head = "";
        int index = url.indexOf("://");
        if (index != -1) {
            head = url.substring(0, index + 3);
            url = url.substring(index + 3);
        }
        index = url.indexOf("/");
        if (index != -1) {
            url = url.substring(0, index + 1);
        }
        return head + url;
    }

    /**
     * 通过url 得到下载名字
     * @param url
     * @return
     */
    public static String downName(String url){
        int index = url.lastIndexOf("/");
        String result = url.substring(index+1);
//        Zprint.log(CommonUtils.class,"下载文件名字",result);
        return result;
    }

    /**
     * @return 现在时间的格式化输出
     */
    public static String nowTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HHmmss",Locale.getDefault());
        Date date = new Date();
        String time = format.format(date);
        return time;
    }
}
