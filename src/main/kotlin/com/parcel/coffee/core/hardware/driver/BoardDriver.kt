package com.parcel.coffee.core.hardware.driver

import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler

abstract class BoardDriver {
    abstract fun signalToRelay(relayNum : Int, on : Boolean)

    abstract fun addButtonPushHandler(buttonNum: Int, handler : ButtonPushHandler)

}