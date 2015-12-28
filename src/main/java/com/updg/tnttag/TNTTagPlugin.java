package com.updg.tnttag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.updg.CR_API.MQ.senderStatsToCenter;
import com.updg.tnttag.DataServerStats.gameStats;
import com.updg.tnttag.DataServerStats.playerStats;
import com.updg.tnttag.Models.TNTPlayer;
import com.updg.tnttag.Models.enums.GameStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex
 * Date: 17.06.13 18:07
 */
public class TNTTagPlugin extends JavaPlugin {
    public static String prefix = ChatColor.BOLD + "" + ChatColor.DARK_AQUA + "[TNT Tag] " + ChatColor.RESET;
    public static String prefixMoney = ChatColor.BOLD + "" + ChatColor.GOLD;
    public static Game game;
    private static TNTTagPlugin instance;
    public int serverId = 0;

    public static TNTTagPlugin getInstance() {
        return instance;
    }

    public void onEnable() {
        TNTTagPlugin.instance = this;
        this.serverId = getConfig().getInt("serverId", 0);

        getServer().getPluginManager().registerEvents(new Events(), this);
        game = new Game();
        game.getReady();
    }

    public void onDisable() {
        game.setStatus(GameStatus.RELOAD);
        game.sendUpdatesToLobby();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
        TNTPlayer p;
        if (sender instanceof Player) {
            p = game.getPlayer(sender.getName());
        } else {
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("spectate")) {
            if (p.getBukkitModel().hasPermission("dk.spectate")) {
                if (p.isSpectator() || (game.getStatus() == GameStatus.INGAME || game.getStatus() == GameStatus.POSTGAME)) {
                    p.sendMessage("Нельзя менять статус во время игры.");
                    return false;
                }
                if (p.isSpectator() && game.getActivePlayers() >= game.getMaxPlayers()) {
                    p.sendMessage("Ошибка смены статуса. Сервер полный.");
                    return false;
                }
                if (p.isSpectator()) {
                    p.setSpectator(false);
                    game.removeSpectator(p);
                    game.addPlayer(p);
                    p.sendMessage("Теперь ты обычный игрок");
                } else {
                    p.setSpectator(true);
                    game.removePlayer(p);
                    game.addSpectator(p);
                    p.sendMessage("Теперь ты наблюдающий");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Не достаточно прав");
            }
        }
        return false;
    }

    public Location stringToLoc(String string) {
        String[] loc = string.split("\\|");
        World world = Bukkit.getWorld(loc[0]);
        Double x = Double.parseDouble(loc[1]);
        Double y = Double.parseDouble(loc[2]);
        Double z = Double.parseDouble(loc[3]);

        return new Location(world, x, y, z);
    }

    public void sendStats() {
        gameStats game = new gameStats();
        game.setServerId(this.serverId);
        game.setWinner(TNTTagPlugin.game.winner.getId());
        game.setStart(TNTTagPlugin.game.getTimeStart());
        game.setEnd(TNTTagPlugin.game.getTimeEnd());
        List<playerStats> players = new ArrayList<playerStats>();
        playerStats tmpPlayer;
        for (TNTPlayer p : TNTTagPlugin.game.getActivePlayersArray()) {
            tmpPlayer = new playerStats();
            tmpPlayer.setPlayerId(p.getId());
            tmpPlayer.setIsWinner(p.getId() == game.getWinner());
            tmpPlayer.setTimeInGame(p.getStats().getInGameTime());
            players.add(tmpPlayer);
        }
        for (TNTPlayer p : TNTTagPlugin.game.getSpectatorsArray()) {
            if (p.wasInGame()) {
                tmpPlayer = new playerStats();
                tmpPlayer.setPlayerId(p.getId());
                tmpPlayer.setIsWinner(p.getId() == game.getWinner());
                tmpPlayer.setTimeInGame(p.getStats().getInGameTime());
                players.add(tmpPlayer);
            }
        }
        game.setPlayers(players);
        try {
            String stat = new ObjectMapper().writeValueAsString(game);
            senderStatsToCenter.send("tnttag", stat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
