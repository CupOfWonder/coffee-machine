package com.parcel

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.Pin
import com.pi4j.io.gpio.RaspiPin

/**
 * Возращает пин привязанный к реле или кнопке.
 */
object Interfaces
{
    class ButtonProp(val number: Int,val gpioPin: Pin)
    class ReleProp(val number: Int,val gpioPin: Pin)
    {
        private val gpio = GpioFactory.getInstance()
        val gpioPinDigitalOutput = gpio.provisionDigitalOutputPin(gpioPin)

    }

    private val buttons =  arrayListOf(
        ButtonProp(0, RaspiPin.GPIO_07),
        ButtonProp(1, RaspiPin.GPIO_00),
        ButtonProp(2, RaspiPin.GPIO_02),
        ButtonProp(3, RaspiPin.GPIO_03),
        ButtonProp(4, RaspiPin.GPIO_21),
        ButtonProp(5, RaspiPin.GPIO_22)
    )


    private val rele =  arrayListOf(
        ReleProp(0, RaspiPin.GPIO_23),
        ReleProp(1, RaspiPin.GPIO_24),
        ReleProp(2, RaspiPin.GPIO_25),
        ReleProp(3, RaspiPin.GPIO_04),
        ReleProp(4, RaspiPin.GPIO_05),
        ReleProp(5, RaspiPin.GPIO_06),
        ReleProp(6, RaspiPin.GPIO_26),
        ReleProp(7, RaspiPin.GPIO_27),
        ReleProp(8, RaspiPin.GPIO_28),
        ReleProp(9, RaspiPin.GPIO_29),
        ReleProp(9, RaspiPin.GPIO_01),
        ReleProp(9, RaspiPin.GPIO_13),
        ReleProp(9, RaspiPin.GPIO_10),
        ReleProp(9, RaspiPin.GPIO_11)
    )




    /**
     * Возращает пин кнопки номер
     * @param buttonNumber
     */
    fun getButtonPin (buttonNumber : Int) : Pin
    {

        for(b in buttons)
            if(b.number == buttonNumber)
                return b.gpioPin
        throw  InterfacesException("Не существует кнопка  номер $buttonNumber.")
    }

    /**
     * Возвращает пин реле
     * @param releNumber
     */
    fun getRelePin (releNumber : Int) :  GpioPinDigitalOutput
    {
        for(r in rele)
            if(r.number == releNumber)
                return r.gpioPinDigitalOutput
        throw  InterfacesException("Не существует реле номер $releNumber.")
    }
}

/**
 * Ошибка интерфейса.
 */
class InterfacesException(message: String) : Throwable(message)
