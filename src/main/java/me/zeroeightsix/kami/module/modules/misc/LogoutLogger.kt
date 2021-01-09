package me.zeroeightsix.kami.module.modules.misc

import com.mojang.authlib.GameProfile
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.flooredPosition
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.math.CoordinateConverter.asString
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.onMainThread
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object LogoutLogger : Module(
    category = Category.MISC,
) {
    private val saveToFile = setting(getTranslationKey("SaveToFile"), true)
    private val print = setting(getTranslationKey("PrintToChat"), true)

    private val loggedPlayers = HashMap<GameProfile, BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        listener<ConnectionEvent.Disconnect> {
            onMainThread {
                loggedPlayers.clear()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            for (loadedPlayer in world.playerEntities) {
                if (loadedPlayer !is EntityOtherPlayerMP) continue

                val info = connection.getPlayerInfo(loadedPlayer.gameProfile.id)
                loggedPlayers[info.gameProfile] = loadedPlayer.flooredPosition
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<GameProfile>()

                for ((profile, pos) in loggedPlayers) {
                    if (print.value) MessageSendHelper.sendChatMessage("${profile.name} logged out at ${pos.asString()}")
                    if (saveToFile.value) WaypointManager.add(pos, "${profile.name} Logout Spot")
                    toRemove.add(profile)
                }

                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}