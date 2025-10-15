package me.bartuabihd.loginsystem.listeners;

import me.bartuabihd.loginsystem.LoginSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerSessionListener implements Listener {

    private final LoginSystem plugin;

    public PlayerSessionListener(LoginSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("settings.restrictions.ForceSingleSession", true)) {
            if (Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(player.getName()) && !p.getUniqueId().equals(player.getUniqueId()))) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.getLocaleManager().getMessage("join.kick_already_online"));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Yetkili oyuncular için güncelleme bildirimini kontrol et
        // Küçük bir gecikme ekleyerek mesajın join spam'i arasında kaybolmasını önlüyoruz.
        if (player.hasPermission("loginsystem.admin.update")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.isUpdateAvailable()) {
                        player.sendMessage(plugin.getLocaleManager().getMessage("update.notification_player",
                                "{new_version}", plugin.getLatestVersion()
                        ));
                    }
                }
            }.runTaskLater(plugin, 40L); // 2 saniye (40 tick) gecikme
        }

        plugin.getStorage().isPlayerRegistered(player.getUniqueId()).thenAccept(isRegistered -> {
            if (!isRegistered && plugin.getConfig().getBoolean("settings.restrictions.kickNonRegistered", false)) {
                kickPlayer(player, plugin.getLocaleManager().getMessage("join.kick_not_registered"));
                return;
            }
            plugin.startTimeoutTask(player);
            if (isRegistered) {
                player.sendMessage(plugin.getLocaleManager().getMessage("join.login_prompt"));
            } else {
                player.sendMessage(plugin.getLocaleManager().getMessage("join.register_prompt"));
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.cancelTimeoutTask(player);
        plugin.getAuthManager().logoutPlayer(player.getUniqueId());
    }

    private boolean isNotLoggedIn(Player player) {
        return !plugin.getAuthManager().isPlayerLoggedIn(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
            if (isNotLoggedIn(event.getPlayer())) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isNotLoggedIn(player)) {
            if (!plugin.getConfig().getBoolean("settings.restrictions.allowChat", false)) {
                player.sendMessage(plugin.getLocaleManager().getMessage("join.you_cannot_chat_without_logging_in"));
                event.setCancelled(true);
            }
        } else {
            if (plugin.getConfig().getBoolean("settings.restrictions.hideChat", true)) {
                event.getRecipients().removeIf(this::isNotLoggedIn);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (isNotLoggedIn(event.getPlayer())) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            List<String> allowedCommands = plugin.getConfig().getStringList("settings.restrictions.allowCommands");
            if (!allowedCommands.contains(command)) {
                event.getPlayer().sendMessage(plugin.getLocaleManager().getMessage("error.not_logged_in"));
                event.setCancelled(true);
            }
        }
    }

    /**
     * Oyuncunun kendi envanterini veya sandık gibi harici envanterleri açmasını engeller.
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (isNotLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Oyuncunun elindeki item slotunu (hotbar) değiştirmesini (scroll veya 1-9 tuşları ile) engeller.
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (isNotLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Herhangi bir envanterdeki (açık veya kapalı) item'a tıklamasını engeller.
     * Bu, InventoryOpenEvent'i aşabilecek durumlara karşı ek bir güvenlik katmanıdır.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (isNotLoggedIn((Player) event.getWhoClicked())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Oyuncunun envanterinden item atmasını engeller.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isNotLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Oyuncunun dünyayla etkileşime girmesini (blok kırma, blok koyma, sandık açma denemesi vb.) engeller.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isNotLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (isNotLoggedIn(event.getPlayer()) && event.getReason().toLowerCase().contains("afk")) {
            event.setCancelled(true);
        }
    }

    private void kickPlayer(Player player, String message) {
        Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(message));
    }
}