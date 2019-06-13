package com.parcel.coffee.core.hardware.helpers

import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

class RelayJobMap {
    private val jobMap = ConcurrentHashMap<Int, MutableList<RelayJobInfo>>()

    fun rememberRelayThread(relayNum: Int, thread: Thread, jobOptions: RelayJobOptions, relayFinishLatch: CountDownLatch?) {
        var list = jobMap[relayNum]

        if(list == null) {
            list = ArrayList()
            jobMap[relayNum] = list
        }

        val jobInfo = RelayJobInfo(thread, jobOptions, relayFinishLatch)
        list.add(jobInfo)
    }

    fun relayThreadFinished(relayNum: Int, thread: Thread) {
        var list = jobMap[relayNum]

        list?.let {
            list.removeIf { info -> info.thread == thread}
        }
    }

    fun removeAllJobs(jobsToRemove : List<RelayJobInfo>) {
        for(jobList in jobMap.values) {
            jobList.removeAll(jobsToRemove)
        }
    }

    fun currentJobsForSignalNumber(signalNum: Int): List<RelayJobInfo> {
        var result = ArrayList<RelayJobInfo>()
        for(jobList in jobMap.values) {
            jobList
                    .filter { it.jobOptions.stopSignal == signalNum }
                    .forEach {result.add(it) }
        }

        return result;
    }

}