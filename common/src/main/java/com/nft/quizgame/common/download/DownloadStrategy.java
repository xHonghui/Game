package com.nft.quizgame.common.download;

import android.app.Application;

public interface DownloadStrategy {
    void init(Application application);
    void startDownload(String url, String baseFilePath, String fileName, DownloadListener listener);
    void pauseDownload(int taskId);
    void cancelDownload(String url, String baseFilePath, String fileName);
    //downloading completed error none
    int getDownloadStatus(String url, String baseFilePath, String fileName);
    int getDownloadTaskId(String url, String baseFilePath, String fileName);
    DownloadTask getDownloadProgress(String url, String baseFilePath, String fileName);
    void pauseAll();
}
