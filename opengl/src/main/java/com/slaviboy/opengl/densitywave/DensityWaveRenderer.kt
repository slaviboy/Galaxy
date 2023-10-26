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
package com.slaviboy.opengl.densitywave

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.slaviboy.opengl.MatrixGestureDetector
import com.slaviboy.opengl.helpers.Helper
import com.slaviboy.opengl.helpers.StaticMethods
import com.slaviboy.opengl.params.GalaxyParams
import com.slaviboy.opengl.params.VertexColor
import com.slaviboy.opengl.shapes.Lines
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL view that is used for drawing the Density Wave graph in real time, when the user changes
 * the settings
 */
class DensityWaveRenderer(
    var gestureDetector: MatrixGestureDetector = MatrixGestureDetector()
) : GLSurfaceView.Renderer {

    var MVPMatrix: FloatArray                           // model view projection matrix
    var projectionMatrix: FloatArray                    // matrix with applied projection
    var viewMatrix: FloatArray                          // view matrix
    val transformedMatrixOpenGL: FloatArray             // matrix with transformation applied by finger gestures as OpenGL values
    val gestureDetectorMatrixValues: FloatArray         // matrix value from the gesture detector

    var areDefaultTransformationsApplied: Boolean       // whether the default transformation are applied to the gesture detector, in order to scale and center the galaxy at the middle
    var areShapesInitialized: Boolean                   // indicate if the OpenGL shapes are initialized
    var backgroundColor: FloatArray                     // array with the background color as float array holding the r,g,b,a channels
    var lineColor: FloatArray                           // array with the lines color as float array holding the r,g,b,a channels

    lateinit var vertDensityWaves: Lines                // object for drawing the OpenGL lines for the density wave graph
    var galaxyParams: GalaxyParams                      // params for the galaxy that are used when generating the density wave graph

    init {

        // set default OpenGL matrix arrays
        MVPMatrix = FloatArray(16)
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        transformedMatrixOpenGL = FloatArray(16)
        gestureDetectorMatrixValues = FloatArray(9)

        galaxyParams = GalaxyParams()
        areShapesInitialized = false
        areDefaultTransformationsApplied = false
        backgroundColor = floatArrayOf(0f, 0f, 0f, 1f)
        lineColor = floatArrayOf(0f, 1f, 1f, 1f)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    }

    /**
     * The method called when the surface view is being redraw, and all OpenGL shapes are drawn
     */
    override fun onDrawFrame(unused: GL10) {

        init()

        // get view and projection matrices
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // get the transformedMatrixOpenGL matrix with the transformations from the gesture detector
        gestureDetector.transform(MVPMatrix, transformedMatrixOpenGL)

        // copy the matrix transformation before update
        gestureDetector.matrix.getValues(gestureDetectorMatrixValues)
        gestureDetector.matrix.reset()
        gestureDetector.setTransformations()

        // update density waves
        update()

        // restore the previous transformations, after update
        gestureDetector.matrix.setValues(gestureDetectorMatrixValues)
        gestureDetector.setTransformations()

        // now we can draw the lines
        draw()

        // set the default transformations for the gesture detector to scale and center it on middle of the screen
        if (!areDefaultTransformationsApplied) {

            gestureDetector.matrix.apply {

                val scale = StaticMethods.DEFAULT_SCALE * 0.8f
                reset()
                postScale(scale, scale)
                postTranslate(StaticMethods.DEVICE_HALF_WIDTH, StaticMethods.DEVICE_HALF_HEIGHT)
            }
            gestureDetector.setTransformations()

            areDefaultTransformationsApplied = true
        }
    }

    /**
     * When the surface is changed, it is used to update the device size, and the projection matrix. And also
     * to recreate the shapes, since they are dependent on the device size.
     */
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {

        // Adjust the viewport based on geometry changes, such as screen rotation
        GLES30.glViewport(0, 0, width, height)

        // this projection matrix is applied to object coordinates
        Matrix.frustumM(projectionMatrix, 0, -StaticMethods.RATIO, StaticMethods.RATIO, -1f, 1f, 3f / StaticMethods.NEAR, 7f)
    }


    fun init() {
        if (areShapesInitialized) return
        areShapesInitialized = true
        vertDensityWaves = Lines(2.5f, GLES30.GL_STATIC_DRAW)
        initGL()
    }

    fun initGL() {
        vertDensityWaves.initialize()
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    /**
     * Update the lines for the density wave graph
     */
    fun updateDensityWaves() {
        updateDensityWaves(vertDensityWaves, galaxyParams, gestureDetector, lineColor)
    }

    /**
     * Method that update all OpenGL object: density wave
     */
    fun update() {
        updateDensityWaves()
    }

    /**
     * Method that draws all OpenGL objects: density wave
     */
    fun draw() {

        // draw the background color and clear everything previously draw on the scene
        GLES30.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3])
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // draw the density wave
        vertDensityWaves.draw(transformedMatrixOpenGL, transformedMatrixOpenGL)
    }


    companion object {

        /**
         * Method that generate the lines for the density wave graph
         * @param vertDensityWaves OpenGL lines object that holds and draw the lines
         * @param galaxyParams params for the galaxy, that are set by the settings
         * @param gestureDetector gesture detector used by the line
         * @param lineColor float array with 4 Float values for the R,G,B,A channels
         */
        fun updateDensityWaves(vertDensityWaves: Lines, galaxyParams: GalaxyParams, gestureDetector: MatrixGestureDetector, lineColor: FloatArray) {

            /**
             * Add the vertices for a ellipsis that is draw to visualize the density wave graph
             */
            fun addEllipsisVertices(
                vert: ArrayList<VertexColor>, vertIdx: ArrayList<Int>, m: Float, n: Float,
                angle: Float, pertNum: Int, pertAmp: Float, r: Float, g: Float, b: Float, a: Float
            ) {
                val steps = 100
                val x = 0f
                val y = 0f

                // angle is given by Degree Value
                val beta: Float = -angle * Helper.DEG_TO_RAD
                val sinbeta: Float = Math.sin(beta.toDouble()).toFloat()
                val cosbeta: Float = Math.cos(beta.toDouble()).toFloat()

                val firstPointIdx: Int = vert.size
                for (i in 0 until 360 step (360 / steps)) {

                    val alpha: Float = i * Helper.DEG_TO_RAD
                    val sinalpha = Math.sin(alpha.toDouble()).toFloat()
                    val cosalpha = Math.cos(alpha.toDouble()).toFloat()

                    var fx: Float = x + (m * cosalpha * cosbeta - n * sinalpha * sinbeta)
                    var fy: Float = y + (m * cosalpha * sinbeta + n * sinalpha * cosbeta)

                    if (pertNum > 0) {
                        fx += ((m / pertAmp) * Math.sin(alpha * 2.0 * pertNum)).toFloat()
                        fy += ((m / pertAmp) * Math.cos(alpha * 2.0 * pertNum)).toFloat()
                    }

                    if (i > (360 / steps)) {
                        vertIdx.add(vertIdx.last())
                    }

                    vertIdx.add(vert.size)

                    val vc = VertexColor(fx, fy, 0f, r, g, b, a)
                    vert.add(vc)
                }

                // close the loop and reset the element index array
                vertIdx.add(vertIdx.last())
                vertIdx.add(firstPointIdx)

            }

            val vert: ArrayList<VertexColor> = arrayListOf()
            val idx: ArrayList<Int> = arrayListOf()

            // add the density waves ellipses
            val num = 100
            val dr: Float = galaxyParams.radiusFarField / num
            for (i in 0..num) {

                val r: Float = dr * (i + 1)
                addEllipsisVertices(
                    vert,
                    idx,
                    r,
                    r * galaxyParams.getEccentricity(r),
                    Helper.RAD_TO_DEG * galaxyParams.getAngularOffset(r),
                    galaxyParams.pertN,
                    galaxyParams.pertAmp,
                    lineColor[0], lineColor[1], lineColor[2], lineColor[3]
                )
            }

            // add three circles at the boundaries of core, galaxy and galactic medium
            val pertNum = 0
            val pertAmp = 0f

            // yellow color for the inner core ellipse
            var r: Float = galaxyParams.innerCoreRadius
            addEllipsisVertices(vert, idx, r, r, 0f, pertNum, pertAmp, 1f, 1f, 0f, 0.8f)

            // green color for the galaxy radius
            r = galaxyParams.galaxyRadius
            addEllipsisVertices(vert, idx, r, r, 0f, pertNum, pertAmp, 0f, 1f, 0f, 0.8f)

            // red color for the far field ellipse
            r = galaxyParams.radiusFarField
            addEllipsisVertices(vert, idx, r, r, 0f, pertNum, pertAmp, 1f, 0f, 0f, 0.8f)

            // normalize the coordinate by convert the coordinates from graphic coordinate system to OpenGL coordinate system
            val coordinates = FloatArray(vert.size * 2)
            for (j in vert.indices) {
                coordinates[j * 2] = vert[j].position.x
                coordinates[j * 2 + 1] = vert[j].position.y
            }
            gestureDetector.normalizeCoordinates(coordinates)
            for (j in vert.indices) {
                vert[j].position.x = coordinates[j * 2]
                vert[j].position.y = coordinates[j * 2 + 1]
            }

            vertDensityWaves.createBuffer(vert, idx, GLES30.GL_LINES)
        }
    }
}
