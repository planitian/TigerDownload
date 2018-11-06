
## TigerDown
### 是一款使用了 Rxjava2,retrofit2,以及okhttp3的开源库。

### 优点如下
 1. **根据url链接，自动确定文件名及其扩展格式，不用再为格式发愁。**
 2. **自动重连机制**
 3. **写入文件方法，根据返回流的不同，写入方式多样性**
 4. **可以获得下载速度，下载进度**
 
# 配置教程
 5. Add the JitPack repository to your build file（将JitPack存储库添加到构建文件中）**注意  是project的build.gradle文件**
```j
   	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
  
 2. Add the dependency(添加依赖关系)
 ```java
 	dependencies {
	        implementation 'com.github.planitian:TigerDownload:1.1.5'
	}
 ```


## 使用

```java
//下载链接
String url = "https://uu.gdl.netease.com/2183/UU-2.10.5.exe"; 
//文件保存目录
String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;  
TaskInfo downLoadBean = new TaskInfo.Builder(url, savePath)  
        //注册观察者  
  .registerObserver(new DownloadObserver() {  
  
            //taskinfo  携带着下载的信息，如已经读取的字节数，总文件长度（如果，http返回了 文件长度）  
 //下载状态@Link{ DownloadState } @Override  
  public void update(TaskInfo downLoadBean) {  
  
                if (downLoadBean.getContentLength() != 0) {  
                    int progress = (int) (100 * downLoadBean.getReadLength() / downLoadBean.getContentLength());  
                    /*  
 1. 注意 这里只有配置了 runMain(true) 才可以直接更新ui，否则报异常 * */  progressBar.setProgress(progress);  
                /*    runOnUiThread(new Runnable() {  
 @Override public void run() {  
 } });*/  }  
            }  
        }).runMain(true).build();  
//必须先addTask，然后才可以 startTask
DownLoadManager.getInstance().addTask(downLoadBean).startTask(downLoadBean);
```
## DownLoadManager方法详解
 1. addTask(TaskInfo taskInfo)  添加任务，**这是第一步 必须的**
 2. startTask(TaskInfo task)       开始任务
 3. pauseTask(TaskInfo task)     暂停任务，如果想达到取消任务的效果，只需要调用pauseTask(TaskInfo task)。然后，将taskinfo的setReadLength(long readLength)，即可完成。
 4. reStartTask(TaskInfo task)     重新开始
 5. registerObserver(TaskInfo  task, DownloadObserver observer) 注册观察者
 6. removeObserver(TaskInfo taskInfo, DownloadObserver observer) 移除观察者
 7. notifyObserver(TaskInfo taskInfo) 通知观察者   **注意：可以在自己需要的地方 调用 通知**
 8. isDownloading(String taskId)  返回当前队列是否有自己
 9. isAddTask(String taskId)   查询当前任务是否被添加 即addTask


**作者联系方式 QQ：596971449** 


