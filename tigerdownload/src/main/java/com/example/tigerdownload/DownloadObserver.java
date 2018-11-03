package com.example.tigerdownload;

import com.example.tigerdownload.bean.TaskInfo;

/**
 * 观察者 接口
 * 创建时间 2018/9/11
 *
 * @author plani
 */
public interface DownloadObserver  {
    //观察者 接受事件
    void update(TaskInfo downLoadBean);

}
