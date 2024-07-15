var exec = require('cordova/exec');

var FetchDownloader = {
    download: function(url, path, success, error) {
        exec(success, error, "FetchDownloader", "download", [url, path]);
    },
    pause: function(id, success, error) {
        exec(success, error, "FetchDownloader", "pause", [id]);
    },
    resume: function(id, success, error) {
        exec(success, error, "FetchDownloader", "resume", [id]);
    },
    cancel: function(id, success, error) {
        exec(success, error, "FetchDownloader", "cancel", [id]);
    },
    getProgress: function(id, success, error) {
        exec(success, error, "FetchDownloader", "getProgress", [id]);
    },
    restoreDownloads: function(success, error) {
        exec(success, error, "FetchDownloader", "restoreDownloads", []);
    }
};

module.exports = FetchDownloader;