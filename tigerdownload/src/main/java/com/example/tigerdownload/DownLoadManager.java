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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public DownLoadManager addTask(TaskInfo taskInfo) {
        if (downloadMap.containsKey(taskInfo)) {
            Log.d(TAG, "下载队列中 已存在任务"+taskInfo.getTaskId());
        } else {
            downloadMap.put(taskInfo.getTaskId(), taskInfo);
            Log.d(TAG, "下载队列添加任务成功"+taskInfo.getTaskId());
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
    public void removeObserver(TaskInfo taskInfo, DownloadObserver observer) {
        if (downloadMap.containsKey(taskInfo.getTaskId())) {
            downloadMap.get(taskInfo.getTaskId()).removeObserver(observer);
        } else {
            Log.d(TAG,     "下载队列没有此任务  "+taskInfo.getTaskId());
            throw new NullPointerException("下载队列没有此任务 " + taskInfo.getDownloadState());
        }
    }

    //通知观察者
    public void notifyObserver(TaskInfo taskInfo) {
        if (downloadMap.containsKey(taskInfo.getTaskId())) {
            downloadMap.get(taskInfo.getTaskId()).notifyObservers();
        } else {
            Log.d(TAG,      "下载队列没有此任务 "+taskInfo.getTaskId());
            throw new NullPointerException("下载队列没有此任务 " + taskInfo.getDownloadState());
        }
    }


    //上面调用的 真是开始下载的
    private Disposable start(TaskInfo taskInfo) {
        Log.d(TAG,      "后台服务要下载的 taskInfo "+taskInfo.toString());
        Log.d(TAG,      "start 下载已读   "+taskInfo.getReadLength());
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).addInterceptor(new DownLoadInterceptor(taskInfo)).build();
        //没有传递baseUrl 在path哪里传递全部地址
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(CommonUtils.getBasUrl(taskInfo.getUrl()))
                .build();
        DownLoadService service = retrofit.create(DownLoadService.class);
        return download(service, taskInfo);
    }

    private Disposable download(DownLoadService service, TaskInfo taskInfo) {

        Disposable disposable = service.download("bytes=" + taskInfo.getReadLength() + "-", taskInfo.getUrl())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retryWhen(new RetryWhenNetworkException())
                .map(new Function<Response<ResponseBody>, TaskInfo>() {
                    @Override
                    public TaskInfo apply(Response<ResponseBody> response) throws Exception {
                        try {
                            okhttp3.Response responseOkHttp = response.raw();
                            //从下载url中解析出文件名字
                            String fileName = ValidFileName.fileName(taskInfo.getUrl());
                            String extension = null;
                            //文件返回值类型
                            String mime = responseOkHttp.header("Content-Type");
                            //如果有的话 找到对应的文件格式
                            if (mime != null && !mime.isEmpty()) {
                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                                extension = mimeTypeMap.getExtensionFromMimeType(mime);
                            }
                            //如果文件名为空 则默认为当前时间为准
                            if (fileName.isEmpty()) {
                                fileName = CommonUtils.nowTime();
                            }
                            //如果有文件格式的话，以解析的文件格式为准
                            if (extension != null && !extension.isEmpty()) {
                                int dotPos = fileName.lastIndexOf('.');
                                // 文件名有无文件格式
                                if (0 <= dotPos) {
                                    fileName = fileName.substring(0, dotPos + 1) + extension;//替换
                                }else {
                                    fileName = fileName + "." + extension;//直接加上
                                }
                            }
                            //如果Content-Disposition 里面有filename 以这个为准
                            String disposition = responseOkHttp.header("Content-Disposition");
                            if (disposition!=null&&!disposition.isEmpty()&&disposition.contains("filename")){
                                int equal = disposition.lastIndexOf("=");
                                fileName = disposition.substring(equal + 1);
                            }
                           //最后 如果用户自定义的 则以用户为准
                            if (taskInfo.getFileName()==null||taskInfo.getFileName().isEmpty()){
                                taskInfo.setFileName(fileName);
                            }

                            Log.d(TAG,       " 文件名字 "+fileName);
                            File diretory = new File(taskInfo.getDirectory());
                            if (!diretory.exists()){
                                diretory.mkdirs();
                            }

                            ResponseBody responseBody =response.body();
                            Log.d(TAG,       "isSuccess  "+response.isSuccessful());
                            if (!response.isSuccessful()){
                                Log.e("异常:", "response is false");
                                return null;
                            }
                            FileUtils.writeCache(responseBody, new File(taskInfo.getDirectory(),taskInfo.getFileName()), taskInfo);

                        } catch (IOException e) {
                            Log.e("异常:", e.toString());
                            return null;
                        }
                        return taskInfo;
                    }
                }).subscribe(taskInfoEnd -> {
                    disposableMap.remove(taskInfo.getTaskId());
                    if (taskInfoEnd.getReadLength() < taskInfoEnd.getContentLength()) {
                        downloadMap.get(taskInfo.getTaskId()).setReadLength(taskInfoEnd.getReadLength());
                        reStartTask(taskInfo);
                        Log.d(TAG,       "下载文件不完整  但现在 开始重新下载  "+taskInfoEnd.getTaskId());
                    } else {
                        //下载完成  更改状态 通知观察者
                        taskInfo.setDownloadState(DownloadState.STATE_FINISH);
                        Log.d(TAG,       "文件下载完成"+taskInfo.getTaskId());
                        notifyObserver(taskInfo);
                        //看当前是否有等待线程，如果 下载线程数小于两个的情况下
                        waitQueueStart();
                    }
                }, throwable -> {
                    Log.d(TAG,       "下载过程异常  id :"+ taskInfo.getTaskId()+" 已读长度 "+taskInfo.getReadLength()+" case: "+throwable.toString());
                    //下载出现异常  更改状态 通知观察者
                    disposableMap.remove(taskInfo.getTaskId());
                    taskInfo.setDownloadState(DownloadState.STATE_ERROR);
                    waitQueueStart();
                    notifyObserver(taskInfo);
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