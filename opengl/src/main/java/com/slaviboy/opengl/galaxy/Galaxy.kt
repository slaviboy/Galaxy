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
import com.slaviboy.opengl.CumulativeDistributionFunction
import com.slaviboy.opengl.MatrixGestureDetector
import com.slaviboy.opengl.densitywave.DensityWaveRenderer
import com.slaviboy.opengl.helpers.Helper
import com.slaviboy.opengl.params.Color
import com.slaviboy.opengl.params.GalaxyParams
import com.slaviboy.opengl.params.Star
import com.slaviboy.opengl.params.VertexStar
import com.slaviboy.opengl.shapes.Lines
import com.slaviboy.opengl.shapes.Stars

/**
 * A class to encapsulate the geometric details of a spiral galaxy
 */
class Galaxy(
    val gestureDetector: MatrixGestureDetector,
    galaxyRadius: Float = 15000f,
    override var innerCoreRadius: Float = 6000f,
    override var angularOffset: Float = 0.019f,
    override var exInner: Float = 0.8f,
    override var exOuter: Float = 1f,
    override var hasDarkMatter: Boolean = true,
    override var pertN: Int = 0,
    override var pertAmp: Float = 0f,
    override var dustRenderSize: Float = 70f,
    override var baseTemperature: Float = 4000f,

    override var showStars: Boolean = true,
    override var numberOfStars: Int = 60000,
    override var starsSizeFactor: Float = 1f,

    override var showDustParticles: Boolean = true,
    override var numberOfDustParticles: Int = numberOfStars,
    override var dustParticlesSizeFactor: Float = 1f,

    override var showDustFilaments: Boolean = true,
    override var numberOfDustFilaments: Int = numberOfStars / 100,
    override var dustFilamentsSizeFactor: Float = 1f,

    override var showH2Regions: Boolean = true,
    override var numberOfH2Regions: Int = 400,
    override var H2RegionsSizeFactor: Float = 1f,

    override var galaxySizeFactor: Float = 1f

) : GalaxyParams(galaxyRadius, innerCoreRadius, angularOffset, exInner, exOuter, hasDarkMatter, pertN, pertAmp, dustRenderSize, baseTemperature, showStars, numberOfStars, starsSizeFactor, showDustParticles, numberOfDustParticles, dustParticlesSizeFactor, showDustFilaments, numberOfDustFilaments, dustFilamentsSizeFactor, showH2Regions, numberOfH2Regions, H2RegionsSizeFactor, galaxySizeFactor) {

    lateinit var vertDensityWaves: Lines         // object for drawing the OpenGL lines for the density wave graph
    lateinit var vertStars: Stars                // object for drawing the OpenGL circles for the stars and dust particles for the galaxy

    var galaxyObjects: ArrayList<Star>           // array list with all object that are part of the galaxy: stars, dust particles, H2 regions...
    var cdf: CumulativeDistributionFunction      // cumulative distribution function

    var updateDensityWaves: Boolean = true       // indicate if the density wave should be updated
    var updateGalaxy: Boolean = true             // indicate id the galaxy should be updated

    var showDensityWaves: Boolean = false        // indicate if the density curve should be shown
    var isXrayModeOn: Boolean = false            // if the Xray mode is on, it is use with lighter background color
        set(value) {
            field = value
            if (::vertStars.isInitialized) vertStars.isXrayModeOn = value
        }

    init {
        galaxyObjects = arrayListOf()
        cdf = CumulativeDistributionFunction()
    }

    /**
     * Initialize the OpenGL objects for the density wave curve and the stars
     */
    fun initialize() {
        vertDensityWaves = Lines(2f, GLES30.GL_STATIC_DRAW).apply {
            initialize()
        }
        vertStars = Stars().apply {
            initialize()
        }
    }

    /**
     * Create the all shapes used in the galaxy stars, dust, H2 region every one of them is represented as
     */
    fun create() {

        galaxyObjects = arrayListOf()

        // first star is the black hole at the centre of the galaxy
        val blackHole = Star(
            a = 0f,
            b = 0f,
            tiltAngle = 0f,
            theta0 = 0f,
            velTheta = 0f,
            type = 0,
            temp = 6000f,
            mag = 1f
        )
        galaxyObjects.add(blackHole)

        cdf = CumulativeDistributionFunction()
        cdf.setupRealistic(
            1.0f,                   // maximum intensity
            0.02f,                  // k (bulge)
            galaxyRadius / 3.0f,    // disc scale length
            innerCoreRadius,          // bulge radius
            0.0f,                 // start  of the intensity curve
            radiusFarField,           // end of the intensity curve
            1000                // number of supporting points
        )

        initStars()
        initDustParticles()
        initDustFilamentParticles()
        initH2Regions()
        normalizeCoordinates()
    }

    /**
     * Add all the stars to the array list with object that are part of the galaxy: stars, dust particles, H2 regions...
     */
    fun initStars() {

        var x = 0f
        var y = 0f
        var rad = 0f

        // initialize the stars
        for (i in 1 until numberOfStars) {

            if (i % 3 == 0) {
                rad = cdf.valFromProb(Helper.randomNumber())
            } else {
                x = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
                y = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
                rad = Math.sqrt(x * x.toDouble() + y * y).toFloat()
            }

            val a = rad
            val b = rad * getEccentricity(rad)
            val star = Star(
                a = a,
                b = b,
                tiltAngle = getAngularOffset(rad),
                theta0 = 360.0f * Helper.randomNumber(),
                velTheta = getOrbitalVelocity((a + b) / 2.0f),
                type = 0
            )
            star.temp = baseTemperature + rad / 4.5f
            star.mag = 0.02f + 0.55f * Helper.randomNumber()

            // make a small portion of the stars brighter
            if (i < numberOfStars / 60) {
                star.mag *= Helper.randomNumber() * 2.8f
            }

            star.mag *= galaxySizeFactor * starsSizeFactor
            galaxyObjects.add(star)

            /*val rad: Float = cumulativeDistributionFunction.valFromProb(Helper.randomNumber())
            val star = Star(
                a = rad,
                b = (rad * getEccentricity(rad)),
                tiltAngle = getAngularOffset(rad),
                theta0 = 360.0f * Helper.randomNumber(),
                velTheta = getOrbitalVelocity(rad),
                temp = 6000f + (4000f * Helper.randomNumber() - 2000f),
                mag = 0.1f + 0.4f * Helper.randomNumber(),
                type = 0
            )

            // make a small portion of the stars brighter
            if (i < numberOfStars / 60) {
                star.mag = Math.min(star.mag + 0.1f + Helper.randomNumber() * 0.4f, 1.0f)
            }
            stars.add(star)
            */

        }
    }

    /**
     * Add all the dust particle to the array list with object that are part of the galaxy: stars, dust particles, H2 regions...
     */
    fun initDustParticles() {

        var x = 0f
        var y = 0f
        var rad = 0f

        // initialise dust, there are as many dust clouds as there are stars
        for (i in 0 until numberOfDustParticles) {

            if (i % 2 == 0) {

                // random position in the galaxy CORE
                rad = cdf.valFromProb(Helper.randomNumber())
            } else {

                // random position inside the galaxy
                x = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
                y = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
                rad = Math.sqrt(x * x.toDouble() + y * y).toFloat()
            }

            val a = rad
            val b = rad * getEccentricity(rad)
            val dustParticle = Star(
                a = a,
                b = b,
                tiltAngle = getAngularOffset(rad),
                theta0 = 360.0f * Helper.randomNumber(),
                velTheta = getOrbitalVelocity((a + b) / 2.0f),
                type = 1
            )

            // I want the outer parts to appear blue, the inner parts yellow. I'm imposing
            // the following temperature distribution (no science here it just looks right)
            dustParticle.temp = baseTemperature + rad / 4.5f
            dustParticle.mag = 0.02f + 0.15f * Helper.randomNumber()

            dustParticle.mag *= galaxySizeFactor * dustParticlesSizeFactor
            galaxyObjects.add(dustParticle)
        }
    }

    /**
     * Add all the dust filaments to the array list with object that are part of the galaxy: stars, dust particles, H2 regions...
     */
    fun initDustFilamentParticles() {

        var x = 0f
        var y = 0f
        var rad = 0f

        // initialize additional dust filaments
        for (i in 0 until numberOfDustFilaments) {

            x = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
            y = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
            rad = Math.sqrt(x * x.toDouble() + y * y).toFloat()

            val theta: Float = 360.0f * Helper.randomNumber()
            val mag: Float = 0.1f + 0.05f * Helper.randomNumber()
            val num: Int = (100 * Helper.randomNumber()).toInt()

            for (j in 0 until num) {

                rad = rad + 200f - 400f * Helper.randomNumber()

                val a = rad
                val b = rad * getEccentricity(rad)
                val dustParticle = Star(
                    a = a,
                    b = b,
                    tiltAngle = getAngularOffset(rad),
                    theta0 = theta + 10f - 20f * Helper.randomNumber(),
                    velTheta = getOrbitalVelocity((a + b) / 2.0f)
                )

                // I want the outer parts to appear blue, the inner parts yellow. I'm imposing
                // the following temperature distribution (no science here it just looks right)
                dustParticle.temp = baseTemperature + rad / 4.5f - 1000f
                dustParticle.mag = mag + 0.025f * Helper.randomNumber()
                dustParticle.type = 2

                dustParticle.mag *= galaxySizeFactor * dustFilamentsSizeFactor
                galaxyObjects.add(dustParticle)
            }
        }
    }

    /**
     * Add all the H2 regions to the array list with object that are part of the galaxy: stars, dust particles, H2 regions...
     */
    fun initH2Regions() {

        var x = 0f
        var y = 0f
        var rad = 0f

        // initialise H2 regions
        for (i in 0 until numberOfH2Regions) {

            x = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
            y = 2 * galaxyRadius * Helper.randomNumber() - galaxyRadius
            rad = Math.sqrt(x * x.toDouble() + y * y).toFloat()

            val a = rad
            val b = rad * getEccentricity(rad)
            val particleH2 = Star(
                a = a,
                b = b,
                tiltAngle = getAngularOffset(rad),
                theta0 = 360.0f * Helper.randomNumber(),
                velTheta = getOrbitalVelocity((a + b) / 2.0f),
                temp = 6000f + (6000f * Helper.randomNumber()) - 3000f,
                mag = 0.1f + 0.05f * Helper.randomNumber(),
                type = 3
            )

            particleH2.mag *= galaxySizeFactor * H2RegionsSizeFactor
            galaxyObjects.add(particleH2)

            // push particle again with type 4 (bright red core of an H2 region)
            val particleH2Highlight = Star(
                a = particleH2.a,
                b = particleH2.b,
                tiltAngle = particleH2.tiltAngle,
                theta0 = particleH2.theta0,
                velTheta = particleH2.velTheta,
                temp = particleH2.temp,
                mag = particleH2.mag,
                type = 4
            )
            particleH2Highlight.mag *= galaxySizeFactor * H2RegionsSizeFactor
            galaxyObjects.add(particleH2Highlight)
        }
    }

    /**
     * Method to normalize the coordinates of all the object that are part of the galaxy: stars, dust particles, H2 regions...
     * from graphic coordinate system in to OpenGL coordinate system
     */
    fun normalizeCoordinates() {

        // store all the coordinates of each object
        val coordinates = FloatArray(galaxyObjects.size * 2)
        for (i in galaxyObjects.indices) {
            coordinates[i * 2] = galaxyObjects[i].a
            coordinates[i * 2 + 1] = galaxyObjects[i].b
        }

        // normalize the coordinates and convert them in to OpenGL coordinate system
        gestureDetector.normalizeCoordinates(coordinates)

        // now set the OpenGL coordinates to the objects
        for (i in galaxyObjects.indices) {
            galaxyObjects[i].a = coordinates[i * 2]
            galaxyObjects[i].b = coordinates[i * 2 + 1]
        }
    }

    /**
     * Method that update the galaxy params with the information about the density wave, physics and render params
     * @param params the new galaxy params
     * @param recomputeGalaxyObject if all the galaxy objects should be set again, this is heavy operations and is used only when pre-made galaxy models are set or the settings were changed
     */
    fun setParams(params: GalaxyParams? = null, recomputeGalaxyObject: Boolean) {

        if (params != null) {

            // density wave params
            exInner = params.exInner
            exOuter = params.exOuter
            exOuter = params.exOuter
            angularOffset = params.angularOffset
            innerCoreRadius = params.innerCoreRadius
            galaxyRadius = params.galaxyRadius
            radiusFarField = params.galaxyRadius * 2  // there is no science behind this threshold it just looks nice
            dustRenderSize = params.dustRenderSize
            pertN = params.pertN
            pertAmp = params.pertAmp

            // physics params
            baseTemperature = params.baseTemperature
            hasDarkMatter = params.hasDarkMatter

            // render params
            showStars = params.showStars
            numberOfStars = params.numberOfStars
            starsSizeFactor = params.starsSizeFactor
            showDustParticles = params.showDustParticles
            numberOfDustParticles = params.numberOfDustParticles
            dustParticlesSizeFactor = params.dustParticlesSizeFactor
            showDustFilaments = params.showDustFilaments
            numberOfDustFilaments = params.numberOfDustFilaments
            dustFilamentsSizeFactor = params.dustFilamentsSizeFactor
            showH2Regions = params.showH2Regions
            numberOfH2Regions = params.numberOfH2Regions
            H2RegionsSizeFactor = params.H2RegionsSizeFactor
            galaxySizeFactor = params.galaxySizeFactor
        }

        if (recomputeGalaxyObject) {
            create()
        }
    }

    /**
     * Method for drawing the galaxy
     * @param totalTime the total time that has passed
     * @param isPaused if the time is paused
     * @param transformedMatrixOpenGL array with the OpenGL transformations, generated from the gesture detector
     */
    fun draw(totalTime: Float, isPaused: Boolean, transformedMatrixOpenGL: FloatArray) {

        // check if at leas any of the stars, dust, dust filaments, or H2 regions are shown, before drawing the stars
        var features = 0
        if (showStars) features = features or (1 shl 0)
        if (showDustParticles) features = features or (1 shl 1)
        if (showDustFilaments) features = features or (1 shl 2)
        if (showH2Regions) features = features or (1 shl 3)
        if (features != 0) {

            // update the galaxy
            if (!isPaused) {
                vertStars.updateShaderVariables(totalTime, pertN, pertAmp, dustRenderSize.toInt(), features)
            }

            // draw galaxy
            vertStars.draw(transformedMatrixOpenGL, transformedMatrixOpenGL)
        }

        // draw the density wave it is shown
        if (showDensityWaves)
            vertDensityWaves.draw(transformedMatrixOpenGL, transformedMatrixOpenGL)
    }

    /**
     * Update the density wave and the galaxy it self
     */
    fun update() {
        updateDensityWaves()
        updateGalaxy()
    }

    /**
     * Update the galaxy by generating all the galaxy objects: stars, dust particles, H2 regions... from scratch.
     * This is very heavy operations and is used only when pre-made galaxy models are set or the settings were changed
     * @param updateGalaxy if the galaxy should be updated
     */
    fun updateGalaxy(updateGalaxy: Boolean = this.updateGalaxy) {
        if (!updateGalaxy) return

        setParams(null, true)

        val vert: ArrayList<VertexStar> = arrayListOf()
        val idx: ArrayList<Int> = arrayListOf()
        for (i in 1 until galaxyObjects.size) {

            val color: Color = Helper.colorFromTemperature(galaxyObjects[i].temp)
            idx.add(vert.size)
            vert.add(VertexStar(galaxyObjects[i], color))
        }

        vertStars.createBuffer(vert, idx, GLES30.GL_POINTS)
        this.updateGalaxy = false
    }

    /**
     * Update the density wave curve for the galaxy
     * This is very heavy operations and is used only when pre-made galaxy models are set or the settings were changed
     * @param updateDensityWaves if the density wave should be updated
     */
    fun updateDensityWaves(updateDensityWaves: Boolean = this.updateDensityWaves) {
        if (!updateDensityWaves) return

        DensityWaveRenderer.updateDensityWaves(vertDensityWaves, this, gestureDetector, floatArrayOf(1f, 1f, 1f, 0.4f))
        this.updateDensityWaves = false
    }

    /**
     * Force update of both the galaxy and it density wave curve
     */
    fun forceUpdate() {
        updateDensityWaves(true)
        updateGalaxy(true)
    }
}