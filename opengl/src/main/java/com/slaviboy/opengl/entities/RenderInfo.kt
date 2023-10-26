package com.slaviboy.opengl.entities

data class RenderInfo(
    var numberOfStars: RangeValues,
    var numberOfH2Regions: RangeValues,
    var numberOfDustParticles: RangeValues,
    var numberOfDustFilaments: RangeValues,
    var starsSizeFactor: RangeValues,
    var H2RegionsSizeFactor: RangeValues,
    var dustParticlesSizeFactor: RangeValues,
    var dustFilamentsSizeFactor: RangeValues,
    var galaxySizeFactor: RangeValues
)