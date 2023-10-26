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
package  com.slaviboy.opengl.galaxy

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.slaviboy.opengl.ConfigChooser
import com.slaviboy.opengl.helpers.StaticMethods

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
class GalaxyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    var galaxyRenderer: GalaxyRenderer = GalaxyRenderer()

    init {

        if (StaticMethods.ENABLE_ALPHA) {
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        // create an OpenGL ES 3.0 context.
        setEGLContextClientVersion(3)

        // fix for error No Config chosen
        if (StaticMethods.ENABLE_ANTIALIASING) {
            setEGLConfigChooser(ConfigChooser())
        } else {
            this.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        }

        // set the renderer for drawing on the GLSurfaceView
        setRenderer(galaxyRenderer)

        // render the view continuously
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        galaxyRenderer.apply {
            gestureDetector.onTouchEvent(event)
            updateInfiniteGrid = true
        }
        return true
    }
}