package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntityClosest
import net.minecraft.init.Items

@CombatManager.CombatModule
@Module.Info(
        name = "AimBot",
        description = "Automatically aims at entities for you.",
        category = Module.Category.COMBAT,
        modulePriority = 20
)
object AimBot : Module() {
    private val bowOnly = setting("BowOnly", true)
    private val autoSwap = setting("AutoSwap", false, { bowOnly.value })

    init {
        listener<SafeTickEvent> {
            if (bowOnly.value && mc.player.heldItemMainhand.getItem() != Items.BOW) {
                if (autoSwap.value) InventoryUtils.swapSlotToItem(261)
                return@listener
            }
            CombatManager.target?.let {
                faceEntityClosest(it)
            }
        }
    }
}