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
package com.slaviboy.opengl

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import com.slaviboy.opengl.helpers.StaticMethods.DEVICE_HEIGHT
import com.slaviboy.opengl.helpers.StaticMethods.DEVICE_WIDTH
import com.slaviboy.opengl.helpers.StaticMethods.NEAR
import com.slaviboy.opengl.helpers.StaticMethods.RATIO

/**
 * Gesture detection using a transformation matrix, the changes according to
 * gestures made by the user, supported gestures are Move, Scale and Rotate.
 * All transformations are then applied to a matrix, that can be used to
 * transform array with coordinates as float array, paths or canvas elements.
 * @param matrix main matrix that holds all the gesture transformation: rotation, scale and translation
 * @param listener listener with callback, triggered when new transformations are done
 */
class MatrixGestureDetector(var matrix: Matrix = Matrix(), var listener: ((gestureDetector: MatrixGestureDetector) -> Unit) = {}) {

    internal var pointerIndex: Int                   // pointer for the index
    internal val tempMatrix: Matrix                  // temp matrix used in poly to poly method
    internal val source: FloatArray                  // initial coordinates from finger down event for each finger in following order [x1,y1, x2,y2, ...]
    internal val distance: FloatArray                // new coordinates from finger move event for each finger in following order [x1,y1, x2,y2, ...]

    internal val convertMatrix: Matrix               // matrix used in the conversion, that way no new matrix is created for each call
    internal val convertMatrixInvert: Matrix         // invert matrix of the convert matrix, that way no new matrix is created for each call

    var scale: Float                                 // current scale factor used same for x and y directions
    var angle: Float                                 // current rotational angle in degrees
    var translate: PointF                            // current translate values for x and y directions

    var matrixTransformations: FloatArray

    init {

        matrixTransformations = FloatArray(9)

        pointerIndex = 0
        tempMatrix = Matrix()
        source = FloatArray(4)
        distance = FloatArray(4)
        scale = 0f
        angle = 0f
        translate = PointF()
        convertMatrix = Matrix()
        convertMatrixInvert = Matrix()

        setTransformations()
    }

    /**
     * Method called in order to apply transformation made using finger gestures to the
     * matrix using motion event used in the views, to detect touch events.
     * @param event motion event from the view
     */
    fun onTouchEvent(event: MotionEvent) {

        // only two fingers
        val count = event.pointerCount
        if (count > 2) return

        val action = event.actionMasked
        val index = event.actionIndex
        when (action) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {

                // get the coordinates for the particular finger
                val idx = index * 2
                source[idx] = event.getX(index)
                source[idx + 1] = event.getY(index)

                pointerIndex = 0
            }

            MotionEvent.ACTION_MOVE -> {

                for (i in 0 until count) {
                    val idx = pointerIndex + i * 2
                    distance[idx] = event.getX(i)
                    distance[idx + 1] = event.getY(i)
                }

                // use poly to poly to detect transformations
                tempMatrix.setPolyToPoly(source, pointerIndex, distance, pointerIndex, count)
                matrix.postConcat(tempMatrix)
                System.arraycopy(distance, 0, source, 0, distance.size)

                // trigger the callback
                listener.invoke(this)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(index) == 0) pointerIndex = 2
            }
        }

        // set the transformations after matrix update
        setTransformations()

    }

    /**
     * Get the transformations: translate, scale and rotation from the
     * current transformation matrix values.
     */
    fun setTransformations() {

        matrix.getValues(matrixTransformations)

        val scaleX: Float = matrixTransformations[Matrix.MSCALE_X]
        val skewX: Float = matrixTransformations[Matrix.MSKEW_X]
        val skewY: Float = matrixTransformations[Matrix.MSKEW_Y]
        val translateX: Float = matrixTransformations[Matrix.MTRANS_X]
        val translateY: Float = matrixTransformations[Matrix.MTRANS_Y]

        // set translate
        translate.x = translateX
        translate.y = translateY

        // set scale
        scale = Math.sqrt(scaleX * scaleX + skewY * skewY.toDouble()).toFloat()

        // set rotation as angle in degrees
        angle = -(Math.atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).toFloat()
        if (angle == -0f) {
            angle = -angle
        }
    }

    /**
     * Get the converted OpenGL matrix with transformation, from the
     * graphic matrix, that is generated by finger gestures.
     * @param sourceMatrix matrix with previous transformations 4x4 = 16 values
     * @param destinationMatrix matrix with applied transformation
     */
    fun transform(sourceMatrix: FloatArray, destinationMatrix: FloatArray) {
        Companion.transform(sourceMatrix, destinationMatrix, matrix)
    }

    /**
     * Convert the coordinate of point from graphic coordinate system, to a OpenGL
     * coordinate system, and set the result point object x and y values with the
     * result from the conversion.
     * @param x x coordinate in graphic coordinate system
     * @param y y coordinate in graphic coordinate system
     * @param pointOpenGL point with resulted OpenGL coordinates
     */
    fun normalizeCoordinate(x: Float, y: Float, pointOpenGL: PointF = PointF()): PointF {

        // get graphics matrix values
        val v = FloatArray(9)
        matrix.getValues(v)

        // get translate value for the OpenGL coordinate system
        v[2] = normalizeTranslateX(v[2])
        v[5] = normalizeTranslateY(v[5])

        // get the invert matrix
        convertMatrix.setValues(v)
        convertMatrix.invert(convertMatrixInvert)

        // map coordinate using the invert matrix
        val xOpenGL = normalizeTranslateX(x)
        val yOpenGL = normalizeTranslateY(y)
        val coords = floatArrayOf(xOpenGL, yOpenGL)
        convertMatrixInvert.mapPoints(coords)

        // set the result point with the new coordinates
        pointOpenGL.x = coords[0]
        pointOpenGL.y = coords[1]

        return pointOpenGL
    }

    /**
     * Convert given array with coordinates from graphic coordinate system, to a OpenGL coordinate system. The
     * format in which the array is give: [x1,y1, x2,y2, ...]
     * @param coordinates coordinates in graphic coordinate system that will be converted
     * @param coordinatesOpenGL array where the converted coordinates in OpenGL coordinate system will be stored, array must have same array size as the one holding the original coordinate
     */
    fun normalizeCoordinates(coordinates: FloatArray, coordinatesOpenGL: FloatArray = coordinates): FloatArray {

        // get graphics matrix values
        val v = FloatArray(9)
        matrix.getValues(v)

        // get translate value for the OpenGL coordinate system
        v[2] = normalizeTranslateX(v[2])
        v[5] = normalizeTranslateY(v[5])

        // get the invert matrix
        convertMatrix.setValues(v)
        convertMatrix.invert(convertMatrixInvert)

        for (i in 0 until coordinates.size / 2) {

            val x = coordinates[i * 2]
            val y = coordinates[i * 2 + 1]

            // map coordinate using the invert matrix
            val xOpenGL = normalizeTranslateX(x)
            val yOpenGL = normalizeTranslateY(y)

            coordinatesOpenGL[i * 2] = xOpenGL
            coordinatesOpenGL[i * 2 + 1] = yOpenGL
        }

        convertMatrixInvert.mapPoints(coordinatesOpenGL)
        return coordinatesOpenGL
    }

    /**
     * Normalize width value of a shape, by converting it from
     * a graphic to a OpenGL coordinate system.
     * @param width width value
     * @param isScalable whether the shape width size should scale, from scale gestures
     */
    fun normalizeWidth(width: Float, isScalable: Boolean = true): Float {
        var openGLWidth = (width / DEVICE_WIDTH) * NEAR * RATIO * 2
        if (!isScalable) {
            openGLWidth /= scale
        }
        return openGLWidth
    }

    /**
     * Normalize height value of a shape, by converting it from
     * a graphic to a OpenGL coordinate system.
     * @param height height value
     * @param isScalable whether the shape height size should scale, from scale gestures
     */
    fun normalizeHeight(height: Float, isScalable: Boolean = true): Float {
        var openGLHeight = (height / DEVICE_HEIGHT) * NEAR * 2
        if (!isScalable) {
            openGLHeight /= scale
        }
        return openGLHeight
    }

    companion object {

        /**
         * Get the scale factor for a given matrix
         */
        fun Matrix.scale(): Float {
            val points = FloatArray(9)
            getValues(points)

            val scaleX: Float = points[Matrix.MSCALE_X]
            val skewY: Float = points[Matrix.MSKEW_Y]
            return Math.sqrt(scaleX * scaleX + skewY * skewY.toDouble()).toFloat()
        }

        /**
         * Get the rotation angle for a given matrix
         */
        fun Matrix.angle(): Float {
            val points = FloatArray(9)
            getValues(points)

            val scaleX: Float = points[Matrix.MSCALE_X]
            val skewX: Float = points[Matrix.MSKEW_X]
            return -(Math.atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).toFloat()
        }

        /**
         * Get transformations for a given matrix
         */
        fun Matrix.translate(): PointF {
            val points = FloatArray(9)
            getValues(points)

            return PointF(points[Matrix.MTRANS_X], points[Matrix.MTRANS_Y])
        }

        /**
         * Get the converted OpenGL matrix with transformation, from the
         * graphic matrix, that is generated by finger gestures.
         * @param sourceMatrix matrix with previous transformations
         * @param destinationMatrix matrix with applied transformation
         * @param matrix graphic matrix
         */
        fun transform(sourceMatrix: FloatArray, destinationMatrix: FloatArray, matrix: Matrix) {

            // get graphics matrix values
            val v = FloatArray(9)
            matrix.getValues(v)

            // get translate value for the OpenGL coordinate system
            v[2] = normalizeTranslateX(v[2])
            v[5] = normalizeTranslateY(v[5])

            // set rotation, scaling and translation from graphics matrix to form new 4x4 OpenGL matrix
            val openGLMatrix = floatArrayOf(
                v[0], v[3], 0f, 0f,
                v[1], v[4], 0f, 0f,
                0f, 0f, 1f, 0f,
                v[2], v[5], 0f, 1f
            )

            // multiply sourceMatrix and openGLMatrix to generate the new matrix
            android.opengl.Matrix.multiplyMM(destinationMatrix, 0, sourceMatrix, 0, openGLMatrix, 0)
        }

        /**
         * Get the converted translate x coordinate from the graphic matrix translation
         * to the OpenGL translation, since they use different coordinate systems.
         * @param x translate value for the x coordinate
         * @param allowZero whether x=0 should be used to calculate translation
         */
        fun normalizeTranslateX(x: Float, allowZero: Boolean = true): Float {

            if (!allowZero && x == 0f) {
                return 0f
            }

            val translateX = if (x < DEVICE_WIDTH / 2f) {
                -1f + (x / (DEVICE_WIDTH / 2f))
            } else {
                (x - (DEVICE_WIDTH / 2f)) / (DEVICE_WIDTH / 2f)
            }

            return -translateX * NEAR * RATIO
        }

        /**
         * Get the converted translate y coordinate from the graphic matrix translation
         * to the OpenGL translation, since they use different coordinate systems.
         * @param y translate value for the y coordinate
         * @param allowZero whether y=0 should be used to calculate translation
         */
        fun normalizeTranslateY(y: Float, allowZero: Boolean = true): Float {

            if (!allowZero && y == 0f) {
                return 0f
            }

            val translateY = if (y < DEVICE_HEIGHT / 2f) {
                1f - (y / (DEVICE_HEIGHT / 2f))
            } else {
                -(y - (DEVICE_HEIGHT / 2f)) / (DEVICE_HEIGHT / 2f)
            }

            return translateY * NEAR
        }
    }
}