package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TpsCalculator
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.isWeapon
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
object KillAura : Module(
    alias = arrayOf("KA", "Aura", "TriggerBot"),
    category = Category.COMBAT,
    modulePriority = 50
) {
    private val delayMode = setting(getTranslationKey("Mode"), WaitMode.DELAY)
    private val lockView = setting(getTranslationKey("LockView"), false)
    private val spoofRotation = setting(getTranslationKey("SpoofRotation"), true, { !lockView.value })
    private val waitTick = setting(getTranslationKey("SpamDelay"), 2.0f, 1.0f..40.0f, 0.5f, { delayMode.value == WaitMode.SPAM })
    val range = setting(getTranslationKey("Range"), 5f, 0f..8f, 0.25f)
    private val tpsSync = setting(getTranslationKey("TPSSync"), false)
    private val autoWeapon = setting(getTranslationKey("AutoWeapon"), true)
    private val weaponOnly = setting(getTranslationKey("WeaponOnly"), true)
    private val prefer = setting(getTranslationKey("Prefer"), CombatUtils.PreferWeapon.SWORD, { autoWeapon.value })
    private val disableOnDeath = setting(getTranslationKey("DisableOnDeath"), false)

    private var inactiveTicks = 0
    private var tickCount = 0

    private enum class WaitMode {
        DELAY, SPAM
    }

    override fun isActive(): Boolean {
        return inactiveTicks <= 20 && isEnabled
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            inactiveTicks++

            if (player.isDead) {
                if (disableOnDeath.value) disable()
                return@safeListener
            }

            if (!CombatManager.isOnTopPriority(KillAura) || CombatSetting.pause) return@safeListener
            val target = CombatManager.target ?: return@safeListener
            if (player.getDistance(target) > range.value) return@safeListener

            if (autoWeapon.value) {
                CombatUtils.equipBestWeapon(prefer.value)
            }

            if (weaponOnly.value && !player.heldItemMainhand.item.isWeapon) {
                return@safeListener
            }

            inactiveTicks = 0
            rotate(target)
            if (canAttack()) attack(target)
        }
    }

    private fun rotate(target: EntityLivingBase) {
        if (lockView.value) {
            RotationUtils.faceEntityClosest(target)
        } else if (spoofRotation.value) {
            val rotation = RotationUtils.getRotationToEntityClosest(target)
            PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation))
        }
    }

    private fun SafeClientEvent.canAttack(): Boolean {
        return if (delayMode.value == WaitMode.DELAY) {
            val adjustTicks = if (!tpsSync.value) 0f else TpsCalculator.adjustTicks
            player.getCooledAttackStrength(adjustTicks) >= 1f
        } else {
            if (tickCount < waitTick.value) {
                tickCount++
                false
            } else {
                tickCount = 0
                true
            }
        }
    }

    private fun SafeClientEvent.attack(e: Entity) {
        playerController.attackEntity(player, e)
        player.swingArm(EnumHand.MAIN_HAND)
    }
}