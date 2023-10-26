package com.slaviboy.opengl.params

abstract class VertexBase {
    abstract fun writeTo(array: FloatArray, offset: Int)
    abstract fun numberOfFloats(): Int
}