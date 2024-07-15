package com.example;

import com.tonyofrancis.fetch2.*;
import com.tonyofrancis.fetch2.Request;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FetchDownloader extends CordovaPlugin {

    private Fetch fetch;
    private Map<Integer, CallbackContext> progressCallbacks = new HashMap<>();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(cordova.getActivity())
                .setDownloadConcurrentLimit(3)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        setupFetchListener();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "download":
                String url = args.getString(0);
                String path = args.getString(1);
                this.download(url, path, callbackContext);
                return true;
            case "pause":
                int pauseId = args.getInt(0);
                this.pause(pauseId, callbackContext);
                return true;
            case "resume":
                int resumeId = args.getInt(0);
                this.resume(resumeId, callbackContext);
                return true;
            case "cancel":
                int cancelId = args.getInt(0);
                this.cancel(cancelId, callbackContext);
                return true;
            case "getProgress":
                int progressId = args.getInt(0);
                this.getProgress(progressId, callbackContext);
                return true;
            case "restoreDownloads":
                this.restoreDownloads(callbackContext);
                return true;
        }
        return false;
    }

    private void download(String url, String path, final CallbackContext callbackContext) {
        Request request = new Request(url, path);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);

        fetch.enqueue(request, updatedRequest -> {
            progressCallbacks.put(updatedRequest.getId(), callbackContext);
            try {
                JSONObject result = new JSONObject();
                result.put("id", updatedRequest.getId());
                result.put("status", "enqueued");
                callbackContext.success(result);
            } catch (JSONException e) {
                callbackContext.error("Error creating JSON response");
            }
        }, error -> {
            callbackContext.error("Error: " + error.getName());
        });
    }

    private void pause(int id, CallbackContext callbackContext) {
        fetch.pause(id);
        callbackContext.success("Paused download " + id);
    }

    private void resume(int id, CallbackContext callbackContext) {
        fetch.resume(id);
        callbackContext.success("Resumed download " + id);
    }

    private void cancel(int id, CallbackContext callbackContext) {
        fetch.cancel(id);
        callbackContext.success("Cancelled download " + id);
    }

    private void getProgress(int id, CallbackContext callbackContext) {
        fetch.getDownload(id, download -> {
            try {
                JSONObject progress = new JSONObject();
                progress.put("id", download.getId());
                progress.put("status", download.getStatus().name());
                progress.put("progress", download.getProgress());
                progress.put("downloadedBytes", download.getDownloaded());
                progress.put("totalBytes", download.getTotal());
                callbackContext.success(progress);
            } catch (JSONException e) {
                callbackContext.error("Error creating JSON response");
            }
        }, error -> {
            callbackContext.error("Error getting progress: " + error.getName());
        });
    }

    private void restoreDownloads(CallbackContext callbackContext) {
        fetch.getDownloads(downloads -> {
            JSONArray restoredDownloads = new JSONArray();
            for (Download download : downloads) {
                try {
                    JSONObject downloadInfo = new JSONObject();
                    downloadInfo.put("id", download.getId());
                    downloadInfo.put("status", download.getStatus().name());
                    downloadInfo.put("progress", download.getProgress());
                    restoredDownloads.put(downloadInfo);
                } catch (JSONException e) {
                    // Skip this download if there's an error
                }
            }
            callbackContext.success(restoredDownloads);
        }, error -> {
            callbackContext.error("Error restoring downloads: " + error.getName());
        });
    }

    private void setupFetchListener() {
        fetch.addListener(new FetchListener() {
            @Override
            public void onProgress(@NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                CallbackContext callback = progressCallbacks.get(download.getId());
                if (callback != null) {
                    try {
                        JSONObject progress = new JSONObject();
                        progress.put("id", download.getId());
                        progress.put("status", "progress");
                        progress.put("progress", download.getProgress());
                        progress.put("etaInSeconds", etaInMilliSeconds / 1000);
                        progress.put("downloadedBytesPerSecond", downloadedBytesPerSecond);
                        PluginResult progressResult = new PluginResult(PluginResult.Status.OK, progress);
                        progressResult.setKeepCallback(true);
                        callback.sendPluginResult(progressResult);
                    } catch (JSONException e) {
                        // Ignore
                    }
                }
            }

            @Override
            public void onCompleted(@NotNull Download download) {
                CallbackContext callback = progressCallbacks.get(download.getId());
                if (callback != null) {
                    try {
                        JSONObject result = new JSONObject();
                        result.put("id", download.getId());
                        result.put("status", "completed");
                        callback.success(result);
                    } catch (JSONException e) {
                        callback.error("Error creating JSON response");
                    }
                    progressCallbacks.remove(download.getId());
                }
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                CallbackContext callback = progressCallbacks.get(download.getId());
                if (callback != null) {
                    callback.error("Error: " + error.getName());
                    progressCallbacks.remove(download.getId());
                }
            }

            // Implement other methods of FetchListener as needed
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fetch.close();
    }
}