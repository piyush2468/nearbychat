package com.rndtechnosoft.fynder.utility;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.net.URLConnection;

/**
 * Created by Ravi on 1/20/2017.
 */

public class MyImageUtil {


    public static Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v("ImageUtil", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);

        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
//
        Log.v("ImageUtil", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
//        bm.recycle();
        return bm;
    }

    public static boolean isDownloaded(Uri loadUri) {
        if (loadUri == null) {
            return false;
        }
        ImageRequest imageRequest = ImageRequest.fromUri(loadUri);
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest, null);
        return ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey) ||
                ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey);
    }

    public static  boolean isImageFile(File file){
        String mimeType = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
        return mimeType == null || mimeType.startsWith("image");
//        String[] imageFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
//        for (String extension : imageFileExtensions)
//        {
//            if (file.getName().toLowerCase().endsWith(extension))
//            {
//                return true;
//            }
//        }
//        return false;
//        return BitmapFactory.decodeFile(file.getAbsolutePath()) != null;
    }

}
