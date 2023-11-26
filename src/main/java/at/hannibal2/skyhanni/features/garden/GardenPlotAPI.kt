package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.isInside
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GardenPlotAPI {

    private val pestNamePattern = "§aPlot §7- §b(?<name>.*)".toPattern()

    var plots = listOf<Plot>()

    fun getCurrentPlot(): Plot? {
        val location = LocationUtils.playerLocation()
        return plots.firstOrNull { it.box.isInside(location) }
    }

    class Plot(val id: Int, var inventorySlot: Int, val box: AxisAlignedBB)

    val Plot.name get() = GardenAPI.storage?.plotNames?.get(id) ?: "$id"

    fun Plot.isBarn() = id == -1

    init {
        val plotMap = listOf(
            listOf(21, 13, 9, 14, 22),
            listOf(15, 5, 1, 6, 16),
            listOf(10, 2, -1, 3, 11),
            listOf(17, 7, 4, 8, 18),
            listOf(23, 19, 12, 20, 24),
        )
        val list = mutableListOf<Plot>()
        var slot = 2
        for ((y, rows) in plotMap.withIndex()) {
            for ((x, id) in rows.withIndex()) {
                val minX = ((x - 2) * 96 - 48).toDouble()
                val minY = ((y - 2) * 96 - 48).toDouble()
                val maxX = ((x - 2) * 96 + 48).toDouble()
                val maxY = ((y - 2) * 96 + 48).toDouble()
                val box = AxisAlignedBB(minX, 0.0, minY, maxX, 256.0, maxY)
                list.add(
                    Plot(id, slot, box)
                )
                slot++
            }
            slot += 4
        }
        plots = list
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!GardenAPI.inGarden()) return
        if (event.inventoryName != "Configure Plots") return

        val names = GardenAPI.storage?.plotNames ?: return
        for (plot in plots) {
            val itemName = event.inventoryItems[plot.inventorySlot]?.name ?: continue
            pestNamePattern.matchMatcher(itemName) {
                names[plot.id] = group("name")
            }
        }
    }
}