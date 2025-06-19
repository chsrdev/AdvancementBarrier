package dev.chsr.advancementBarrierPaper

import net.kyori.adventure.text.format.NamedTextColor


enum class TaskDifficulty(
    val borderExpandSize: Int,
    val color: NamedTextColor,
    val detailsColor: NamedTextColor
) {
    HARD(50, NamedTextColor.RED, NamedTextColor.DARK_RED),
    NORMAL(25, NamedTextColor.YELLOW, NamedTextColor.GOLD),
    EASY(10, NamedTextColor.GREEN, NamedTextColor.DARK_GREEN)
}