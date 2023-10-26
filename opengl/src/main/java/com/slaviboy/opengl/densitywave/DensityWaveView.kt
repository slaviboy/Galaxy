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
package  com.slaviboy.opengl.densitywave

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.slaviboy.opengl.ConfigChooser
import com.slaviboy.opengl.helpers.StaticMethods
import com.slaviboy.opengl.helpers.StaticMethods.asColorArray

/**
 * A OpenGL view that is used to visualize the density wave graph in real time when the user updates
 * the density wave properties from the settings
 */
class DensityWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : GLSurfaceView(context, attrs) {

    var densityWaveRenderer: DensityWaveRenderer = DensityWaveRenderer()

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
        setRenderer(densityWaveRenderer)

        // set the color for the background and the lines
        densityWaveRenderer.backgroundColor = (Color.argb(255, 245, 245, 245)).asColorArray()
        densityWaveRenderer.lineColor = (Color.argb(40, 0, 0, 0)).asColorArray()

        // render the view only when needed
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}