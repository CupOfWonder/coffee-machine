package com.parcel.coffee.core.hardware.helpers

import com.parcel.coffee.core.hardware.options.data.RelayJobOptions
import java.util.concurrent.CountDownLatch

/**
 * Информация о том что текущее реле работает по текущей программе в данном потоке
 */
class RelayJobInfo(
    val thread: Thread,
    val jobOptions: RelayJobOptions,
    val countDownLatch: CountDownLatch?
)