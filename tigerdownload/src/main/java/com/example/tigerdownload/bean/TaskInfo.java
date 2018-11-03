package com.example.tigerdownload.bean;

import com.example.tigerdownload.DownloadObserable;
import com.example.tigerdownload.DownloadObserver;
import com.example.tigerdownload.DownloadState;
import com.example.tigerdownload.utils.UUIDutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public class TaskInfo implements DownloadObserable, Serializable {
    //下载id
    private String        taskId;
    //存储目录
    private String        directory;
    //文件名字
    private String        fileName;
    //下载文件总长度
    private long          contentLength;
    //已下载的长度
    private long          readLength;
    //文件下载地址
    private String        url;
    //下载状态
    private DownloadState downloadState;

    //被观察者集合
    private transient List<DownloadObserver> observers = new ArrayList<>();

    /**
     * 下载速度,单位M/S
     */
    private float downloadSpeed;


    private TaskInfo() {
    }


    public String getTaskId() {
        return taskId;
    }


    public DownloadState getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(DownloadState downloadState) {
        this.downloadState = downloadState;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getReadLength() {
        return readLength;
    }

    public void setReadLength(long readLength) {
        this.readLength = readLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(float downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }


    @Override
    public void registerObserver(DownloadObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(DownloadObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (DownloadObserver observer : observers) {
            observer.update(this);
        }
    }


    public static class Builder {
        //文件下载地址
        private String                 url;
        //存储目录
        private String                 directory;
        //文件名字
        private String                 fileName;
        private List<DownloadObserver> observers;

        public Builder(String url, String directory) {
            this.url = url;
            this.directory = directory;
            this.observers = new ArrayList<>();
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder registerObserver(DownloadObserver observer) {
            this.observers.add(observer);
            return this;
        }

        public TaskInfo build() {
            TaskInfo downLoadBean = new TaskInfo();
            downLoadBean.taskId = UUIDutils.uuid();
            downLoadBean.url = this.url;
            downLoadBean.directory = this.directory;
            downLoadBean.fileName = this.fileName;
            downLoadBean.observers = this.observers;
            return downLoadBean;
        }
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "taskId='" + taskId + '\'' +
                ", directory='" + directory + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentLength=" + contentLength +
                ", readLength=" + readLength +
                ", url='" + url + '\'' +
                ", downloadState=" + downloadState +
                ", observers=" + observers +
                ", downloadSpeed=" + downloadSpeed +
                '}';
    }
}
