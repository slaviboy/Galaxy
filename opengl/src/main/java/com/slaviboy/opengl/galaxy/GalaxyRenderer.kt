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
package com.slaviboy.opengl.galaxy

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.slaviboy.opengl.InfiniteGrid
import com.slaviboy.opengl.MatrixGestureDetector
import com.slaviboy.opengl.entities.DensityWavesInfo
import com.slaviboy.opengl.entities.PhysicsInfo
import com.slaviboy.opengl.entities.RenderInfo
import com.slaviboy.opengl.helpers.StaticMethods
import com.slaviboy.opengl.params.GalaxyParams
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GalaxyRenderer(
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

    var updateSettings: Boolean                         // if the settings are updated by the user, and the changes should be applied to the OpenGL objects
    var isPaused: Boolean                               // if the rotation of the galaxy should be paused
    var gestureDetectorZoomMin: Float                   // indicate the minimum zoom level for the main gesture detector, that scales all shapes, the infinite grid and delete circle
    var gestureDetectorZoomMax: Float                   // indicate the maximum zoom level for the main gesture detector, that scales all shapes. the infinite grid and delete circle


    lateinit var infiniteGrid: InfiniteGrid             // object for drawing the OpenGL lines for the infinite grid
    var galaxies: ArrayList<Galaxy>                     // array list holding all the galaxy object that is responsible for drawing the galaxy with all its elements and the density curves
    var galaxyPreset: ArrayList<GalaxyParams>           // array list with pre-made galaxies that can be used

    var totalTime: Float                                // the current total time
    var timeStepSize: Float                             // the time that will be added to the total time

    var storedMatrixTransformations: FloatArray? = null // the previously store matrix transformations used, when user rotates screen to restore the last position of the galaxy

    var updateInfiniteGrid: Boolean = true              // if the infinite grid should be updated, used to update it only on finger gestures
    var showInfiniteGrid: Boolean = false               // if the infinite grid should be shown

    // if the density curve for all galaxies should be shown
    var showDensityWaves: Boolean = false
        set(value) {
            field = value
            galaxies.forEach {
                it.showDensityWaves = value
            }
        }

    // if the Xray mode is on, it is use with lighter background color
    var isXrayModeOn: Boolean = false
        set(value) {
            field = value
            galaxies.forEach {
                it.isXrayModeOn = value
            }
        }

    // if the dust particles for all galaxies should be shown
    var showDust: Boolean = true
        set(value) {
            field = value
            galaxies.forEach {
                it.showDustParticles = value
            }
        }

    // if the dust filaments for all galaxies should be shown
    var showDustFilaments: Boolean = true
        set(value) {
            field = value
            galaxies.forEach {
                it.showDustFilaments = value
            }
        }

    // if the stars for all galaxies should be shown
    var showStars: Boolean = true
        set(value) {
            field = value
            galaxies.forEach {
                it.showStars = value
            }
        }

    // if the H2 regions for all galaxies should be shown
    var showH2: Boolean = true
        set(value) {
            field = value
            galaxies.forEach {
                it.showH2Regions = value
            }
        }

    // if the dark matter for all galaxies should turned on/off
    var hasDarkMatter: Boolean = true
        set(value) {
            field = value
            galaxies.forEach {
                it.hasDarkMatter = value
            }
        }

    init {

        // set default OpenGL matrix arrays
        MVPMatrix = FloatArray(16)
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        transformedMatrixOpenGL = FloatArray(16)
        gestureDetectorMatrixValues = FloatArray(9)

        areShapesInitialized = false
        areDefaultTransformationsApplied = false
        backgroundColor = floatArrayOf(0f, 0f, 0f, 0f)

        galaxies = arrayListOf(Galaxy(gestureDetector))
        updateSettings = true
        isPaused = false
        gestureDetectorZoomMin = 0.002f
        gestureDetectorZoomMax = 30f

        galaxyPreset = arrayListOf()
        totalTime = 0f
        timeStepSize = 100_000.0f
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

        if (!areDefaultTransformationsApplied) {
            gestureDetector.matrix.reset()
            gestureDetector.setTransformations()
        }

        // get the transformedMatrixOpenGL matrix with the transformations from the gesture detector
        gestureDetector.transform(MVPMatrix, transformedMatrixOpenGL)

        // update and render(draw) OpenGL shapes
        update()
        draw()

        // set the default transformations for the gesture detector to scale and center it on middle of the screen
        if (!areDefaultTransformationsApplied) {

            if (storedMatrixTransformations != null) {

                // use the previously stored transformations, triggered when all is launched again or device is rotated
                gestureDetector.matrix.setValues(storedMatrixTransformations)
                gestureDetector.setTransformations()
            } else {

                // set the default scale and position when app is launched for first time, or from the settings it uses the default position on every launch
                gestureDetector.matrix.apply {
                    reset()
                    postScale(StaticMethods.DEFAULT_SCALE, StaticMethods.DEFAULT_SCALE)
                    postTranslate(StaticMethods.DEVICE_HALF_WIDTH, StaticMethods.DEVICE_HALF_HEIGHT)
                }
                gestureDetector.setTransformations()
            }

            // this will force update of the infinite grid
            updateInfiniteGrid = true

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

        infiniteGrid = InfiniteGrid(gestureDetector)
        infiniteGrid.updateSize(StaticMethods.DEVICE_WIDTH, StaticMethods.DEVICE_HEIGHT)

        initShapes()
        initGalaxyPresets()
    }

    /**
     * Initialize the galaxy preset, which is array with pre-made galaxies, that can be chosen by the user
     */
    fun initGalaxyPresets() {

        val galaxyPresetValues: Array<Array<Any>> = arrayOf(

            arrayOf(16000f, 4000f, -.0003f, .8f, .85f, true, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(13000f, 4000f, .00064f, .9f, .9f, true, 0, 0f, 75f, 4100f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(13000f, 4000f, .0004f, 1.35f, 1.05f, true, 0, 0f, 70f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(13000f, 4500f, .0002f, .65f, .95f, true, 3, 72f, 80f, 4000f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(15000f, 4000f, .0003f, 1.45f, 1.0f, true, 0, 0f, 80f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(14000f, 12500f, .0002f, 0.65f, 0.95f, true, 3, 72f, 85f, 2200f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(13000f, 1500f, .0004f, 1.1f, 1.0f, true, 1, 20f, 80f, 2800f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            arrayOf(13000f, 4000f, .0004f, .85f, .95f, true, 1, 20f, 80f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f)

            // GalaxyParams(19000f, 13000f, 0.3201f, 0.85f, 0.95f, true, 2, 40f, 10f, 4000f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            // GalaxyParams(1000f, 8000f, .00003f, 30.08f, 31.15f, false, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            // GalaxyParams(1000f, 8000f, .10003f, 30.08f, 3.15f, true, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            // GalaxyParams(1000f, 4000f, .0003f, 30.08f, 3.15f, true, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            // GalaxyParams(16000f, 4000f, .0003f, 0.08f, 3.15f, true, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f),
            // GalaxyParams(16000f, 4000f, .0003f, 3.8f, .15f, true, 0, 40f, 58f, 4500f, 40000, 1f, 40000, 1f, 400, 1f, 400, 1f, 1f)

        )

        galaxyPresetValues.forEach {
            val galaxyParams = GalaxyParams(it)
            galaxyPreset.add(galaxyParams)
        }
        /*val galaxy = galaxies.first()
        galaxy.reset(galaxyPreset[2], false)*/
    }

    fun selectPreset(idx: Int) {
        val galaxy = galaxies.first()
        galaxy.setParams(galaxyPreset[idx], false)
        galaxy.forceUpdate()
    }

    fun initShapes() {

        // initialize the OpenGL shapes
        infiniteGrid.initialize()

        // init all galaxies
        galaxies.forEach {
            it.initialize()
        }

        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    /**
     * Update all galaxies
     */
    fun updateGalaxies() {

        galaxies.forEach {
            it.update()
        }
    }

    /**
     * Update infinite grid and set the flag to false, indicating that there is no need
     * to update it, that way it is updated only when onTouch() events are registered
     */
    fun updateInfiniteGrid() {
        infiniteGrid.update()
        updateInfiniteGrid = false
    }

    /**
     * Set the new settings
     */
    fun setConfiguration(
        densityWavesInfo: DensityWavesInfo,
        physicsInfo: PhysicsInfo,
        renderInfo: RenderInfo
    ) {
        densityWavesInfo.apply {
            galaxies.forEach {
                it.innerCoreRadius = galaxyCoreRadius.currentValue
                it.galaxyRadius = galaxyRadius.currentValue
                it.angularOffset = angularOffset.currentValue
                it.exInner = innerEccentricity.currentValue
                it.exOuter = outerEccentricity.currentValue
                it.pertN = numberOfEllipseDisturbances.currentValue.toInt()
                it.pertAmp = ellipseDisturbanceDampingFactor.currentValue
            }
        }
        physicsInfo.apply {
            timeStepSize = timeStepLength.currentValue
            galaxies.forEach {
                it.baseTemperature = baseTemperature.currentValue
            }
        }
        renderInfo.apply {
            galaxies.forEach {
                it.numberOfStars = numberOfStars.currentValue.toInt()
                it.numberOfH2Regions = numberOfH2Regions.currentValue.toInt()
                it.numberOfDustParticles = numberOfDustParticles.currentValue.toInt()
                it.numberOfDustFilaments = numberOfDustFilaments.currentValue.toInt()
                it.starsSizeFactor = starsSizeFactor.currentValue
                it.H2RegionsSizeFactor = H2RegionsSizeFactor.currentValue
                it.dustParticlesSizeFactor = dustParticlesSizeFactor.currentValue
                it.dustFilamentsSizeFactor = dustFilamentsSizeFactor.currentValue
                //it.galaxySizeFactor = galaxySizeFactor.currentValue
            }

        }

        // this will force update of the galaxies and the density wave curves with the new settings
        updateSettings = true
    }

    /**
     * Method called in order to update the values from the settings
     */
    fun updateSettings() {

        // copy the previous transformation values and reset before updating stars and density waves
        gestureDetector.matrix.getValues(gestureDetectorMatrixValues)
        gestureDetector.matrix.reset()
        gestureDetector.setTransformations()

        // update stars and density waves
        galaxies.forEach {
            it.forceUpdate()
        }

        // restore the previous transformations
        gestureDetector.matrix.setValues(gestureDetectorMatrixValues)
        gestureDetector.setTransformations()

        updateSettings = false
    }

    /**
     * Method that update all OpenGL object: stars, infinite grid and density wave
     */
    fun update() {

        // update the total time
        if (!isPaused)
            totalTime += timeStepSize

        if (updateSettings)
            updateSettings()

        if (updateInfiniteGrid)
            updateInfiniteGrid()

        updateGalaxies()
    }

    /**
     * Method that draws all OpenGL objects: stars, infinite grid and density wave
     */
    fun draw() {

        // draw the background color and clear everything previously draw on the scene
        GLES30.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3])
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // draw galaxy
        galaxies.forEach {
            it.draw(totalTime, isPaused, transformedMatrixOpenGL)
        }

        // draw the infinite grid if it is shown
        if (showInfiniteGrid)
            infiniteGrid.draw(transformedMatrixOpenGL)

    }

    /**
     * Method that calculates the zoom level, instead of zooming in and out with the zoomFact 0.1/-0.1
     * We make it logarithmic, so we can zoom quicker when the zoom level is deeper, that way we zoom
     * by increasing the zoomFact: 0.1 -> 0.2 -> 0.3 -> 0.4 -> 0.5 -> ..., depending on the zoom levels.
     * If we press the zoomIn we increase the zoomLevel (+1), otherwise if we press the zoomOut we decrese
     * by (-1)
     *
     *  | zoomLevel |    1    |    2    |    3    |    4    |...
     *  | zoomFact  |   0.1   |   0.2   |   0.3   |   0.4   |...
     *  | scale     |   1.1   |   1.3   |   1.6   |    2    |...
     */
    fun calculateZoomLevel(zoomState: Int): Int {

        // get the current scale level for example if it is 1.3, then we start adding zoomFact
        // to the totalScale until we get a match, so we start with {1}, then {1} +(0.1) = {1.1}, then
        // {1.1} +(0.2) = {1.3},... that way we can determine the zoom level from the current scale
        val scale = gestureDetector.scale
        val zoomFactor = 0.1f
        var totalScale = 1f
        var totalZoomLevel = 0

        /**
         * Method that indicate if the total scale and the initial scale are equal, since we use float and
         * not BigDecimal type, 1.6000000002 will not be equal to 1.6, that is why we need to take care
         * of the precision error
         */
        fun scaleIsEqual(): Boolean {
            return Math.abs(totalScale - scale) < 0.0001f
        }

        // if scale is 1 then the zoom level is 0, then return the next zoom level 1
        if (scaleIsEqual()) {
            return 1
        }

        while (true) {

            // if the current scale is bigger then 1, that means it was previously zoomIn
            if (scale > 1) {

                // depending if the zoom in our out button was clicked, we check for > or >=
                if (zoomState == ZOOM_STATE_ZOOM_IN) {
                    if (totalScale > scale) {
                        break
                    }
                } else {
                    if (totalScale > scale || scaleIsEqual()) {
                        break
                    }
                }
                totalZoomLevel++
                totalScale += zoomFactor * totalZoomLevel

            } else {
                // if the current scale is less then 1, that means it was previously zoomOut

                if (zoomState == ZOOM_STATE_ZOOM_IN) {
                    if (totalScale < scale || scaleIsEqual()) {
                        break
                    }
                } else {
                    if (totalScale < scale) {
                        break
                    }
                }
                totalZoomLevel++
                totalScale -= zoomFactor * totalZoomLevel
            }
        }
        return totalZoomLevel
    }

    /**
     * Calculate the zoom factor from the zoom level
     */
    fun calculateZoomFact(zoomState: Int, zoomFactor: Float): Float {
        val zoomLevel = calculateZoomLevel(zoomState)
        return zoomFactor * Math.abs(zoomLevel) / 2
    }


    /**
     * Zoom in, by changing the scale for the transformation matrix to (previous scale + 0.1f)
     * @param zoomFactor with how much to zoom out the scene
     */
    fun zoomIn(zoomFactor: Float = 0.001f) {

        val newZoomFact = calculateZoomFact(ZOOM_STATE_ZOOM_IN, zoomFactor)
        zoomInOut(newZoomFact)
    }

    /**
     * Zoom out, by changing the scale for the transformation matrix to (previous scale - 0.1f)
     * @param zoomFactor with how much to zoom out the scene
     */
    fun zoomOut(zoomFactor: Float = -0.001f) {

        val newZoomFact = calculateZoomFact(ZOOM_STATE_ZOOM_OUT, zoomFactor)
        zoomInOut(newZoomFact)
    }

    /**
     * Zoom restore the normal, by changing the scale for the transformation matrix to 1.0f
     */
    fun zoomNormal() {

        // this will reset the scale to 1.0f
        val previousScale = gestureDetector.scale
        zoom(0.03f / previousScale)
    }

    /**
     * Method used only from zoomIn and zoomOut methods, it resets the zoom level to 1.0f and
     * then changes is to the new scale level.
     * @param zoomFactor with how much to zoom in or out the scene
     */
    fun zoomInOut(
        zoomFactor: Float = 0f,
        pivotX: Float = StaticMethods.DEVICE_HALF_WIDTH, pivotY: Float = StaticMethods.DEVICE_HALF_HEIGHT
    ) {

        // get previous scale factor
        val previousScale = gestureDetector.scale

        // this will reset the scale to 1.0f
        gestureDetector.matrix.postScale(1f / previousScale, 1f / previousScale, pivotX, pivotY)

        // get the new scale
        var newScale = previousScale + zoomFactor

        // check if we exceed the minimum or maximum allowed scale(zoom) levels
        if (newScale > gestureDetectorZoomMax) {
            newScale = gestureDetectorZoomMax
        }
        if (newScale < gestureDetectorZoomMin) {
            newScale = gestureDetectorZoomMin
        }

        zoom(newScale, pivotX, pivotY)
    }

    /**
     * Method that zoom in, out or just reset to normal 1.0f the transformation matrix
     * @param newScale the new scale for the matrix
     */
    fun zoom(newScale: Float = 1f, pivotX: Float = StaticMethods.DEVICE_HALF_WIDTH, pivotY: Float = StaticMethods.DEVICE_HALF_HEIGHT) {

        // apply scale the matrix
        gestureDetector.matrix.postScale(newScale, newScale, pivotX, pivotY)
        gestureDetector.setTransformations()

        // update the zoom of the infinite grid
        if (showInfiniteGrid) {
            infiniteGrid.update()
        }

        // redraw the scene
        update()
        draw()
    }

    companion object {
        const val ZOOM_STATE_ZOOM_NORMAL = 0   // if the last zoom state was Zoom Normal (reset the zoom to normal)
        const val ZOOM_STATE_ZOOM_IN = 1       // if the last zoom state was Zoom In
        const val ZOOM_STATE_ZOOM_OUT = 2      // if the last zoom state was Zoom Out
    }
}
