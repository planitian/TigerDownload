package com.example.tigerdownload;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public interface DownLoadService {

    /**
     * @param start  起始字节
     * @param url
     * @return
     */
    @Streaming   //防止下载文件出现OOM
    @GET
    Observable<Response<ResponseBody>> download(@Header("RANGE") String start, @Url String url);
}
