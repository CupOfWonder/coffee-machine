package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import java.util.*

open class TechSensorOptions(@Expose open val relays: ArrayList<RelayJobOptions>) {

}