package org.kamiblue.client.manager

import kotlinx.coroutines.Deferred
import org.kamiblue.client.AsyncLoader
import org.kamiblue.client.KamiBlueMod
import org.kamiblue.client.event.KamiEventBus
import org.kamiblue.client.util.StopTimer
import org.kamiblue.commons.utils.ClassUtils

internal object ManagerLoader : AsyncLoader<List<Class<out Manager>>> {
    override var deferred: Deferred<List<Class<out Manager>>>? = null

    override fun preLoad0(): List<Class<out Manager>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses("org.kamiblue.client.manager.managers", Manager::class.java)
        val time = stopTimer.stop()

        KamiBlueMod.LOG.info("${list.size} managers found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out Manager>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            ClassUtils.getInstance(clazz).also { KamiEventBus.subscribe(it) }
        }

        val time = stopTimer.stop()
        KamiBlueMod.LOG.info("${input.size} managers loaded, took ${time}ms")
    }
}