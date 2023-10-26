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
package com.slaviboy.opengl.shapes

import android.opengl.GLES30
import com.slaviboy.opengl.params.VertexBase
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Abstract class from which the other shapes Lines and Stars are implemented
 * @param drawingMode GL_STATIC_DRAW or GL_DYNAMIC_DRAW
 */
abstract class Shape<TVertex : VertexBase>(
    var drawingMode: Int = GLES30.GL_DYNAMIC_DRAW
) {

    class AttributeDefinition(
        var attribIdx: Int = 0,
        var size: Int = 0,
        var offset: Int = 0
    )

    var vbo: IntArray                               // vertex buffer object
    var ibo: IntArray                               // index buffer object
    var vao: IntArray                               // vertex array object

    var vert: ArrayList<TVertex>                    // array list with the vertices 'TVertex' is generic type, since it can be
    var idx: ArrayList<Int>                         // index buffer array list
    var attributes: ArrayList<AttributeDefinition>  // array list with the different attributes for that particular OpenGL shape

    var shaderProgram: Int                          // the shader program generated from the GLSL string
    var primitiveType: Int                          // the type indicating from which primitive shapes the OpenGL shape is made GL_LINES, GL_POINTS, GL_TRIANGLES...

    val arrayElementCount: Int
        get() {
            return idx.size
        }

    companion object {
        val msgArray: IntArray = IntArray(1)

        /**
         * Method for creating the different shaders for the fragment or the vertex
         * @param type the shader type GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param string the GLSL string with the C/C++ code for the shader
         */
        fun createShader(type: Int, string: String): Int {

            // generate the shader
            val shader: Int = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, string)
            GLES30.glCompileShader(shader)

            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, msgArray, 0)
            if (msgArray[0] != GLES30.GL_TRUE) {

                val errorMsg = GLES30.glGetShaderInfoLog(shader)
                val typeString = if (type == GLES30.GL_VERTEX_SHADER) "Vertex" else "Fragment"

                GLES30.glDeleteShader(shader)

                throw Exception("VertexBuffer: $typeString shader compilation failed: $errorMsg")
            }

            return shader
        }
    }

    init {
        vbo = IntArray(1)
        ibo = IntArray(1)
        vao = IntArray(1)

        vert = arrayListOf()
        idx = arrayListOf()
        attributes = arrayListOf()

        shaderProgram = -1
        primitiveType = 0
    }

    /**
     * Method that return the string GLSL C/C++ code for the vertex shader for that
     * particular OpenGL shape
     */
    abstract fun getVertexShaderSource(): String

    /**
     * Method that return the string GLSL C/C++ code for the fragment shader for that
     * particular OpenGL shape
     */
    abstract fun getFragmentShaderSource(): String

    fun initialize() {

        // initialize buffers
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glGenBuffers(1, ibo, 0)
        GLES30.glGenVertexArrays(1, vao, 0)

        // initialize vertex shader
        val vertexString: String = getVertexShaderSource()
        val vertexShader: Int = createShader(GLES30.GL_VERTEX_SHADER, vertexString)

        // initialize fragment shader
        val fragmentString: String = getFragmentShaderSource()
        val fragmentShader: Int = createShader(GLES30.GL_FRAGMENT_SHADER, fragmentString)

        shaderProgram = GLES30.glCreateProgram()
        if (shaderProgram == 0) {
            throw Exception("VertexBufferBase.initialize(): shaderProgram cannot be created!")
        }

        // attach the shader and link the program
        GLES30.glAttachShader(shaderProgram, vertexShader)
        GLES30.glAttachShader(shaderProgram, fragmentShader)
        GLES30.glLinkProgram(shaderProgram)

        GLES30.glGetProgramiv(shaderProgram, GLES30.GL_LINK_STATUS, msgArray, 0)
        if (msgArray[0] != GLES30.GL_TRUE) {

            val errorMsg: String = GLES30.glGetProgramInfoLog(shaderProgram)

            GLES30.glDeleteProgram(shaderProgram)
            GLES30.glDeleteShader(vertexShader)
            GLES30.glDeleteShader(fragmentShader)

            throw Exception("VertexBufferBase.initialize():: shader program linking failed!: $errorMsg")
        }

        // detach shaders after a successful link
        GLES30.glDetachShader(shaderProgram, vertexShader)
        GLES30.glDetachShader(shaderProgram, fragmentShader)
    }

    fun disableVertexAttribArray() {

        attributes.forEach {
            GLES30.glDisableVertexAttribArray(it.attribIdx)
        }
    }

    /**
     * Method for deleting all the buffers and disabling the attributes
     */
    fun release() {

        disableVertexAttribArray()

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

        if (vbo[0] != 0) GLES30.glDeleteBuffers(1, vbo, 0)
        if (ibo[0] != 0) GLES30.glDeleteBuffers(1, ibo, 0)
        if (vao[0] != 0) GLES30.glDeleteVertexArrays(1, vao, 0)
    }

    /**
     * Method call when the OpenGL shape is drawn, and it has custom shader
     * variables
     */
    open fun onSetCustomShaderVariables() {}

    /**
     * Method called before the OpenGL shape is being drawn
     */
    open fun onBeforeDraw() {}

    /**
     * The draw method called for redrawing the OpenGL shape
     * @param viewMatrix the view matrix
     * @param projectionMatrix the projection matrix
     */
    open fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (shaderProgram == 0) return

        GLES30.glUseProgram(shaderProgram)

        val viewMatIdx = GLES30.glGetUniformLocation(shaderProgram, "viewMat")
        GLES30.glUniformMatrix4fv(viewMatIdx, 1, false, viewMatrix, 0)

        val projMatIdx = GLES30.glGetUniformLocation(shaderProgram, "projMat")
        GLES30.glUniformMatrix4fv(projMatIdx, 1, false, projectionMatrix, 0)

        onSetCustomShaderVariables()

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)

        onBeforeDraw()

        GLES30.glBindVertexArray(vao[0])
        GLES30.glDrawElements(primitiveType, idx.size, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)

        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glUseProgram(0)
    }

    /**
     * Method for creating buffer from the vertex and index arrays
     * @param vertData array with vertex data
     * @param idx the index array
     */
    fun createBuffer(numberOfFloats: Int, vertData: FloatArray, idx: IntArray = IntArray(vertData.size / numberOfFloats) { i -> i }) {

        val floatBuffer = ByteBuffer.allocateDirect(vertData.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        floatBuffer.put(vertData).position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertData.size * Float.SIZE_BYTES, floatBuffer, drawingMode)

        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo[0])

        // set up vertex buffer array
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])

        // Set up vertex buffer attributes
        attributes.forEach {
            GLES30.glEnableVertexAttribArray(it.attribIdx)
            GLES30.glVertexAttribPointer(it.attribIdx, it.size, GLES30.GL_FLOAT, false, numberOfFloats * 4, it.offset)
        }

        // Set up index buffer array
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo[0])

        // copy index data into a IntBuffer
        val intBuffer = ByteBuffer.allocateDirect(idx.size * Int.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asIntBuffer()
        intBuffer.put(idx).position(0)

        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, idx.size * Int.SIZE_BYTES, intBuffer, GLES30.GL_STATIC_DRAW)

        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            throw Error("VertexBufferBase: Cannot create vbo! (Error $error)")
        }

        GLES30.glBindVertexArray(0)
    }

    /**
     * Method for creating buffer from the vertex and index arrays
     * @param vert the vertex array list
     * @param idx the index array list
     * @param primitiveType the type GL_LINES, GL_POINTS, GL_TRIANGLES
     */
    fun createBuffer(vert: ArrayList<TVertex>, idx: ArrayList<Int>, primitiveType: Int) {

        if (vert.isEmpty() || idx.isEmpty()) return

        this.vert = vert
        this.idx = idx
        this.primitiveType = primitiveType

        // extract the vertex data to the float array
        val numberOfFloats: Int = vert[0].numberOfFloats()
        val floatArray = FloatArray(vert.size * numberOfFloats)
        for (i in vert.indices) {
            vert[i].writeTo(floatArray, i * numberOfFloats)
        }

        createBuffer(numberOfFloats, floatArray, idx.toIntArray())
    }

}