package com.slaviboy.opengl.params

class Star(
    var theta0: Float = 0f,     // initial angular position on the ellipse
    var velTheta: Float = 0f,   // angular velocity
    var tiltAngle: Float = 0f,  // tilt angle of the ellipse
    var a: Float = 0f,          // semi-minor axis
    var b: Float = 0f,          // semi-major axis
    var temp: Float = 0f,       // star temperature
    var mag: Float = 0f,        // star brightness
    var type: Int = 0           // Type 0:star, 1:dust, 2 and 3: H2 regions
)