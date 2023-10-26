package com.slaviboy.galaxy

import android.graphics.Color.BLACK
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import com.slaviboy.opengl.helpers.StaticMethods
import com.slaviboy.opengl.helpers.StaticMethods.asColorArray
import com.slaviboy.opengl.entities.DensityWavesInfo
import com.slaviboy.opengl.entities.PhysicsInfo
import com.slaviboy.opengl.entities.RangeValues
import com.slaviboy.opengl.entities.RenderInfo
import com.slaviboy.opengl.galaxy.GalaxyView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        StaticMethods.setSizeComponents(it.size.width.toFloat(), it.size.height.toFloat())
                    },
                factory = { context ->
                    GalaxyView(context)
                },
                update = { view ->
                    view.galaxyRenderer.apply {
                        showStars = true
                        showH2 = true
                        showDust = true
                        showDustFilaments = true
                        showDensityWaves = false
                        showInfiniteGrid = false
                        isXrayModeOn = false
                        backgroundColor = (BLACK).asColorArray()
                        setConfiguration(
                            getDensityWavesInfo(),
                            getPhysicsInfo(),
                            getRenderInfo()
                        )
                    }
                }
            )
        }
    }

    private fun getDensityWavesInfo(): DensityWavesInfo {
        return DensityWavesInfo(
            galaxyCoreRadius = RangeValues(
                lowerBound = 10f,
                upperBound = 20000f,
                currentValue = 4000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            galaxyRadius = RangeValues(
                lowerBound = 1000f,
                upperBound = 20000f,
                currentValue = 13000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            angularOffset = RangeValues(
                lowerBound = -0.0025f,
                upperBound = 0.0025f,
                currentValue = 0.0004f,
                roundDecimalPlaces = 4,
                valuesAreAsPercentage = false
            ),
            innerEccentricity = RangeValues(
                lowerBound = 0f,
                upperBound = 50f,
                currentValue = 0.85f,
                roundDecimalPlaces = 2,
                valuesAreAsPercentage = false
            ),
            outerEccentricity = RangeValues(
                lowerBound = 0f,
                upperBound = 50f,
                currentValue = 0.95f,
                roundDecimalPlaces = 2,
                valuesAreAsPercentage = false
            ),
            numberOfEllipseDisturbances = RangeValues(
                lowerBound = 0f,
                upperBound = 25f,
                currentValue = 2f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            ellipseDisturbanceDampingFactor = RangeValues(
                lowerBound = 2f,
                upperBound = 100f,
                currentValue = 40f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            )
        )
    }

    private fun getPhysicsInfo(): PhysicsInfo {
        return PhysicsInfo(
            darkMatter = true,
            timeStepLength = RangeValues(
                lowerBound = -500000f,
                upperBound = 500000f,
                currentValue = 60000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            baseTemperature = RangeValues(
                lowerBound = 1000f,
                upperBound = 10000f,
                currentValue = 4000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            )
        )
    }

    private fun getRenderInfo(): RenderInfo {
        return RenderInfo(
            numberOfStars = RangeValues(
                lowerBound = 0f,
                upperBound = 100000f,
                currentValue = 60000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            numberOfH2Regions = RangeValues(
                lowerBound = 0f,
                upperBound = 600f,
                currentValue = 400f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            numberOfDustParticles = RangeValues(
                lowerBound = 0f,
                upperBound = 100000f,
                currentValue = 60000f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            numberOfDustFilaments = RangeValues(
                lowerBound = 0f,
                upperBound = 1000f,
                currentValue = 600f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = false
            ),
            starsSizeFactor = RangeValues(
                lowerBound = 0f,
                upperBound = 2f,
                currentValue = 1f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = true
            ),
            H2RegionsSizeFactor = RangeValues(
                lowerBound = 0f,
                upperBound = 2f,
                currentValue = 1f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = true
            ),
            dustParticlesSizeFactor = RangeValues(
                lowerBound = 0f,
                upperBound = 2f,
                currentValue = 1f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = true
            ),
            dustFilamentsSizeFactor = RangeValues(
                lowerBound = 0f,
                upperBound = 2f,
                currentValue = 1f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = true
            ),
            galaxySizeFactor = RangeValues(
                lowerBound = 0f,
                upperBound = 2f,
                currentValue = 1f,
                roundDecimalPlaces = 0,
                valuesAreAsPercentage = true
            )
        )
    }
}