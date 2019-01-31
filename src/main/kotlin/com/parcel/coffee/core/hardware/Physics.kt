package com.parcel

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.util.CommandArgumentParser
import java.io.File
import java.util.*

/**
 * объеккт для хранения информации о запущенных потоках.
 */
object Proceses
{
    /**
     * При запуске потоков, каждое реле добавляет сюда метку соответсвующую ее номеру по ТЗ, при завершении работы реле метку убирает.
     */
    val buttonPresedProceses = arrayListOf<Int>()
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
        Button(4, arrayListOf(Rele(0, 1, 3), Rele(1, 2, 1), Rele(4, 4, 8)))
    )

    fun generate()
    {
        for(b in buttons)
            b.generate()
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

    //private val listeners = ArrayList<RpiButtonListener>()



    /**
     * Инициализируем железо.
     */
    fun generate()
    {
        val gpio = GpioFactory.getInstance()
        //инициализвция кнопки
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
            if(this.button.isHigh() && Proceses.buttonPresedProceses.size == 0) {
                println("Button $buttonNumber pressed")
                for (r in reles)
                    r.action()
            }
        })
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
    fun action()
    {
        Proceses.buttonPresedProceses.add(releNumber)
        var thread = Thread(Runnable {
            Thread.sleep(timeOn)
            open()
            Thread.sleep(timeJob)
            close()

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
        val index = Proceses.buttonPresedProceses.indexOf(releNumber)
        if (Proceses.buttonPresedProceses.size > index) Proceses.buttonPresedProceses.removeAt(index)
    }

}