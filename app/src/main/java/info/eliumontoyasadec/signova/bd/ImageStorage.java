package info.eliumontoyasadec.signova.bd;
// ImageStorage.java
import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class ImageStorage {

    public static String saveBitmapToInternal(Context context, Bitmap bitmap) throws Exception {
        File dir = new File(context.getFilesDir(), "images");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "img_" + System.currentTimeMillis() + ".png";
        File outFile = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        return outFile.getAbsolutePath();
    }
}