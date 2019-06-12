package com.parcel.coffee.core.hardware.driver

import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import com.parcel.coffee.core.hardware.helpers.StopSignalHandler
import com.parcel.coffee.core.hardware.helpers.TechSensorHandler

abstract class BoardDriver {
    abstract fun signalToRelay(relayNum : Int, on : Boolean)

    abstract fun addButtonPushHandler(buttonNum: Int, handler : ButtonPushHandler)

    abstract fun addTechSensorHandler(sensorNum: Int, handler : TechSensorHandler)

    abstract fun setStopSignalHandler(handler: StopSignalHandler)
}