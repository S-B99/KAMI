package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.inventory.ClickType
import java.util.*

object PlayerInventoryManager : Manager() {
    private val timer = TimerUtils.TickTimer()
    private val actionQueue = TreeSet<InventoryTask>(Comparator.reverseOrder())
    private val lockObject = Any()
    private var currentId = 0

    init {
        listener<RenderOverlayEvent>(0) {
            if (!timer.tick((1000.0f / LagCompensator.tickRate).toLong())) return@listener

            synchronized(lockObject) {
                actionQueue.removeIf { it.isDone }
                actionQueue.firstOrNull()
            }?.nextInfo()?.let {
                InventoryUtils.inventoryClick(it.windowId, it.slot, it.mouseButton, it.type)
            }

            if (actionQueue.isEmpty()) currentId = 0
        }

        listener<ConnectionEvent.Disconnect> {
            actionQueue.clear()
            currentId = 0
        }
    }

    /**
     * Adds a new task to the inventory manager
     *
     * @param clickInfo group of the click info in this task
     *
     * @return [TaskState] representing the state of this task
     */
    fun Module.addInventoryTask(vararg clickInfo: ClickInfo) =
            InventoryTask(currentId++, modulePriority, clickInfo).let {
                actionQueue.add(it)
                it.taskState
            }

    private data class InventoryTask(
            private val id: Int,
            private val priority: Int,
            private val infoArray: Array<out ClickInfo>,
            val taskState: TaskState = TaskState(),
            private var index: Int = 0
    ) : Comparable<InventoryTask> {
        val isDone get() = taskState.done

        fun nextInfo() =
                infoArray.getOrNull(index++).also {
                    if (it == null) taskState.done = true
                }


        override fun compareTo(other: InventoryTask): Int {
            val result = priority - other.priority
            return if (result != 0) result
            else other.id - id
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is InventoryTask) return false

            if (!infoArray.contentEquals(other.infoArray)) return false
            if (index != other.index) return false

            return true
        }

        override fun hashCode() = 31 * infoArray.contentHashCode() + index

    }

    data class ClickInfo(val windowId: Int = 0, val slot: Int, val mouseButton: Int = 0, val type: ClickType)

    data class TaskState(var done: Boolean = false)
}