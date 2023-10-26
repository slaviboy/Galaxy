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

class CumulativeDistributionFunction {

    var min: Float = 0.0f
    var max: Float = 0.0f
    var width: Float = 0.0f
    var steps: Int = 0

    var i0: Float = 0.0f
    var k: Float = 0.0f
    var a: Float = 0.0f
    var r_bulge: Float = 0.0f

    var m1: ArrayList<Float> = arrayListOf()
    var y1: ArrayList<Float> = arrayListOf()
    var x1: ArrayList<Float> = arrayListOf()

    var m2: ArrayList<Float> = arrayListOf()
    var y2: ArrayList<Float> = arrayListOf()
    var x2: ArrayList<Float> = arrayListOf()

    fun probFromVal(fVal: Float): Float {
        if (fVal < this.min || fVal > this.max)
            throw Exception("out of range")

        val h: Float = 2 * ((this.max - this.min) / this.steps)
        val i: Int = ((fVal - this.min) / h).toInt()
        val remainder: Float = fVal - i * h

        return (this.y1[i] + this.m1[i] * remainder)
    }

    fun valFromProb(fVal: Float): Float {

        if (fVal < 0f || fVal > 1f) {
            throw Exception("out of range")
        }

        val h = 1.0 / (y2.size - 1)
        val i: Int = Math.floor(fVal / h).toInt()
        val remainder = fVal - i * h

        return (this.y2[i] + this.m2[i] * remainder).toFloat()
    }

    fun setupRealistic(i0: Float, k: Float, a: Float, rad_bulge: Float, min: Float, max: Float, nsteps: Int) {
        this.min = min
        this.max = max
        this.steps = nsteps

        this.i0 = i0
        this.k = k
        this.a = a
        this.r_bulge = rad_bulge

        this.buildCdf(nsteps)
    }

    fun buildCdf(nsteps: Int) {
        var h: Float = (max - min) / nsteps
        var x: Float = 0.0f
        var y: Float = 0.0f

        x1.clear()
        y1.clear()
        x2.clear()
        y2.clear()
        m1.clear()
        m2.clear()

        // Simpson rule for integration of the distribution function
        y1.add(0.0f)
        x1.add(0.0f)
        for (i in 0 until nsteps step 2) {
            x = h * (i + 2)
            y += h / 3 * (this.intensity(this.min + i * h) + 4 * this.intensity(this.min + (i + 1) * h) + this.intensity(this.min + (i + 2) * h))

            m1.add((y - y1[y1.size - 1]) / (2 * h))
            x1.add(x)
            y1.add(y)

        }
        m1.add(0.0f)

        // all arrays must have the same length
        if (m1.size != x1.size || m1.size != y1.size)
            throw Exception("CumulativeDistributionFunction::BuildCDF: array size mismatch (1)!")

        for (i in 0 until y1.size) {
            y1[i] /= y1[y1.size - 1]
            m1[i] /= y1[y1.size - 1]
        }

        x2.add(0.0f)
        y2.add(0.0f)

        var p: Float = 0.0f
        var k: Int = 0
        h = 1.0f / nsteps
        for (i in 1 until nsteps) {

            p = i * h
            y = x1[k] + (p - y1[k]) / m1[k]

            m2.add((y - y2[y2.size - 1]) / h)
            x2.add(p)
            y2.add(y)
        }
        m2.add(0.0f)

        // all arrays must have the same length
        if (m2.size != x2.size || m2.size != y2.size)
            throw Exception("CumulativeDistributionFunction::BuildCDF: array size mismatch (1)!")
    }

    fun intensityBulge(r: Float, i0: Float, k: Float): Float {
        return (i0 * Math.exp(-k * Math.pow(r.toDouble(), 0.25))).toFloat()
    }

    fun intensityDisc(r: Float, i0: Float, a: Float): Float {
        return (i0 * Math.exp(-r / a.toDouble())).toFloat()
    }

    fun intensity(x: Float): Float {
        return if (x < r_bulge) this.intensityBulge(x, i0, k)
        else this.intensityDisc(x - r_bulge, this.intensityBulge(r_bulge, i0, k), a)
    }
}