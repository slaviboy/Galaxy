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
package com.slaviboy.opengl.helpers

import com.slaviboy.opengl.params.Color

object Helper {

    // convert parsec to kilometre
    const val PC_TO_KM: Float = 3.08567758129e13f

    // seconds per year
    const val SEC_PER_YEAR: Float = (365.25 * 86400).toFloat()

    // deg to radian conversion factor
    const val DEG_TO_RAD: Float = (Math.PI / 180.0).toFloat()

    // radian to deg conversion factor
    const val RAD_TO_DEG: Float = (180.0 / Math.PI).toFloat()

    // radian to deg conversion factor
    const val CONTANT_OF_GRAVITY: Float = 6.672e-11f

    // constant of gravity
    const val PI: Float = 3.14159265358979323846.toFloat()

    val colors: Array<Color> = arrayOf(
        Color(1f, -0.00987248f, -0.0166818f, 1f),
        Color(1f, 0.000671682f, -0.0173831f, 1f),
        Color(1f, 0.0113477f, -0.0179839f, 1f),
        Color(1f, 0.0221357f, -0.0184684f, 1f),
        Color(1f, 0.0330177f, -0.0188214f, 1f),
        Color(1f, 0.0439771f, -0.0190283f, 1f),
        Color(1f, 0.0549989f, -0.0190754f, 1f),
        Color(1f, 0.0660696f, -0.0189496f, 1f),
        Color(1f, 0.0771766f, -0.0186391f, 1f),
        Color(1f, 0.0883086f, -0.0181329f, 1f),
        Color(1f, 0.0994553f, -0.017421f, 1f),
        Color(1f, 0.110607f, -0.0164945f, 1f),
        Color(1f, 0.121756f, -0.0153455f, 1f),
        Color(1f, 0.132894f, -0.0139671f, 1f),
        Color(1f, 0.144013f, -0.0123534f, 1f),
        Color(1f, 0.155107f, -0.0104993f, 1f),
        Color(1f, 0.166171f, -0.0084008f, 1f),
        Color(1f, 0.177198f, -0.00605465f, 1f),
        Color(1f, 0.188184f, -0.00345843f, 1f),
        Color(1f, 0.199125f, -0.000610485f, 1f),
        Color(1f, 0.210015f, 0.00249014f, 1f),
        Color(1f, 0.220853f, 0.00584373f, 1f),
        Color(1f, 0.231633f, 0.00944995f, 1f),
        Color(1f, 0.242353f, 0.0133079f, 1f),
        Color(1f, 0.25301f, 0.0174162f, 1f),
        Color(1f, 0.263601f, 0.021773f, 1f),
        Color(1f, 0.274125f, 0.0263759f, 1f),
        Color(1f, 0.284579f, 0.0312223f, 1f),
        Color(1f, 0.294962f, 0.0363091f, 1f),
        Color(1f, 0.305271f, 0.0416328f, 1f),
        Color(1f, 0.315505f, 0.0471899f, 1f),
        Color(1f, 0.325662f, 0.0529765f, 1f),
        Color(1f, 0.335742f, 0.0589884f, 1f),
        Color(1f, 0.345744f, 0.0652213f, 1f),
        Color(1f, 0.355666f, 0.0716707f, 1f),
        Color(1f, 0.365508f, 0.078332f, 1f),
        Color(1f, 0.375268f, 0.0852003f, 1f),
        Color(1f, 0.384948f, 0.0922709f, 1f),
        Color(1f, 0.394544f, 0.0995389f, 1f),
        Color(1f, 0.404059f, 0.106999f, 1f),
        Color(1f, 0.41349f, 0.114646f, 1f),
        Color(1f, 0.422838f, 0.122476f, 1f),
        Color(1f, 0.432103f, 0.130482f, 1f),
        Color(1f, 0.441284f, 0.138661f, 1f),
        Color(1f, 0.450381f, 0.147005f, 1f),
        Color(1f, 0.459395f, 0.155512f, 1f),
        Color(1f, 0.468325f, 0.164175f, 1f),
        Color(1f, 0.477172f, 0.172989f, 1f),
        Color(1f, 0.485935f, 0.181949f, 1f),
        Color(1f, 0.494614f, 0.19105f, 1f),
        Color(1f, 0.503211f, 0.200288f, 1f),
        Color(1f, 0.511724f, 0.209657f, 1f),
        Color(1f, 0.520155f, 0.219152f, 1f),
        Color(1f, 0.528504f, 0.228769f, 1f),
        Color(1f, 0.536771f, 0.238502f, 1f),
        Color(1f, 0.544955f, 0.248347f, 1f),
        Color(1f, 0.553059f, 0.2583f, 1f),
        Color(1f, 0.561082f, 0.268356f, 1f),
        Color(1f, 0.569024f, 0.27851f, 1f),
        Color(1f, 0.576886f, 0.288758f, 1f),
        Color(1f, 0.584668f, 0.299095f, 1f),
        Color(1f, 0.592372f, 0.309518f, 1f),
        Color(1f, 0.599996f, 0.320022f, 1f),
        Color(1f, 0.607543f, 0.330603f, 1f),
        Color(1f, 0.615012f, 0.341257f, 1f),
        Color(1f, 0.622403f, 0.35198f, 1f),
        Color(1f, 0.629719f, 0.362768f, 1f),
        Color(1f, 0.636958f, 0.373617f, 1f),
        Color(1f, 0.644122f, 0.384524f, 1f),
        Color(1f, 0.65121f, 0.395486f, 1f),
        Color(1f, 0.658225f, 0.406497f, 1f),
        Color(1f, 0.665166f, 0.417556f, 1f),
        Color(1f, 0.672034f, 0.428659f, 1f),
        Color(1f, 0.678829f, 0.439802f, 1f),
        Color(1f, 0.685552f, 0.450982f, 1f),
        Color(1f, 0.692204f, 0.462196f, 1f),
        Color(1f, 0.698786f, 0.473441f, 1f),
        Color(1f, 0.705297f, 0.484714f, 1f),
        Color(1f, 0.711739f, 0.496013f, 1f),
        Color(1f, 0.718112f, 0.507333f, 1f),
        Color(1f, 0.724417f, 0.518673f, 1f),
        Color(1f, 0.730654f, 0.53003f, 1f),
        Color(1f, 0.736825f, 0.541402f, 1f),
        Color(1f, 0.742929f, 0.552785f, 1f),
        Color(1f, 0.748968f, 0.564177f, 1f),
        Color(1f, 0.754942f, 0.575576f, 1f),
        Color(1f, 0.760851f, 0.586979f, 1f),
        Color(1f, 0.766696f, 0.598385f, 1f),
        Color(1f, 0.772479f, 0.609791f, 1f),
        Color(1f, 0.778199f, 0.621195f, 1f),
        Color(1f, 0.783858f, 0.632595f, 1f),
        Color(1f, 0.789455f, 0.643989f, 1f),
        Color(1f, 0.794991f, 0.655375f, 1f),
        Color(1f, 0.800468f, 0.666751f, 1f),
        Color(1f, 0.805886f, 0.678116f, 1f),
        Color(1f, 0.811245f, 0.689467f, 1f),
        Color(1f, 0.816546f, 0.700803f, 1f),
        Color(1f, 0.82179f, 0.712122f, 1f),
        Color(1f, 0.826976f, 0.723423f, 1f),
        Color(1f, 0.832107f, 0.734704f, 1f),
        Color(1f, 0.837183f, 0.745964f, 1f),
        Color(1f, 0.842203f, 0.757201f, 1f),
        Color(1f, 0.847169f, 0.768414f, 1f),
        Color(1f, 0.852082f, 0.779601f, 1f),
        Color(1f, 0.856941f, 0.790762f, 1f),
        Color(1f, 0.861748f, 0.801895f, 1f),
        Color(1f, 0.866503f, 0.812999f, 1f),
        Color(1f, 0.871207f, 0.824073f, 1f),
        Color(1f, 0.87586f, 0.835115f, 1f),
        Color(1f, 0.880463f, 0.846125f, 1f),
        Color(1f, 0.885017f, 0.857102f, 1f),
        Color(1f, 0.889521f, 0.868044f, 1f),
        Color(1f, 0.893977f, 0.878951f, 1f),
        Color(1f, 0.898386f, 0.889822f, 1f),
        Color(1f, 0.902747f, 0.900657f, 1f),
        Color(1f, 0.907061f, 0.911453f, 1f),
        Color(1f, 0.91133f, 0.922211f, 1f),
        Color(1f, 0.915552f, 0.932929f, 1f),
        Color(1f, 0.91973f, 0.943608f, 1f),
        Color(1f, 0.923863f, 0.954246f, 1f),
        Color(1f, 0.927952f, 0.964842f, 1f),
        Color(1f, 0.931998f, 0.975397f, 1f),
        Color(1f, 0.936001f, 0.985909f, 1f),
        Color(1f, 0.939961f, 0.996379f, 1f),
        Color(0.993241f, 0.9375f, 1f, 1f),
        Color(0.983104f, 0.931743f, 1f, 1f),
        Color(0.973213f, 0.926103f, 1f, 1f),
        Color(0.963562f, 0.920576f, 1f, 1f),
        Color(0.954141f, 0.915159f, 1f, 1f),
        Color(0.944943f, 0.909849f, 1f, 1f),
        Color(0.935961f, 0.904643f, 1f, 1f),
        Color(0.927189f, 0.899538f, 1f, 1f),
        Color(0.918618f, 0.894531f, 1f, 1f),
        Color(0.910244f, 0.88962f, 1f, 1f),
        Color(0.902059f, 0.884801f, 1f, 1f),
        Color(0.894058f, 0.880074f, 1f, 1f),
        Color(0.886236f, 0.875434f, 1f, 1f),
        Color(0.878586f, 0.87088f, 1f, 1f),
        Color(0.871103f, 0.86641f, 1f, 1f),
        Color(0.863783f, 0.862021f, 1f, 1f),
        Color(0.856621f, 0.857712f, 1f, 1f),
        Color(0.849611f, 0.853479f, 1f, 1f),
        Color(0.84275f, 0.849322f, 1f, 1f),
        Color(0.836033f, 0.845239f, 1f, 1f),
        Color(0.829456f, 0.841227f, 1f, 1f),
        Color(0.823014f, 0.837285f, 1f, 1f),
        Color(0.816705f, 0.83341f, 1f, 1f),
        Color(0.810524f, 0.829602f, 1f, 1f),
        Color(0.804468f, 0.825859f, 1f, 1f),
        Color(0.798532f, 0.82218f, 1f, 1f),
        Color(0.792715f, 0.818562f, 1f, 1f),
        Color(0.787012f, 0.815004f, 1f, 1f),
        Color(0.781421f, 0.811505f, 1f, 1f),
        Color(0.775939f, 0.808063f, 1f, 1f),
        Color(0.770561f, 0.804678f, 1f, 1f),
        Color(0.765287f, 0.801348f, 1f, 1f),
        Color(0.760112f, 0.798071f, 1f, 1f),
        Color(0.755035f, 0.794846f, 1f, 1f),
        Color(0.750053f, 0.791672f, 1f, 1f),
        Color(0.745164f, 0.788549f, 1f, 1f),
        Color(0.740364f, 0.785474f, 1f, 1f),
        Color(0.735652f, 0.782448f, 1f, 1f),
        Color(0.731026f, 0.779468f, 1f, 1f),
        Color(0.726482f, 0.776534f, 1f, 1f),
        Color(0.722021f, 0.773644f, 1f, 1f),
        Color(0.717638f, 0.770798f, 1f, 1f),
        Color(0.713333f, 0.767996f, 1f, 1f),
        Color(0.709103f, 0.765235f, 1f, 1f),
        Color(0.704947f, 0.762515f, 1f, 1f),
        Color(0.700862f, 0.759835f, 1f, 1f),
        Color(0.696848f, 0.757195f, 1f, 1f),
        Color(0.692902f, 0.754593f, 1f, 1f),
        Color(0.689023f, 0.752029f, 1f, 1f),
        Color(0.685208f, 0.749502f, 1f, 1f),
        Color(0.681458f, 0.747011f, 1f, 1f),
        Color(0.67777f, 0.744555f, 1f, 1f),
        Color(0.674143f, 0.742134f, 1f, 1f),
        Color(0.670574f, 0.739747f, 1f, 1f),
        Color(0.667064f, 0.737394f, 1f, 1f),
        Color(0.663611f, 0.735073f, 1f, 1f),
        Color(0.660213f, 0.732785f, 1f, 1f),
        Color(0.656869f, 0.730528f, 1f, 1f),
        Color(0.653579f, 0.728301f, 1f, 1f),
        Color(0.65034f, 0.726105f, 1f, 1f),
        Color(0.647151f, 0.723939f, 1f, 1f),
        Color(0.644013f, 0.721801f, 1f, 1f),
        Color(0.640922f, 0.719692f, 1f, 1f),
        Color(0.637879f, 0.717611f, 1f, 1f),
        Color(0.634883f, 0.715558f, 1f, 1f),
        Color(0.631932f, 0.713531f, 1f, 1f),
        Color(0.629025f, 0.711531f, 1f, 1f),
        Color(0.626162f, 0.709557f, 1f, 1f),
        Color(0.623342f, 0.707609f, 1f, 1f),
        Color(0.620563f, 0.705685f, 1f, 1f),
        Color(0.617825f, 0.703786f, 1f, 1f),
        Color(0.615127f, 0.701911f, 1f, 1f),
        Color(0.612469f, 0.70006f, 1f, 1f),
        Color(0.609848f, 0.698231f, 1f, 1f),
        Color(0.607266f, 0.696426f, 1f, 1f),
        Color(0.60472f, 0.694643f, 1f, 1f)
    )

    fun powerTwoFloor(value: UInt): UInt {
        var power: UInt = 2u
        var nextVal: UInt = power * 2u

        while (true) {

            nextVal *= 2u
            if (nextVal <= value) break

            power = power shl 1
        }

        return power shl 1
    }

    fun randomNumber(): Float {
        return Math.random().toFloat()
    }

    /**
     * Get the star color from its temperature
     */
    fun colorFromTemperature(temperature: Float): Color {

        val minTemperature = 1000.0
        val maxTemperature = 10000.0
        var index: Int = (Math.floor((temperature - minTemperature) / (maxTemperature - minTemperature) * colors.size)).toInt()
        index = Math.min(colors.size - 1, index)
        index = Math.max(0, index)

        return colors[index]
    }


    // Velocity curve with dark matter
    fun velocityWithDarkMatter(r: Float): Float {
        if (r == 0f) return 0f

        val MZ: Float = 100f
        val massHalo: Float = massHalo(r)
        val massDisc: Float = massDisc(r)
        return (20000.0 * Math.sqrt(CONTANT_OF_GRAVITY * (massHalo + massDisc + MZ) / r.toDouble())).toFloat()
    }

    // velocity curve without dark matter
    fun velocityWithoutDarkMatter(r: Float): Float {
        if (r == 0f) return 0f

        val MZ: Float = 100f
        return (20000.0f * Math.sqrt(CONTANT_OF_GRAVITY * (massDisc(r) + MZ) / r.toDouble())).toFloat()
    }

    fun massDisc(r: Float): Float {
        val d: Float = 2000f        // Dicke der Scheibe
        val rho_so: Float = 1f    // Dichte im Mittelpunkt
        val rH: Float = 2000f    // Radius auf dem die Dichte um die Hälfte gefallen ist
        return (rho_so * Math.exp(-r / rH.toDouble()) * (r * r) * Math.PI * d).toFloat()
    }

    fun massHalo(r: Float): Float {
        val rho_h0: Float = 0.15f
        val rC: Float = 2500f
        return (rho_h0 * 1f / (1 + Math.pow((r / rC).toDouble(), 2.0)) * (4 * Math.PI * Math.pow(r.toDouble(), 3.0) / 3)).toFloat()
    }
}
