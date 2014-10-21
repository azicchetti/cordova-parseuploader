cordova-parseuploader
=====================

Cordova plugin to upload *image files* on Parse.com using native APIs.
I've yet to refactor the code to allow uploading of other file types.

If you want to upload a file to Parse in pure javascript, you have to provide its data in base64:

```
var parseFile = new Parse.File("picture.jpg", { base64: pictureBase64Data });
parseFile.save().then(function(){
	...
}, function(err){
	...
});
```

This method may work for text files but it isn't really suited for for big images, for instance the ones
you may get from the PHOTOLIBRARY of your phone.
Remember that Cordova ignores the "quality" parameter when photos are selected from the device's gallery, thus not downscaling them to a lower quality.
The doc also says to set Camera.destinationType to FILE_URI rather than DATA_URL to avoid memory problems.

This is when the ParseUploader comes in handy.


Dependencies
============

This plugin requires the phonegap-parse-plugin (<a href="https://github.com/avivais/phonegap-parse-plugin"></a> or <a href="https://github.com/azicchetti/phonegap-parse-plugin"></a>)


Usage
=====

Before using this plugin, you MUST initialize the native Parse SDK using the phonegap-parse-plugin:
```
parsePlugin.initialize(appId, clientKey, function() {
	//from now on, you can use the uploader
}, function(e) {
	//error
});
```

The JS interface exposes the `upload(fileURI, successCallback, errorCallback)` function:

```
cordova.plugins.ParseUploader.upload(fileURI, function(pfdata){
	...
});
```

fileURI is usually returned by Cordova when you invoke certain filesystem-related functions.
These may be found in the File or Camera plugins, for instance.


Uploading a picture from the PHOTOLIBRARY to Parse:

```
navigator.camera.getPicture(function(fileURI){
	cordova.plugins.ParseUploader.upload(fileURI, function(pfdata){
		var pfile = new Parse.File(pfdata.name);
		pfile._url = pfdata.url;
		//pfile is now a working Parse.File object!
	}, function(err){
		//upload failed
	});
}, function(error){
	//error handling
}, {
	destinationType: Camera.DestinationType.FILE_URI,
	sourceType: Camera.PictureSourceType.PHOTOLIBRARY,
	mediaType: Camera.MediaType.PICTURE,
	correctOrientation: true
});
```

The successCallback is invoked with the `pfdata` argument, containing the name and the url of the uploaded file.
Use that to build a valid Parse.File javascript object (the above example shows how it is done).


Notes
=====

Images are downscaled to a max width of 800px and to a max height of 600px, preserving the image ratio.
I'll refactor the code to allow specifying different sizes.


