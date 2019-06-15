package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BoardOptions {

    companion object {
        val ALLOWED_BUTTON_NUMBERS = 1..6
        val ALLOWED_RELAY_NUMBERS = 1..16
        val ALLOWED_TECH_SENSOR_NUMBERS = 1..2
        val ALLOWED_STOP_SIGNAL_NUMBERS = 1..2
    }

    @Expose
    @SerializedName("buttons")
    var buttonOptions = ArrayList<ButtonOptions>();

    @Expose
    @SerializedName("techSensors")
    var technicalSensorOptions = ArrayList<TechSensorOptions>()

    fun validate() {
        buttonOptions.forEach { it.validate() }
        technicalSensorOptions.forEach { it.validate() }
    }


}