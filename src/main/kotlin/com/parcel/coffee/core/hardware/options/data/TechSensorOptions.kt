package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import java.util.*

class TechSensorOptions(
    @Expose
    val sensorNumber: Int,

    @Expose
    val relays: ArrayList<RelayJobOptions>
)