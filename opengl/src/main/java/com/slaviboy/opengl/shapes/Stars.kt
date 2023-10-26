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
import com.slaviboy.opengl.params.VertexStar

/**
 * Class that is used for generating OpenGL Stars shapes
 */
class Stars : Shape<VertexStar>(GLES30.GL_STATIC_DRAW) {

    var isXrayModeOn: Boolean = false // if we should invert the color, to view the galaxy

    var pertN: Int = 0
    var dustSize: Int = 0
    var pertAmp: Float = 0f
    var time: Float = 0f
    var displayFeatures: Int = 0
    var blendFunc: Int = GLES30.GL_ONE
    var blendEquation: Int = GLES30.GL_FUNC_ADD

    var attTheta0: Int = 0
    var attVelTheta: Int = 1
    var attTiltAngle: Int = 2
    var attSemiMajorAxis: Int = 3
    var attSemiMinorAxis: Int = 4
    var attTemperature: Int = 5
    var attMagnitude: Int = 6
    var attType: Int = 7
    var attColor: Int = 8

    init {
        attributes = arrayListOf(
            AttributeDefinition(attTheta0, 1, 0),
            AttributeDefinition(attVelTheta, 1, 4),
            AttributeDefinition(attTiltAngle, 1, 8),
            AttributeDefinition(attSemiMajorAxis, 1, 12),
            AttributeDefinition(attSemiMinorAxis, 1, 16),
            AttributeDefinition(attTemperature, 1, 20),
            AttributeDefinition(attMagnitude, 1, 24),
            AttributeDefinition(attType, 1, 28),
            AttributeDefinition(attColor, 4, 32)
        )
    }

    fun updateShaderVariables(time: Float, num: Int, amp: Float, dustSize: Int, displayFeatures: Int) {
        this.time = time
        this.pertN = num
        this.pertAmp = amp
        this.dustSize = dustSize
        this.displayFeatures = displayFeatures
    }

    override fun onSetCustomShaderVariables() {
        if (shaderProgram == 0)
            throw Exception("onSetCustomShaderVariables(): Shader program is null!")

        val varDustSize = GLES30.glGetUniformLocation(shaderProgram, "dustSize")
        GLES30.glUniform1i(varDustSize, dustSize)

        val varPertN = GLES30.glGetUniformLocation(shaderProgram, "pertN")
        GLES30.glUniform1i(varPertN, pertN)

        val varPertAmp = GLES30.glGetUniformLocation(shaderProgram, "pertAmp")
        GLES30.glUniform1f(varPertAmp, pertAmp)

        val varTime = GLES30.glGetUniformLocation(shaderProgram, "time")
        GLES30.glUniform1f(varTime, time)

        val varDisplayFeatures = GLES30.glGetUniformLocation(shaderProgram, "displayFeatures")
        GLES30.glUniform1i(varDisplayFeatures, displayFeatures)
    }

    override fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (shaderProgram == 0) {
            throw Exception("draw(...): Shader program is null!")
        }

        GLES30.glUseProgram(shaderProgram)

        val viewMatIdx = GLES30.glGetUniformLocation(shaderProgram, "viewMat")
        GLES30.glUniformMatrix4fv(viewMatIdx, 1, false, viewMatrix, 0)

        val projMatIdx = GLES30.glGetUniformLocation(shaderProgram, "projMat")
        GLES30.glUniformMatrix4fv(projMatIdx, 1, false, projectionMatrix, 0)

        onSetCustomShaderVariables()

        GLES30.glEnable(GLES30.GL_BLEND)
        if (isXrayModeOn) {
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        } else {
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, blendFunc)
            GLES30.glBlendEquation(blendEquation)
        }

        onBeforeDraw()

        GLES30.glBindVertexArray(vao[0])
        GLES30.glDrawElements(primitiveType, arrayElementCount, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)

        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        GLES30.glUseProgram(0)
    }


    override fun getVertexShaderSource(): String {
        return """#version 300 es

            #define DEG_TO_RAD 0.01745329251
            uniform mat4 projMat;
            uniform mat4 viewMat;
            uniform int pertN;
            uniform int dustSize;
            uniform int displayFeatures;
            uniform float pertAmp;
            uniform float time;
            
            layout(location = 0) in float theta0;
            layout(location = 1) in float velTheta;
            layout(location = 2) in float tiltAngle;
            layout(location = 3) in float a;
            layout(location = 4) in float b;
            layout(location = 5) in float temp;
            layout(location = 6) in float mag;
            layout(location = 7) in float type;
            layout(location = 8) in vec4 color;
            out vec4 vertexColor;
            out float vertexType;
            flat out int features;
            
            vec2 calcPos(float a, float b, float theta, float velTheta, float time, float tiltAngle) {
                
                float thetaActual = theta + velTheta * time;
                float beta = -tiltAngle;
                float alpha = thetaActual * DEG_TO_RAD;
                float cosalpha = cos(alpha);
                float sinalpha = sin(alpha);
                float cosbeta = cos(beta);
                float sinbeta = sin(beta);
                vec2 center = vec2(0,0);
                vec2 ps = vec2(center.x + (a * cosalpha * cosbeta - b * sinalpha * sinbeta),
                               center.y + (a * cosalpha * sinbeta + b * sinalpha * cosbeta));
                if (pertAmp > 0.0 && pertN > 0) {
                    ps.x += (a / pertAmp) * sin(alpha * 2.0 * float(pertN));
                    ps.y += (a / pertAmp) * cos(alpha * 2.0 * float(pertN));
                }
                return ps;
            }
            
            void main()
            {
                vec2 ps = calcPos(a, b, theta0, velTheta, time, tiltAngle);
                if (type==0.0) {
                    gl_PointSize = mag * 4.0;
                    vertexColor = color * mag;
                } else if (type==1.0) {	
                    gl_PointSize = mag * 5.0 * float(dustSize);
                    vertexColor = color * mag;
                } else if (type==2.0) {
                    gl_PointSize = mag * 2.0 * float(dustSize);
                    vertexColor = color * mag;
                } else if (type==3.0) {
                    vec2 ps2 = calcPos(a + 1000.0, b, theta0, velTheta, time, tiltAngle);
                    float dst = distance(ps, ps2);
                    float size = ((1000.0 - dst) / 10.0) - 50.0;
                    gl_PointSize = size;
                    vertexColor = color * mag * vec4(2.0, 0.5, 0.5, 1.0);
                } else if (type==4.0) {
                    vec2 ps2 = calcPos(a + 1000.0, b, theta0, velTheta, time, tiltAngle);
                    float dst = distance(ps, ps2);
                    float size = ((1000.0 - dst) / 10.0) - 50.0;
                    gl_PointSize = size/10.0;
                    vertexColor = vec4(1.0,1.0,1.0,1.0);
                }
                gl_Position =  projMat * vec4(ps, 0.0, 1.0);
                vertexType = type;
                features = displayFeatures;
            }""".trimIndent()
    }

    override fun getFragmentShaderSource(): String {
        return """#version 300 es

            precision mediump float;
            in vec4 vertexColor;
            in float vertexType;
            flat in int features;
            out vec4 FragColor;

            void main()
            {
                if (vertexType==0.0) {
                    if ( (features & 1) ==0)
                        discard;
                    FragColor = vertexColor;
                    vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
                    float alpha = 1.0 - length(circCoord);
                    FragColor = vec4(vertexColor.xyz, alpha);
                } else if (vertexType==1.0) {
                    if ( (features & 2) ==0)
                        discard;
                    vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
                    float alpha = 0.05 * (1.0 - length(circCoord));
                    FragColor = vec4(vertexColor.xyz, alpha);
                } else if (vertexType==2.0) {
                    if ( (features & 4) ==0)
                        discard;
                    vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
                    float alpha = 0.07 * (1.0 - length(circCoord));
                    FragColor = vec4(vertexColor.xyz, alpha);
                } else if (vertexType==3.0) {
                    if ((features & 8) == 0)
                        discard;
                    vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
                    float alpha = 1.0 - length(circCoord);
                    FragColor = vec4(vertexColor.xyz, alpha);
                } else if (vertexType==4.0) {
                    if ((features & 8)== 0)
                        discard;
                    vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
                    float alpha = 1.0 - length(circCoord);
                    FragColor = vec4(vertexColor.xyz, alpha);
                }
            }
        """.trimIndent()
    }
}
