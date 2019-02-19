package com.parcel

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler
import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.util.CommandArgumentParser
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * объеккт для хранения информации о запущенных потоках.
 */
object Processes
{
    /**
     * При запуске потоков, каждое реле добавляет сюда метку соответсвующую ее номеру по ТЗ, при завершении работы реле метку убирает.
     */
    val buttonPressedProceses = arrayListOf<Int>()
}


/**
 * Наша плата.
 */
class Board
{

    @Expose(serialize = false)
    private val settingsFileName = "Settings.json"

    @Expose
    var buttons =arrayListOf(
            Button(0, arrayListOf(Rele(0, 1000, 3000), Rele(2, 200, 10), Rele(1, 400, 800))),
            Button(1, arrayListOf(Rele(2, 1, 3), Rele(1, 2, 1), Rele(0, 4, 8))),
            Button(2, arrayListOf(Rele(3, 1, 3), Rele(4, 2, 1), Rele(5, 4, 8))),
            Button(3, arrayListOf(Rele(2, 1, 3), Rele(1, 2, 1), Rele(1, 4, 8))),
            Button(4, arrayListOf(Rele(0, 1, 3), Rele(1, 2, 1), Rele(4, 4, 8))),
            Button(5, arrayListOf(Rele(0, 1, 3), Rele(1, 2, 1), Rele(4, 4, 8)))
    )

    @Expose(serialize = false, deserialize = false)
    var buttonMap = HashMap<Int, Button>();

    fun generate()
    {
        for(b in buttons)
            b.generate()
        refreshButtonMap()
    }

    fun refreshButtonMap() {
        buttons.forEach { buttonMap[it.buttonNumber] = it }
    }

    @Override
    override fun toString() :String
    {
        var builder =  GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
        var gson = builder.create()
        return gson.toJson(this)
    }



    /**
     * Сохраниь настройки в файл.
     */
    fun save() : Boolean
    {
        try {
            val file = File(settingsFileName)

            //проверяем, что если файл не существует то создаем его
            if (file.exists())
                file.delete()
            file.createNewFile()

            file.writeText(toString())
            return true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Обновить настройки из файла.
     */
    fun update() : Boolean
    {
        /**
         *  Собрать файл из стрингов.
         */
        fun fromString(json: String)  : Boolean
        {
            try {
                var builder = GsonBuilder()
                var gson = builder.create()
                var pgs = gson.fromJson(json, Board().javaClass)
                this.buttons = pgs.buttons

                refreshButtonMap()
                return true
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                return false
            }
        }//fun fromString(json: String)  : Boolean
        try {
            val file = File(settingsFileName)
            //проверяем, что если файл не существует то создаем его
            if (file.exists())
            {
                return  fromString(file.readText())
            }
            else
            {
                save()
                println("File not found")
                return false
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return false
        }

    }

    fun setButtonPushHandler(buttonNum: Int, handler : ButtonPushHandler) {
        var button = buttonMap[buttonNum]

        if(button != null) {
            button.setButtonPushHandler(handler)
        }
    }

    fun setButtonWorkFinishHandler(buttonNum: Int, handler : WorkFinishHandler) {
        var button = buttonMap[buttonNum]

        if(button != null) {
            button.setWorkFinishHandler(handler)
        }
    }


}

/**
 * Слушатель события нажатия кнопки.
 */
interface RpiButtonListener {
    fun buttonPositionChanged(pin: Pin, value: Boolean)
}


/**
 * Кнопка.
 * @param buttonNumber  ноомер кнопки по ТЗ
 * @param reles релюшки))
 */
class Button(@Expose val buttonNumber: Int, @Expose val reles: ArrayList<Rele>)
{

    @Expose(serialize = false)
    private lateinit var pin: Pin
    @Expose(serialize = false)
    private lateinit var button: GpioPinDigitalInput

    @Expose(serialize = false, deserialize = false)
    private var pushHandler : ButtonPushHandler? = null

    @Expose(serialize = false, deserialize = false)
    private var workFinishHandler : WorkFinishHandler? = null

    //private val listeners = ArrayList<RpiButtonListener>()

    /**
     * Инициализируем железо.
     */
    fun generate()
    {
        val gpio = GpioFactory.getInstance()
        //инициализация кнопки
        pin = CommandArgumentParser.getPin(RaspiPin::class.java, Interfaces.getButtonPin(buttonNumber))
        val pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.PULL_UP)
        button = gpio.provisionDigitalInputPin(pin, pull)

        //инициализация релюх
        for (r in reles)
        {
            r.generate()
        }

        //подписка на события
        this.button.addListener(GpioPinListenerDigital {
            //value = this.button.isHigh()
            if(this.button.isHigh() && Processes.buttonPressedProceses.size == 0) {
                println("Button $buttonNumber pressed")

                var relayFinishLatch = CountDownLatch(reles.size)
                for (r in reles)
                    r.action(relayFinishLatch)

                handlePushHandlers()

                if(relayFinishLatch.await(10, TimeUnit.MINUTES)) {
                    handleWorkFinishHandler()
                } else {
                    println("10 minutes elapsed!")
                }

            }
        })
    }

    private fun handleWorkFinishHandler() {
        workFinishHandler?.onWorkFinish()
    }

    private fun handlePushHandlers() {
        pushHandler?.onButtonPush()
    }

    fun setButtonPushHandler(handler: ButtonPushHandler) {
        pushHandler = handler
    }

    fun setWorkFinishHandler(handler: WorkFinishHandler) {
        workFinishHandler = handler
    }



}




/**
 * Класс описывающий поведение реле.
 * @param releNumber номе реле
 * @param timeOn время через которое реле включается
 * @param timeJob время через которое реле выключается
 */
class Rele(@Expose val releNumber: Int,@Expose  val timeOn: Long,@Expose val timeJob: Long)
{
    @Expose(serialize = false)
    private lateinit var pin : GpioPinDigitalOutput

    /**
     * Инверсия пина.
     */
    @Expose
    var inverse = false
    /**
     * Имя реле.
     */
    @Expose
    var name = "Напишите сюда имя реле что бы не путаться."


    /**
     * Инициализировать.
     */
    fun generate()
    {
        pin = Interfaces.getRelePin(releNumber)
    }

    /**
     * Отработать алгоритм
     */
    fun action(workFinishLatch: CountDownLatch)
    {
        Processes.buttonPressedProceses.add(releNumber)
        var thread = Thread(Runnable {
            Thread.sleep(timeOn)
            open()
            Thread.sleep(timeJob)
            close()

            workFinishLatch.countDown()
        })
        thread.start()
    }

    /**
     * Открыит реле.
     */
    private fun open(){

        println("Rele $releNumber open. timeOn = $timeOn timeJob = $timeJob")
        if(!inverse) pin.high() else pin.low()
    }

    /**
     * Закрыть реле.
     */
    private fun close(){
        println("Rele $releNumber closed. timeOn = $timeOn timeJob = $timeJob")
        if(!inverse) pin.low() else pin.high()
        val index = Processes.buttonPressedProceses.indexOf(releNumber)
        if (Processes.buttonPressedProceses.size > index) Processes.buttonPressedProceses.removeAt(index)
    }

}