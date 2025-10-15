package me.bartuabihd.loginsystem.commands;

import me.bartuabihd.loginsystem.LoginSystem;
import me.bartuabihd.loginsystem.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginSystemAdminCommand implements CommandExecutor {

    private final LoginSystem plugin;

    public LoginSystemAdminCommand(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            handleHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "lang":
            case "language":
                handleLang(sender, args);
                break;
            case "register":
                handleRegister(sender, args);
                break;
            case "unregister":
                handleUnregister(sender, args);
                break;
            case "changepass":
            case "changepassword":
                handleChangePassword(sender, args);
                break;
            case "forcelogin":
                handleForceLogin(sender, args);
                break;
            case "help":
            default:
                handleHelp(sender);
                break;
        }
        return true;
    }

    private void handleHelp(CommandSender sender) {
        if (!sender.hasPermission("loginsystem.admin.help")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_header"));
        if (sender.hasPermission("loginsystem.admin.reload"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_reload"));
        if (sender.hasPermission("loginsystem.admin.lang"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_lang"));
        if (sender.hasPermission("loginsystem.admin.register"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_register"));
        if (sender.hasPermission("loginsystem.admin.unregister"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_unregister"));
        if (sender.hasPermission("loginsystem.admin.changepass"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_changepass"));
        if (sender.hasPermission("loginsystem.admin.forcelogin"))
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_forcelogin"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("loginsystem.admin.reload")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        plugin.performReload();
        sender.sendMessage(plugin.getLocaleManager().getMessage("admin.reload_success"));
    }

    private void handleLang(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginsystem.admin.lang")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_lang"));
            return;
        }

        String newLang = args[1].toLowerCase();

        // 1. config.yml dosyasındaki ayarı güncelle
        plugin.getConfig().set("settings.messagesLanguage", newLang);
        // 2. Değişikliği diske kaydet
        plugin.saveConfig();
        // 3. Eklentiyi yeniden yükleyerek değişikliği aktif et
        plugin.performReload();

        // 4. Yeni dildeki başarı mesajını gönder
        sender.sendMessage(plugin.getLocaleManager().getMessage("admin.lang_success", "{lang}", newLang));
    }

    private void handleRegister(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginsystem.admin.register")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_register"));
            return;
        }
        String playerName = args[1];
        String password = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        plugin.getStorage().isPlayerRegistered(target.getUniqueId()).thenAccept(isRegistered -> {
            if (isRegistered) {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.player_already_registered"));
                return;
            }
            String hashedPassword = plugin.getHashingManager().hash(password);
            PlayerData data = new PlayerData(target.getUniqueId(), target.getName(), hashedPassword, "ADMIN_FORCED_REG");
            plugin.getStorage().registerPlayer(data).thenRun(() -> {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.force_register_success", "{player}", target.getName()));
            });
        });
    }

    private void handleUnregister(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginsystem.admin.unregister")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_unregister"));
            return;
        }
        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        plugin.getStorage().isPlayerRegistered(target.getUniqueId()).thenAccept(isRegistered -> {
            if (!isRegistered) {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.player_not_registered"));
                return;
            }
            plugin.getStorage().unregisterPlayer(target.getUniqueId()).thenRun(() -> {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.force_unregister_success", "{player}", target.getName()));
                if(target.isOnline()) {
                    target.getPlayer().kickPlayer(plugin.getLocaleManager().getMessage("unregister.success_kick"));
                }
            });
        });
    }

    private void handleChangePassword(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginsystem.admin.changepass")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_changepass"));
            return;
        }
        String playerName = args[1];
        String newPassword = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        plugin.getStorage().isPlayerRegistered(target.getUniqueId()).thenAccept(isRegistered -> {
            if (!isRegistered) {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.player_not_registered"));
                return;
            }
            String newHashedPassword = plugin.getHashingManager().hash(newPassword);
            plugin.getStorage().updatePassword(target.getUniqueId(), newHashedPassword).thenRun(() -> {
                sender.sendMessage(plugin.getLocaleManager().getMessage("admin.changepass_success", "{player}", target.getName()));
            });
        });
    }

    private void handleForceLogin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("loginsystem.admin.forcelogin")) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.no_permission"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.help_forcelogin"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getLocaleManager().getMessage("admin.forcelogin_player_not_online"));
            return;
        }

        plugin.getApi().forceLogin(target);
        sender.sendMessage(plugin.getLocaleManager().getMessage("admin.forcelogin_success", "{player}", target.getName()));
    }
}