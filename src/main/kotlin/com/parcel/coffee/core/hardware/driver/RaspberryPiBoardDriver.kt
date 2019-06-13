package com.parcel.coffee.core.hardware.driver

import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import com.parcel.coffee.core.hardware.helpers.StopSignalHandler
import com.parcel.coffee.core.hardware.helpers.TechSensorHandler
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
            15 to RaspiPin.GPIO_08,
            16 to RaspiPin.GPIO_09
    )

    private val techSensors = mapOf<Int, Pin> (
            1 to RaspiPin.GPIO_12,
            2 to RaspiPin.GPIO_14
    )

    private val stopSensors = mapOf<Int, Pin>(
            1 to RaspiPin.GPIO_15,
            2 to RaspiPin.GPIO_16
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
        return digitalInputForPin(buttonPins[buttonNum])
    }

    override fun addTechSensorHandler(sensorNum: Int, handler: TechSensorHandler) {
        val sensorInput = getSensorPinInput(sensorNum)

        sensorInput?.addListener(GpioPinListenerDigital {
            if(sensorInput.isHigh) {
                logger.info("Tech sensor $sensorNum triggered")
                handler.onSensorTriggered()
            }
        })
    }

    private fun getSensorPinInput(sensorNum: Int): GpioPinDigitalInput? {
        return digitalInputForPin(techSensors[sensorNum])
    }

    private fun getStopSignalPinInput(stopSignalNum: Int): GpioPinDigitalInput? {
        return digitalInputForPin(stopSensors[stopSignalNum])
    }

    private fun digitalInputForPin(pin : Pin?) : GpioPinDigitalInput? {
        pin?.let {
            val raspiPin = CommandArgumentParser.getPin(RaspiPin::class.java, pin)
            val pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.PULL_UP)

            return gpio.provisionDigitalInputPin(raspiPin, pull)
        }

        return null
    }


    override fun setStopSignalHandler(handler: StopSignalHandler) {
        stopSensors.forEach {
            val signalNum = it.key
            val stopInput = getStopSignalPinInput(signalNum)

            stopInput?.let {
                it.addListener(GpioPinListenerDigital {
                    if(stopInput.isHigh) {
                        handler.onStopSignalReceived(signalNum)
                    }
                })
            }
        }
    }

}