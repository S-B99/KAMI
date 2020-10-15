package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.MotionTracker
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.BlockPos

object CombatManager : Manager() {
    private val combatModules: List<Module>

    var targetList = emptyList<EntityLivingBase>()
    var target: EntityLivingBase? = null
        set(value) {
            motionTracker.target = value
            field = value
        }
    var crystalPlaceList = emptyList<Triple<BlockPos, Float, Float>>() // <BlockPos, Target Damage, Self Damage>, immutable list = thread safe
    var crystalMap = emptyMap<EntityEnderCrystal, Pair<Float, Float>>() // <Crystal, <Target Damage, Self Damage>>
    val motionTracker = MotionTracker(null)

    fun isActiveAndTopPriority(module: Module) = module.isActive() && isOnTopPriority(module)

    fun isOnTopPriority(module: Module): Boolean {
        return getTopPriority() <= module.modulePriority
    }

    fun getTopPriority(): Int {
        return getTopModule()?.modulePriority ?: -1
    }

    fun getTopModule(): Module? {
        var topModule: Module? = null
        for (module in combatModules) {
            if (!module.isActive()) continue
            if (module.modulePriority < topModule?.modulePriority ?: 0) continue
            topModule = module
        }
        return topModule
    }

    init {
        val cacheList = ArrayList<Module>()
        for (module in ModuleManager.getModules()) {
            if (module.category != Module.Category.COMBAT) continue
            if (module.modulePriority == -1) continue
            cacheList.add(module)
        }
        combatModules = cacheList.toList()
    }
}