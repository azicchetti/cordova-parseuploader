var exec = require('cordova/exec');

exports.upload = function(fileURI, success, error) {
    exec(success, error, "ParseUploader", "upload", [fileURI]);
};
