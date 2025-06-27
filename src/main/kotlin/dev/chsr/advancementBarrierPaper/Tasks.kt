package dev.chsr.advancementBarrierPaper

import io.papermc.paper.event.player.PlayerTradeEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Arrow
import org.bukkit.entity.Bat
import org.bukkit.entity.Cat
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fish
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Wolf
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.scheduler.BukkitTask
import kotlin.compareTo


class TaskFactory(private val f: (Int) -> Task) {
    var difficulty: Int = 0

    fun create(): Task {
        return f(difficulty)
    }
}

object Tasks {
    val factoryList = listOf(
        // INSANE
        TaskFactory { SkeletonKillBatWithBow() },

        // HARD
        TaskFactory { TameParrot() },

        // NORMAL
        TaskFactory { KillCreeperByCreeper() },
        TaskFactory { Harvest(80 + it * 10) },
        TaskFactory { FishWithBow(1 + it) },
        TaskFactory { TameWolf() },
        TaskFactory { EatCake() },
        TaskFactory { KillBatWithBow(1 + it) },
        TaskFactory { TameCat() },
        TaskFactory { CreateIronGolem(1 + it) },
        TaskFactory { CraftGoldenApple(1 + it) },

        // EASY
        TaskFactory { RideHorse(60 + 10 * it) },
        TaskFactory { CollectLog(64 + it * 5) },
        TaskFactory { CatchFish(10 + it) },
        TaskFactory { CampfireCook(4 + it * 4) },
        TaskFactory { BreedAnimals(1 + it) },
        TaskFactory { ShearSheep(10 + it) },
        TaskFactory { MilkCow() },
        TaskFactory { MineGravelForFlint(1 + it) },
        TaskFactory { CompostItems(10 + it * 5) },
        TaskFactory { TradeWithVillager(1 + it) },
        TaskFactory { EatSuspiciousStew() },
        TaskFactory { BrewPotion(1 + it) }
    )

    class CollectLog(val goal: Int) : Task("Gather $goal log", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            val logTypes = listOf(
                Material.OAK_LOG, Material.ACACIA_LOG,
                Material.BIRCH_LOG, Material.CHERRY_LOG,
                Material.DARK_OAK_LOG, Material.JUNGLE_LOG,
                Material.MANGROVE_LOG, Material.SPRUCE_LOG,
                Material.PALE_OAK_LOG
            )

            if (event.block.type in logTypes)
                progress = progress!! + 1

            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class CatchFish(val goal: Int) : Task("Catch $goal fish", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: PlayerFishEvent) {
            if (event.state == PlayerFishEvent.State.CAUGHT_FISH)
                progress = progress!! + 1

            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class CampfireCook(val goal: Int) : Task("Cook any $goal pieces of food on a campfire", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBlockCook(event: BlockCookEvent) {
            if (event.block.type == Material.CAMPFIRE)
                progress = progress!! + 1

            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class BreedAnimals(val goal: Int) : Task("Breed any two animals $goal times", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityBreed(event: EntityBreedEvent) {
            if (event.breeder is Player) {
                progress = progress!! + 1
                if (progress!! >= goal)
                    TaskManager.completeTask()
            }
        }
    }

    class Harvest(val goal: Int) : Task("Harvest $goal wheat", TaskDifficulty.NORMAL, progress = 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            if (event.block.type == Material.WHEAT) {
                val blockData = event.block.blockData
                if (blockData is Ageable) {
                    if (blockData.age == blockData.maximumAge) {
                        progress = (progress ?: 0) + 1
                    }
                }
            }

            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class KillCreeperByCreeper : Task("Kill creeper by explosion of another creeper", TaskDifficulty.NORMAL, null) {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return

            if (event.entity is Creeper && event.damager is Creeper)
                Bukkit.getScheduler().runTask(AdvancementBarrierPaper.instance, Runnable {
                    if (event.entity.isDead)
                        TaskManager.completeTask()
                })
        }
    }

    class ShearSheep(val goal: Int) : Task("Shear $goal sheeps", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
            if (event.entity is Sheep) {
                progress = progress!! + 1
                if (progress!! >= goal) {
                    TaskManager.completeTask()
                }
            }
        }
    }

    class MilkCow : Task("Milk a cow", TaskDifficulty.EASY, null) {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerInteract(event: PlayerInteractEntityEvent) {
            if (event.rightClicked.type == EntityType.COW && event.player.inventory.itemInMainHand.type == Material.BUCKET)
                TaskManager.completeTask()
        }
    }

    class RideHorse(val goal: Int) : Task("Ride a horse for $goal seconds", TaskDifficulty.NORMAL, 0) {
        private val ridingPlayers = mutableMapOf<Player, Int>()
        private var bukkitTask: BukkitTask? = null

        @EventHandler(ignoreCancelled = true)
        fun onPlayerMount(event: VehicleEnterEvent) {
            if (event.entered is Player && event.vehicle.type == EntityType.HORSE) {
                val player = event.entered as Player
                ridingPlayers[player] = 0
                bukkitTask = Bukkit.getScheduler().runTaskTimer(AdvancementBarrierPaper.instance, Runnable {
                    if (player.isInsideVehicle && player.vehicle is Horse) {
                        ridingPlayers[player] = ridingPlayers[player]!! + 1
                        progress = progress!! + 1
                        if (progress!! >= goal) {
                            TaskManager.completeTask()
                            bukkitTask?.cancel()
                        }
                    } else {
                        ridingPlayers.remove(player)
                        if (ridingPlayers.isEmpty())
                            progress = 0
                        else progress = ridingPlayers.maxOf { it.value }
                        bukkitTask?.cancel()
                    }
                }, 20L, 20L)
            }
        }
    }

    class TameWolf : Task("Tame a wolf", TaskDifficulty.NORMAL, null) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityTame(event: EntityTameEvent) {
            if (event.owner is Player && event.entity is Wolf) {
                TaskManager.completeTask()
            }
        }
    }

    class FishWithBow(val goal: Int) : Task("Catch $goal fish with a bow", TaskDifficulty.NORMAL, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE
                && event.damager is Arrow
                && event.entity is Fish
            ) {
                progress = progress!! + 1
                if (progress!! >= goal)
                    TaskManager.completeTask()
            }
        }
    }

    class KillBatWithBow(val goal: Int) : Task("Kill $goal bats with a bow", TaskDifficulty.NORMAL, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE
                && event.damager is Arrow
                && event.entity is Bat
            ) {
                progress = progress!! + 1
                if (progress!! >= goal)
                    TaskManager.completeTask()
            }
        }
    }

    class SkeletonKillBatWithBow() : Task("Make a skeleton hit a bat", TaskDifficulty.INSANE, null) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE
                && event.damager is Arrow
                && event.entity is Bat
                && (event.damager as Arrow).shooter is Skeleton
            ) {
                TaskManager.completeTask()
            }
        }
    }

    class MineGravelForFlint(val goal: Int) : Task("Get $goal flint from gravel", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: BlockDropItemEvent) {
            if (event.items.any { it.itemStack.type == Material.FLINT }) {
                progress = progress!! + 1
                if (progress!! >= goal)
                    TaskManager.completeTask()
            }
        }
    }

    class EatCake : Task("Eat a slice of cake", TaskDifficulty.NORMAL, null) {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (event.clickedBlock != null)
                if (event.clickedBlock!!.type == Material.CAKE)
                    TaskManager.completeTask()
        }
    }

    class CompostItems(val goal: Int) : Task("Use a composter $goal times", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerInteract(event: PlayerInteractEvent) {
            val block = event.clickedBlock ?: return

            if (block.type != Material.COMPOSTER) return
            if (event.action != Action.RIGHT_CLICK_BLOCK) return

            val state = block.blockData as? Levelled ?: return
            val levelBefore = state.level

            Bukkit.getScheduler().runTask(AdvancementBarrierPaper.instance, Runnable {
                val updatedState = block.blockData as? Levelled ?: return@Runnable
                val levelAfter = updatedState.level
                if (levelAfter > levelBefore) {
                    progress = (progress ?: 0) + 1
                    if (progress!! >= goal) {
                        TaskManager.completeTask()
                    }
                }
            })
        }

    }

    class TradeWithVillager(val goal: Int) : Task("Trade with any villager $goal times", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onInventoryClick(event: PlayerTradeEvent) {
            progress = progress!! + 1
            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class EatSuspiciousStew : Task("Eat a suspicious stew", TaskDifficulty.EASY, null) {
        @EventHandler(ignoreCancelled = true)
        fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
            if (event.item.type == Material.SUSPICIOUS_STEW) {
                TaskManager.completeTask()
            }
        }
    }

    class BrewPotion(val goal: Int) : Task("Brew $goal potions", TaskDifficulty.EASY, 0) {
        @EventHandler(ignoreCancelled = true)
        fun onBrew(event: BrewEvent) {
            progress = progress!! + 1
            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }

    class TameCat : Task("Tame a cat", TaskDifficulty.NORMAL, null) {
        @EventHandler(ignoreCancelled = true)
        fun onEntityTame(event: EntityTameEvent) {
            if (event.entity is Cat && event.owner is Player) {
                TaskManager.completeTask()
            }
        }
    }

    class CreateIronGolem(val goal: Int) : Task("Summon $goal iron golems", TaskDifficulty.NORMAL, 0) {
        @EventHandler
        fun onEntitySpawn(event: CreatureSpawnEvent) {
            if (event.entityType == EntityType.IRON_GOLEM && event.spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
                progress = progress!! + 1
                if (progress!! >= goal)
                    TaskManager.completeTask()
            }
        }
    }

    class TameParrot : Task("Tame a parrot", TaskDifficulty.HARD, null) {
        @EventHandler
        fun onEntityTame(event: EntityTameEvent) {
            if (event.entity.type == EntityType.PARROT) {
                TaskManager.completeTask()
            }
        }
    }

    class CraftGoldenApple(val goal: Int) : Task("Craft $goal golden apple", TaskDifficulty.NORMAL, 0) {
        @EventHandler
        fun onCraft(event: CraftItemEvent) {
            if (event.recipe.result.type == Material.GOLDEN_APPLE) {
                progress = progress!! + 1
            }
            if (progress!! >= goal)
                TaskManager.completeTask()
        }
    }
}
