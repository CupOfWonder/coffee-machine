package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class ButtonOptions(
        @Expose
        val buttonNumber: Int,

        @Expose
        @SerializedName("relays")
        val relays: ArrayList<RelayJobOptions>
)