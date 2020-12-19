package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager.colorFormatValue
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.setting.SettingsRegister
import me.zeroeightsix.kami.setting.builder.SettingBuilder
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.text.TextFormatting

object BindCommand : ClientCommand(
    name = "bind",
    description = "Bind and unbind modules"
) {
    val modifiersEnabled: Setting<Boolean> = SettingBuilder.register(Settings.b("modifiersEnabled", false), "binds")

    init {
        SettingsRegister.register("modifiersEnabled", modifiersEnabled)

        literal("list") {
            execute("List used module binds") {
                val modules = ModuleManager.getModules().filter { it.bind.value.key > 0 }.sortedBy { it.bindName }

                MessageSendHelper.sendChatMessage("Used binds: ${modules.size.colorFormatValue}")
                modules.forEach {
                    MessageSendHelper.sendRawChatMessage("${it.bindName.colorFormatValue} ${it.name}")
                }
            }
        }

        literal("reset", "unbind") {
            module("module") { moduleArg ->
                execute("Reset the bind of a module to nothing") {
                    moduleArg.value.bind.resetValue()
                    MessageSendHelper.sendChatMessage("Reset bind for ${moduleArg.name}!")
                }
            }
        }

        literal("modifiers") {
            boolean("enabled") { modifiersArg ->
                execute("Disallow binds while holding a modifier") {
                    modifiersEnabled.value = modifiersArg.value
                    MessageSendHelper.sendChatMessage(
                        "Modifiers ${if (modifiersArg.value) " ${TextFormatting.GREEN}enabled" else " ${TextFormatting.RED}disabled"}"
                    )
                }
            }
        }

        module("module") { moduleArg ->
            string("bind") { bindArg ->
                execute("Bind a module to a key") {
                    val module = moduleArg.value
                    val bind = bindArg.value

                    if (bind.equals("none", true)) {
                        module.bind.resetValue()
                        MessageSendHelper.sendChatMessage("Reset bind for ${module.name}!")
                        return@execute
                    }

                    val key = Wrapper.getKey(bind)

                    if (key == 0) {
                        Wrapper.sendUnknownKeyError(bind)
                    } else {
                        module.bind.value.key = key
                        MessageSendHelper.sendChatMessage("Bind for ${module.name} set to ${module.bindName.colorFormatValue}!")
                    }
                }
            }

            execute("Get the bind of a module") {
                MessageSendHelper.sendChatMessage("${moduleArg.value.name} is bound to ${moduleArg.value.bindName.colorFormatValue}")
            }
        }
    }
}