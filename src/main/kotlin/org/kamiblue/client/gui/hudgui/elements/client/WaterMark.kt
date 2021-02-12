package org.kamiblue.client.gui.hudgui.elements.client

import org.kamiblue.client.KamiBlueMod
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.module.modules.client.Capes
import org.kamiblue.client.util.graphics.VertexHelper
import org.lwjgl.opengl.GL11.glScalef

object WaterMark : LabelHud(
    name = "Watermark",
    category = Category.CLIENT,
    description = "KAMI Blue watermark",
    enabledByDefault = true
) {

    override val hudWidth: Float get() = (displayText.getWidth() + 2.0f) / scale
    override val hudHeight: Float get() = displayText.getHeight(2) / scale

    override val closeable: Boolean get() = Capes.isPremium

    override fun onGuiInit() {
        super.onGuiInit()
        visible = visible
    }

    override fun SafeClientEvent.updateText() {
        displayText.add(KamiBlueMod.NAME, primaryColor)
        displayText.add(KamiBlueMod.VERSION_SIMPLE, secondaryColor)
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        val reversedScale = 1.0f / scale
        glScalef(reversedScale, reversedScale, reversedScale)
        super.renderHud(vertexHelper)
    }

    init {
        posX = 0.0f
        posY = 0.0f
    }
}