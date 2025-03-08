package me.lucwallace.yptmc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.time.*
import java.time.temporal.ChronoUnit


class YptMC : JavaPlugin() {
    private var db: Database? = null
    private var api: API? = null
    private var running = false
    private var updateTask: BukkitTask? = null
    private var studyRatio: Int = 3

    // Plugin startup logic
    override fun onEnable() {
        db = Database("ypt-mc.db")
        api = API(config.getString("token")!!, config.getInt("group_id"))
        running = true
        studyRatio = config.getInt("study_ratio")

        val scheduler = Bukkit.getScheduler()
        scheduler.runTaskAsynchronously(this, Runnable { resetTodayDBMinutes() })
        scheduler.runTaskAsynchronously(this, Runnable { tickDBMinutes() })
        updateTask = scheduler.runTaskTimerAsynchronously(this, Runnable { updateYPTMinutes() }, 0L, 20L * 300)
        server.pluginManager.registerEvents(JoinListener(db), this)

        getCommand("ypt")!!.setExecutor(Command(db!!, api!!))

        saveDefaultConfig()
    }

    // Plugin shutdown logic
    override fun onDisable() {
        running = false
        updateTask?.cancel()
        db?.close()
    }

    private fun updateYPTMinutes() {
        logger.info("Updating YPT minutes")

        val users = api?.getStudyStatus()
        users?.filter { it.status != null && it.status.msElapsed > 0 }?.forEach {
            db?.updateUserStatus(it.userID, it.status?.msElapsed!! / 1000 / 60 / studyRatio, it.status.isStudying)
        }
    }

    // Decrease minutes by one for all players on the server
    private fun tickDBMinutes() {
        logger.info("DB minute tick task started")

        while (running) {
            val tickEnd = Instant.now().plusSeconds(60)
            val players = Bukkit.getOnlinePlayers()

            players.forEach {
                db?.tickPlayer(it.uniqueId, 1)
            }
            val users = db?.getUsers()
            users?.forEach {
                val player = Bukkit.getPlayer(it.uuid)

                if (player == null) return@forEach
                else if (it.isStudying) {
                    Bukkit.getScheduler().runTask(this, Runnable {
                        player.kick(Component.text("You cannot play while studying.").color(NamedTextColor.RED))
                    })
                    return@forEach
                } else if (it.totalMinutes < 1) {
                    Bukkit.getScheduler().runTask(this, Runnable {
                        player.kick(Component.text("You have run out of play time.").color(NamedTextColor.RED))
                    })
                    return@forEach
                }

                when (it.totalMinutes) {
                    1 -> player.sendMessage(
                        Component.text("You have 1 minute of play time remaining.").color(
                            NamedTextColor.RED
                        )
                    )

                    5 -> player.sendMessage(
                        Component.text("You have 5 minutes of play time remaining.").color(
                            NamedTextColor.RED
                        )
                    )

                    15 -> player.sendMessage(
                        Component.text("You have 15 minutes of play time remaining.").color(
                            NamedTextColor.YELLOW
                        )
                    )
                }
            }
            val duration = Duration.between(Instant.now(), tickEnd)
            Thread.sleep(duration.toMillis())
        }

        logger.info("DB minute tick task ended")
    }

    // Reset database minutes at YPT reset time (5 am)
    private fun resetTodayDBMinutes() {
        logger.info("DB minute reset task started")
        while (running) {
            val now = ZonedDateTime.now()
            val reset = now.toLocalDate().plusDays(1).atTime(5, 0).atZone(now.zone)
            Thread.sleep(ChronoUnit.MILLIS.between(now, reset))

            if (!running) return
            db?.resetTodayMinutes()
        }
        logger.info("DB minute reset task ended")
    }
}
