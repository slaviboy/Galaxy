package com.slaviboy.opengl.entities

data class MoreInfo(
    var storeLastTransformation: Boolean = true,
    var mainGestureDetectorValues: FloatArray = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f
    )
)