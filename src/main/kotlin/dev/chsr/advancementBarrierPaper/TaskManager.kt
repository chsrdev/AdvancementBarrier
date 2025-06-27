package dev.chsr.advancementBarrierPaper

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.HandlerList

object TaskManager {
    var currentTaskFactory: TaskFactory = Tasks.factoryList.random()
    var currentTask: Task = currentTaskFactory.create()
    var tickTimer = 60 * 15 * 20 // 15 minutes
    var showTask = true

    init {
        Bukkit.getScheduler().runTaskTimer(AdvancementBarrierPaper.instance, Runnable {
            if (currentTask.isCompleted)
                return@Runnable
            if (!showTask)
                return@Runnable
            if (tickTimer <= 0) {
                nextTask()
                return@Runnable
            }

            var seconds = tickTimer / 20
            val minutes = seconds / 60
            seconds -= minutes * 60

            currentTask.updateText()
            var text = currentTask.text
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("($minutes:$seconds)", currentTask.difficulty.detailsColor))

            Bukkit.getOnlinePlayers().forEach {
                it.sendActionBar(text)
            }

            tickTimer -= 5
        }, 0L, 5L)
    }

    fun nextTask() {
        setTask(Tasks.factoryList.random())
    }

    fun setTask(taskFactory: TaskFactory) {
        HandlerList.unregisterAll(currentTask)
        currentTaskFactory = taskFactory
        currentTask = currentTaskFactory.create()
        AdvancementBarrierPaper.instance.server.pluginManager.registerEvents(
            currentTask,
            AdvancementBarrierPaper.instance
        )
        tickTimer = 60 * 15 * 20

        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
        }
    }

    fun completeTask() {
        if (currentTask.isCompleted)
            return

        HandlerList.unregisterAll(currentTask)
        currentTask.isCompleted = true
        currentTaskFactory.difficulty++
        expandBorder(currentTask.difficulty.borderExpandSize)
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            it.sendActionBar(
                Component.text("Completed!", NamedTextColor.GREEN)
            )
        }

        Bukkit.getScheduler().runTaskLater(AdvancementBarrierPaper.instance, Runnable {
            nextTask()
        }, 100L)

    }
}
