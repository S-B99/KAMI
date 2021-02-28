package org.kamiblue.client.module.modules.combat

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.combat.SurroundUtils
import org.kamiblue.client.util.combat.SurroundUtils.checkHole
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.threads.safeListener

@CombatManager.CombatModule
internal object Anchor : Module(
    name = "Anchor",
    description = "Automatically stops when you are above hole",
    category = Category.COMBAT,
    modulePriority = 10
) {
    private val vRange by setting("vRange", 5, 1..10, 1)
    private val mode by setting("Mode", AnchorMode.BOTH)
    private val turnOffAfter by setting("TurnOffAfter", false)
    private val pitchTrigger by setting("PitchTrigger", false)
    private val pitch by setting("Pitch", 80, -90..90, 1, { pitchTrigger })
    private val hard by setting("Hard", true)
    private var prevInHole = false
    private var disableInput = false

    private enum class AnchorMode {
        BOTH, BEDROCK
    }


    /**
     * Checks whether the specified block position is a hole or not
     */
    private fun checkHole(event: SafeClientEvent, pos: BlockPos): Boolean {
        val type = event.checkHole(pos)
        return ((mode == AnchorMode.BOTH && type != SurroundUtils.HoleType.NONE) ||
            (mode == AnchorMode.BEDROCK && type == SurroundUtils.HoleType.BEDROCK))
    }

    /**
     * Checks whether the player should stop movement or not
     */
    private fun shouldStop(event: SafeClientEvent): Boolean {
        if (pitchTrigger && mc.player.rotationPitch < MathHelper.wrapDegrees(pitch)) return false
        for (dy in 1..vRange) {
            val pos = mc.player.positionVector.toBlockPos().add(0.0, -dy.toDouble(), 0.0)
            if (checkHole(event, pos)) {
                return true
            }
        }
        return false
    }

    init {
        onDisable {
            prevInHole = false
        }

        safeListener<PlayerTravelEvent> {
            if (checkHole(this, player.positionVector.toBlockPos()) &&
                !world.isAirBlock(player.positionVector.add(0.0, -0.1, 0.0).toBlockPos())) {
                prevInHole = true
                disableInput = false
                if (turnOffAfter)
                    disable()
                return@safeListener
            }
            for (dy in -vRange..0) {
                if (!shouldStop(this)) {
                    prevInHole = false
                    disableInput = false
                    return@safeListener
                }
                if (prevInHole) return@safeListener
                disableInput = true

                SurroundUtils.centerPlayer(hard)
                if (hard) {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
        }
    }
}