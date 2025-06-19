package dev.chsr.advancementBarrierPaper

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class NextTaskCMD : CommandExecutor
{
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        TaskManager.nextTask()
        return true
    }
}