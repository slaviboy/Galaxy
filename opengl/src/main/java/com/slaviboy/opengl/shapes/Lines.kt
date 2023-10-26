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
import com.slaviboy.opengl.params.VertexColor

/**
 * Class that is used for generating OpenGL Lines shapes
 * @param strokeWidth the stroke width of the lines
 * @param drawingMode GL_STATIC_DRAW or GL_DYNAMIC_DRAW
 */
class Lines(
    var strokeWidth: Float = 1f,
    drawingMode: Int = 0
) : Shape<VertexColor>(drawingMode) {

    var attributePosition: Int = 0  // attribute for the position of the lines
    var attributeColor: Int = 1     // attribute for the color of the lines

    init {
        attributes = arrayListOf(
            AttributeDefinition(attributePosition, 3, 0),
            AttributeDefinition(attributeColor, 4, 3 * 4)
        )
    }

    override fun onBeforeDraw() {
        GLES30.glLineWidth(strokeWidth)
    }

    override fun getVertexShaderSource(): String {
        return """#version 300 es

            precision mediump float;
            uniform mat4 projMat;
            uniform mat4 viewMat;
            layout(location = 0) in vec3 position;
            layout(location = 1) in vec4 color;
            out vec4 vertexColor;
    
            void main ()
            {
                gl_Position = projMat * vec4(position, 1); 
                vertexColor = color;
            }
        """.trimIndent()
    }

    override fun getFragmentShaderSource(): String {
        return """#version 300 es

            precision mediump float;
            out vec4 FragColor;
            in vec4 vertexColor;
    
            void main ()
            {
                FragColor = vertexColor;
            }
        """.trimIndent()
    }
}
