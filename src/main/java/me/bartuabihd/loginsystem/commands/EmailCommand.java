package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.api.v2.event.EmailChangeEvent;
import me.bartuabihd.loginsystem.api.v2.event.EmailRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class EmailCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public EmailCommand(LoginSystem plugin) {
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

        if (args.length == 0) {
            printUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
            case "set":
            case "change":
                handleAddEmail(player, args);
                break;
            case "remove":
                handleRemoveEmail(player);
                break;
            default:
                printUsage(player);
                break;
        }

        return true;
    }

    private void handleAddEmail(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getLocaleManager().getMessage("email.add_usage"));
            return;
        }
        String newEmail = args[1];

        // E-posta formatını config.yml'den gelen Regex ile kontrol et
        String emailRegex = plugin.getConfig().getString("settings.restrictions.allowedEmailCharacters", "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$");
        if (!Pattern.matches(emailRegex, newEmail)) {
            player.sendMessage(plugin.getLocaleManager().getMessage("email.invalid_format"));
            return;
        }

        // Event'e gönderebilmek için önce oyuncunun eski e-postasını al
        plugin.getStorage().getPlayerData(player.getUniqueId()).thenAccept(playerData -> {
            String oldEmail = (playerData != null) ? playerData.getEmail() : null;

            // Yeni e-postanın zaten kullanımda olup olmadığını kontrol et
            plugin.getStorage().isEmailInUse(newEmail).thenAccept(isUsed -> {
                if (isUsed) {
                    player.sendMessage(plugin.getLocaleManager().getMessage("email.already_in_use"));
                    return;
                }

                // E-postayı veritabanında güncelle
                plugin.getStorage().updateEmail(player.getUniqueId(), newEmail).thenRun(() -> {
                    // API Event'ini tetikle
                    Bukkit.getPluginManager().callEvent(new EmailChangeEvent(player, oldEmail, newEmail));
                    // Oyuncuya başarı mesajı gönder
                    player.sendMessage(plugin.getLocaleManager().getMessage("email.add_success", "{email}", newEmail));
                });
            });
        });
    }

    private void handleRemoveEmail(Player player) {
        plugin.getStorage().getPlayerData(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null || playerData.getEmail() == null) {
                player.sendMessage(plugin.getLocaleManager().getMessage("email.not_set"));
                return;
            }

            String removedEmail = playerData.getEmail();

            // E-postayı veritabanında null olarak ayarla (sil)
            plugin.getStorage().updateEmail(player.getUniqueId(), null).thenRun(() -> {
                // API Event'ini tetikle
                Bukkit.getPluginManager().callEvent(new EmailRemoveEvent(player, removedEmail));
                // Oyuncuya başarı mesajı gönder
                player.sendMessage(plugin.getLocaleManager().getMessage("email.remove_success"));
            });
        });
    }

    private void printUsage(Player player) {
        player.sendMessage(plugin.getLocaleManager().getMessage("email.usage_header"));
        player.sendMessage(plugin.getLocaleManager().getMessage("email.usage_add"));
        player.sendMessage(plugin.getLocaleManager().getMessage("email.usage_remove"));
    }
}