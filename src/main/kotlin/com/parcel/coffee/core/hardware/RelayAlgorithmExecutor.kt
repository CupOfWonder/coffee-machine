package com.parcel.coffee.core.hardware

import com.parcel.coffee.core.hardware.driver.BoardDriver
import com.parcel.coffee.core.hardware.helpers.RelayJobInfo
import com.parcel.coffee.core.hardware.helpers.RelayJobMap
import com.parcel.coffee.core.hardware.helpers.RelayJobStatus
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
        var thread = Thread(Runnable {
            try {
                val jobInfo : RelayJobInfo
                        = relayJobMap.rememberJobStarted(relayOpts.relayNumber, Thread.currentThread(), relayOpts)
                Thread.sleep(relayOpts.timeOn)

                jobInfo.jobStatus = RelayJobStatus.WORKING
                openRelay(relayOpts.relayNumber, relayOpts.inverse)

                relayOpts.timeJob?.let {
                    Thread.sleep(it)

                    jobInfo.jobStatus = RelayJobStatus.READY_TO_CLOSE
                    closeRelayOnNoOtherJob(relayOpts.relayNumber, relayOpts.inverse)
                }

                relayFinishLatch?.countDown()


                relayJobMap.relayJobFinished(relayOpts.relayNumber, jobInfo)
            } catch (e : InterruptedException) {
                logger.debug("Relay ${relayOpts.relayNumber} thread was interrupted")
                relayFinishLatch?.countDown()
            }
        })


        thread.start()
    }

    private fun openRelay(relayNum: Int, inverse : Boolean) {
        val signal = !inverse
        driver.signalToRelay(relayNum, signal)
    }

    /**
     * Алгоритм следующий: реле закрывается, если параллельно не происходит другого
     * алгоритма, в котором оно должно быть открыто
     */
    private fun closeRelayOnNoOtherJob(relayNum: Int, inverse: Boolean) {
        if(!anyJobWorkingOnRelay(relayNum)) {
            val signal = inverse
            driver.signalToRelay(relayNum, signal)
        }
    }

    private fun anyJobWorkingOnRelay(relayNum: Int): Boolean {
        val jobInfoList = relayJobMap.getAllRelayJobs(relayNum);

        return jobInfoList?.any { j -> j.jobStatus == RelayJobStatus.WORKING } ?: false
    }

    fun stopBySignal(signalNum: Int) {
        logger.info("Received stop signal $signalNum")

        val jobInfoList : List<RelayJobInfo> = relayJobMap.currentJobsForStopSignal(signalNum)

        jobInfoList.forEach {
            it.thread.interrupt()

            val relayNum = it.jobOptions.relayNumber;
            closeRelayOnNoOtherJob(relayNum, it.jobOptions.inverse)
            logger.info("Stopped relay $relayNum on signal $signalNum")
        }
        relayJobMap.removeAllJobs(jobInfoList)

    }


}