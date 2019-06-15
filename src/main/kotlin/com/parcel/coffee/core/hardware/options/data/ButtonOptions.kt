package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.parcel.coffee.core.hardware.helpers.ConfigurationException
import java.util.*

class ButtonOptions(
        @Expose
        val buttonNumber: Int,

        @Expose
        @SerializedName("relays")
        val relays: ArrayList<RelayJobOptions>
) {
    fun validate() {
        if(!BoardOptions.ALLOWED_BUTTON_NUMBERS.contains(buttonNumber)) {
            throw ConfigurationException("Номер кнопки $buttonNumber недопустим")
        }
        relays.forEach { it.validate() }
    }
}