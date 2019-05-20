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

    // 静默安装  网上
    public static boolean clientInstall(String apkPath) {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 " + apkPath);
            PrintWriter
                    .println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r " + apkPath);
            // PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            boolean result = returnResult(value);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            deleteApk(apkPath);
        }
        return false;
    }


    private static boolean returnResult(int value) {

        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }


    // 判断是否有root权限
    public static boolean hasRootPerssion() {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

        /**
         * 删除文件
         *
         * @param apkName
         */
    public static void deleteApk(String apkName) {
        File file = new File(apkName);
        if (!file.exists()) {
            return;
        }
        file.delete();
    }



    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
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
