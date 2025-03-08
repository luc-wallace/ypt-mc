package me.lucwallace.yptmc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command(private val db: Database?, private val api: API?) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        val subcommand = args[0]
        when (subcommand) {
            "time" -> {
                if (sender !is Player) return false

                val user = db?.getUserByUUID(sender.uniqueId)
                if (user == null) {
                    sender.sendMessage(
                        Component.text("You are not registered on the server, please message an administrator.").color(
                            NamedTextColor.RED
                        )
                    )
                    return true
                }
                sender.sendMessage(
                    Component.text("You have ${user.totalMinutes} minute(s) left.").color(NamedTextColor.YELLOW)
                )
            }

            "register" -> {
                if (sender !is Server && !sender.isOp) {
                    sender.sendMessage(
                        Component.text("You do not have permission to use this command.")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                val p = args.getOrNull(1)
                val id = args.getOrNull(2)?.toIntOrNull()
                if (id == null || p == null) return false

                val player = Bukkit.getOfflinePlayer(p)
                db?.registerUser(player.uniqueId, id)
                sender.sendMessage(
                    Component.text("Registered user ${player.name} with YPT ID $id").color(NamedTextColor.BLUE)
                )
            }

            "status" -> {
                val users = db?.getUsers()
                var message = ""

                users?.forEach {
                    val player = Bukkit.getOfflinePlayer(it.uuid)
                    var status = "Offline"
                    if (player.isOnline) {
                        status = "On the server"
                    } else if (it.isStudying) {
                        status = "Studying"
                    }
                    message += "${player.name}: ${status} [${it.totalMinutes} minute(s)]\n"
                }

                sender.sendMessage(message.dropLast(1))
            }

            "group" -> {
                if (sender !is Server && !sender.isOp) {
                    sender.sendMessage(
                        Component.text("You do not have permission to use this command.")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }

                val users = api?.getStudyStatus()
                var message = ""

                users?.forEach {
                    message += "${it.username} [ID: ${it.userID}]\n"
                }

                sender.sendMessage(message.dropLast(1))
            }

            "renumerate" -> {
                if (sender !is Server && !sender.isOp) {
                    sender.sendMessage(
                        Component.text("You do not have permission to use this command.")
                            .color(NamedTextColor.RED)
                    )
                    return true
                }
                val p = args.getOrNull(1) ?: return false
                val minutes = args.getOrNull(2)?.toIntOrNull() ?: return false
                if (minutes < 1) return false


                val player = Bukkit.getOfflinePlayer(p)
                db?.renumerateUser(player.uniqueId, minutes)
                sender.sendMessage(
                    Component.text("Added $minutes minute(s) to ${player.name}.")
                        .color(NamedTextColor.GREEN)
                )
            }

            else -> return false
        }
        return true
    }
}