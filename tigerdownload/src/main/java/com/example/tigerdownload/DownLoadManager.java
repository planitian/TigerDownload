package com.example.tigerdownload;

import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.tigerdownload.bean.TaskInfo;
import com.example.tigerdownload.utils.CommonUtils;
import com.example.tigerdownload.utils.FileUtils;
import com.example.tigerdownload.utils.ValidFileName;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * 创建时间 2018/9/4
 *
 * @author plani
 */
public class DownLoadManager {
    private static final String                       TAG              = "DownLoadManager";
    private        SimpleArrayMap<String, TaskInfo>   downloadMap      = new SimpleArrayMap<>();
    private        SimpleArrayMap<String, Disposable> disposableMap    = new SimpleArrayMap<>();
    private static DownLoadManager                    mdownLoadManager = new DownLoadManager();
    private        ArrayBlockingQueue<TaskInfo>         waitQueue        = new ArrayBlockingQueue<>(5);

    public static DownLoadManager getInstance() {
        if (mdownLoadManager == null) {
            synchronized (mdownLoadManager) {
                if (mdownLoadManager == null) {
                    mdownLoadManager = new DownLoadManager();
                }
            }
        }
        return mdownLoadManager;
    }

    //添加任务
    public DownLoadManager addTask(TaskInfo downLoadBean) {
        if (downloadMap.containsKey(downLoadBean)) {
            Log.d(TAG, "下载队列中 已存在任务"+downLoadBean.getTaskId());
        } else {
            downloadMap.put(downLoadBean.getTaskId(), downLoadBean);
            Log.d(TAG, "下载队列添加任务成功"+downLoadBean.getTaskId());
        }
        return this;
    }

    //开始任务
    public boolean startTask(TaskInfo task) {
        boolean success = true;
        String taskID = task.getTaskId();
        if (downloadMap.containsKey(taskID)) {
            if (disposableMap.size() > 1) {
                boolean isInto = waitQueue.offer(task);
                if (isInto) {
                    Log.d(TAG, "下载队列已满，加入等待队列");
                    //设置状态
                    downloadMap.get(taskID).setDownloadState(DownloadState.STATE_WAIT);
                    //通知观察者
                    notifyObserver(downloadMap.get(taskID));
                } else {
                    Log.d(TAG, "过多的下载进程，并不会加快速度");
                    success = false;
                }
            }
            downloadMap.get(taskID).setDownloadState(DownloadState.STATE_NEWTASK);
            notifyObserver(downloadMap.get(taskID));
            Disposable disposable = start(downloadMap.get(taskID));
            this.disposableMap.put(taskID, disposable);
            Log.d(TAG, "disposable 队列添加 "+taskID);
        } else {
            Log.d(TAG, "开始下载失败 下载队列没有此任务"+taskID);
            throw new NullPointerException("开始下载失败 下载队列没有此任务 " + taskID);
        }
        return success;
    }

    //暂停任务
    public void pauseTask(TaskInfo task) {
        String taskId = task.getTaskId();
        if (downloadMap.containsKey(taskId)) {
            downloadMap.get(taskId).setDownloadState(DownloadState.STATE_PAUSE);
            if (this.disposableMap.containsKey(taskId)) {
                this.disposableMap.get(taskId).dispose();
                this.disposableMap.remove(taskId);
                Log.d(TAG,  "disposable 队列删除 "+taskId);
            } else {
                Log.d(TAG,  "下载队列中有此任务，但还没有开始下载"+taskId);
            }
        } else {
            Log.d(TAG,   "下载队列中 没有此任务"+taskId);
//            throw new NullPointerException("下载队列没有此任务 " + taskId);
        }
    }

    //重新开始
    public void reStartTask(TaskInfo task) {
        String taskId = task.getTaskId();
        if (downloadMap.containsKey(taskId)) {
            Log.d(TAG,   "disposableMap "+disposableMap.containsKey(taskId));
            //判断当前 正在下载队列 是否包含 想要下载的
            if (!disposableMap.containsKey(taskId)) {
                startTask(task);
                Log.d(TAG,   "下载队列有任务，现在重新开始下载 "+taskId);
            }
            Log.d(TAG,   "重新下载前 文件下载进度  已读  "+downloadMap.get(taskId).getReadLength()+" 全部长度  "+downloadMap.get(taskId).getContentLength());

        } else {
            Log.d(TAG,    "下载队列没有此任务"+ taskId);
            throw new NullPointerException("下载队列没有此任务 " + taskId);
        }
    }

    //返回当前队列是否有自己
    public boolean isDownloading(String taskId) {
        return disposableMap.containsKey(taskId);
    }

    //查询当前任务是否被添加
    public boolean isAddTask(String taskId) {
        return downloadMap.containsKey(taskId);
    }

    //注册观察者
    public void registerObserver(TaskInfo  task, DownloadObserver observer) {
        String taskId = task.getTaskId();
        if (downloadMap.containsKey(taskId)) {
            downloadMap.get(taskId).registerObserver(observer);
            Log.d(TAG,    "下载队列 任务  添加观察者 "+taskId);
        } else {
            Log.d(TAG,     "下载队列没有此任务  "+taskId);
            throw new NullPointerException("下载队列没有此任务 " + taskId);
        }
    }

    //移除观察者
    public void removeObserver(TaskInfo downLoadBean, DownloadObserver observer) {
        if (downloadMap.containsKey(downLoadBean.getTaskId())) {
            downloadMap.get(downLoadBean.getTaskId()).removeObserver(observer);
        } else {
            Log.d(TAG,     "下载队列没有此任务  "+downLoadBean.getTaskId());
            throw new NullPointerException("下载队列没有此任务 " + downLoadBean.getDownloadState());
        }
    }

    //通知观察者
    public void notifyObserver(TaskInfo downLoadBean) {
        if (downloadMap.containsKey(downLoadBean.getTaskId())) {
            downloadMap.get(downLoadBean.getTaskId()).notifyObservers();
        } else {
            Log.d(TAG,      "下载队列没有此任务 "+downLoadBean.getTaskId());
            throw new NullPointerException("下载队列没有此任务 " + downLoadBean.getDownloadState());
        }
    }


    //上面调用的 真是开始下载的
    private Disposable start(TaskInfo downLoadBean) {
        Log.d(TAG,      "后台服务要下载的 downloadbean "+downLoadBean.toString());
        Log.d(TAG,      "start 下载已读   "+downLoadBean.getReadLength());
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).addInterceptor(new DownLoadInterceptor(downLoadBean)).build();
        //没有传递baseUrl 在path哪里传递全部地址
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(CommonUtils.getBasUrl(downLoadBean.getUrl()))
                .build();
        DownLoadService service = retrofit.create(DownLoadService.class);
        return download(service, downLoadBean);
    }

    private Disposable download(DownLoadService service, TaskInfo downLoadBean) {

        Disposable disposable = service.download("bytes=" + downLoadBean.getReadLength() + "-", downLoadBean.getUrl())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retryWhen(new RetryWhenNetworkException())
                .map(new Function<Response<ResponseBody>, TaskInfo>() {
                    @Override
                    public TaskInfo apply(Response<ResponseBody> response) throws Exception {
                        try {
                            okhttp3.Response responseOkHttp = response.raw();
                            String fileName = ValidFileName.fileName(downLoadBean.getUrl());
                            String extension = null;
                            String mime = responseOkHttp.header("Content-Type");
                            if (mime != null && !mime.isEmpty()) {
                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                extension = mimeTypeMap.getExtensionFromMimeType(mime);
                            }

                            if (fileName.isEmpty()) {
                                fileName = System.currentTimeMillis() + "";
                            }

                            if (extension != null && !extension.isEmpty()) {
                                int dotPos = fileName.lastIndexOf('.');
                                if (0 <= dotPos) {
                                    fileName = fileName.substring(0, dotPos + 1) + extension;
                                }else {
                                    fileName = fileName + "." + extension;
                                }
                            }

                            String disposition = responseOkHttp.header("Content-Disposition");
                            if (disposition!=null&&!disposition.isEmpty()&&disposition.contains("filename")){
                                int equal = disposition.lastIndexOf("=");
                                fileName = disposition.substring(equal + 1);
                            }
                            Log.d(TAG,       " 文件名字 "+fileName);
                            if (downLoadBean.getFileName()==null||downLoadBean.getFileName().isEmpty()){
                                downLoadBean.setFileName(fileName);
                            }
                            File diretory = new File(downLoadBean.getDirectory());
                            if (!diretory.exists()){
                                diretory.mkdirs();
                            }

                            ResponseBody responseBody =response.body();
                            Log.d(TAG,       "isSuccess  "+response.isSuccessful());
                            if (!response.isSuccessful()){
                                Log.e("异常:", "response is false");
                                return null;
                            }
                            FileUtils.writeCache(responseBody, new File(downLoadBean.getDirectory(),downLoadBean.getFileName()), downLoadBean);

                        } catch (IOException e) {
                            Log.e("异常:", e.toString());
                            return null;
                        }
                        return downLoadBean;
                    }
                }).subscribe(downLoadBeanEnd -> {
                    disposableMap.remove(downLoadBean.getTaskId());
                    if (downLoadBeanEnd.getReadLength() < downLoadBeanEnd.getContentLength()) {
                        downloadMap.get(downLoadBean.getTaskId()).setReadLength(downLoadBeanEnd.getReadLength());
                        reStartTask(downLoadBean);
                        Log.d(TAG,       "下载文件不完整  但现在 开始重新下载  "+downLoadBeanEnd.getTaskId());
                    } else {
                        //下载完成  更改状态 通知观察者
                        downLoadBean.setDownloadState(DownloadState.STATE_FINISH);
                        Log.d(TAG,       "文件下载完成"+downLoadBean.getTaskId());
                        notifyObserver(downLoadBean);
                        //看当前是否有等待线程，如果 下载线程数小于两个的情况下
                        waitQueueStart();
                    }
                }, throwable -> {
                    Log.d(TAG,       "下载过程异常  id :"+ downLoadBean.getTaskId()+" 已读长度 "+downLoadBean.getReadLength()+" case: "+throwable.toString());
                    //下载出现异常  更改状态 通知观察者
                    disposableMap.remove(downLoadBean.getTaskId());
                    downLoadBean.setDownloadState(DownloadState.STATE_ERROR);
                    waitQueueStart();
                    notifyObserver(downLoadBean);
                });
        return disposable;
    }


    /**
     * 等待队列开始下载
     */
    public void waitQueueStart() {
        for (TaskInfo s : waitQueue) {
            Log.d(TAG,       "等待队列 数据 "+ s);

        }
        if (disposableMap.size() < 2) {
            //移除并返回头部的值，如果队列为空，返回null
           TaskInfo task = waitQueue.poll();
            if (task != null) {
                startTask(task);
            }
        }
    }
}