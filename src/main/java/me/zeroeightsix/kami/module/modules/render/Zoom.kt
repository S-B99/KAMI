package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object Zoom : Module(
    category = Category.RENDER,
    showOnArray = false
) {
    private var fov = 0f
    private var sensi = 0f

    private val fovChange = setting(getTranslationKey("FOV"), 40.0f, 1.0f..180.0f, 0.5f)
    private val modifySensitivity = setting(getTranslationKey("ModifySensitivity"), true)
    private val sensitivityMultiplier = setting(getTranslationKey("SensitivityMultiplier"), 1.0f, 0.25f..2.0f, 0.25f, { modifySensitivity.value })
    private val smoothCamera = setting(getTranslationKey("CinematicCamera"), false)

    init {
        onEnable {
            fov = mc.gameSettings.fovSetting
            sensi = mc.gameSettings.mouseSensitivity

            mc.gameSettings.fovSetting = fovChange.value
            if (modifySensitivity.value) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
            mc.gameSettings.smoothCamera = smoothCamera.value
        }

        onDisable {
            mc.gameSettings.fovSetting = fov
            mc.gameSettings.mouseSensitivity = sensi
            mc.gameSettings.smoothCamera = false
        }

        fovChange.listeners.add {
            if (isEnabled) mc.gameSettings.fovSetting = fovChange.value
        }
        modifySensitivity.listeners.add {
            if (isEnabled) if (modifySensitivity.value) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
            else mc.gameSettings.mouseSensitivity = sensi
        }
        sensitivityMultiplier.listeners.add {
            if (isEnabled) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
        }
        smoothCamera.listeners.add {
            if (isEnabled) mc.gameSettings.smoothCamera = smoothCamera.value
        }
    }
}