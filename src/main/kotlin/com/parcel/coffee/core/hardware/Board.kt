package com.parcel.coffee.core.hardware

import com.parcel.coffee.core.hardware.driver.RaspberryPiBoardDriver
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import com.parcel.coffee.core.hardware.helpers.StopSignalHandler
import com.parcel.coffee.core.hardware.helpers.TechSensorHandler
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler
import com.parcel.coffee.core.hardware.options.OptionsLoader
import com.parcel.coffee.core.hardware.options.data.BoardOptions
import com.parcel.coffee.core.hardware.options.data.ButtonOptions
import com.parcel.coffee.core.hardware.options.data.TechSensorOptions

class Board {
    private var driver = RaspberryPiBoardDriver();

    private lateinit var options : BoardOptions

    private var buttonOptionsMap = HashMap<Int, ButtonOptions>()

    private var buttonPushHandlers = HashMap<Int, ButtonPushHandler>()
    private var workFinishHandlers = HashMap<Int, WorkFinishHandler>()


    private val algorithmExecutor = RelayAlgorithmExecutor(driver)

    fun loadOptionsAndInit() {
        loadOrGenerateOptions()
        initHelperMaps()
        initTechSensorTriggers()
        initStopSignalTriggers()
    }

    private fun loadOrGenerateOptions() {
        val optionsLoader = OptionsLoader()
        this.options = optionsLoader.loadOptionsOrGenerateDefault()
    }

    private fun initHelperMaps() {
        options.buttonOptions.forEach {
            buttonOptionsMap.set(it.buttonNumber, it)
        }
    }

    fun executeButtonAlgorithm(buttonNum : Int) {
        val buttonOptions = buttonOptionsMap[buttonNum]

        val finishHandler = workFinishHandlers[buttonNum]

        buttonOptions?.let {
            algorithmExecutor.executeRelayAlgorithm(buttonOptions.relays, finishHandler)
        }
    }

    fun setWorkFinishHandler(buttonNum : Int, handler: WorkFinishHandler) {
        workFinishHandlers[buttonNum] = handler
    }

    fun setButtonPushHandler(buttonNum: Int, handler: ButtonPushHandler) {
        buttonPushHandlers[buttonNum] = handler
        driver.addButtonPushHandler(buttonNum, handler)
    }

    private fun initTechSensorTriggers() {
        val techSensorOptions : List<TechSensorOptions> = options.technicalSensorOptions;

        techSensorOptions.forEach {
            driver.addTechSensorHandler(it.sensorNumber, object : TechSensorHandler {
                override fun onSensorTriggered() {
                    algorithmExecutor.executeRelayAlgorithm(it.relays)
                }
            })
        }
    }

    private fun initStopSignalTriggers() {
        driver.setStopSignalHandler(object : StopSignalHandler {
            override fun onStopSignalReceived(stopSignalNum: Int) {
                algorithmExecutor.stopBySignal(stopSignalNum)
            }
        })
    }

    fun emulateStopSignal(stopSignalNum: Int) {
        algorithmExecutor.stopBySignal(stopSignalNum)
    }

}