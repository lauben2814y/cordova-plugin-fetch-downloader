var exec = require('cordova/exec');

var FetchDownload = {
    startDownload: function(url, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'FetchDownload', 'startDownload', [url]);
    },
    pauseDownload: function(id, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'FetchDownload', 'pauseDownload', [id]);
    },
    resumeDownload: function(id, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'FetchDownload', 'resumeDownload', [id]);
    },
    stopDownload: function(id, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'FetchDownload', 'stopDownload', [id]);
    },
    getDownloads: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'FetchDownload', 'getDownloads', []);
    },
    onProgress: function(id, progressCallback) {
        exec(progressCallback, null, 'FetchDownload', 'onProgress', [id]);
    }
};

module.exports = FetchDownload;
