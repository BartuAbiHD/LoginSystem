package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.api.v2.event.LogoutEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LogoutCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public LogoutCommand(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("error.not_a_player"));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId())) {
            player.sendMessage(plugin.getLocaleManager().getMessage("logout.not_logged_in"));
            return true;
        }

        plugin.getAuthManager().logoutPlayer(player.getUniqueId());
        Bukkit.getPluginManager().callEvent(new LogoutEvent(player));
        player.sendMessage(plugin.getLocaleManager().getMessage("logout.success"));

        // Oyuncuyu kicklemek, güvenli bir şekilde tekrar login ekranına dönmesini sağlar.
        // Bukkit API'si ana thread'de çalışmayı gerektirdiği için BukkitRunnable kullanıyoruz.
        new BukkitRunnable() {
            @Override
            public void run() {
                player.kickPlayer(plugin.getLocaleManager().getMessage("logout.kick_message"));
            }
        }.runTask(plugin);

        return true;
    }
}