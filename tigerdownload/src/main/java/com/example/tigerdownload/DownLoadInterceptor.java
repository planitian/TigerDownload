package com.example.tigerdownload;

import android.nfc.Tag;
import android.util.Log;

import com.example.tigerdownload.bean.TaskInfo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载进度拦截器   在okhttp客户端初始化时候添加进去
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public class DownLoadInterceptor implements Interceptor {
    private static final String TAG = "DownLoadInterceptor";
    private TaskInfo downLoadBean;

    public DownLoadInterceptor(TaskInfo downLoadBean) {
        this.downLoadBean = downLoadBean;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //得到原始的request
        Request original = chain.request();
        //对request 进行 重新创建 返回新的
        Request request = original.newBuilder().addHeader("Accept-Encoding", "identity").build();
        //得到 原始response
        Response  response = chain.proceed(request);
        Log.d(TAG, "响应头  " + response.headers());
        //得到Response  对它的response 进行包装  用我们自己定义的DownLoadResponseBody
        return response.newBuilder().body(new DownLoadResponseBody(downLoadBean,response.body())).build();
    }
}
