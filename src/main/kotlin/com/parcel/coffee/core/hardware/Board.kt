package com.parcel.coffee.core.hardware

import com.parcel.coffee.core.hardware.driver.RaspberryPiBoardDriver
import com.parcel.coffee.core.hardware.helpers.*
import com.parcel.coffee.core.hardware.options.OptionsLoader
import com.parcel.coffee.core.hardware.options.data.BoardOptions
import com.parcel.coffee.core.hardware.options.data.ButtonOptions
import com.parcel.coffee.core.hardware.options.data.TechSensorOptions
import org.apache.log4j.Logger

class Board {
    private val logger = Logger.getLogger(this.javaClass)

    private var driver = RaspberryPiBoardDriver()

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

        try {
            this.options = optionsLoader.loadOptionsOrGenerateDefault()
            this.options.validate()
        } catch (e : ConfigurationException) {
            logger.error("Ошибка конфигурации: ${e.message}")
            System.exit(1);
        }

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