package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BoardOptions {

    @Expose
    @SerializedName("buttons")
    var buttonOptions = ArrayList<ButtonOptions>();

    @Expose
    @SerializedName("techSensors")
    var technicalSensorOptions = ArrayList<TechSensorOptions>()

}