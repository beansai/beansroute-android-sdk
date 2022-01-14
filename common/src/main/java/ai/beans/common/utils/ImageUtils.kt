package ai.beans.common.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun isImageContent(context: Context, uri: Uri?): Boolean {
    if (uri == null)
        return false

    var scheme: String? = ""
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        scheme = context?.getContentResolver()?.getType(uri)
    } else {
        var extension = ""
        val path = uri.path
        if (!TextUtils.isEmpty(path)) {
            val i = path!!.lastIndexOf('.')
            if (i > 0) {
                extension = uri.path!!.substring(i + 1)
                scheme = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
            }
        }
    }
    return !TextUtils.isEmpty(scheme) && scheme!!.startsWith("image/")
}

fun getImageFileFromUri(activity : Activity, uri : Uri) : File {
    //We turn the image into a bitmap....
    //then save bitmap bits into a file...
    //free the bitmap and return the file
    var bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri)
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    var file = File(activity.getExternalFilesDir(null), Date().toString() + "_" + "BeansMapperImage.jpg")
    var fs = FileOutputStream(file)
    fs.use{ outputStream -> byteArrayOutputStream.writeTo(outputStream) }
    fs.close()
    return file
}

fun deleteLocalImage(image_path : String) {
    if (!(image_path!!.startsWith("http:") || image_path!!.startsWith("content:"))) {
        //its a file from "internal storage...we can try and delete it
        MainScope().launch(Dispatchers.IO) {
            val file = File(image_path)
            if(file != null) {
                file.delete()
            }
        }
    }
}

fun getUriFromImageUrl(url: String?) : Uri? {
    var uri: Uri? = null
    if(url != null) {
        if (url!!.startsWith("https:") || url!!.startsWith("http:") || url!!.startsWith("content:")) {
            uri = Uri.parse(url)
        } else {
            uri = Uri.fromFile(File(url))
        }
    }
    return uri

}
