package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.parcel.coffee.core.hardware.helpers.ConfigurationException
import java.util.*

class TechSensorOptions(
    @Expose
    val sensorNumber: Int,

    @Expose
    val relays: ArrayList<RelayJobOptions>
) {

    fun validate() {
        if(!BoardOptions.ALLOWED_TECH_SENSOR_NUMBERS.contains(sensorNumber)) {
            throw ConfigurationException("Номер технического сенсора $sensorNumber недопустим")
        }
    }
}