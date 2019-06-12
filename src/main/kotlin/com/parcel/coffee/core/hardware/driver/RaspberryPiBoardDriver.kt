package com.parcel.coffee.core.hardware.driver

import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.util.CommandArgumentParser
import org.apache.log4j.Logger

class RaspberryPiBoardDriver : BoardDriver() {

    private val logger = Logger.getLogger(this.javaClass)

    private val gpio = GpioFactory.getInstance()

    private val buttonPins =  mapOf<Int, Pin>(
            1 to RaspiPin.GPIO_07,
            2 to RaspiPin.GPIO_00,
            3 to RaspiPin.GPIO_02,
            4 to RaspiPin.GPIO_03,
            5 to RaspiPin.GPIO_21,
            6 to RaspiPin.GPIO_22
    )

    private val relayPins = mapOf<Int, Pin>(
            1 to RaspiPin.GPIO_23,
            2 to RaspiPin.GPIO_24,
            3 to RaspiPin.GPIO_25,
            4 to RaspiPin.GPIO_04,
            5 to RaspiPin.GPIO_05,
            6 to RaspiPin.GPIO_06,
            7 to RaspiPin.GPIO_26,
            8 to RaspiPin.GPIO_27,
            9 to RaspiPin.GPIO_28,
            10 to RaspiPin.GPIO_29,
            11 to RaspiPin.GPIO_01,
            12 to RaspiPin.GPIO_13,
            13 to RaspiPin.GPIO_10,
            14 to RaspiPin.GPIO_11,
            15 to RaspiPin.GPIO_02,
            16 to RaspiPin.GPIO_03
    )



    override fun addButtonPushHandler(buttonNum: Int, handler: ButtonPushHandler) {
        val buttonInput = getButtonPinInput(buttonNum)

        buttonInput?.addListener(GpioPinListenerDigital {
            if(buttonInput.isHigh) {
                logger.info("Button $buttonNum pressed")
                handler.onButtonPush()
            }
        })
    }

    override fun signalToRelay(relayNum: Int, on: Boolean) {
        val pin : GpioPinDigitalOutput? = getRelayPinOutput(relayNum)

        if(on) pin?.high() else pin?.low()
    }

    private fun getRelayPinOutput(relayNum: Int): GpioPinDigitalOutput? {
        val pin = relayPins[relayNum]
        return gpio.provisionDigitalOutputPin(pin)
    }

    private fun getButtonPinInput(buttonNum: Int) : GpioPinDigitalInput? {
        val pin = CommandArgumentParser.getPin(RaspiPin::class.java, buttonPins[buttonNum])
        val pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.PULL_UP)

        return gpio.provisionDigitalInputPin(pin, pull)
    }
}