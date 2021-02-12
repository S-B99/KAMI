package org.kamiblue.client.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.KamiBlueMod
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.util.ConfigUtils
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object MacroManager : Manager {
    private var macroMap = LinkedHashMap<Int, ArrayList<String>>()
    val isEmpty get() = macroMap.isEmpty()
    val macros: Map<Int, List<String>> get() = macroMap

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<LinkedHashMap<Int, List<String>>>() {}.type
    private val file get() = File(KamiBlueMod.DIRECTORY + "macros.json")

    init {
        listener<InputEvent.KeyInputEvent> {
            sendMacro(Keyboard.getEventKey())
        }
    }

    fun loadMacros(): Boolean {
        ConfigUtils.fixEmptyJson(file)

        return try {
            FileReader(file).buffered().use {
                macroMap = gson.fromJson(it, type)
            }
            KamiBlueMod.LOG.info("Macro loaded")
            true
        } catch (e: Exception) {
            KamiBlueMod.LOG.warn("Failed loading macro", e)
            false
        }
    }

    fun saveMacros(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(macroMap, it)
            }
            KamiBlueMod.LOG.info("Macro saved")
            true
        } catch (e: Exception) {
            KamiBlueMod.LOG.warn("Failed saving macro", e)
            false
        }
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keyCode int keycode of the key the was pressed
     */
    private fun sendMacro(keyCode: Int) {
        val macros = getMacros(keyCode) ?: return
        for (macro in macros) {
            if (macro.startsWith(CommandManager.prefix)) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
                MessageSendHelper.sendKamiCommand(macro) // ie, the false here
            } else {
                MessageManager.sendMessageDirect(macro)
            }
        }
    }

    fun getMacros(keycode: Int) = macroMap[keycode]

    fun addMacroToKey(keycode: Int, macro: String) {
        macroMap.getOrPut(keycode, ::ArrayList).add(macro)
    }

    fun removeMacro(keycode: Int) {
        macroMap.remove(keycode)
    }

}