package com.ddang_.chatmanager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public final class Chatmanager extends JavaPlugin implements Listener, CommandExecutor {

    static Plugin instance;
    private static int chatLimit;
    private static final HashMap<String, Long> chatLimitPerPlayer = new HashMap<>();

    @Override
    public void onEnable() {

        instance = this;

        //명령어 등록
        this.getCommand("채팅제한").setExecutor(this);

        //리스너 등록
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        File file = new File(instance.getDataFolder(), File.separator+"system.yml");
        YamlConfiguration system = YamlConfiguration.loadConfiguration(file);

        chatLimit = system.getInt("system.chatlimit");
    }

    @Override
    public void onDisable() {
        File file = new File(instance.getDataFolder(), File.separator+"system.yml");
        YamlConfiguration system = YamlConfiguration.loadConfiguration(file);
        system.set("system.chatlimit", chatLimit);
        try {
            system.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void chat(PlayerChatEvent e){
        Player p = e.getPlayer();
        chatLimitPerPlayer.putIfAbsent(p.getName(), 0L);
        long has = chatLimitPerPlayer.get(p.getName());
        int left = Math.toIntExact((has / 1000) - (System.currentTimeMillis() / 1000) + chatLimit);
        if (left > 0){
            p.sendMessage("§c  [!]  §7채팅이 너무 빠릅니다. 남은 대기 시간: §f"+left+"§7초");
            e.setCancelled(true);
            return;
        }
        chatLimitPerPlayer.put(p.getName(), System.currentTimeMillis());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player p = (Player) sender;

            //관리자 권한이 있는가?
            if (!p.isOp()){
                return false;
            }

            if (args.length == 0){
                p.sendMessage("§c  [!] §f/채팅제한 <정수> §7- 채팅 지연 시간을 <정수>로 설정합니다.");
                return false;
            }

            if (args.length >= 2) {
                p.sendMessage("§c  [!] §f/채팅제한 <정수> §7를 입력해주십시오.");
                return false;
            }

            //정수인가?
            if (isInt(args[0])){
                chatLimit = Integer.parseInt(args[0]);
                p.sendMessage("§a  [!] §f채팅 지연 시간 제한 속도를 "+args[0]+"으로 설정했습니다.");
                return false;
            }

            p.sendMessage("§c  [!] §f/채팅 제한 <정수> §7 중 <정수> 부분에 정수를 입력해주십시오.");


        }
        return false;
    }

    private static Boolean isInt(String str){
        try{
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }
}
