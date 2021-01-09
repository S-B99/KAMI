package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils

object SafeWalk : Module(
    category = Category.MOVEMENT,
) {
    private val baritoneCompat = setting(getTranslationKey("BaritoneCompatibility"), true)

    fun shouldSafewalk(): Boolean {
        return isEnabled && (baritoneCompat.value && BaritoneUtils.primary?.customGoalProcess?.goal == null || !baritoneCompat.value)
    }
}