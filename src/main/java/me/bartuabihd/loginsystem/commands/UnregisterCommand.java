package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.manager.HashingManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UnregisterCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public UnregisterCommand(LoginSystem plugin) {
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
            player.sendMessage(plugin.getLocaleManager().getMessage("error.not_logged_in"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getLocaleManager().getMessage("unregister.usage"));
            player.sendMessage(plugin.getLocaleManager().getMessage("unregister.warning"));
            return true;
        }

        String password = args[0];

        // Asenkron olarak oyuncu verisini çek
        plugin.getStorage().getPlayerData(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null) {
                // Bu durum normalde yaşanmamalı çünkü oyuncu zaten giriş yapmış durumda
                player.sendMessage(plugin.getLocaleManager().getMessage("error.generic", "{error}", plugin.getLocaleManager().getMessage("error.player_data_not_found")));
                return;
            }

            // Şifreyi doğrulamak için HashingManager'ı kullan
            HashingManager.CheckResult result = plugin.getHashingManager().checkPassword(password, playerData.getHashedPassword());

            if (result.isValid) {
                // Şifre doğru, kaydı silme işlemine devam et.
                plugin.getStorage().unregisterPlayer(player.getUniqueId()).thenRun(() -> {
                    // İşlem bittikten sonra oyuncuyu session'dan çıkar ve sunucudan at
                    plugin.getAuthManager().logoutPlayer(player.getUniqueId());

                    // Kick işlemi ana sunucu thread'inde yapılmalı
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.kickPlayer(plugin.getLocaleManager().getMessage("unregister.success_kick"));
                        }
                    }.runTask(plugin);
                });
            } else {
                // Hatalı şifre mesajı için login komutundaki mesajı tekrar kullanıyoruz.
                player.sendMessage(plugin.getLocaleManager().getMessage("login.wrong_password"));
            }
        });

        return true;
    }
}