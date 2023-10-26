package com.slaviboy.opengl.params

data class VertexColor(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val r: Float = 0f,
    val g: Float = 0f,
    val b: Float = 0f,
    val a: Float = 0f
) : VertexBase() {

    var position: Vec3 = Vec3(x, y, z)
    var color: Color = Color(r, g, b, a)

    override fun numberOfFloats(): Int {
        return 3 + 4
    }

    override fun writeTo(array: FloatArray, offset: Int) {

        // the first 3 values are for the position (x,y,z)
        array[offset + 0] = position.x
        array[offset + 1] = position.y
        array[offset + 2] = position.z

        // the last 4 values are for the color (r,g,b,a)
        array[offset + 3] = color.r
        array[offset + 4] = color.g
        array[offset + 5] = color.b
        array[offset + 6] = color.a
    }
}