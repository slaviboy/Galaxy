package com.slaviboy.opengl.entities

data class DensityWavesInfo(
    var galaxyCoreRadius: RangeValues,
    var galaxyRadius: RangeValues,
    var angularOffset: RangeValues,
    var innerEccentricity: RangeValues,
    var outerEccentricity: RangeValues,
    var numberOfEllipseDisturbances: RangeValues,
    var ellipseDisturbanceDampingFactor: RangeValues
)