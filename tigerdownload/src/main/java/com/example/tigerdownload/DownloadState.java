package com.example.tigerdownload;

/**
 * 创建时间 2018/9/11
 *
 * @author plani
 */
public enum DownloadState{


    /**
     * 任务建立 但没有开始
     */
    STATE_NORMAL,

    /**
     * 新任务建立
     */
    STATE_NEWTASK,

    /**
     * 等待下载
     */
    STATE_WAIT,

    /**
     * 正在下载中
     */
    STATE_lOADING,

    /**
     * 暂停
     */
    STATE_PAUSE,

    /**
     * 更新
     */
    STATE_UPDATE,

    /**
     * 下载完成
     */
    STATE_FINISH,
    /**
     * 下载失败
     */
    STATE_ERROR,
    /**
     * 安装失败
     */
    STATE_INSTALL_ERROR,
    /**
     * 安装成功
     */
    STATE_INSTALL_SUCCESS;

}
