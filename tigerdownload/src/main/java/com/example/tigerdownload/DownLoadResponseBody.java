package com.example.tigerdownload;

import com.example.tigerdownload.bean.TaskInfo;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 返回主体  在这里面 读取下载进度
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public class DownLoadResponseBody extends ResponseBody {
    private ResponseBody   responseBody;
    private BufferedSource bufferedSource;
    private TaskInfo       downLoadBean;
    private long           lastReadLength;

    public DownLoadResponseBody(TaskInfo downLoadBean, ResponseBody responseBody) {
        this.downLoadBean = downLoadBean;
        this.responseBody = responseBody;
        this.lastReadLength = downLoadBean.getReadLength();
        this.downLoadBean.setDownloadState(DownloadState.STATE_lOADING);
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long last = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                long now = System.currentTimeMillis();
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (now - last > 1000 || bytesRead == -1) {
                    last = now;
                    if (null != downLoadBean) {
                        if (lastReadLength == 0) {
                            downLoadBean.setReadLength(totalBytesRead);
                            downLoadBean.setContentLength(responseBody.contentLength());
                            downLoadBean.setDownloadState(DownloadState.STATE_lOADING);
                            updateDownloadSpeed(downLoadBean);
                            DownLoadManager.getInstance().notifyObserver(downLoadBean);
                        } else {
                            downLoadBean.setReadLength(lastReadLength + totalBytesRead);
                            downLoadBean.setDownloadState(DownloadState.STATE_lOADING);
                            updateDownloadSpeed(downLoadBean);
                            DownLoadManager.getInstance().notifyObserver(downLoadBean);
                        }
                    }
                }
                return bytesRead;
            }
        };
    }

    private long oldTime = -1, oldLength = -1;

    //计算下载速度
    private void updateDownloadSpeed(TaskInfo downLoadBean) {
        if ((System.currentTimeMillis() - oldTime) < 1000) {
            return;
        }

        if (oldTime > 0) {

            float speed = (downLoadBean.getReadLength() - oldLength)
                    / ((System.currentTimeMillis() - oldTime) / 1000f);
            downLoadBean.setDownloadSpeed(speed);
        }
        oldTime = System.currentTimeMillis();
        oldLength = downLoadBean.getReadLength();
    }
}
