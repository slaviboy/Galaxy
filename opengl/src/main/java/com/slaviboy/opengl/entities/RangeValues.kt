package com.slaviboy.opengl.entities

data class RangeValues(
    var lowerBound: Float = 0f,
    var upperBound: Float = 100f,
    var currentValue: Float = 0.0f,
    var roundDecimalPlaces: Int = 1,
    var valuesAreAsPercentage: Boolean = false
)