package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.StrafeUtils
import kotlin.math.*

/**
 * Created july 14th 2020 by historian
 */
@Module.Info(
        name = "Strafe",
        category = Module.Category.MOVEMENT,
        description = "Improves control in air"
)
class Strafe : Module() {
    private val airSpeedBoost = register(Settings.b("AirSpeedBoost", true))
    private val timerBoost = register(Settings.b("TimerBoost", false))
    private val autoJump = register(Settings.b("AutoJump", true))

    private var jumpTicks = 0

    /* if you skid this you omega gay */
    override fun onUpdate() {
        if(mc.player.moveForward != 0F || mc.player.moveStrafing != 0F) {
            StrafeUtils.setSpeed(StrafeUtils.getSpeed())
            if(airSpeedBoost.value)mc.player.jumpMovementFactor = 0.029F
            if(timerBoost.value)mc.timer.tickLength = 50 / 1.09F
            if(autoJump.value && mc.player.onGround && jumpTicks == 0) {
                mc.player.motionY = 0.41
                if(mc.player.isSprinting) {
                    mc.player.motionX -= sin(StrafeUtils.getMoveYaw()) * 0.2
                    mc.player.motionZ += cos(StrafeUtils.getMoveYaw()) * 0.2
                }
                mc.player.isAirBorne = true
                jumpTicks = 5
            }
            if(jumpTicks > 0)jumpTicks--
        }
    }

    override fun onDisable() {
        mc.player.jumpMovementFactor = 0.02F
        mc.timer.tickLength = 50F
        jumpTicks = 0
    }
}
