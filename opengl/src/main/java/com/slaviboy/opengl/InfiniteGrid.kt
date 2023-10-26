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

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.opengl.GLES30
import com.slaviboy.opengl.helpers.StaticMethods
import com.slaviboy.opengl.params.VertexColor
import com.slaviboy.opengl.shapes.Lines

/**
 * Class for generating infinite grid by generating only the lines that are visible
 * by the user and are displayed on the canvas. The class have method for updating
 * the parallel lines and drawing them on given canvas.
 * @param gestureDetector the gesture detector for the lines
 * @param distanceBetweenVerticalLines distance between each two vertical lines
 * @param distanceBetweenHorizontalLines distance between each two horizontal lines
 * @param verticalLinesStrokeWidthThick stroke width for the thick vertical lines
 * @param verticalLinesStrokeWidthNormal stroke width for the normal vertical lines
 * @param horizontalLinesStrokeWidthThick stroke width for the thick horizontal lines
 * @param horizontalLinesStrokeWidthNormal stroke width for the normal horizontal lines
 * @param verticalLinesThickLineIndex through how many normal lines a thick line will be drawn for vertical lines
 * @param horizontalLinesThickLineIndex through how many normal lines a thick line will be drawn for horizontal lines
 * @param verticalNormalLinesColor color for normal vertical lines
 * @param verticalThickLinesColor color for thick vertical lines
 * @param horizontalNormalLinesColor color for normal horizontal lines
 * @param horizontalThickLinesColor color for thick horizontal lines
 */
class InfiniteGrid(
    var gestureDetector: MatrixGestureDetector,
    var distanceBetweenVerticalLines: Float = DISTANCE_BETWEEN_LINES,
    var distanceBetweenHorizontalLines: Float = DISTANCE_BETWEEN_LINES,
    var verticalLinesStrokeWidthThick: Float = STROKE_WIDTH_THICK,
    var verticalLinesStrokeWidthNormal: Float = STROKE_WIDTH_NORMAL,
    var horizontalLinesStrokeWidthThick: Float = STROKE_WIDTH_THICK,
    var horizontalLinesStrokeWidthNormal: Float = STROKE_WIDTH_NORMAL,
    var verticalLinesThickLineIndex: Int = THICK_LINE_INDEX,
    var horizontalLinesThickLineIndex: Int = THICK_LINE_INDEX,
    var verticalNormalLinesColor: Int = LINE_COLOR,
    var verticalThickLinesColor: Int = LINE_COLOR,
    var horizontalNormalLinesColor: Int = LINE_COLOR,
    var horizontalThickLinesColor: Int = LINE_COLOR
) {

    val cornerOnNegativeSide: ArrayList<Float>          // canvas corners on the negative side of the base line (used for both vertical and horizontal base lines)
    val cornerOnPositiveSide: ArrayList<Float>          // canvas corners on the positive side of the base line (used for both vertical and horizontal base lines)
    var verticalParallelLines: ArrayList<LineInfo>      // array list with the vertical parallel lines
    var horizontalParallelLines: ArrayList<LineInfo>    // array list with the horizontal parallel lines
    var transformedBasePoints: FloatArray               // array with the transformed base points, by the transform matrix
    var deviceWidth: Float                              // width of the device in graphic coordinate system
    var deviceHeight: Float                             // height of the device in graphic coordinate system
    var originalBasePoints: FloatArray                  // the coordinates of the original base points in graphic coordinate system
    var canvasBoundLinesCoordinates: FloatArray         // line coordinate of the canvas bound
    val gestureDetectorMatix: Matrix                    // copy of the matrix from the mainGestureDetector without including the translation to device center


    var horizontalNormalLines: Lines
    var horizontalThickLines: Lines
    var verticalNormalLines: Lines
    var verticalThickLines: Lines

    companion object {
        const val DISTANCE_BETWEEN_LINES: Float = 1400f
        const val STROKE_WIDTH_THICK: Float = 1f
        const val STROKE_WIDTH_NORMAL: Float = 2f
        const val THICK_LINE_INDEX: Int = 5
        val LINE_COLOR: Int = Color.parseColor("#77999999")
    }

    init {

        horizontalNormalLines = Lines(horizontalLinesStrokeWidthNormal, GLES30.GL_DYNAMIC_DRAW)
        horizontalThickLines = Lines(horizontalLinesStrokeWidthThick, GLES30.GL_DYNAMIC_DRAW)
        verticalNormalLines = Lines(verticalLinesStrokeWidthNormal, GLES30.GL_DYNAMIC_DRAW)
        verticalThickLines = Lines(verticalLinesStrokeWidthThick, GLES30.GL_DYNAMIC_DRAW)

        gestureDetectorMatix = Matrix()
        deviceWidth = 0f
        deviceHeight = 0f
        originalBasePoints = floatArrayOf()
        canvasBoundLinesCoordinates = floatArrayOf()

        cornerOnNegativeSide = ArrayList()
        cornerOnPositiveSide = ArrayList()
        verticalParallelLines = ArrayList()
        horizontalParallelLines = ArrayList()
        transformedBasePoints = FloatArray(10)

    }

    /**
     * Method for settings the view size and initializing the original base points and the lines
     * coordinate for the bound area of the canvas(view)
     * @param width width of the view in graphic coordinates system
     * @param height height of the view in graphic coordinate system
     */
    fun updateSize(width: Float, height: Float) {

        deviceWidth = width
        deviceHeight = height
        val halfDeviceWidth = width / 2f
        val halfDeviceHeight = height / 2f

        // lines coordinates for the canvas bound
        canvasBoundLinesCoordinates = floatArrayOf(
            0f, 0f, deviceWidth, 0f,                        // top side    -QR
            0f, deviceHeight, deviceWidth, deviceHeight,    // bottom side -MN
            0f, 0f, 0f, deviceHeight,                       // left side   -QM
            deviceWidth, 0f, deviceWidth, deviceHeight      // right side  -RN
        )

        // the original base points
        originalBasePoints = floatArrayOf(
            halfDeviceWidth, 0f,                // C
            0f, halfDeviceHeight,               // A
            halfDeviceWidth, halfDeviceHeight,  // O
            halfDeviceWidth, deviceHeight,      // D
            deviceWidth, halfDeviceHeight       // B
        )

    }

    /**
     * Get the vertical parallel line segments, that are clipped by the canvas
     * and are visible by the user.
     * @param transformedDistance the transformed distance between parallel lines
     * @param thickLineSkipLines how many normal lines to skip, before a thick line
     */
    fun updateVerticalParallelLines(
        transformedDistance: Float,
        thickLineSkipLines: Int,
        Tx: Float, Ty: Float, Sx: Float, Sy: Float,
        Ax: Float, Ay: Float, Bx: Float, By: Float,
        _Ix: Float, _Iy: Float, _Jx: Float, _Jy: Float,
        Qx: Float, Qy: Float, Rx: Float, Ry: Float,
        Mx: Float, My: Float, Nx: Float, Ny: Float,
        Cx: Float, Cy: Float, Dx: Float, Dy: Float
    ) {

        var Ix = _Ix
        var Iy = _Iy
        var Jx = _Jx
        var Jy = _Jy
        if (Tx == Sx) {
            // CD | QM => set (A as I) and (B as J)
            Ix = Ax
            Iy = Ay

            Jx = Bx
            Jy = By
        }

        // get distance AI and BI
        val AI = distanceBetweenTwoPoints(Ax, Ay, Ix, Iy)
        val BI = distanceBetweenTwoPoints(Bx, By, Ix, Iy)

        // get distance AJ and BJ
        val AJ = distanceBetweenTwoPoints(Ax, Ay, Jx, Jy)
        val BJ = distanceBetweenTwoPoints(Bx, By, Jx, Jy)

        // I and J are on the negative side, if A is closer to I and J than B
        val INegative = AI < BI
        val JNegative = AJ < BJ

        // clear the arrays with containing the corners, that are on the negative and positive sides
        cornerOnNegativeSide.clear()
        cornerOnPositiveSide.clear()

        // top corners, check Q and R if they are on the positive or negative side
        fillVerticalCornerSides(Ix, Tx, INegative, Qx, Qy)
        fillVerticalCornerSides(Ix, Tx, INegative, Rx, Ry)

        //  bottom corners, check M and N if they are on the positive or negative side
        fillVerticalCornerSides(Jx, Sx, JNegative, Mx, My)
        fillVerticalCornerSides(Jx, Sx, JNegative, Nx, Ny)

        // get the maximum distance between the corners on the positive side and the base line
        var maxPositiveSideDistance = -Float.MAX_VALUE
        for (i in cornerOnPositiveSide.indices step 2) {
            val x = cornerOnPositiveSide[i]
            val y = cornerOnPositiveSide[i + 1]

            val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
            if (dist > maxPositiveSideDistance) {
                maxPositiveSideDistance = dist
            }
        }

        // get the maximum distance between the corners from the negative side and the base line
        var maxNegativeSideDistance = -Float.MAX_VALUE
        for (i in cornerOnNegativeSide.indices step 2) {
            val x = cornerOnNegativeSide[i]
            val y = cornerOnNegativeSide[i + 1]

            val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
            if (dist > maxNegativeSideDistance) {
                maxNegativeSideDistance = dist
            }
        }

        // get the total number of lines, on the positive and negative sides
        val totalPositiveSideLines = (maxPositiveSideDistance / transformedDistance).toInt()
        val totalNegativeSideLines = (maxNegativeSideDistance / transformedDistance).toInt()

        // check it T and S are outside the canvas bound
        val tIsOutside = !(Tx >= 0f || Tx < deviceWidth)
        val sIsOutside = !(Sx >= 0f || Sx < deviceWidth)

        // get the minimum distance, between the four corners and the base line, that is outside the visible canvas
        val offsetDistance = if (tIsOutside && sIsOutside) {
            var minSideDistance = Float.MAX_VALUE
            val arr =
                if (cornerOnNegativeSide.size > 0) cornerOnNegativeSide else cornerOnPositiveSide
            for (i in arr.indices step 2) {
                val x = arr[i]
                val y = arr[i + 1]

                val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
                if (dist < minSideDistance) {
                    minSideDistance = dist
                }
            }
            minSideDistance
        } else {
            0f
        }

        val offsetNumberOfLines = (offsetDistance / transformedDistance).toInt()
        verticalParallelLines.clear()

        // get the base line
        val baseLine = checkLineIntersectionWithCanvas(Cx, Cy, Dx, Dy)
        if (baseLine != null) {
            baseLine.isThick = true
            verticalParallelLines.add(baseLine)
        }

        // get the positive parallel lines
        var totalDistance = transformedDistance + (offsetNumberOfLines * transformedDistance)
        for (i in offsetNumberOfLines until totalPositiveSideLines) {
            val parallel = getParallelLine(Cx, Cy, Dx, Dy, totalDistance, false)
            val line =
                checkLineIntersectionWithCanvas(parallel.x1, parallel.y1, parallel.x2, parallel.y2)

            if (line != null) {
                line.isThick =
                    i != 0 && (i - offsetNumberOfLines + 1) % thickLineSkipLines == 0
                verticalParallelLines.add(line)
            }
            totalDistance += transformedDistance
        }

        // get the negative parallel lines
        totalDistance = transformedDistance + (offsetNumberOfLines * transformedDistance)
        for (i in offsetNumberOfLines until totalNegativeSideLines) {
            val parallel = getParallelLine(Cx, Cy, Dx, Dy, totalDistance, true)
            val line =
                checkLineIntersectionWithCanvas(parallel.x1, parallel.y1, parallel.x2, parallel.y2)
            if (line != null) {
                line.isThick =
                    i != 0 && (i - offsetNumberOfLines + 1) % thickLineSkipLines == 0
                verticalParallelLines.add(line)
            }
            totalDistance += transformedDistance
        }
    }

    /**
     * Get the horizontal parallel line segments, that are clipped by the canvas
     * and are visible by the user.
     * @param transformedDistance the transformed distance between parallel lines
     * @param thickLineSkipLines how many normal lines to skip, before a thick line
     */
    fun updateHorizontalParallelLines(
        transformedDistance: Float,
        thickLineSkipLines: Int,
        Tx: Float, Ty: Float, Sx: Float, Sy: Float,
        Ax: Float, Ay: Float, Bx: Float, By: Float,
        _Ix: Float, _Iy: Float, _Jx: Float, _Jy: Float,
        Qx: Float, Qy: Float, Rx: Float, Ry: Float,
        Mx: Float, My: Float, Nx: Float, Ny: Float,
        Cx: Float, Cy: Float, Dx: Float, Dy: Float
    ) {

        var Ix = _Ix
        var Iy = _Iy
        var Jx = _Jx
        var Jy = _Jy
        if (Ty == Sy) {
            // CD | QM => set (A as I) and (B as J)
            Ix = Ax
            Iy = Ay

            Jx = Bx
            Jy = By
        }

        // get distance AI and BI
        val AI = distanceBetweenTwoPoints(Ax, Ay, Ix, Iy)
        val BI = distanceBetweenTwoPoints(Bx, By, Ix, Iy)

        // get distance AJ and BJ
        val AJ = distanceBetweenTwoPoints(Ax, Ay, Jx, Jy)
        val BJ = distanceBetweenTwoPoints(Bx, By, Jx, Jy)

        // I and J are on the negative side, if A is closer to I and J than B
        val INegative = AI < BI
        val JNegative = AJ < BJ

        // clear the arrays with containing the corners, that are on the negative and positive sides
        cornerOnNegativeSide.clear()
        cornerOnPositiveSide.clear()

        // top corners, check Q and R for positive or negative side
        fillHorizontalCornerSides(Iy, Ty, INegative, Qx, Qy)
        fillHorizontalCornerSides(Iy, Ty, INegative, Rx, Ry)

        //  bottom corners, check M and N for positive or negative side
        fillHorizontalCornerSides(Jy, Sy, JNegative, Mx, My)
        fillHorizontalCornerSides(Jy, Sy, JNegative, Nx, Ny)

        // get the maximum distance between the corners from the positive side and the base line
        var maxPositiveSideDistance = -Float.MAX_VALUE
        for (i in cornerOnPositiveSide.indices step 2) {
            val x = cornerOnPositiveSide[i]
            val y = cornerOnPositiveSide[i + 1]

            val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
            if (dist > maxPositiveSideDistance) {
                maxPositiveSideDistance = dist
            }
        }

        // get the maximum distance between the corners from the negative side and the base line
        var maxNegativeSideDistance = -Float.MAX_VALUE
        for (i in cornerOnNegativeSide.indices step 2) {
            val x = cornerOnNegativeSide[i]
            val y = cornerOnNegativeSide[i + 1]

            val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
            if (dist > maxNegativeSideDistance) {
                maxNegativeSideDistance = dist
            }
        }

        // get the total number of lines, on the positive and negative sides
        val totalPositiveSideLines = (maxPositiveSideDistance / transformedDistance).toInt()
        val totalNegativeSideLines = (maxNegativeSideDistance / transformedDistance).toInt()

        // check it T and S are outside the canvas bound
        val tIsOutside = !(Ty >= 0f || Ty < deviceHeight)
        val sIsOutside = !(Sy >= 0f || Sy < deviceHeight)

        // get the minimum distance, between the four corners and the base line, that is outside the visible canvas
        val offsetDistance = if (tIsOutside && sIsOutside) {
            var minSideDistance = Float.MAX_VALUE
            val arr =
                if (cornerOnNegativeSide.size > 0) cornerOnNegativeSide else cornerOnPositiveSide
            for (i in arr.indices step 2) {
                val x = arr[i]
                val y = arr[i + 1]

                val dist = distanceBetweenPointAndLine(x, y, Cx, Cy, Dx, Dy)
                if (dist < minSideDistance) {
                    minSideDistance = dist
                }
            }
            minSideDistance
        } else {
            0f
        }

        val offsetNumberOfLines = (offsetDistance / transformedDistance).toInt()
        horizontalParallelLines.clear()

        // get the base line
        val baseLine = checkLineIntersectionWithCanvas(Cx, Cy, Dx, Dy)
        if (baseLine != null) {
            baseLine.isThick = true
            horizontalParallelLines.add(baseLine)
        }

        // get the positive parallel lines
        var totalDistance = transformedDistance + (offsetNumberOfLines * transformedDistance)
        for (i in offsetNumberOfLines until totalPositiveSideLines) {
            val parallel = getParallelLine(Cx, Cy, Dx, Dy, totalDistance, true)
            val line =
                checkLineIntersectionWithCanvas(parallel.x1, parallel.y1, parallel.x2, parallel.y2)

            if (line != null) {
                line.isThick =
                    i != 0 && (i - offsetNumberOfLines + 1) % thickLineSkipLines == 0
                horizontalParallelLines.add(line)
            }
            totalDistance += transformedDistance
        }

        // get the negative parallel lines
        totalDistance = transformedDistance + (offsetNumberOfLines * transformedDistance)
        for (i in offsetNumberOfLines until totalNegativeSideLines) {
            val parallel = getParallelLine(Cx, Cy, Dx, Dy, totalDistance, false)
            val line =
                checkLineIntersectionWithCanvas(parallel.x1, parallel.y1, parallel.x2, parallel.y2)
            if (line != null) {
                line.isThick =
                    i != 0 && (i - offsetNumberOfLines + 1) % thickLineSkipLines == 0
                horizontalParallelLines.add(line)
            }
            totalDistance += transformedDistance
        }
    }

    /**
     * Fill the arrays showing whether a corner points is on the negative or positive side
     * of the base lines. Since is for vertical lines, it checks the X coordinates.
     * @param x1 x coordinate for the first control point
     * @param x2 x coordinate for the second control point
     * @param isOnNegativeSideBase whether the control point is on the negative side
     * @param x x coordinate for corner test point
     * @param y y coordinate for corner test point
     */
    fun fillVerticalCornerSides(
        x1: Float,
        x2: Float,
        isOnNegativeSideBase: Boolean,
        x: Float,
        y: Float
    ) {

        // get the signs, by checking the X coordinates (+ or -)
        val smallerThanZeroBase = x1 - x2 < 0
        val smallerThanZero = x - x2 < 0

        // set whether the point is on the positive side
        val isOnPositiveSide = if (smallerThanZeroBase == smallerThanZero) {
            // if both sign match
            !isOnNegativeSideBase
        } else {
            // if signs are different
            isOnNegativeSideBase
        }

        if (isOnPositiveSide) {
            // add to positive array
            cornerOnPositiveSide.add(x)
            cornerOnPositiveSide.add(y)
        } else {
            // add to negative array
            cornerOnNegativeSide.add(x)
            cornerOnNegativeSide.add(y)
        }
    }

    /**
     * Fill the arrays showing whether a corner points is on the negative or positive side
     * of the base lines. Since is for horizontal lines, it checks the Y coordinates.
     * @param y1 y coordinate for the first control point
     * @param y2 y coordinate for the second control point
     * @param isOnNegativeSideBase whether the control point is on the negative side
     * @param x x coordinate for corner test point
     * @param y y coordinate for corner test point
     */
    fun fillHorizontalCornerSides(
        y1: Float,
        y2: Float,
        isOnNegativeSideBase: Boolean,
        x: Float,
        y: Float
    ) {

        // get the signs, by checking the Y coordinates (+ or -)
        val smallerThanZeroBase = y1 - y2 < 0
        val smallerThanZero = y - y2 < 0

        // set whether the point is on the positive side
        val isOnPositiveSide = if (smallerThanZeroBase == smallerThanZero) {
            // if both sign match
            !isOnNegativeSideBase
        } else {
            // if signs are different
            isOnNegativeSideBase
        }

        if (isOnPositiveSide) {
            // add coordinates to positive array
            cornerOnPositiveSide.add(x)
            cornerOnPositiveSide.add(y)
        } else {
            // add coordinates to negative array
            cornerOnNegativeSide.add(x)
            cornerOnNegativeSide.add(y)
        }
    }

    /**
     * Return the line segment from the intersection between a given line and the canvas
     * bound rectangle. If the intersection happens outside the bound rectangle null is
     * returned.
     * @param x1 line first point x coordinate
     * @param y1 line first point y coordinate
     * @param x2 line second point x coordinate
     * @param y2 line second point y coordinate
     */
    fun checkLineIntersectionWithCanvas(x1: Float, y1: Float, x2: Float, y2: Float): LineInfo? {

        val line = LineInfo()
        var foundPoints = 0

        val arr = canvasBoundLinesCoordinates
        for (i in arr.indices step 4) {

            val x3 = arr[i]
            val y3 = arr[i + 1]
            val x4 = arr[i + 2]
            val y4 = arr[i + 3]
            val pointIntersection = checkLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4)

            if (pointIntersection.onLine2) {
                if (foundPoints == 0) {
                    line.x1 = pointIntersection.x
                    line.y1 = pointIntersection.y
                } else if (foundPoints == 1) {
                    line.x2 = pointIntersection.x
                    line.y2 = pointIntersection.y
                    return line
                }
                foundPoints++
            }
        }
        return null
    }

    /**
     * Check intersection of two lines, and return the point of intersection and whether the point is on
     * any of the two line segments. For first line points are [x1,y1,x2,y2], and for the second line [x3,y3,x4,y4]
     * @param x1 line1 first point x coordinate
     * @param y1 line1 first point y coordinate
     * @param x2 line1 second point x coordinate
     * @param y2 line1 second point y coordinate
     * @param x3 line2 first point x coordinate
     * @param y3 line2 first point y coordinate
     * @param x4 line2 second point x coordinate
     * @param y4 line2 second point y coordinate
     */
    private fun checkLineIntersection(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float): LineIntersection {

        // if the lines intersect, the result contains the x and y of the intersection (treating the lines as infinite) and booleans for whether line segment 1 or line segment 2 contain the point
        var denominator = 0f
        var a = 0f
        var b = 0f
        var numerator1 = 0f
        var numerator2 = 0f
        var x = 0f
        var y = 0f
        var onLine1 = false
        var onLine2 = false

        denominator = ((y4 - y3) * (x2 - x1)) - ((x4 - x3) * (y2 - y1))
        if (denominator == 0f) {
            return LineIntersection(x, y, onLine1, onLine2)
        }

        a = y1 - y3
        b = x1 - x3
        numerator1 = ((x4 - x3) * a) - ((y4 - y3) * b)
        numerator2 = ((x2 - x1) * a) - ((y2 - y1) * b)
        a = numerator1 / denominator
        b = numerator2 / denominator

        // if we cast these lines infinitely in both directions, they intersect here:
        x = x1 + (a * (x2 - x1));
        y = y1 + (a * (y2 - y1));

        // if line1 is a segment and line2 is infinite, they intersect
        if (a > 0 && a < 1) {
            onLine1 = true
        }
        // if line2 is a segment and line1 is infinite, they intersect
        if (b > 0 && b < 1) {
            onLine2 = true
        }

        // if line1 and line2 are segments, they intersect if both of the above are true onLine1 and onLine2
        return LineIntersection(x, y, onLine1, onLine2)
    }

    /**
     * Get the parallel line, to a existing line on given distance. Direction is determined, by the
     * boolean argument.
     * @param x1 first point x coordinate
     * @param y1 first point y coordinate
     * @param x2 second point x coordinate
     * @param x2 second point y coordinate
     * @param dist distance between the existing line and the searched line
     * @param onNegativeSide if line should be on the negative or positive side
     */
    fun getParallelLine(x1: Float, y1: Float, x2: Float, y2: Float, dist: Float, onNegativeSide: Boolean): LineInfo {
        val dx = x2 - x1
        val dy = y2 - y1

        val len = Math.sqrt(0.0 + dx * dx + dy * dy).toFloat()
        val udx = dx / len
        val udy = dy / len

        val px: Float
        val py: Float
        if (onNegativeSide) {
            px = -udy
            py = udx
        } else {
            px = udy
            py = -udx
        }

        val nx = x1 + px * dist
        val ny = y1 + py * dist

        val sx = nx + dx
        val sy = ny + dy

        return LineInfo(nx, ny, sx, sy)
    }

    /**
     * Distance between two points.
     * @param x1 first point x coordinate
     * @param y1 first point y coordinate
     * @param x2 second point x coordinate
     * @param y2 second point y coordinate
     */
    fun distanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = (x1 - x2).toDouble()
        val dy = (y1 - y2)
        return Math.sqrt(dx * dx + dy * dy).toFloat()
    }

    /**
     * Distance between point and a given line, this is the distance of the perpendicular
     * line from the point to a given line.
     * @param x point x coordinate
     * @param y point y coordinate
     * @param x1 line first point x coordinate
     * @param y1 line first point y coordinate
     * @param x2 line second point x coordinate
     * @param y2 line second point y coordinate
     */
    fun distanceBetweenPointAndLine(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {

        // position of point rel one end of line
        val A = x - x1
        val B = y - y1

        // vector along line
        val C = x2 - x1
        val D = y2 - y1

        // orthogonal vector
        val E = -D
        val dot = A * E + B * C
        val len_sq = E * E + C * C
        return (Math.abs(dot) / Math.sqrt(len_sq.toDouble())).toFloat()
    }

    //region Matrix extension functions

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
        return (Math.atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).toFloat()
    }

    /**
     * Get transformations for a given matrix
     */
    fun Matrix.transform(): PointF {
        val points = FloatArray(9)
        getValues(points)
        return PointF(points[Matrix.MTRANS_X], points[Matrix.MTRANS_Y])
    }
    //endregion

    /**
     * Update the arrays with vertical and horizontal parallel lines
     *
     * @param distanceBetweenHorizontalLines distance between each two horizontal lines
     * @param distanceBetweenVerticalLines distance between each two vertical lines
     * @param verticalLinesThickLineIndex how many normal vertical lines to skip before setting thick line
     * @param horizontalLinesThickLineIndex how many normal horizontal lines to skip before setting thick line
     */
    fun updateParallelLines(
        distanceBetweenHorizontalLines: Float,
        distanceBetweenVerticalLines: Float,
        verticalLinesThickLineIndex: Int,
        horizontalLinesThickLineIndex: Int
    ) {

        // transformed the base points, but without including the translation to device center
        // that is used by the matrix form the gestureDetector, that is why we need to remove it
        gestureDetectorMatix.apply {
            set(gestureDetector.matrix)
            preTranslate(-StaticMethods.DEVICE_HALF_WIDTH, -StaticMethods.DEVICE_HEIGHT)
            mapPoints(transformedBasePoints, originalBasePoints)
        }

        val scale = gestureDetector.scale
        val newDistanceBetweenHorizontalLines = distanceBetweenHorizontalLines * scale
        val newDistanceBetweenVerticalLines = distanceBetweenVerticalLines * scale

        // Q, R, M and N
        val Qx = canvasBoundLinesCoordinates[0]
        val Qy = canvasBoundLinesCoordinates[1]
        val Rx = canvasBoundLinesCoordinates[2]
        val Ry = canvasBoundLinesCoordinates[3]
        val Mx = canvasBoundLinesCoordinates[4]
        val My = canvasBoundLinesCoordinates[5]
        val Nx = canvasBoundLinesCoordinates[6]
        val Ny = canvasBoundLinesCoordinates[7]

        // A, B, C, D and O
        val Cx = transformedBasePoints[0]
        val Cy = transformedBasePoints[1]
        val Ax = transformedBasePoints[2]
        val Ay = transformedBasePoints[3]
        val Ox = transformedBasePoints[4]
        val Oy = transformedBasePoints[5]
        val Dx = transformedBasePoints[6]
        val Dy = transformedBasePoints[7]
        val Bx = transformedBasePoints[8]
        val By = transformedBasePoints[9]

        // T, S, I and J for vertical parallel lines
        val T1 = checkLineIntersection(
            Cx, Cy, Dx, Dy,
            Qx, Qy, Rx, Ry
        )
        val S1 = checkLineIntersection(
            Cx, Cy, Dx, Dy,
            Mx, My, Nx, Ny
        )
        val I1 = checkLineIntersection(
            Ax, Ay, Bx, By,
            Qx, Qy, Rx, Ry
        )
        val J1 = checkLineIntersection(
            Ax, Ay, Bx, By,
            Mx, My, Nx, Ny
        )

        // T, S, I and J for horizontal parallel lines
        val T2 = checkLineIntersection(
            Ax, Ay, Bx, By,
            Qx, Qy, Mx, My
        )
        val S2 = checkLineIntersection(
            Ax, Ay, Bx, By,
            Rx, Ry, Nx, Ny
        )
        val I2 = checkLineIntersection(
            Cx, Cy, Dx, Dy,
            Qx, Qy, Mx, My
        )
        val J2 = checkLineIntersection(
            Cx, Cy, Dx, Dy,
            Rx, Ry, Nx, Ny
        )

        // generate parallel lines
        updateVerticalParallelLines(
            newDistanceBetweenVerticalLines,
            verticalLinesThickLineIndex,
            T1.x, T1.y, S1.x, S1.y,
            Ax, Ay, Bx, By,
            I1.x, I1.y, J1.x, J1.y,
            Qx, Qy, Rx, Ry,
            Mx, My, Nx, Ny,
            Cx, Cy, Dx, Dy
        )

        updateHorizontalParallelLines(
            newDistanceBetweenHorizontalLines,
            horizontalLinesThickLineIndex,
            T2.x, T2.y, S2.x, S2.y,
            Cx, Cy, Dx, Dy,
            I2.x, I2.y, J2.x, J2.y,
            Qx, Qy, Mx, My,
            Rx, Ry, Nx, Ny,
            Ax, Ay, Bx, By
        )
    }


    val horizontalNormalLinesArray: ArrayList<LineInfo> = arrayListOf()
    val horizontalThickLinesArray: ArrayList<LineInfo> = arrayListOf()
    val verticalNormalLinesArray: ArrayList<LineInfo> = arrayListOf()
    val verticalThickLinesArray: ArrayList<LineInfo> = arrayListOf()

    fun update() {

        // update the parallel lines arrays
        updateParallelLines(
            distanceBetweenHorizontalLines,
            distanceBetweenVerticalLines,
            verticalLinesThickLineIndex,
            horizontalLinesThickLineIndex
        )

        // clear the previous values
        horizontalNormalLinesArray.clear()
        horizontalThickLinesArray.clear()
        verticalNormalLinesArray.clear()
        verticalThickLinesArray.clear()

        // draw the vertical parallel lines
        for (i in verticalParallelLines.indices) {
            val line = verticalParallelLines[i]
            if (line.isThick) {
                verticalThickLinesArray.add(line)
            } else {
                verticalNormalLinesArray.add(line)
            }
        }

        // draw the horizontal parallel lines
        for (i in horizontalParallelLines.indices) {
            val line = horizontalParallelLines[i]
            if (line.isThick) {
                horizontalThickLinesArray.add(line)
            } else {
                horizontalNormalLinesArray.add(line)
            }
        }


        fun b(lines: Lines, color: Int, linesInfo: ArrayList<LineInfo>) {

            val linesCoordinates = FloatArray(linesInfo.size * 4)
            for (i in linesInfo.indices) {

                val line = linesInfo[i]
                val j = i * 4

                linesCoordinates[j] = line.x1
                linesCoordinates[j + 1] = line.y1
                linesCoordinates[j + 2] = line.x2
                linesCoordinates[j + 3] = line.y2
            }
            gestureDetector.normalizeCoordinates(linesCoordinates)

            val r = Color.red(color) / 255f
            val g = Color.green(color) / 255f
            val b = Color.blue(color) / 255f
            val a = Color.alpha(color) / 255f

            val numberOfFloats = 7
            val numberPoints = linesCoordinates.size / 2
            val vert: ArrayList<VertexColor> = ArrayList(numberPoints)
            val idx: ArrayList<Int> = ArrayList(numberPoints)
            for (i in 0 until linesCoordinates.size / 2) {

                val x = linesCoordinates[i * 2]
                val y = linesCoordinates[i * 2 + 1]

                vert.add(
                    VertexColor(x, y, 0f, r, g, b, a)
                )
                idx.add(i)
            }

            lines.createBuffer(vert, idx, GLES30.GL_LINES)
        }
        b(horizontalNormalLines, horizontalNormalLinesColor, horizontalNormalLinesArray)
        b(horizontalThickLines, horizontalThickLinesColor, horizontalThickLinesArray)
        b(verticalNormalLines, verticalNormalLinesColor, verticalNormalLinesArray)
        b(verticalThickLines, verticalThickLinesColor, verticalThickLinesArray)

    }

    fun draw(matrix: FloatArray) {

        horizontalNormalLines.draw(matrix, matrix)
        horizontalThickLines.draw(matrix, matrix)
        verticalNormalLines.draw(matrix, matrix)
        verticalThickLines.draw(matrix, matrix)
    }

    fun initialize() {
        verticalThickLines.initialize()
        verticalNormalLines.initialize()
        horizontalThickLines.initialize()
        horizontalNormalLines.initialize()
    }

    /**
     * Class for representing line holding float coordinates
     * @param x1 first points x coordinate
     * @param y1 first points y coordinate
     * @param x2 second points x coordinate
     * @param y2 second points y coordinate
     */
    class LineInfo(var x1: Float = 0f, var y1: Float = 0f, var x2: Float = 0f, var y2: Float = 0f, var isThick: Boolean = false) {
        override fun toString(): String {
            return "$x1 $y1 $x2 $y2"
        }
    }

    /**
     * Class for lines intersection
     * @param x intersection points x coordinate
     * @param y intersection points y coordinate
     * @param onLine1 if intersection point is on first line segment
     * @param onLine2 if intersection point is on second line segment
     */
    class LineIntersection(var x: Float = 0f, var y: Float = 0f, var onLine1: Boolean = false, var onLine2: Boolean = false)
}

