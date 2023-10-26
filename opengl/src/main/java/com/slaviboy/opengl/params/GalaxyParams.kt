package com.slaviboy.opengl.params

import com.slaviboy.opengl.helpers.Helper

/**
 * Class holding the params for rendering the density wave curve and the stars,dust...
 * @param galaxyRadius radius of the galaxy in ly(light-years)
 * @param innerCoreRadius the radius of the inner core in ly(light-years)
 * @param angularOffset angular offset per parsec measured in degrees
 * @param exInner eccentricity of the innermost ellipse
 * @param exOuter eccentricity of the outermost ellipse
 * @param hasDarkMatter indicate if the galaxy has dark matter
 * @param pertN number of ellipse disturbances
 * @param pertAmp ellipse disturbance damping factor
 * @param dustRenderSize the size of the rendered dust particles
 * @param baseTemperature the base temperature of the galaxy, this indicates the color of the galaxy
 * @param showStars if the stars should be drawn
 * @param numberOfStars the total number of stars for the galaxy
 * @param starsSizeFactor a factor to increase the magnitude(size) of the stars [0,2]
 * @param showDustParticles if the dust particles should be drawn
 * @param numberOfDustParticles the total number of dust particles
 * @param dustParticlesSizeFactor a factor to increase the magnitude(size) of the dust particles in range [0,2]
 * @param showDustFilaments if the dust filament particles should be drawn
 * @param numberOfDustFilaments the total number of dust filaments
 * @param dustFilamentsSizeFactor a factor to increase the magnitude(size) of the dust filament particles in range [0,2]
 * @param showH2Regions if the H2 regions(particles) should be drawn
 * @param numberOfH2Regions the total number of H2 regions(particles)
 * @param H2RegionsSizeFactor a factor to increase the magnitude(size) of the H2 regions(particles) in range [0,2]
 * @param galaxySizeFactor a factor that increases the size of all element: [stars, dust particle, dust filaments, H2 regions]
 */
open class GalaxyParams(
    galaxyRadius: Float = 15000f,
    open var innerCoreRadius: Float = 6000f,
    open var angularOffset: Float = 0.019f,
    open var exInner: Float = 0.8f,
    open var exOuter: Float = 1f,
    open var hasDarkMatter: Boolean = true,
    open var pertN: Int = 0,
    open var pertAmp: Float = 0f,
    open var dustRenderSize: Float = 70f,
    open var baseTemperature: Float = 4000f,

    open var showStars: Boolean = true,
    open var numberOfStars: Int = 60000,
    open var starsSizeFactor: Float = 1f,

    open var showDustParticles: Boolean = true,
    open var numberOfDustParticles: Int = numberOfStars,
    open var dustParticlesSizeFactor: Float = 1f,

    open var showDustFilaments: Boolean = true,
    open var numberOfDustFilaments: Int = numberOfStars / 100,
    open var dustFilamentsSizeFactor: Float = 1f,

    open var showH2Regions: Boolean = true,
    open var numberOfH2Regions: Int = 400,
    open var H2RegionsSizeFactor: Float = 1f,

    open var galaxySizeFactor: Float = 1f
) {

    constructor(p: GalaxyParams) : this(
        p.galaxyRadius, p.innerCoreRadius, p.angularOffset, p.exInner, p.exOuter, p.hasDarkMatter, p.pertN, p.pertAmp, p.dustRenderSize,
        p.baseTemperature, p.showStars, p.numberOfStars, p.starsSizeFactor, p.showDustParticles, p.numberOfDustParticles, p.dustParticlesSizeFactor,
        p.showDustFilaments, p.numberOfDustFilaments, p.dustFilamentsSizeFactor, p.showH2Regions, p.numberOfH2Regions, p.H2RegionsSizeFactor, p.galaxySizeFactor
    )

    constructor(paramsValues: Array<Any>) : this(fromArray(paramsValues))

    open var radiusFarField: Float = galaxyRadius * 2 // the radius after which all density waves must have circular shape
    open var galaxyRadius: Float = galaxyRadius
        set(value) {
            field = value
            radiusFarField = value * 2
        }

    fun getEccentricity(r: Float): Float {
        return getEccentricity(
            innerCoreRadius, galaxyRadius,
            exInner, exOuter,
            radiusFarField, r
        )
    }

    fun getOrbitalVelocity(rad: Float): Float {
        return getOrbitalVelocity(hasDarkMatter, rad)
    }

    fun getAngularOffset(rad: Float): Float {
        return getAngularOffset(angularOffset, rad)
    }

    fun update(
        innerCoreRadius: Float, galaxyRadius: Float, angularOffset: Float, exInner: Float,
        exOuter: Float, pertN: Int, pertAmp: Float
    ) {
        this.innerCoreRadius = innerCoreRadius
        this.galaxyRadius = galaxyRadius
        this.angularOffset = angularOffset
        this.exInner = exInner
        this.exOuter = exOuter
        this.pertN = pertN
        this.pertAmp = pertAmp
    }

    companion object {

        fun getEccentricity(
            innerCoreRadius: Float, galaxyRadius: Float, exInner: Float, exOuter: Float,
            radiusFarField: Float, r: Float
        ): Float {

            return if (r < innerCoreRadius) {
                // core region of the galaxy. Innermost part is round eccentricity increasing linear to the border of the core.
                1 + (r / innerCoreRadius) * (exInner - 1)
            } else if (r > innerCoreRadius && r <= galaxyRadius) {
                exInner + (r - innerCoreRadius) / (galaxyRadius - innerCoreRadius) * (exOuter - exInner)
            } else if (r > galaxyRadius && r < radiusFarField) {
                // eccentricity is slowly reduced to 1.
                exOuter + (r - galaxyRadius) / (radiusFarField - galaxyRadius) * (1 - exOuter)
            } else 1f
        }

        fun getOrbitalVelocity(hasDarkMatter: Boolean, rad: Float): Float {

            // velocity in kilometer per seconds
            val velocityKPS = if (hasDarkMatter) {
                Helper.velocityWithDarkMatter(rad)
            } else {
                Helper.velocityWithoutDarkMatter(rad)
            }

            // calculate velocity in degree per year
            val u: Float = 2.0f * Helper.PI * rad * Helper.PC_TO_KM
            val time: Float = u / (velocityKPS * Helper.SEC_PER_YEAR)

            return 360.0f / time
        }

        fun getAngularOffset(angleOffset: Float, rad: Float): Float {
            return rad * angleOffset
        }

        fun fromArray(params: Array<Any>): GalaxyParams {
            if (params.size != 23) return GalaxyParams()

            // set the params from an array list, used for the pre-made glaxy models
            return GalaxyParams(
                params[0] as Float,
                params[1] as Float,
                params[2] as Float,
                params[3] as Float,
                params[4] as Float,
                params[5] as Boolean,
                params[6] as Int,
                params[7] as Float,
                params[8] as Float,
                params[9] as Float,
                params[10] as Boolean,
                params[11] as Int,
                params[12] as Float,
                params[13] as Boolean,
                params[14] as Int,
                params[15] as Float,
                params[16] as Boolean,
                params[17] as Int,
                params[18] as Float,
                params[19] as Boolean,
                params[20] as Int,
                params[21] as Float,
                params[22] as Float
            )
        }
    }
}