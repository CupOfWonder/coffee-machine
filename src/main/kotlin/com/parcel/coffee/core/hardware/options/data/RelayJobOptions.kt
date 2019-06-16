package com.parcel.coffee.core.hardware.options.data

import com.google.gson.annotations.Expose
import com.parcel.coffee.core.hardware.helpers.ConfigurationException

class RelayJobOptions(@Expose val relayNumber: Int, @Expose val timeOn: Long, @Expose val timeJob: Long?) {

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

    fun validate() {
        if(timeJob == null && stopSignal == null) {
            throw ConfigurationException("Реле $relayNumber должно иметь или параметр stopSignal, или timeJob")
        }
        if(!BoardOptions.ALLOWED_RELAY_NUMBERS.contains(relayNumber)) {
            throw ConfigurationException("Номер реле $relayNumber недопустим")
        }
        stopSignal?.let {
            if(!BoardOptions.ALLOWED_STOP_SIGNAL_NUMBERS.contains(it)) {
                throw ConfigurationException("Номер стоп-сигнала $stopSignal недопустим")
            }
        }
    }
}