package dev.chsr.advancementBarrierPaper

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetTaskCMD : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        TaskManager.setTask(Tasks.factoryList[args[0].toInt()])
        return true
    }
}