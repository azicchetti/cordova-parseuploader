package org.apache.cordova.plugins;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

/**
* This class uploads a file through Parse services.
*/
public class ParseUploader extends CordovaPlugin {

	private final String TAG = "CORDOVA_PARSE_UPLOADER";
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("upload")) {
			String fileURI = args.getString(0);
			try {
				this.upload(fileURI, callbackContext);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	private void upload(String fileURI, final CallbackContext callbackContext) throws IOException {
		if (fileURI != null && fileURI.length() > 0) {
			//reading the file into a byte array

			Uri uri = android.net.Uri.parse(fileURI);
			//ContentResolver cr = cordova.getActivity().getContentResolver();
			//InputStream is = cr.openInputStream(uri);
			
			//Bitmap mp = getBitmap(uri);
			
			//ByteArrayOutputStream bos = new ByteArrayOutputStream();
			//byte[] b = new byte[1024];
			//int bytesRead;
			//while ((bytesRead = is.read(b)) != -1) {
			//   bos.write(b, 0, bytesRead);
			//}
			
			ByteArrayOutputStream bos = getBitmap(uri);
			byte[] fileBytes = bos.toByteArray();
			
			String fileName = uri.getLastPathSegment();
			final ParseFile file = new ParseFile(fileName, fileBytes);
			
			file.saveInBackground(new SaveCallback() {
				public void done(ParseException e) {
					if (e == null) { //success
						JSONObject o = new JSONObject();
						try {
							 o.put("name", file.getName());
							 o.put("url", file.getUrl());
						} catch (JSONException er){ er.printStackTrace(); }
						
						PluginResult res = new PluginResult(PluginResult.Status.OK, o);
						callbackContext.sendPluginResult(res);
					} else { //fail
						callbackContext.error(e.getMessage());
					}
				}
			});
		} else {
			callbackContext.error("Expected a non-empty file path argument.");
		}
	}
	
	private ByteArrayOutputStream getBitmap(Uri uri) throws FileNotFoundException {

		ContentResolver cr = cordova.getActivity().getContentResolver();
		InputStream is = cr.openInputStream(uri);
		
		InputStream in = null;
		try {
		    final int IMAGE_MAX_SIZE = 480000; // 0.480 MP
		    in = cr.openInputStream(uri);

		    // Decode image size
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(in, null, o);
		    in.close();

		    int scale = 1;
		    while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > 
		          IMAGE_MAX_SIZE) {
		       scale++;
		    }
		    Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

		    Bitmap b = null;
		    in = cr.openInputStream(uri);
		    if (scale > 1) {
		        scale--;
		        // scale to max possible inSampleSize that still yields an image
		        // larger than target
		        o = new BitmapFactory.Options();
		        o.inSampleSize = scale;
		        b = BitmapFactory.decodeStream(in, null, o);

		        // resize to desired dimensions
		        int height = b.getHeight();
		        int width = b.getWidth();
		        Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

		        double y = Math.sqrt(IMAGE_MAX_SIZE
		                / (((double) width) / height));
		        double x = (y / height) * width;

		        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, 
		           (int) y, true);
		        b.recycle();
		        b = scaledBitmap;

		        System.gc();
		    } else {
		        b = BitmapFactory.decodeStream(in);
		    }
		    in.close();

		    Log.d(TAG, "bitmap size - width: " +b.getWidth() + ", height: " + b.getHeight());
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    b.compress(Bitmap.CompressFormat.JPEG, 80, out); 
		    return out;
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage(),e);
		    return null;
		}
	}
}
