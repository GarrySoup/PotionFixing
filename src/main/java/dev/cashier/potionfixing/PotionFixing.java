package dev.cashier.potionfixing;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.*;

public final class PotionFixing extends JavaPlugin implements Listener {

    public static Map<Player,String> choices = new HashMap<>();
    public static Map<String,PotionPreset> presets = new HashMap<>();
    final FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigurationSection presetsSection = config.getConfigurationSection("presets");
        for (String key : presetsSection.getKeys(false)) {
            double throwMultiplier = presetsSection.getDouble(key + ".throwMultiplier");
            double fallMultiplier = presetsSection.getDouble(key + ".fallMultiplier");
            double offset = presetsSection.getDouble(key + ".offset");
            PotionPreset preset = new PotionPreset(throwMultiplier, fallMultiplier, offset);
            presets.put(key, preset);
        }


        getServer().getPluginManager().registerEvents(new PotionListener(),this);
        // 注册命令/potion <subcommand>
        getCommand("potion").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length > 0) {
                    String subcommand = args[0];
                    if (subcommand.equalsIgnoreCase("setplayer")) {
                        if (args.length > 2) {
                            String playername = args[1];
                            String profilename = args[2];
                            if (presets.containsKey(profilename)) {
                                Player target = Bukkit.getPlayer(playername);
                                if (target != null) {
                                    choices.put(target, profilename);
                                    player.sendMessage("You have set the potion preset of " + playername + " to " + profilename + ".");
                                } else {
                                    player.sendMessage("That player is not online or does not exist.");
                                }
                            } else {
                                player.sendMessage("A potion preset with that name does not exist.");
                            }
                        } else {
                            player.sendMessage("Usage: /potion setplayer <playername> <profilename>");
                        }
                    } else if (subcommand.equalsIgnoreCase("delete")) {
                        if (args.length > 1) {
                            String profilename = args[1];
                            if (presets.containsKey(profilename)) {
                                presets.remove(profilename);
                                player.sendMessage("You have deleted the potion preset named " + profilename + ".");
                                FileConfiguration config1 = getConfig();
                                config1.set("presets." + profilename, null);
                                saveConfig();
                            } else {
                                player.sendMessage("A potion preset with that name does not exist.");
                            }
                        } else {
                            player.sendMessage("Usage: /potion delete <profilename>");
                        }
                    } else if (subcommand.equalsIgnoreCase("create")) {
                        if (args.length > 1) {
                            String profilename = args[1];
                            if (!presets.containsKey(profilename)) {
                                PotionPreset preset = new PotionPreset(1.0, 1.0, 0.0);
                                presets.put(profilename, preset);
                                config.set("presets." + profilename + ".throwMultiplier", 1.0);
                                config.set("presets." + profilename + ".fallMultiplier", 1.0);
                                config.set("presets." + profilename + ".offset", 0.0);
                                saveConfig();
                                player.sendMessage("You have created a new potion preset named " + profilename + ".");
                            } else {
                                player.sendMessage("A potion preset with that name already exists.");
                            }
                        } else {
                            player.sendMessage("Usage: /potion create <profilename>");
                        }
                    } else if (subcommand.equalsIgnoreCase("set")) {
                        if (args.length > 3) {
                            String profilename = args[1];
                            String parameter = args[2];
                            String value = args[3];
                            if (presets.containsKey(profilename)) {
                                PotionPreset preset = presets.get(profilename);
                                if (parameter.equalsIgnoreCase("throwMultiplier") || parameter.equalsIgnoreCase("fallMultiplier") || parameter.equalsIgnoreCase("offset")) {
                                    try {
                                        double doubleValue = Double.parseDouble(value);
                                        switch (parameter) {
                                            case "throwMultiplier":
                                                preset.throwMultiplier = doubleValue;
                                                break;
                                            case "fallMultiplier":
                                                preset.fallMultiplier = doubleValue;
                                                break;
                                            case "offset":
                                                preset.offset = doubleValue;
                                                break;
                                        }
                                        config.set("presets." + profilename + "." + parameter, doubleValue);
                                        saveConfig();
                                        player.sendMessage("You have set the " + parameter + " of the potion preset " + profilename + " to " + value + ".");
                                    } catch (NumberFormatException e) {
                                        player.sendMessage("Invalid value. Please enter a valid number.");
                                    }
                                } else {
                                    player.sendMessage("Invalid parameter. Available parameters are: throwMultiplier, fallMultiplier, offset.");
                                }
                            } else {
                                player.sendMessage("A potion preset with that name does not exist.");
                            }
                        } else {
                            player.sendMessage("Usage: /potion set <profilename> <parameter> <value>");
                        }
                    } else if (subcommand.equalsIgnoreCase("reload")) {
                        reloadConfig();
                        presets.clear();
                        choices.clear();
                        loadPresets();
                        loadChoices();
                        player.sendMessage("You have reloaded the plugin configuration.");
                    } else if (subcommand.equalsIgnoreCase("list")) {
                        player.sendMessage("Available potion presets and values:");
                        for (Map.Entry<String, PotionPreset> entry : presets.entrySet()) {
                            String name = entry.getKey();
                            PotionPreset preset = entry.getValue();
                            player.sendMessage(name + ": throwMultiplier = " + preset.throwMultiplier + ", fallMultiplier = " + preset.fallMultiplier + ", offset = " + preset.offset);
                        }
                    }
                } else {
                    player.sendMessage("Usage: /potion <subcommand>");
                    player.sendMessage("Usage: /potion create");
                    player.sendMessage("Usage: /potion setplayer");
                    player.sendMessage("Usage: /potion delete");
                    player.sendMessage("Usage: /potion reload");
                    player.sendMessage("Usage: /potion list");
                }
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        });

        getCommand("potion").setTabCompleter((sender, command, alias, args) -> {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.add("create");
                completions.add("setplayer");
                completions.add("delete");
                completions.add("set");
                completions.add("reload");
                completions.add("list");
            } else if (args.length == 2) {
                String subcommand = args[0];
                if (subcommand.equalsIgnoreCase("setplayer")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                } else if (subcommand.equalsIgnoreCase("delete") || subcommand.equalsIgnoreCase("set")) {

                    Iterator<String> iterator = presets.keySet().iterator();
                    while (iterator.hasNext()) {
                        String name = iterator.next();
                        completions.add(name);
                    }
                }
            } else if (args.length == 3) {
                String subcommand = args[0];
                if (subcommand.equalsIgnoreCase("set")) {
                    completions.add("throwMultiplier");
                    completions.add("fallMultiplier");
                    completions.add("offset");
                }
            }
            Iterator<String> iterator = presets.keySet().iterator();
            List<String> names = new ArrayList<>();
            while (iterator.hasNext()) {
                String name = iterator.next();
                names.add(name);
            }
            StringUtil.copyPartialMatches(args[args.length - 1], names, completions);
            Collections.sort(completions);
            return completions;
        });
}

            @Override
    public void onDisable() {
    }

    public void loadPresets() {
        ConfigurationSection section = config.getConfigurationSection("presets");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                double throwMultiplier = section.getDouble(name + ".throwMultiplier", 1.0);
                double fallMultiplier = section.getDouble(name + ".fallMultiplier", 1.0);
                double offset = section.getDouble(name + ".offset", 0.0);
                PotionPreset preset = new PotionPreset(throwMultiplier, fallMultiplier, offset);
                presets.put(name, preset);
            }
        }
    }

    public void loadChoices() {
        // 获取配置文件中的choices部分
        ConfigurationSection section = config.getConfigurationSection("choices");
        if (section != null) {
            // 遍历所有的玩家名字和预设名字
            for (String playername : section.getKeys(false)) {
                String profilename = section.getString(playername, "");
                // 获取对应的玩家对象，并存入choices映射中
                Player player = Bukkit.getPlayer(playername);
                if (player != null) {
                    choices.put(player, profilename);
                }
            }
        }
    }
}
