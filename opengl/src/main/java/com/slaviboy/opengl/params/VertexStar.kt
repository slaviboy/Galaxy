package com.slaviboy.opengl.params

class VertexStar(
    var star: Star = Star(),
    var color: Color = Color()
) : VertexBase() {

    override fun numberOfFloats(): Int {
        return 8 + 4
    }

    override fun writeTo(array: FloatArray, offset: Int) {

        // properties for the star
        array[offset + 0] = star.theta0
        array[offset + 1] = star.velTheta
        array[offset + 2] = star.tiltAngle
        array[offset + 3] = star.a
        array[offset + 4] = star.b
        array[offset + 5] = star.temp
        array[offset + 6] = star.mag
        array[offset + 7] = star.type.toFloat()

        // the last 4 values are for the color (r,g,b,a)
        array[offset + 8] = color.r
        array[offset + 9] = color.g
        array[offset + 10] = color.b
        array[offset + 11] = color.a
    }
}