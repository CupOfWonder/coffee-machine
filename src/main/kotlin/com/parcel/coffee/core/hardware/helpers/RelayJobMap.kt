package com.parcel.coffee.core.hardware.helpers

import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import java.util.concurrent.ConcurrentHashMap

class RelayJobMap {
    private val jobMap = ConcurrentHashMap<Int, MutableList<RelayJobInfo>>()

    fun rememberJobStarted(relayNum: Int, thread: Thread, jobOptions: RelayJobOptions): RelayJobInfo {
        var list = jobMap[relayNum]

        if(list == null) {
            list = ArrayList()
            jobMap[relayNum] = list
        }

        val jobInfo = RelayJobInfo(thread, jobOptions, RelayJobStatus.WAITING_FOR_START)
        list.add(jobInfo)

        return jobInfo;
    }

    fun relayJobFinished(relayNum: Int, jobInfo: RelayJobInfo) {
        val list = jobMap[relayNum]

        list?.remove(jobInfo)
    }

    fun removeAllJobs(jobsToRemove : List<RelayJobInfo>) {
        for(jobList in jobMap.values) {
            jobList.removeAll(jobsToRemove)
        }
    }

    fun currentJobsForStopSignal(signalNum: Int): List<RelayJobInfo> {
        var result = ArrayList<RelayJobInfo>()
        for(jobList in jobMap.values) {
            jobList
                    .filter { it.jobOptions.stopSignal == signalNum }
                    .forEach {result.add(it) }
        }

        return result;
    }

    fun getAllRelayJobs(relayNum: Int): MutableList<RelayJobInfo>? {
        return jobMap[relayNum]
    }

}