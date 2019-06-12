package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.SerializedName

class BoardOptions {

    @SerializedName("buttons")
    var buttonOptions = ArrayList<ButtonOptions>();

    @SerializedName("techSensors")
    var technicalSensorOptions = ArrayList<TechSensorOptions>()

}