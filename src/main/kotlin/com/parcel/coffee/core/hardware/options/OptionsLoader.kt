package com.parcel.coffee.core.hardware.options

import com.google.gson.GsonBuilder
import com.parcel.coffee.core.hardware.options.data.BoardOptions
import com.parcel.coffee.core.hardware.options.data.ButtonOptions
import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import org.apache.log4j.Logger
import java.io.File

class OptionsLoader {

    private val settingsFileName = "Settings.json"

    private val logger = Logger.getLogger(this.javaClass)

    fun loadOptionsOrGenerateDefault() : BoardOptions {
        val file = File(settingsFileName)
        //проверяем, что если файл не существует то создаем его
        if (file.exists()) {
            val options = fromString(file.readText())
            logger.info("Successfully loaded options from $settingsFileName")
            logger.info("currentOptions: "+optionsToJson(options))
            return options;
        } else {
            logger.info("File $settingsFileName not found! Creating default one")

            val defaultOptions = defaultOptions()
            saveOptions(defaultOptions)

            return defaultOptions
        }
    }

    /**
     *  Собрать файл из стрингов.
     */
    fun fromString(json: String)  : BoardOptions {
        var builder = GsonBuilder()
        var gson = builder.create()
        var pgs = gson.fromJson(json, BoardOptions().javaClass)

        return pgs
    }

    /**
     * Сохраниь настройки в файл.
     */
    fun saveOptions(options: BoardOptions) : Boolean {
        try {
            val file = File(settingsFileName)

            //проверяем, что если файл не существует то создаем его
            if (file.exists())
                file.delete()
            file.createNewFile()

            file.writeText(optionsToJson(options))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun defaultOptions(): BoardOptions {
        val buttonOptions = arrayListOf(
                ButtonOptions(0,
                        arrayListOf(
                                RelayJobOptions(0, 1000, 3000),
                                RelayJobOptions(2, 200, 10),
                                RelayJobOptions(1, 400, 800)
                        )
                ),
                ButtonOptions(1,
                        arrayListOf(
                                RelayJobOptions(2, 1, 3),
                                RelayJobOptions(1, 2, 1),
                                RelayJobOptions(0, 4, 8)
                        )
                ),
                ButtonOptions(2,
                        arrayListOf(
                                RelayJobOptions(3, 1, 3),
                                RelayJobOptions(4, 2, 1),
                                RelayJobOptions(5, 4, 8)
                        )
                ),
                ButtonOptions(3,
                        arrayListOf(
                                RelayJobOptions(2, 1, 3),
                                RelayJobOptions(1, 2, 1),
                                RelayJobOptions(1, 4, 8)
                        )
                ),
                ButtonOptions(4,
                        arrayListOf(
                                RelayJobOptions(0, 1, 3),
                                RelayJobOptions(1, 2, 1),
                                RelayJobOptions(4, 4, 8)
                        )
                ),
                ButtonOptions(5,
                        arrayListOf(
                                RelayJobOptions(0, 1, 3),
                                RelayJobOptions(1, 2, 1),
                                RelayJobOptions(4, 4, 8)
                        )
                )
        )

        val boardOptions = BoardOptions()
        boardOptions.buttonOptions = buttonOptions;

        return boardOptions;
    }

    private fun optionsToJson(options: BoardOptions) : String {
        val builder =  GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
        val gson = builder.create()

        return gson.toJson(options)
    }
}