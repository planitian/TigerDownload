package com.example.tigerdownload;

/**
 * 被观察者接口
 * 创建时间 2018/9/11
 *
 * @author plani
 */
public interface DownloadObserable {
   //注册观察者
   void registerObserver(DownloadObserver observer);
   ///移除观察者
    void removeObserver(DownloadObserver observer);
    //通知观察者
    void notifyObservers();
}
