package com.parcel.coffee.core.hardware

import com.parcel.coffee.core.hardware.driver.BoardDriver
import com.parcel.coffee.core.hardware.helpers.RelayJobInfo
import com.parcel.coffee.core.hardware.helpers.RelayJobMap
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler
import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import org.apache.log4j.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RelayAlgorithmExecutor(private val driver: BoardDriver) {

    private val logger  = Logger.getLogger(this.javaClass)

    private var relayJobMap = RelayJobMap()

    fun executeRelayAlgorithm(jobOptions: List<RelayJobOptions>, finishHandler: WorkFinishHandler? = null) {
        val relayFinishLatch = CountDownLatch(jobOptions.size)

        jobOptions.forEach {
            executeRelaySingleJob(it, relayFinishLatch)
        }

        if(relayFinishLatch.await(10, TimeUnit.MINUTES)) {
            finishHandler?.onWorkFinish()
        } else {
            println("10 minutes elapsed!")
        }
    }

    private fun executeRelaySingleJob(relayOpts: RelayJobOptions, relayFinishLatch: CountDownLatch? = null) {
        logger.debug("RelayOptions: $relayOpts")
        var thread = Thread(Runnable {
            try {
                Thread.sleep(relayOpts.timeOn)

                openRelay(relayOpts.relayNumber, relayOpts.inverse)

                relayOpts.timeJob?.let {
                    Thread.sleep(it)
                    closeRelay(relayOpts.relayNumber, relayOpts.inverse)
                }

                relayFinishLatch?.countDown()
                relayJobMap.relayThreadFinished(relayOpts.relayNumber, Thread.currentThread())
            } catch (e : InterruptedException) {

            }
        })

        relayJobMap.rememberRelayThread(relayOpts.relayNumber, thread, relayOpts, relayFinishLatch)
        thread.start()
    }

    private fun openRelay(relayNum: Int, inverse : Boolean) {
        val signal = !inverse
        driver.signalToRelay(relayNum, signal)
    }

    private fun closeRelay(relayNum: Int, inverse: Boolean) {
        val signal = inverse
        driver.signalToRelay(relayNum, signal)
    }

    fun stopBySignal(relayNum: Int) {
        logger.info("Received stop signal on relay $relayNum")

        val jobInfoList : List<RelayJobInfo> = relayJobMap.currentJobsForRelayNumber(relayNum)

        jobInfoList.forEach {
            it.thread.interrupt()
            it.countDownLatch?.countDown()
            closeRelay(relayNum, it.jobOptions.inverse)
        }
        relayJobMap.removeAllJobs(relayNum)

    }
}