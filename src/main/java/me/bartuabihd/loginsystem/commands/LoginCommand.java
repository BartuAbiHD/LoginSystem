package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.api.v2.event.LoginEvent;
import me.bartuabihd.loginsystem.manager.HashingManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class LoginCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public LoginCommand(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("error.not_a_player"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId())) {
            player.sendMessage(plugin.getLocaleManager().getMessage("error.already_logged_in"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getLocaleManager().getMessage("login.usage"));
            return true;
        }

        String enteredPassword = args[0];

        // Asenkron olarak oyuncu verisini çek (SADECE BİR KEZ)
        plugin.getStorage().getPlayerData(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null) {
                // Bu mesaj zaten asenkron thread'de, bu yüzden sorun yok gibi görünse de
                // en güvenlisi tüm Bukkit işlemlerini ana thread'e taşımaktır.
                runOnMainThread(() -> player.sendMessage(plugin.getLocaleManager().getMessage("login.not_registered")));
                return;
            }

            // HashingManager'ı kullanarak şifreyi doğrula
            HashingManager.CheckResult result = plugin.getHashingManager().checkPassword(enteredPassword, playerData.getHashedPassword());

            runOnMainThread(() -> {
                if (result.isValid) {
                    // --- GİRİŞ BAŞARILI ---
                    plugin.cancelTimeoutTask(player);
                    plugin.getAuthManager().loginPlayer(player.getUniqueId());

                    Bukkit.getPluginManager().callEvent(new LoginEvent(player));

                    player.sendMessage(plugin.getLocaleManager().getMessage("login.success"));

                    result.newHashToUpdate.ifPresent(newHash -> {
                        plugin.getStorage().updatePassword(player.getUniqueId(), newHash);
                        player.sendMessage(plugin.getLocaleManager().getMessage("login.security_updated"));
                    });

                    if (plugin.getConfig().getBoolean("settings.restrictions.ForceSpawnLocOnJoin.enabled", false)) {
                        List<String> worlds = plugin.getConfig().getStringList("settings.restrictions.ForceSpawnLocOnJoin.worlds");
                        if (worlds.contains(player.getWorld().getName())) {
                            player.teleport(player.getWorld().getSpawnLocation());
                        }
                    }
                } else {
                    // --- GİRİŞ BAŞARISIZ ---
                    player.sendMessage(plugin.getLocaleManager().getMessage("login.wrong_password"));
                    if (plugin.getConfig().getBoolean("settings.restrictions.kickOnWrongPassword", true)) {
                        player.kickPlayer(plugin.getLocaleManager().getMessage("login.kick_wrong_password"));
                    }
                }
            });
        });

        return true;
    }

    private void runOnMainThread(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTask(plugin);
    }

}