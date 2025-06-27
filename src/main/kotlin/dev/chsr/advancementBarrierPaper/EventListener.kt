package dev.chsr.advancementBarrierPaper

import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent


class EventListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (event.advancement.display == null) return
        when (event.advancement.display!!.frame()) {
            AdvancementDisplay.Frame.CHALLENGE -> expandBorder(10)
            AdvancementDisplay.Frame.GOAL -> expandBorder(5)
            AdvancementDisplay.Frame.TASK -> expandBorder(2)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        if ((event.entity is EnderDragon || event.entity is Wither) && event.entity.killer != null)
            expandBorder(100)
    }
}