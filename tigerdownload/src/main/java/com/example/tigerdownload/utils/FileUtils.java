package com.example.tigerdownload.utils;

import android.util.Log;

import com.example.tigerdownload.DownloadState;
import com.example.tigerdownload.bean.TaskInfo;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.ResponseBody;


/**
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    //写入文件
    public static void writeCache(ResponseBody responseBody, File file, TaskInfo info) throws IOException {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        long allLength;
        info.setDownloadState(DownloadState.STATE_lOADING);
        if (info.getContentLength() == 0) {
            if (responseBody.contentLength() == -1) {
                normalWrite(responseBody,file,info);
                return;
            }else {
                allLength = responseBody.contentLength();
            }
        } else {
            allLength = info.getContentLength();
        }
        mapWrite(responseBody,file,info,allLength);
    }

    //内存映射进行写文件 效率高
    private static void mapWrite(ResponseBody responseBody, File file, TaskInfo info, long allLength) throws IOException {
        InputStream inputStream = responseBody.byteStream();
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;
        randomAccessFile = new RandomAccessFile(file, "rwd");
        channelOut = randomAccessFile.getChannel();
        //将文件区域映射到内存
        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                info.getReadLength(), allLength - info.getReadLength());
        byte[] buffer = new byte[1024 * 8];
        int len;
        int record = 0;

        while ((len = inputStream.read(buffer)) != -1) {
            mappedBuffer.put(buffer, 0, len);
            record = record + len;
        }
        inputStream.close();
        if (record != info.getContentLength()) {
            throw new NullPointerException();
        }
        if (channelOut != null) {
            channelOut.close();
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }

    //普通下载  不知道文件确切长度的情况下
    private static void normalWrite(ResponseBody responseBody, File file, TaskInfo info) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file,false));
        Log.d(TAG, "normalWrite: " + bufferedInputStream.available());
        byte[] temp = new byte[1024];
        int len;
        while ((len = bufferedInputStream.read(temp)) != -1) {
            bufferedOutputStream.write(temp, 0, len);
        }
        bufferedOutputStream.flush();
        bufferedInputStream.close();
        bufferedOutputStream.close();
    }


    //创建下载目录
    public static boolean createDownloadPath(String loadPath) {
        File dic = new File(loadPath);
        if (!dic.exists()) {
            if (dic.mkdirs()) {
                Log.d(TAG, "下载目录创建成功");
                return true;
            }
            return true;
        }
        return false;
    }
}
