package me.abasing.immortality;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class Immortality extends JavaPlugin implements Listener, TabCompleter {
    private final Map<UUID, Boolean> ImmortalityPlayers = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> activationDelays = new HashMap<>();
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesConfig();
        Objects.requireNonNull(getCommand("immortality")).setExecutor(new ImmortalityCommandExecutor());
        Objects.requireNonNull(getCommand("immortality")).setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("messages." + key, key));
    }

    private void toggleImmortality(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean ImmortalityEnabled = !ImmortalityPlayers.getOrDefault(playerUUID, false);

        // Cooldown logic
        if (getConfig().getBoolean("enable-cooldown-time", true) && !player.hasPermission("Immortality.bypass.cooldown")) {
            long cooldownTime = getConfig().getInt("cooldown-time", 600) * 1000L;
            long lastUsed = cooldowns.getOrDefault(playerUUID, 0L);
            if (System.currentTimeMillis() - lastUsed < cooldownTime) {
                player.sendMessage(getMessage("cooldownActive"));
                return;
            }
            cooldowns.put(playerUUID, System.currentTimeMillis());
        }

        // Health threshold logic
        if (getConfig().getBoolean("enable-health-threshold", true) && !player.hasPermission("Immortality.bypass.health.requirment")) {
            double healthThreshold = getConfig().getDouble("health-threshold", 5.0);
            if (player.getHealth() < healthThreshold) {
                player.sendMessage(getMessage("healthTooLow"));
                return;
            }
        }

        // Activation delay logic
        if (getConfig().getBoolean("enable-activation-delay", true) && !player.hasPermission("Immortality.bypass.activation.delay")) {
            int delay = getConfig().getInt("activation-delay", 5);
            player.sendMessage(getMessage("activationDelayed").replace("%seconds%", String.valueOf(delay)));
            activationDelays.put(playerUUID, System.currentTimeMillis() + delay * 1000L);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (System.currentTimeMillis() >= activationDelays.getOrDefault(playerUUID, 0L)) {
                    setImmortality(player, ImmortalityEnabled);
                }
            }, delay * 20L);
            return;
        }

        // Environment restrictions logic
        if (getConfig().getBoolean("enable-environment-restrictions", true)) {
            List<String> allowedWorlds = getConfig().getStringList("environment-restrictions.allowed-worlds");
            List<String> allowedBiomes = getConfig().getStringList("environment-restrictions.allowed-biomes");
            if (!allowedWorlds.contains(player.getWorld().getName()) || !allowedBiomes.contains(player.getLocation().getBlock().getBiome().name())) {
                player.sendMessage(getMessage("environmentRestricted"));
                return;
            }
        }

        setImmortality(player, ImmortalityEnabled);
    }

    private void setImmortality(Player player, boolean enabled) {
        UUID playerUUID = player.getUniqueId();
        ImmortalityPlayers.put(playerUUID, enabled);
        player.sendMessage(enabled ? getMessage("ImmortalityEnabled") : getMessage("ImmortalityDisabled"));
    }

    private void sendImmortalityList(Player player) {
        player.sendMessage(getMessage("ImmortalityListHeader"));
        ImmortalityPlayers.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> {
                    Player targetPlayer = getServer().getPlayer(entry.getKey());
                    return (targetPlayer != null) ? targetPlayer.getName() : null;
                }).filter(Objects::nonNull)
                .forEach(playerName -> player.sendMessage(getMessage("ImmortalityListEntry").replace("%player%", playerName)));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();
            if (ImmortalityPlayers.getOrDefault(playerUUID, false) && player.getHealth() - event.getFinalDamage() <= 0.5D) {
                setPlayerHealth(player, Math.max(0.5D, player.getHealth() - event.getFinalDamage() + 0.5D));
                event.setDamage(0.0D);
                event.setCancelled(true);
            }
        }
    }

    private void setPlayerHealth(Player player, double health) {
        try {
            Method setHealthMethod = player.getClass().getMethod("setHealth", double.class);
            setHealthMethod.invoke(player, health);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error setting player health", e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("toggle", "list", "reload").stream()
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private class ImmortalityCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            try {
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("immortality.reload")) {
                        reloadConfig();
                        loadMessagesConfig();
                        sender.sendMessage(getMessage("reloadSuccess"));
                    } else {
                        sender.sendMessage(getMessage("noPermission"));
                    }
                    return true;
                }

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                        sendImmortalityList(player);
                        return true;
                    }
                    if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
                        Player targetPlayer = getServer().getPlayer(args[1]);
                        if (targetPlayer != null) {
                            toggleImmortality(targetPlayer);
                            return true;
                        }
                        player.sendMessage(getMessage("playerNotFound"));
                        return true;
                    }
                    toggleImmortality(player);
                }
                return true;
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "An error occurred while executing the command.");
                getLogger().log(Level.SEVERE, "Error executing command", e);
                return false;
            }
        }
    }
}