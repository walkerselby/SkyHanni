package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

class WaypointSaver {
    private val config get() = SkyHanniMod.feature.dev.waypoint
    private var timeLastSaved: Long = 0
    private var locations = mutableListOf<LorenzVec>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!Keyboard.getEventKeyState()) return
        if (NEUItems.neuHasFocus()) return
        if (System.currentTimeMillis() - timeLastSaved < 250) return

        Minecraft.getMinecraft().currentScreen?.let {
            if (it !is GuiInventory && it !is GuiChest && it !is GuiEditSign) return
        }

        val key = if (Keyboard.getEventKey() == 0) Keyboard.getEventCharacter().code + 256 else Keyboard.getEventKey()
        if (config.deleteKey == key) {
            locations = locations.dropLast(1).toMutableList()
            locations.copyLocations()
        }
        if (config.saveKey == key) {
            val newLocation = LocationUtils.playerLocation().roundLocation()
            if (locations.isNotEmpty()) {
                if (newLocation == locations.last()) return
            }
            locations.add(newLocation)
            locations.copyLocations()
        }
    }

    private fun MutableList<LorenzVec>.copyLocations() {
        val resultList = mutableListOf<String>()
        timeLastSaved = System.currentTimeMillis()
        for (location in this) {
            val x = location.x.toString().replace(",", ".")
            val y = location.y.toString().replace(",", ".")
            val z = location.z.toString().replace(",", ".")
            resultList.add("\"$x:$y:$z\"")
        }
        OSUtils.copyToClipboard(resultList.joinToString((",\n")))
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LorenzUtils.inSkyBlock) return
        for (location in locations) {
            event.drawWaypointFilled(location, LorenzColor.GREEN.toColor())
        }
    }
}