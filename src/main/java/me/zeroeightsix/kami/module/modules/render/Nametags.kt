package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EnchantmentUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.color.ColorGradient
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.*
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt

@Module.Info(
        name = "Nametags",
        description = "Draws descriptive nametags above entities",
        category = Module.Category.RENDER
)
object Nametags : Module() {
    private val page = register(Settings.e<Page>("Page", Page.ENTITY_TYPE))

    /* Entity type settings */
    private val self = register(Settings.booleanBuilder("Self").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE }.build())
    private val experience = register(Settings.booleanBuilder("Experience").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE })
    private val items = register(Settings.booleanBuilder("Items").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val players = register(Settings.booleanBuilder("Players").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val mobs = register(Settings.booleanBuilder("Mobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE && mobs.value })
    private val invisible = register(Settings.booleanBuilder("Invisible").withValue(true).withVisibility { page.value == Page.ENTITY_TYPE })
    private val range = register(Settings.integerBuilder("Range").withValue(64).withRange(0, 128).withStep(4).withVisibility { page.value == Page.ENTITY_TYPE })

    /* Content */
    private val line1left = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line1Left").withValue(ContentType.NONE).withVisibility { page.value == Page.CONTENT })
    private val line1center = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line1Center").withValue(ContentType.NONE).withVisibility { page.value == Page.CONTENT })
    private val line1right = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line1Right").withValue(ContentType.NONE).withVisibility { page.value == Page.CONTENT })
    private val line2left = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line2Left").withValue(ContentType.NAME).withVisibility { page.value == Page.CONTENT })
    private val line2center = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line2enter").withValue(ContentType.PING).withVisibility { page.value == Page.CONTENT })
    private val line2right = register(Settings.enumBuilder<ContentType>(ContentType::class.java, "Line2Right").withValue(ContentType.TOTAL_HP).withVisibility { page.value == Page.CONTENT })
    private val itemCount = register(Settings.booleanBuilder("ItemCount").withValue(true).withVisibility { page.value == Page.CONTENT && items.value })
    private val maxItems = register(Settings.integerBuilder("MaxItems").withValue(5).withRange(2, 16).withStep(1).withVisibility { page.value == Page.CONTENT && items.value })

    /* Item */
    private val mainHand = register(Settings.booleanBuilder("MainHand").withValue(true).withVisibility { page.value == Page.ITEM })
    private val offhand = register(Settings.booleanBuilder("OffHand").withValue(true).withVisibility { page.value == Page.ITEM })
    private val invertHand = register(Settings.booleanBuilder("InvertHand").withValue(false).withVisibility { page.value == Page.ITEM && (mainHand.value || offhand.value) })
    private val armor = register(Settings.booleanBuilder("Armor").withValue(true).withVisibility { page.value == Page.ITEM })
    private val count = register(Settings.booleanBuilder("Count").withValue(true).withVisibility { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val dura = register(Settings.booleanBuilder("Dura").withValue(true).withVisibility { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val enchantment = register(Settings.booleanBuilder("Enchantment").withValue(true).withVisibility { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val itemScale = register(Settings.floatBuilder("ItemScale").withValue(1f).withRange(0.25f, 2f).withStep(0.25f).withVisibility { page.value == Page.ITEM })

    /* Frame */
    private val nameFrame = register(Settings.booleanBuilder("NameFrame").withValue(true).withVisibility { page.value == Page.FRAME })
    private val itemFrame = register(Settings.booleanBuilder("ItemFrame").withValue(false).withVisibility { page.value == Page.FRAME })
    private val filled = register(Settings.booleanBuilder("Filled").withValue(true).withVisibility { page.value == Page.FRAME })
    private val rFilled = register(Settings.integerBuilder("FilledRed").withValue(39).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && filled.value })
    private val gFilled = register(Settings.integerBuilder("FilledGreen").withValue(36).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && filled.value })
    private val bFilled = register(Settings.integerBuilder("FilledBlue").withValue(64).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && filled.value })
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withValue(200).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && filled.value })
    private val outline = register(Settings.booleanBuilder("Outline").withValue(true).withVisibility { page.value == Page.FRAME })
    private val rOutline = register(Settings.integerBuilder("OutlineRed").withValue(155).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && outline.value })
    private val gOutline = register(Settings.integerBuilder("OutlineGreen").withValue(144).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && outline.value })
    private val bOutline = register(Settings.integerBuilder("OutlineBlue").withValue(255).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && outline.value })
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withValue(240).withRange(0, 255).withStep(1).withVisibility { page.value == Page.FRAME && outline.value })
    private val outlineWidth = register(Settings.floatBuilder("OutlineWidth").withValue(2f).withRange(0f, 5f).withVisibility { page.value == Page.FRAME && outline.value })
    private val margins = register(Settings.floatBuilder("Margins").withValue(2f).withRange(0f, 10f).withVisibility { page.value == Page.FRAME })
    private val cornerRadius = register(Settings.floatBuilder("CornerRadius").withValue(2f).withRange(0f, 10f).withVisibility { page.value == Page.FRAME })

    /* Rendering settings */
    private val rText = register(Settings.integerBuilder("TextRed").withValue(232).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val gText = register(Settings.integerBuilder("TextGreen").withValue(229).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val bText = register(Settings.integerBuilder("TextBlue").withValue(255).withRange(0, 255).withStep(1).withVisibility { page.value == Page.RENDERING })
    private val textShadow = register(Settings.booleanBuilder("TextShadow").withValue(true).withVisibility { page.value == Page.RENDERING })
    private val yOffset = register(Settings.floatBuilder("YOffset").withValue(0.5f).withRange(-2.5f, 2.5f).withStep(0.05f).withVisibility { page.value == Page.RENDERING })
    private val scale = register(Settings.floatBuilder("Scale").withValue(1f).withRange(0.25f, 5f).withStep(0.25f).withVisibility { page.value == Page.RENDERING })
    private val distScaleFactor = register(Settings.floatBuilder("DistanceScaleFactor").withValue(0.0f).withRange(0f, 1f).withVisibility { page.value == Page.RENDERING })
    private val minDistScale = register(Settings.floatBuilder("MinDistanceScale").withValue(0.35f).withRange(0f, 1f).withVisibility { page.value == Page.RENDERING })

    private enum class Page {
        ENTITY_TYPE, CONTENT, ITEM, FRAME, RENDERING
    }

    private enum class ContentType {
        NONE, NAME, TYPE, TOTAL_HP, HP, ABSORPTION, PING, DISTANCE, TOTEM_POPS
    }

    private val pingColorGradient = ColorGradient(
            0f to ColorHolder(101, 101, 101),
            0.1f to ColorHolder(20, 232, 20),
            20f to ColorHolder(20, 232, 20),
            150f to ColorHolder(20, 232, 20),
            300f to ColorHolder(150, 0, 0)
    )

    private val healthColorGradient = ColorGradient(
            0f to ColorHolder(180, 20, 20),
            50f to ColorHolder(240, 220, 20),
            100f to ColorHolder(20, 232, 20)
    )

    private val line1Settings = arrayOf(line1left, line1center, line1right)
    private val line2Settings = arrayOf(line2left, line2center, line2right)
    private val entityMap = TreeMap<Entity, TextComponent>(compareByDescending { mc.player.getPositionEyes(1f).distanceTo(it.getPositionEyes(1f)) })
    private val itemMap = TreeSet<ItemGroup>(compareByDescending { mc.player.getPositionEyes(1f).distanceTo(it.getCenter(1f)) })

    private var updateTick = 0
    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    override fun onRender() {
        if (entityMap.isEmpty() && itemMap.isEmpty()) return
        GlStateUtils.rescaleActual()
        val camPos = getCamPos()
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        for ((entity, textComponent) in entityMap) {
            val pos = EntityUtils.getInterpolatedPos(entity, KamiTessellator.pTicks()).add(0.0, (entity.height + yOffset.value).toDouble(), 0.0)
            val screenPos = ProjectionUtils.toScreenPos(pos)
            val dist = camPos.distanceTo(pos).toFloat() * 0.2f
            val distFactor = if (distScaleFactor.value == 0f) 1f else max(1f / (dist * distScaleFactor.value + 1f), minDistScale.value)

            drawNametag(screenPos, (scale.value * 2f) * distFactor, vertexHelper, textComponent)
            drawItems(screenPos, (scale.value * 2f) * distFactor, vertexHelper, entity, textComponent)
        }
        for (itemGroup in itemMap) {
            val pos = itemGroup.getCenter(KamiTessellator.pTicks()).add(0.0, yOffset.value.toDouble(), 0.0)
            val screenPos = ProjectionUtils.toScreenPos(pos)
            val dist = camPos.distanceTo(pos).toFloat() * 0.2f
            val distFactor = if (distScaleFactor.value == 0f) 1f else max(1f / (dist * distScaleFactor.value + 1f), minDistScale.value)

            drawNametag(screenPos, (scale.value * 2f) * distFactor, vertexHelper, itemGroup.textComponent)
        }
        GlStateUtils.rescaleMc()
    }

    private fun getCamPos(): Vec3d {
        return EntityUtils.getInterpolatedPos(mc.renderViewEntity
                ?: mc.player, KamiTessellator.pTicks()).add(ActiveRenderInfo.getCameraPosition())
    }

    private fun drawNametag(screenPos: Vec3d, scale: Float, vertexHelper: VertexHelper, textComponent: TextComponent) {
        glPushMatrix()
        glTranslatef(screenPos.x.roundToInt() + 0.375f, screenPos.y.roundToInt() + 0.375f, 0f)
        glScalef(scale, scale, 1f)
        val halfWidth = textComponent.getWidth() / 2.0 + margins.value + 2.0
        val halfHeight = textComponent.getHeight(2, true) / 2.0 + margins.value + 2.0
        if (nameFrame.value) drawFrame(vertexHelper, Vec2d(-halfWidth - 0.5, -halfHeight), Vec2d(halfWidth - 0.5, halfHeight))
        textComponent.draw(drawShadow = textShadow.value, skipEmptyLine = true, horizontalAlign = TextComponent.HAlign.CENTER, verticalAlign = TextComponent.VAlign.CENTER)
        textComponent.draw(drawShadow = textShadow.value, skipEmptyLine = true, horizontalAlign = TextComponent.HAlign.CENTER, verticalAlign = TextComponent.VAlign.CENTER)
        glPopMatrix()
    }

    private fun drawItems(screenPos: Vec3d, nameTagScale: Float, vertexHelper: VertexHelper, entity: Entity, textComponent: TextComponent) {
        if (entity !is EntityLivingBase) return
        val itemList = ArrayList<Pair<ItemStack, TextComponent>>()

        getEnumHand(if (invertHand.value) EnumHandSide.RIGHT else EnumHandSide.LEFT)?.let { // Hand
            val itemStack = entity.getHeldItem(it)
            itemList.add(itemStack to getEnchantmentText(itemStack))
        }

        if (armor.value) for (armor in entity.armorInventoryList.reversed()) itemList.add(armor to getEnchantmentText(armor)) // Armor

        getEnumHand(if (invertHand.value) EnumHandSide.LEFT else EnumHandSide.RIGHT)?.let { // Hand
            val itemStack = entity.getHeldItem(it)
            itemList.add(itemStack to getEnchantmentText(itemStack))
        }

        if (itemList.isEmpty()) return
        val halfHeight = textComponent.getHeight(2, true) / 2.0 + margins.value + 2.0
        val halfWidth = (itemList.count { !it.first.isEmpty() } * 24) / 2f

        glPushMatrix()
        glTranslatef(screenPos.x.roundToInt() + 0.375f, screenPos.y.roundToInt() + 0.375f, 0f) // Translate to nametag pos
        glScalef(nameTagScale, nameTagScale, 1f) // Scale to nametag scale
        glTranslated(0.0, -ceil(halfHeight), 0.0) // Translate to top of nametag
        glScalef((itemScale.value * 2f) / nameTagScale, (itemScale.value * 2f) / nameTagScale, 1f) // Scale to item scale
        glTranslatef(0f, -4f, 0f)

        val drawDura = dura.value && itemList.firstOrNull { it.first.isItemStackDamageable } != null

        if (itemFrame.value) {
            glTranslatef(0f, -margins.value, 0f)
            val duraHeight = if (drawDura) mc.fontRenderer.FONT_HEIGHT / 2f + 2f else 0f
            val enchantmentHeight = if (enchantment.value) (itemList.map { it.second.getHeight(3) }.max() ?: 0) + 4 else 0
            val height = 16 + duraHeight + enchantmentHeight / 2f
            val posBegin = Vec2d(-halfWidth - margins.value.toDouble(), -height - margins.value.toDouble())
            val posEnd = Vec2d(halfWidth + margins.value.toDouble(), margins.value.toDouble())
            drawFrame(vertexHelper, posBegin, posEnd)
        }

        glTranslatef(-halfWidth + 4f, -16f, 0f)
        if (drawDura) glTranslatef(0f, mc.fontRenderer.FONT_HEIGHT / -2f - 2f, 0f)
        RenderHelper.enableGUIStandardItemLighting()

        for ((itemStack, enchantmentText) in itemList) {
            if (itemStack.isEmpty()) continue
            GlStateUtils.blend(true)
            mc.renderItem.zLevel = -911f
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0)
            mc.renderItem.zLevel = 0f
            glColor4f(1f, 1f, 1f, 1f)

            if (drawDura && itemStack.isItemStackDamageable) {
                glPushMatrix()
                glTranslatef(8f, 17f, 0f)
                glScalef(0.5f, 0.5f, 1f)
                val duraPercentage = 100f - (itemStack.itemDamage.toFloat() / itemStack.maxDamage.toFloat()) * 100f
                val color = healthColorGradient.get(duraPercentage).toHex()
                val text = duraPercentage.roundToInt().toString()
                val textWidth = mc.fontRenderer.getStringWidth(text)
                mc.fontRenderer.drawString(text, -round(textWidth / 2f), 0f, color, textShadow.value)
                glPopMatrix()
            }

            if (count.value && itemStack.count > 1) {
                val itemCount = itemStack.count.toString()
                mc.fontRenderer.drawStringWithShadow(itemCount, 17f - mc.fontRenderer.getStringWidth(itemCount), 9f, 0xFFFFFF)
            }

            glTranslatef(0f, -2f, 0f)
            if (enchantment.value) enchantmentText.draw(scale = 0.5f, lineSpace = 3, verticalAlign = TextComponent.VAlign.BOTTOM)

            glTranslatef(24f, 2f, 0f)
        }
        glColor4f(1f, 1f, 1f, 1f)

        RenderHelper.disableStandardItemLighting()
        glPopMatrix()
    }

    private fun getEnchantmentText(itemStack: ItemStack): TextComponent {
        val textComponent = TextComponent()
        val enchantmentList = EnchantmentUtils.getAllEnchantments(itemStack)
        for (leveledEnchantment in enchantmentList) {
            textComponent.add(leveledEnchantment.alias)
            textComponent.addLine(leveledEnchantment.levelText, 0x9B90FF)
        }
        return textComponent
    }

    private fun getEnumHand(enumHandSide: EnumHandSide) =
            if (mc.gameSettings.mainHand == enumHandSide && mainHand.value) EnumHand.MAIN_HAND
            else if (mc.gameSettings.mainHand != enumHandSide && offhand.value) EnumHand.OFF_HAND
            else null

    private fun drawFrame(vertexHelper: VertexHelper, posBegin: Vec2d, posEnd: Vec2d) {
        if (cornerRadius.value == 0f) {
            if (filled.value)
                RenderUtils2D.drawRectFilled(vertexHelper, posBegin, posEnd, ColorHolder(rFilled.value, gFilled.value, bFilled.value, aFilled.value))
            if (outline.value && outlineWidth.value != 0f)
                RenderUtils2D.drawRectOutline(vertexHelper, posBegin, posEnd, outlineWidth.value, ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value))
        } else {
            if (filled.value)
                RenderUtils2D.drawRoundedRectFilled(vertexHelper, posBegin, posEnd, cornerRadius.value.toDouble(), 8, ColorHolder(rFilled.value, gFilled.value, bFilled.value, aFilled.value))
            if (outline.value && outlineWidth.value != 0f)
                RenderUtils2D.drawRoundedRectOutline(vertexHelper, posBegin, posEnd, cornerRadius.value.toDouble(), 8, outlineWidth.value, ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value))
        }
    }

    override fun onUpdate() {
        if (timer.tick(5L) && checkSetting()) MessageSendHelper.sendChatMessage("$chatName Totem pops is not yet implemented")

        // Updating stuff in different ticks to avoid overloading
        when (updateTick) {
            0 -> { // Adding items
                if (!items.value) {
                    itemMap.clear()
                } else {
                    loop@ for (entity in mc.world.loadedEntityList) {
                        if (entity.ticksExisted < 1) continue // To avoid stupid duplicated entity
                        if (entity !is EntityItem) continue
                        if (mc.player.getDistance(entity) > range.value) continue
                        for (itemGroup in itemMap) {
                            if (itemGroup.contains(entity)) continue@loop // If we have this item already in the groups then we skip it
                        }
                        for (itemGroup in itemMap) {
                            if (itemGroup.add(entity)) continue@loop // If we add the item to any of the group successfully then we continue
                        }
                        ItemGroup().apply { add(entity) }.also { itemMap.add(it) } // If we can't find an existing group then we make a new one and add it to the map
                    }
                }
            }
            1 -> { // Adding Entity
                for (entity in mc.world.loadedEntityList) {
                    if (entity.ticksExisted < 1) continue // To avoid stupid duplicated entity
                    if (!checkEntityType(entity)) continue
                    if (entity is EntityItem) continue
                    if (mc.player.getDistance(entity) > range.value) continue
                    else entityMap.putIfAbsent(entity, TextComponent())
                }
            }
            2 -> { // Removing items
                for (itemGroup in itemMap) {
                    itemGroup.updateItems()
                }
                itemMap.removeIf { it.isEmpty() }
            }
            3 -> { // Removing Entity
                entityMap.keys.removeIf { !it.isAddedToWorld || it.isDead || !checkEntityType(it) || mc.player.getDistance(it) > range.value }
            }
            4 -> { // Merging Items
                for (itemGroup in itemMap) for (otherGroup in itemMap) {
                    if (itemGroup == otherGroup) continue
                    itemGroup.merge(otherGroup)
                }
                itemMap.removeIf { it.isEmpty() }
            }
        }
        updateTick = (updateTick + 1) % 5

        // Update item nametags tick by tick
        for (itemGroup in itemMap) {
            itemGroup.updateText()
        }

        // Update entity nametags tick by tick
        for ((entity, textComponent) in entityMap) {
            textComponent.clear()
            if (entity is EntityXPOrb) {
                textComponent.add(entity.name)
                textComponent.add(" x${entity.xpValue}")
            } else {
                var isLine1Empty = true
                for (contentType in line1Settings) {
                    getContent(contentType.value, entity)?.let {
                        textComponent.add(it)
                        isLine1Empty = false
                    }
                }
                if (!isLine1Empty) textComponent.currentLine++
                for (contentType in line2Settings) {
                    getContent(contentType.value, entity)?.let {
                        textComponent.add(it)
                    }
                }
            }
        }
    }

    private fun checkSetting(): Boolean {
        for (setting in line1Settings) {
            if (setting.value != ContentType.TOTEM_POPS) continue
            return true
        }
        for (setting in line2Settings) {
            if (setting.value != ContentType.TOTEM_POPS) continue
            return true
        }
        return false
    }

    private fun getContent(contentType: ContentType, entity: Entity) = when (contentType) {
        ContentType.NONE -> {
            null
        }
        ContentType.NAME -> {
            val name = entity.displayName.unformattedText
            TextComponent.TextElement(name, getTextColor())
        }
        ContentType.TYPE -> {
            TextComponent.TextElement(getEntityType(entity), getTextColor())
        }
        ContentType.TOTAL_HP -> {
            if (entity !is EntityLivingBase) {
                null
            } else {
                val totalHp = MathUtils.round(entity.health + entity.absorptionAmount, 1).toString()
                TextComponent.TextElement(totalHp, getHpColor(entity))
            }
        }
        ContentType.HP -> {
            if (entity !is EntityLivingBase) {
                null
            } else {
                val hp = MathUtils.round(entity.health, 1).toString()
                TextComponent.TextElement(hp, getHpColor(entity))
            }
        }
        ContentType.ABSORPTION -> {
            if (entity !is EntityLivingBase || entity.absorptionAmount == 0f) {
                null
            } else {
                val absorption = MathUtils.round(entity.absorptionAmount, 1).toString()
                TextComponent.TextElement(absorption, 0xEECC20)
            }
        }
        ContentType.PING -> {
            if (entity !is EntityOtherPlayerMP) {
                null
            } else {
                val ping = mc.connection?.getPlayerInfo(entity.uniqueID)?.responseTime ?: 0
                TextComponent.TextElement("${ping}ms", pingColorGradient.get(ping.toFloat()).toHex())
            }
        }
        ContentType.DISTANCE -> {
            val dist = MathUtils.round(mc.player.getDistance(entity), 1).toString()
            TextComponent.TextElement("${dist}m", getTextColor())
        }
        ContentType.TOTEM_POPS -> {
            //TODO
            null
        }
    }

    private fun getTextColor() = ColorConverter.rgbToInt(rText.value, gText.value, bText.value)

    private fun getEntityType(entity: Entity) = entity.javaClass.simpleName.replace("Entity", "")
            .replace("Other", "")
            .replace("MP", "")
            .replace("SP", "")
            .replace(" ", "")

    private fun getHpColor(entity: EntityLivingBase) = healthColorGradient.get((entity.health / entity.maxHealth) * 100f).toHex()

    fun checkEntityType(entity: Entity) = (self.value || entity != mc.player)
            && (!entity.isInvisible || invisible.value)
            && (entity is EntityXPOrb && experience.value
            || entity is EntityPlayer && players.value && EntityUtils.playerTypeCheck(entity, true, true)
            || EntityUtils.mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value))

    private class ItemGroup {
        private val itemSet = HashSet<EntityItem>()
        val textComponent = TextComponent()

        fun merge(other: ItemGroup) {
            val thisCenter = this.getCenter(1f)
            val otherCenter = other.getCenter(1f)
            val dist = thisCenter.distanceTo(otherCenter)
            if (dist < 4f) {
                val ableToMerge = ArrayList<EntityItem>()
                for (entityItem in other.itemSet) {
                    val pos = entityItem.positionVector
                    val distanceToThis = pos.distanceTo(thisCenter)
                    val distanceToOther = pos.distanceTo(otherCenter)
                    if (this.itemSet.size >= other.itemSet.size || distanceToThis < distanceToOther) ableToMerge.add(entityItem)
                }
                for (entityItem in ableToMerge) {
                    if (this.add(entityItem)) other.remove(entityItem)
                }
            }
        }

        fun add(item: EntityItem): Boolean {
            for (otherItem in itemSet) {
                if (otherItem.getDistance(item) > 2.0f) return false
            }
            return itemSet.add(item)
        }

        fun remove(item: EntityItem): Boolean {
            return itemSet.remove(item)
        }

        fun isEmpty() = itemSet.isEmpty()

        fun contains(item: EntityItem) = itemSet.contains(item)

        fun getCenter(partialTicks: Float): Vec3d {
            if (isEmpty()) return Vec3d.ZERO
            val sizeFactor = 1.0 / itemSet.size
            var center = Vec3d.ZERO
            for (entityItem in itemSet) {
                val pos = EntityUtils.getInterpolatedPos(entityItem, partialTicks)
                center = center.add(pos.scale(sizeFactor))
            }
            return center
        }

        fun updateItems() {
            // Removes items
            val toRemove = ArrayList<EntityItem>()
            for (entityItem in itemSet) {
                if (!entityItem.isAddedToWorld || entityItem.isDead) {
                    toRemove.add(entityItem)
                } else {
                    var remove = false
                    for (otherItem in itemSet) {
                        if (otherItem == entityItem) continue
                        if (otherItem.getDistance(entityItem) <= 2f) continue
                        remove = true
                    }
                    if (remove) toRemove.add(entityItem)
                }
            }
            itemSet.removeAll(toRemove)
        }

        fun updateText() {
            // Updates item count text
            val itemCountMap = TreeMap<String, Int>(Comparator.naturalOrder())
            for (entityItem in itemSet) {
                val itemStack = entityItem.item
                val name = itemStack.getItem().getItemStackDisplayName(itemStack)
                val count = itemCountMap.getOrDefault(name, 0) + itemStack.count
                itemCountMap[name] = count
            }
            textComponent.clear()
            for ((index, entry) in itemCountMap.entries.sortedByDescending { it.value }.withIndex()) {
                val text = if (itemCount.value) "${entry.key} x${entry.value}" else entry.key
                textComponent.addLine(text, getTextColor())
                if (index + 1 >= maxItems.value) {
                    val remaining = itemCountMap.size - index - 1
                    if (remaining > 0) textComponent.addLine("...and $remaining more")
                    break
                }
            }
        }
    }
}