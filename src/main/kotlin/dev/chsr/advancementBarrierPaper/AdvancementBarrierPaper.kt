package dev.chsr.advancementBarrierPaper

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class AdvancementBarrierPaper : JavaPlugin() {
    companion object {
        lateinit var instance: AdvancementBarrierPaper
            private set

        lateinit var taskDifficultyData: Map<String, Int>
            private set
    }

    override fun onEnable() {
        instance = this

        loadTaskDifficulties()

        Tasks.factoryList.forEach { factory ->
            val className = factory.create()::class.simpleName
            val difficulty = taskDifficultyData[className] ?: 0
            factory.difficulty = difficulty
        }

        server.pluginManager.registerEvents(EventListener(), this)
        getCommand("nextTask")?.setExecutor(NextTaskCMD())
        getCommand("setTask")?.setExecutor(SetTaskCMD())

        TaskManager.nextTask()
    }

    override fun onDisable() {
        saveTaskDifficulties()
    }

    private fun loadTaskDifficulties() {
        val file = File(dataFolder, "tasks.yml")
        if (!file.exists()) {
            saveResource("tasks.yml", false)
        }

        val config = YamlConfiguration.loadConfiguration(file)
        taskDifficultyData = config.getKeys(false).associateWith { config.getInt(it) }
    }

    private fun saveTaskDifficulties() {
        val file = File(dataFolder, "tasks.yml")
        val config = YamlConfiguration()

        Tasks.factoryList.forEach { factory ->
            val className = factory.create()::class.simpleName ?: return@forEach
            config.set(className, factory.difficulty)
        }

        config.save(file)
    }
}

fun expandBorder(size: Int) {
    TaskManager.showTask = false
    Bukkit.getWorlds().first().worldBorder.size += size
    val text = Component.text()
        .append(Component.text("Borders expanded", NamedTextColor.GREEN))
        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
        .append(Component.text("+$size", NamedTextColor.DARK_GREEN))
        .build()

    Bukkit.getOnlinePlayers().forEach { it.sendActionBar(text) }
    Bukkit.getScheduler().runTaskLater(AdvancementBarrierPaper.instance, Runnable {
        TaskManager.showTask = true
    }, 60L)

}
