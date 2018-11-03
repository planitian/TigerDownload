package com.example.tigerdownload;

/**
 * 下载进度回调  已舍弃 因为只适用于一个、ui  不适用列表
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public interface DownloadProgressListener {

    /**
     * @param read 已下载长度
     * @param contentLength  总长度
     * @param done  是否下载完成
     */
    void progress(long read, long contentLength, boolean done);

}
