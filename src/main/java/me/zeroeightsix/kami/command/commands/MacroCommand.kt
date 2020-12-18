package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager.colorFormatValue
import me.zeroeightsix.kami.manager.managers.MacroManager
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper

object MacroCommand : ClientCommand(
    name = "macro",
    alias = arrayOf("m"),
    description = "Manage your command / message macros"
) {
    init {
        literal("list") {
            string("key") { keyArg ->
                execute("List macros for a key") {
                    val key = Wrapper.getKey(keyArg.value)

                    if (key < 1) {
                        Wrapper.sendUnknownKeyError(keyArg.value)
                        return@execute
                    }

                    val macros = MacroManager.macros.filter { it.key == key }
                    val formattedName = Wrapper.getKeyName(key).colorFormatValue

                    if (macros.isEmpty()) {
                        MessageSendHelper.sendChatMessage("&cYou have no macros for the key $formattedName")
                    } else {
                        MessageSendHelper.sendChatMessage("You have has the following macros for $formattedName: ")
                        for ((_, value) in macros) {
                            MessageSendHelper.sendRawChatMessage("$formattedName $value")
                        }
                    }
                }
            }

            execute("List all macros") {
                if (MacroManager.isEmpty) {
                    MessageSendHelper.sendChatMessage("&cYou have no macros")
                } else {
                    MessageSendHelper.sendChatMessage("You have the following macros: ")
                    for ((key, value) in MacroManager.macros.entries.sortedBy { it.key }) {
                        MessageSendHelper.sendRawChatMessage("${Wrapper.getKeyName(key).colorFormatValue} $value")
                    }
                }

            }
        }

        literal("clear") {
            string("key") { keyArg ->
                execute("Clear macros for a key") {
                    val key = Wrapper.getKey(keyArg.value)

                    if (key < 1) {
                        Wrapper.sendUnknownKeyError(keyArg.value)
                        return@execute
                    }

                    MacroManager.removeMacro(key)
                    MacroManager.saveMacros()
                    MacroManager.loadMacros()
                    MessageSendHelper.sendChatMessage("Cleared macros for ${Wrapper.getKeyName(key).colorFormatValue}")
                }
            }
        }

        string("key") { keyArg ->
            greedy("command / message") { greedyArg ->
                execute("Set a command / message for a key") {
                    val key = Wrapper.getKey(keyArg.value)

                    if (key < 1) {
                        Wrapper.sendUnknownKeyError(keyArg.value)
                        return@execute
                    }

                    MacroManager.addMacroToKey(key, greedyArg.value)
                    MacroManager.saveMacros()
                    MessageSendHelper.sendChatMessage("Added macro ${greedyArg.value.colorFormatValue} for key " +
                        Wrapper.getKeyName(key).colorFormatValue)
                }
            }
        }
    }
}