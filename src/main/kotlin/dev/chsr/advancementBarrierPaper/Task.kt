package dev.chsr.advancementBarrierPaper

import dev.chsr.advancementBarrierPaper.TaskManager.currentTask
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.Listener

open class Task(
    var task: String,
    val difficulty: TaskDifficulty,
    var progress: Int? = null,
    var isCompleted: Boolean = false,
) : Listener {
    lateinit var text: Component

    fun updateText() {
        text = Component.text(task, difficulty.color)
        if (progress != null) {
            text = text.append(Component.text(" ($progress)", difficulty.detailsColor))
        }
    }
}

