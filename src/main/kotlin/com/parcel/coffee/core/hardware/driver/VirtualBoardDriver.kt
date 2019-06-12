package com.parcel.coffee.core.hardware.driver

import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import org.apache.log4j.Logger

class VirtualBoardDriver : BoardDriver() {

    private val logger = Logger.getLogger(this.javaClass)

    override fun addButtonPushHandler(buttonNum: Int, handler: ButtonPushHandler) {
        logger.info("Added button push handler to button $buttonNum")
    }

    override fun signalToRelay(relayNum: Int, on: Boolean) {
        logger.info("Signal " +  (if(on) "ON" else "OFF") + " was sent to relay $relayNum")
    }
}