package com.example.fetchdownload;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.Download;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Error;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FetchDownload extends CordovaPlugin {

    private Fetch fetch;
    private Map<Integer, CallbackContext> progressCallbacks = new HashMap<>();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(cordova.getContext())
                .setDownloadConcurrentLimit(3)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);

        fetch.addListener(new FetchListener() {
            @Override
            public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {}

            @Override
            public void onCompleted(@NotNull Download download) {
                notifyProgress(download);
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                notifyProgress(download);
            }

            @Override
            public void onProgress(@NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                notifyProgress(download);
            }

            @Override
            public void onPaused(@NotNull Download download) {
                notifyProgress(download);
            }

            @Override
            public void onResumed(@NotNull Download download) {
                notifyProgress(download);
            }

            @Override
            public void onCancelled(@NotNull Download download) {
                notifyProgress(download);
            }

            @Override
            public void onRemoved(@NotNull Download download) {}

            @Override
            public void onDeleted(@NotNull Download download) {}
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startDownload")) {
            String url = args.getString(0);
            startDownload(url, callbackContext);
            return true;
        } else if (action.equals("pauseDownload")) {
            int id = args.getInt(0);
            pauseDownload(id, callbackContext);
            return true;
        } else if (action.equals("resumeDownload")) {
            int id = args.getInt(0);
            resumeDownload(id, callbackContext);
            return true;
        } else if (action.equals("stopDownload")) {
            int id = args.getInt(0);
            stopDownload(id, callbackContext);
            return true;
        } else if (action.equals("getDownloads")) {
            getDownloads(callbackContext);
            return true;
        } else if (action.equals("onProgress")) {
            int id = args.getInt(0);
            progressCallbacks.put(id, callbackContext);
            return true;
        }
        return false;
    }

    private void startDownload(String url, CallbackContext callbackContext) {
        Request request = new Request(url, cordova.getContext().getFilesDir() + "/downloads");
        fetch.enqueue(request, updatedRequest -> {
            callbackContext.success(updatedRequest.getId());
        }, error -> {
            callbackContext.error(error.toString());
        });
    }

    private void pauseDownload(int id, CallbackContext callbackContext) {
        fetch.pause(id);
        callbackContext.success();
    }

    private void resumeDownload(int id, CallbackContext callbackContext) {
        fetch.resume(id);
        callbackContext.success();
    }

    private void stopDownload(int id, CallbackContext callbackContext) {
        fetch.remove(id);
        callbackContext.success();
    }

    private void getDownloads(CallbackContext callbackContext) {
        fetch.getDownloads(downloads -> {
            JSONArray downloadsArray = new JSONArray();
            for (Download download : downloads) {
                JSONObject downloadObject = new JSONObject();
                try {
                    downloadObject.put("id", download.getId());
                    downloadObject.put("url", download.getUrl());
                    downloadObject.put("status", download.getStatus().toString());
                    downloadObject.put("progress", download.getProgress());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                downloadsArray.put(downloadObject);
            }
            callbackContext.success(downloadsArray);
        });
    }

    private void notifyProgress(Download download) {
        CallbackContext callbackContext = progressCallbacks.get(download.getId());
        if (callbackContext != null) {
            JSONObject progressObject = new JSONObject();
            try {
                progressObject.put("id", download.getId());
                progressObject.put("progress", download.getProgress());
                progressObject.put("status", download.getStatus().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(PluginResult.Status.OK, progressObject);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }
}
