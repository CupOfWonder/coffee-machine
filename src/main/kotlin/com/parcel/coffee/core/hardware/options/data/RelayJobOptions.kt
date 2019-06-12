package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.pi4j.io.gpio.GpioPinDigitalOutput

class RelayJobOptions(@Expose val relayNumber: Int, @Expose val timeOn: Long, @Expose val timeJob: Long?) {

    @Expose(serialize = false)
    private lateinit var pin : GpioPinDigitalOutput

    @Expose
    var stopSignal : Int? = null

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

    override fun toString(): String {
        return "RelayJobOptions(relayNumber=$relayNumber, timeOn=$timeOn, timeJob=$timeJob, stopSignal=$stopSignal, inverse=$inverse, name='$name')"
    }
}