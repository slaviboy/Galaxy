/*
* Copyright (C) 2024 Stanislav Georgiev
* https://github.com/slaviboy
*
*  NOTICE:  All information contained herein is, and remains the property
*  of Stanislav Georgiev and its suppliers, if any. The intellectual and
*  technical concepts contained herein are proprietary to Stanislav Georgiev
*  and its suppliers and may be covered by U.S. and Foreign Patents, patents
*  in process, and are protected by trade secret or copyright law. Dissemination
*  of this information or reproduction of this material is strictly forbidden
*  unless prior written permission is obtained from Stanislav Georgiev.
*/
package com.slaviboy.opengl.helpers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Object with static methods that are useful for everyday use, it includes method
 * that help read and write files.
 */
object StaticMethods {

    fun Boolean.toByte(): Byte {
        return if (this) 255.toByte() else 0.toByte()
    }

    fun Byte.toBoolean(): Boolean {
        return this == 255.toByte()
    }

    fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    /**
     * Extension method that request a focus to the edit text and opens the keyboard
     */
    fun EditText.focus() {

        // force open of the keyboard
        val inputMethodManager: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        this.isFocusable = true
        this.isFocusableInTouchMode = true
        this.visibility = View.VISIBLE
        this.isEnabled = true
        this.isCursorVisible = true
        this.post {
            (this.context as? Activity)?.runOnUiThread {
                this.requestFocus()
            }
        }
    }

    /**
     * Get the resource if from its string name
     * @param resIdName name of the value we want to retrieve its resource id
     * @param resType type of the resource "string", "drawable", "raw", "layout"...
     */
    fun Context.resIdByName(resIdName: String, resType: String): Int {
        if (resIdName.isEmpty()) return -1
        return resources.getIdentifier(resIdName, resType, packageName)
    }

    /**
     * Extension method for observing a live data only once, after which remove the observer
     */
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    /**
     * Get the resource id using a string value it can be either a string, drawable, or element id
     * getResourceId("myAppName", "string", getPackageName());
     */
    fun getResourceId(context: Context, variableName: String, resourceName: String, packageName: String): Int {
        return try {
            context.resources.getIdentifier(variableName, resourceName, packageName)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Extension function that allows for both -normal and bold, when we want to set particular part of the
     * string to bold we enclose them with double **, 'this is normal text, but **this is bold**'
     */
    fun String.makePartialTextsBold(): SpannableStringBuilder {
        var copy = this
        return SpannableStringBuilder().apply {
            var setSpan = true
            var next: String
            do {
                setSpan = !setSpan
                next = if (length == 0) copy.substringBefore("**", "")
                else copy.substringBefore("**")
                val start = length
                append(next)
                if (setSpan) {
                    setSpan(StyleSpan(Typeface.BOLD), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                copy = copy.removePrefix(next).removePrefix("**")
            } while (copy.isNotEmpty())
        }
    }

    /**
     * Generate round bitmap from existing bitmap
     * @param bitmap existing bitmap that will get round corners
     * @param backgroundColor background color behind the cut corners, if save as JPEG since alpha is lost we can set background color
     * @param cornerRadius corner radius of the round rectangle
     */
    fun getRoundedCornerBitmap(
        bitmap: Bitmap, backgroundColor: Int = Color.TRANSPARENT, cornerRadius: Float = 10f
    ): Bitmap {

        val roundBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(roundBitmap)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawColor(Color.TRANSPARENT)
        paint.color = color
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        // draw the round bitmap behind, the background color because when converted into
        // jpeg the background color becomes black
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(output)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(roundBitmap, rect, rect, null)
        roundBitmap.recycle()

        return output
    }

    /**
     * Rotate a point around a center with given angle
     * @param cx rotary center point x coordinate
     * @param cy rotary center point y coordinate
     * @param x x coordinate of the point that will be rotated
     * @param y y coordinate of the point that will be rotated
     * @param angle angle of rotation in degrees
     * @param anticlockWise rotate clockwise or anti-clockwise
     * @param resultPoint object where the result rotational point will be stored
     */
    fun rotate(cx: Float, cy: Float, x: Float, y: Float, angle: Float, anticlockWise: Boolean = false, resultPoint: PointF = PointF()): PointF {

        if (angle == 0f) {
            resultPoint.x = x
            resultPoint.y = y
            return resultPoint
        }

        val radians = if (anticlockWise) {
            (Math.PI / 180) * angle
        } else {
            (Math.PI / -180) * angle
        }

        val cos = Math.cos(radians)
        val sin = Math.sin(radians)
        val nx = (cos * (x - cx)) + (sin * (y - cy)) + cx
        val ny = (cos * (y - cy)) - (sin * (x - cx)) + cy

        resultPoint.x = nx.toFloat()
        resultPoint.y = ny.toFloat()
        return resultPoint
    }

    /**
     * Find the angle between two points
     * @param cx x coordinate of the center point
     * @param cy y coordinate of the center point
     * @param x x coordinate of the point that is rotating
     * @param y y coordinate of the point that is rotating
     */
    fun angleBetweenTwoPoints(cx: Float, cy: Float, x: Float, y: Float): Float {

        val dy = y - cy
        val dx = x - cx
        var angle = Math.atan2(dy.toDouble(), dx.toDouble())      // range (-PI, PI]
        angle *= 180 / Math.PI                                    // radians to degrees, range (-180, 180]
        angle = if (angle < 0) Math.abs(angle) else 360 - angle   // range [0, 360)

        return angle.toFloat()
    }

    /**
     * Distance between two points
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     */
    fun distancePointToPoint(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = (x1 - x2)
        val dy = (y1 - y2)
        return Math.sqrt(dx.toDouble() * dx + dy * dy).toFloat()
    }

    /**
     * Get the screen size of the device, the height include the toolbar and navigation bar
     * @param context activity context
     */
    fun getScreenSizeIncludingTopBottomBar(context: Context): Rect {

        val orientation = context.resources.configuration.orientation
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            windowManager.defaultDisplay
        }
        val screenSize = Point()
        display?.getRealSize(screenSize)
        val x = screenSize.x
        val y = screenSize.y
        return Rect(
            0, 0,
            x,  //if (orientation == Configuration.ORIENTATION_PORTRAIT) x else y, // width
            y   //if (orientation == Configuration.ORIENTATION_PORTRAIT) y else x  // height
        )
    }

    /**
     * Hide the keyboard
     * @param context activity context
     * @param view textview that is focused and has forced the keyboard
     */
    fun hideKeyboardFrom(context: Context, view: View?) {
        view ?: return
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    /**
     * Load string from the raw folder using a resource id of the given file.
     * @param resources resource from the context
     * @param resId resource id of the file
     */
    fun loadStringFromRawResource(resources: Resources, resId: Int): String {
        val rawResource = resources.openRawResource(resId)
        val content = streamToString(rawResource)
        try {
            rawResource.close()
        } catch (e: IOException) {
            throw e
        }
        return content
    }

    /**
     * Read the file from the raw folder using input stream
     */
    private fun streamToString(inputStream: InputStream): String {
        var l: String?
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        try {
            while (bufferedReader.readLine().also { l = it } != null) {
                stringBuilder.append(l)
            }
        } catch (e: IOException) {
        }
        return stringBuilder.toString()
    }

    /**
     * Load json or text file from the assets folder. Files inside the assets folder are read
     * only, so to save any changes you need to save new file in the eternal storage. Its is
     * used to read the initial values in the json/text file, after that read and write are
     * done inside the eternal storage.
     * @param context activity context
     * @param directoryName value of the directory example: "gson/opengl/"
     * @param fileName name of the file example: "opengl_data.json"
     */
    fun loadTextFromAsset(context: Context, directoryName: String, fileName: String): String {
        return try {
            val inputStream: InputStream = context.assets.open("$directoryName$fileName")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Load text or json file that is saved in the external storage of the phone
     * @param context activity context
     * @param fileDirectory value of the directory example: "gson/opengl/"
     * @param fileName name of the file example: "opengl_data.json"
     */
    fun openFile(context: Context, fileDirectory: String, fileName: String): String {

        // path to /data/user/0/com.slaviboy.galaxy/app_cached_projects
        val directory: File = context.getDir(fileDirectory, Context.MODE_PRIVATE)
        val file = File(directory, fileName)
        val fileInputStream: FileInputStream

        var result: String = ""
        if (file.exists()) {
            try {
                fileInputStream = FileInputStream(file)
                fileInputStream.bufferedReader().use {
                    result = it.readText()
                }
                fileInputStream.close()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return result
    }


    /**
     * Save text or json file to the external storage of the phone, used
     * to save setting if user changes the default one that are set from the assets folder.
     * @param context activity context
     * @param fileDirectory name of the directory example: "gson/opengl/"
     * @param fileName name of the file example: "opengl_data.json"
     * @param data string data
     */
    fun saveFile(context: Context, fileDirectory: String, fileName: String, data: String, usePublicCacheDirectory: Boolean) {

        // path to /data/user/0/com.slaviboy.galaxy/app_cached_projects
        val directory: File = if (usePublicCacheDirectory) {
            File(context.filesDir?.absolutePath + "/$fileDirectory")
        } else {
            context.getDir(fileDirectory, Context.MODE_PRIVATE)
        }
        if (!directory.exists()) directory.mkdir()

        val file = File(directory, fileName)
        val fileOutputStream: FileOutputStream

        try {

            fileOutputStream = FileOutputStream(file)
            fileOutputStream.bufferedWriter().use {
                it.write(data)
            }
            fileOutputStream.flush()
            fileOutputStream.close()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Convert every float value from FloatArray to its corresponding 4 byte values, that
     * are stored in ByteArray.
     * @param floatArray array with the float values
     */
    fun floatToByteArray(floatArray: FloatArray): ByteArray {

        val byteArray = ByteArray(floatArray.size * 4)
        for (i in floatArray.indices) {
            val intValue = floatArray[i].toRawBits()
            intToBytes(intValue, byteArray, i)
        }
        return byteArray
    }

    /**
     * Convert every int value from IntArray to its corresponding 4 byte values, that
     * are stored in ByteArray.
     * @param intArray array with the integer values
     */
    fun intToByteArray(intArray: IntArray): ByteArray {

        val byteArray = ByteArray(intArray.size * 4)
        for (i in intArray.indices) {
            intToBytes(intArray[i], byteArray, i)
        }
        return byteArray
    }

    /**
     * Convert every int value from ArrayList<Int> to its corresponding 4 byte values, that
     * are stored in ByteArray.
     * @param intArrayList array list with the integer values
     */
    fun intToByteArray(intArrayList: ArrayList<Int>): ByteArray {

        val byteArray = ByteArray(intArrayList.size * 4)
        for (i in intArrayList.indices) {
            intToBytes(intArrayList[i], byteArray, i)
        }
        return byteArray
    }

    /**
     * Convert every 4 bytes values from ByteArray to its corresponding integer value, and store the
     * result in to IntArray
     * @param byteArray array with the byte values
     */
    fun byteToIntArray(byteArray: ByteArray): IntArray {

        val intArray = IntArray(byteArray.size / 4)
        for (i in intArray.indices) {
            intArray[i] = bytesToInt(byteArray, i)
        }
        return intArray
    }

    /**
     * Convert every 4 bytes values from ByteArray to its corresponding integer value, and store the
     * result in to ArrayList<Int>
     * @param byteArray array with the byte values
     */
    fun byteToIntArrayList(byteArray: ByteArray): ArrayList<Int> {

        val intArrayList = ArrayList<Int>(byteArray.size / 4)
        for (i in 0 until byteArray.size / 4) {
            val intValue = bytesToInt(byteArray, i)
            intArrayList.add(intValue)
        }
        return intArrayList
    }

    /**
     * Convert every 4 bytes values from ByteArray to its corresponding float value, and store the
     * result in to FloatArray
     * @param byteArray array with the byte values
     */
    fun byteToFloatArray(byteArray: ByteArray): FloatArray {

        val floatArray = FloatArray(byteArray.size / 4)
        for (i in floatArray.indices) {
            val intValue = bytesToInt(byteArray, i)
            floatArray[i] = Float.fromBits(intValue)
        }
        return floatArray
    }

    /**
     * Convert 4 bytes from give ByteArray at given index into a 32-bit integer values
     * @param byteArray array holding the byte values
     * @param i index where to start getting the 4 bytes for reconstructing a integer values
     */
    fun bytesToInt(byteArray: ByteArray, i: Int): Int {

        val j = i * 4
        val b4 = byteArray[j]
        val b3 = byteArray[j + 1]
        val b2 = byteArray[j + 2]
        val b1 = byteArray[j + 3]
        return 0xFF and b1.toInt() shl 24 or (0xFF and b2.toInt() shl 16) or (0xFF and b3.toInt() shl 8) or (0xFF and b4.toInt())
    }

    /**
     * Convert single 32-bit integer value to its corresponding 4 byte values, that are then
     * stored in given ByteArray at given index.
     * @param intValue integer values that will be converted in to 4 byte values
     * @param byteArray array where the 4 bytes will be stored
     * @param i start index for the byte array where the 4 bytes values ill be stored
     */
    fun intToBytes(intValue: Int, byteArray: ByteArray, i: Int) {

        // get the 4 byte values from the 32-bit int values
        val j = i * 4
        byteArray[j] = intValue.toByte()
        byteArray[j + 1] = (intValue shr 8).toByte()
        byteArray[j + 2] = (intValue shr 16).toByte()
        byteArray[j + 3] = (intValue shr 24).toByte()
    }

    /**
     * Inline function that is called, when the final measurement is made and
     * the view is about to be draw.
     */
    inline fun View.afterMeasured(crossinline function: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    function()
                }
            }
        })
    }

    //region OpenGLStatic

    fun Int.asColorArray(): FloatArray {

        val r = Color.red(this) / 255f
        val g = Color.green(this) / 255f
        val b = Color.blue(this) / 255f
        val a = Color.alpha(this) / 255f
        return floatArrayOf(r, g, b, a)
    }

    /**
     * Set the size components that include the device size and the ratio.
     * @param width width for the device
     * @param height height for the device
     */
    fun setSizeComponents(width: Float, height: Float) {

        // set constants that are used by other class
        DEVICE_WIDTH = width
        DEVICE_HEIGHT = height
        DEVICE_HALF_WIDTH = width / 2f
        DEVICE_HALF_HEIGHT = height / 2f
        RATIO = (DEVICE_WIDTH / DEVICE_HEIGHT)

    }

    var DEVICE_HALF_WIDTH: Float = 0f                           // half of the device width
    var DEVICE_HALF_HEIGHT: Float = 0f                          // half of the device height
    var DEVICE_WIDTH: Float = 0f                                // device width of current device
    var DEVICE_HEIGHT: Float = 0f                               // device height of current device
    var RATIO: Float = 0f                                       // device width to height (width/height) ratio
    var ENABLE_TABLET_MODE: Boolean = false                     // if the device is tabled every devices with width > 600dp is consider a tablet
    var TABLET_HEIGHT_SCALE_FACTOR: Float = 1f                  // scale factor value used, when the device is tablet or is turned in to landscape mode, and is used to scale down everything by multiplying it by this value
    var TABLET_WIDTH_SCALE_FACTOR: Float = 1f                   // scale factor value used, when the device is tablet or is turned in to landscape mode, and is used to scale down everything by multiplying it by this value
    var ORIENTATION: Int = Configuration.ORIENTATION_PORTRAIT   // current orientation of the device
    var DEFAULT_POSITION: PointF = PointF(0f, 0f)
    var DEFAULT_SCALE: Float = 0.034902867f
    const val NEAR: Int = 3                                     // near from the frustum, since it is 1 and not 3 as presented by the android team

    var ENABLE_ALPHA: Boolean = true                            // if alpha transparency is enabled
    var ENABLE_ANTIALIASING: Boolean = false                    // if antialiasing is enabled
    //endregion
}