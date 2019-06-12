package com.parcel.coffee.core.hardware.helpers

import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

class RelayJobMap {
    private val jobMap = ConcurrentHashMap<Int, MutableList<RelayJobInfo>>()

    fun rememberRelayThread(relayNum: Int, thread: Thread, jobOptions: RelayJobOptions, relayFinishLatch: CountDownLatch?) {
        var list = jobMap[relayNum] ?: ArrayList()
        val jobInfo = RelayJobInfo(thread, jobOptions, relayFinishLatch)
        list.add(jobInfo)
    }

    fun relayThreadFinished(relayNum: Int, thread: Thread) {
        var list = jobMap[relayNum]

        list?.let {
            list.removeIf { info -> info.thread == thread}
        }
    }

    fun currentJobsForRelayNumber(relayNum: Int) : List<RelayJobInfo> {
        return jobMap[relayNum] ?: emptyList()
    }

    fun removeAllJobs(relayNum: Int) {
        jobMap.remove(relayNum)
    }

}