package me.lucwallace.yptmc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener(private val db: Database?) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = db?.getUserByUUID(player.uniqueId)
        if (user == null) {
            player.kick(
                Component.text("You are not registered on the YPT MC system. Please message an administrator.")
                    .color(NamedTextColor.RED)
            )
            return
        } else if (user.isStudying) {
            player.kick(Component.text("You cannot play on the server while studying.").color(NamedTextColor.RED))
            return
        } else if (user.totalMinutes == 0) {
            event.player.kick(Component.text("You have no play time left.").color(NamedTextColor.RED))
            return
        }

        event.player.sendMessage(
            Component.text("You have ${user.totalMinutes} minute(s) left.").color(NamedTextColor.YELLOW)
        )
    }
}

