package com.updg.tnttag;

import com.updg.CR_API.APIPlugin;
import com.updg.CR_API.MQ.senderUpdatesToCenter;
import com.updg.tnttag.Models.TNTPlayer;
import com.updg.tnttag.Models.enums.GameStatus;
import com.updg.tnttag.Threads.TopBarThread;
import com.updg.tnttag.Utils.EconomicSettings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Alex
 * Date: 17.06.13  20:26
 */
public class Game {
    private HashMap<String, TNTPlayer> players = new HashMap<String, TNTPlayer>();
    private HashMap<String, TNTPlayer> spectators = new HashMap<String, TNTPlayer>();

    private Location lobby;
    private Location spawn;
    private GameStatus status;
    private int minPlayers = 0;
    private int maxPlayers = 12;

    public int tillGameDefault = 15;
    public int tillGame = 15;
    public int relaxTime = 10;

    public TNTPlayer winner;
    private int tid;

    private long timeStart = 0;
    private long timeEnd = 0;

    private int timerThread = 0;
    public boolean isRelax = false;
    public int gameTimer = 0;
    public int totalGameTimer = 0;

    public Game() {
        this.lobby = TNTTagPlugin.getInstance().stringToLoc(TNTTagPlugin.getInstance().getConfig().getString("lobby"));
        this.spawn = TNTTagPlugin.getInstance().stringToLoc(TNTTagPlugin.getInstance().getConfig().getString("spawn"));
        this.minPlayers = TNTTagPlugin.getInstance().getConfig().getInt("minPlayers", 2);
        this.maxPlayers = TNTTagPlugin.getInstance().getConfig().getInt("maxPlayers", 24);
        this.relaxTime = TNTTagPlugin.getInstance().getConfig().getInt("relaxTimer", 10);
        this.tillGameDefault = TNTTagPlugin.getInstance().getConfig().getInt("gameTimer", 60);
        this.tillGame = this.tillGameDefault;
    }

    public void sendUpdatesToLobby() {
        String s = GameStatus.WAITING.toString();
        if (TNTTagPlugin.game.getMaxPlayers() <= TNTTagPlugin.game.getActivePlayers())
            s = "IN_GAME";
        if (TNTTagPlugin.game.getStatus() == GameStatus.WAITING) {
            if (TNTTagPlugin.game.tillGame < TNTTagPlugin.game.tillGameDefault)
                senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + TNTTagPlugin.game.getActivePlayers() + ":" + TNTTagPlugin.game.getMaxPlayers() + ":До игры " + TNTTagPlugin.game.tillGame + " c.");
            else
                senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":" + s + ":" + "В ОЖИДАНИИ" + ":" + TNTTagPlugin.game.getActivePlayers() + ":" + TNTTagPlugin.game.getMaxPlayers() + ":Набор игроков");
        } else if (TNTTagPlugin.game.getStatus() == GameStatus.PRE_GAME)
            senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":IN_GAME:" + "НАЧАЛО" + ":" + TNTTagPlugin.game.getActivePlayers() + ":" + TNTTagPlugin.game.getMaxPlayers());
        else if (TNTTagPlugin.game.getStatus() == GameStatus.POSTGAME) {
            senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":IN_GAME:" + "ИГРА ОКОНЧЕНА" + ":" + TNTTagPlugin.game.getActivePlayers() + ":" + TNTTagPlugin.game.getMaxPlayers() + ":Победил " + TNTTagPlugin.game.winner.getName());
        } else if (TNTTagPlugin.game.getStatus() == GameStatus.INGAME || TNTTagPlugin.game.getStatus() == GameStatus.POSTGAME)
            senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":IN_GAME:" + "ИГРА" + ":" + TNTTagPlugin.game.getActivePlayers() + ":" + TNTTagPlugin.game.getMaxPlayers() + ":Бой");
        else if (TNTTagPlugin.game.getStatus() == GameStatus.RELOAD)
            senderUpdatesToCenter.send(TNTTagPlugin.getInstance().serverId + ":DISABLED:" + "ОФФЛАЙН" + ":0:0:");

    }

    public boolean isAbleToStart() {
        return this.status == GameStatus.WAITING && this.players.size() >= this.minPlayers;
    }

    public void getReady() {
        this.status = GameStatus.WAITING;
        new TopBarThread().start();
        sendUpdatesToLobby();
    }

    public void preGame() {
        if (tid == 0)
            tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(TNTTagPlugin.getInstance(), new Runnable() {
                public void run() {
                    if (Bukkit.getOnlinePlayers().length < getMinPlayers()) {
                        Bukkit.broadcastMessage(TNTTagPlugin.prefix + "Старт игры отменен так как игрок(и) покинули сервер.");
                        sendUpdatesToLobby();
                        Bukkit.getScheduler().cancelTask(tid);
                        tid = 0;
                        tillGame = tillGameDefault;
                    } else if (tillGame > 0) {
                        tillGame--;
                    } else {
                        Bukkit.getScheduler().cancelTask(tid);
                        TNTTagPlugin.game.startGame();
                    }
                }
            }, 0, 20);
    }

    public void startGame() {
        if (status == GameStatus.INGAME)
            return;
        this.status = GameStatus.INGAME;
        for (TNTPlayer p : this.players.values()) {
            p.getBukkitModel().setGameMode(GameMode.SURVIVAL);
            p.getBukkitModel().setFlying(false);
            p.getBukkitModel().setAllowFlight(false);
            p.getBukkitModel().teleport(getSpawn());
            p.getBukkitModel().getInventory().clear();
            p.setWasInGame(true);
        }

        Bukkit.broadcastMessage(TNTTagPlugin.prefix + ChatColor.RED + "БОЙ!");
        timeStart = System.currentTimeMillis() / 1000;
        status = GameStatus.INGAME;
        startTimerThread();
        sendUpdatesToLobby();
    }

    public void startTimerThread() {
        isRelax = true;
        gameTimer = 1;
        this.timerThread = Bukkit.getScheduler().scheduleSyncRepeatingTask(TNTTagPlugin.getInstance(), new Runnable() {
            public void run() {
                if (getStatus() == GameStatus.INGAME) {
                    gameTimer--;
                    if (gameTimer == 0 && isRelax) {
                        if (getActivePlayers() > 24) {
                            makeThings(6);
                        } else if (getActivePlayers() > 19) {
                            makeThings(5);
                        } else if (getActivePlayers() > 15) {
                            makeThings(4);
                        } else if (getActivePlayers() > 13) {
                            makeThings(3);
                        } else if (getActivePlayers() > 9) {
                            makeThings(2);
                        } else {
                            makeThings(1);
                        }
                        if (getActivePlayers() <= 3) {
                            for (TNTPlayer p : getActivePlayersArray()) {
                                p.getBukkitModel().teleport(spawn);
                                p.sendMessage(TNTTagPlugin.prefix + ChatColor.RED + ChatColor.BOLD + "Дедматч! Все игроки телепортированы на спавн.");
                            }
                        }
                        isRelax = false;
                        if (getActivePlayers() > 10)
                            gameTimer = totalGameTimer = 60;
                        else
                            gameTimer = totalGameTimer = 30;
                    }
                    if (gameTimer == 0 && !isRelax) {
                        blowAllTHings();
                        isRelax = true;
                        gameTimer = totalGameTimer = relaxTime;
                    }
                }
            }
        }, 0, 20);
    }

    public void makeThings(int count) {
        int i = 0;
        Random rnd = new Random();
        ArrayList<TNTPlayer> l = new ArrayList<TNTPlayer>(this.getActivePlayersArray());
        while (i < count) {
            int r = rnd.nextInt(l.size() - 1);
            if (!l.get(r).isThing()) {
                l.get(r).nowThing();
                i++;
            }
        }
    }

    public void blowAllTHings() {
        ArrayList<TNTPlayer> arr = new ArrayList<TNTPlayer>(getActivePlayersArray());
        for (TNTPlayer p : arr) {
            if (p.isThing()) {
                p.getBukkitModel().getWorld().createExplosion(p.getBukkitModel().getLocation().getBlock().getRelative(BlockFace.UP).getLocation(), 4F, false);
                p.getBukkitModel().getWorld().playSound(p.getBukkitModel().getLocation(), Sound.FUSE, 3f, 1F);
                killPlayer(p.getBukkitModel());
            }
        }
        if (getActivePlayers() == 1) {
            endGame();
        }
    }

    public Location getSpawn() {
        spawn.setPitch(12);
        spawn.setYaw(-90);
        return spawn;
    }

    public void endGame() {
        if (this.status == GameStatus.INGAME)
            this.status = GameStatus.POSTGAME;
        else
            return;
        if (this.players.size() < 2) {
            this.timeEnd = System.currentTimeMillis() / 1000L;
            for (TNTPlayer p : this.players.values()) {
                p.sendMessage(TNTTagPlugin.prefix + "Ты выиграл бой!");
                p.sendMessage(TNTTagPlugin.prefixMoney + "+" + EconomicSettings.win + " опыта.");
                p.getStats().setInGameTime(System.currentTimeMillis() / 1000L - this.timeStart);
                winner = p;
                winner.addExp(EconomicSettings.win);
            }
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (winner != null && !p1.getName().equals(winner.getName())) {
                    p1.sendMessage(TNTTagPlugin.prefix + "Игрок " + winner.getName() + " выиграл!");
                }
            }
        } else {
            for (TNTPlayer p : this.players.values()) {
                p.sendMessage(TNTTagPlugin.prefix + "Игра остановлена системой.");
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(TNTTagPlugin.prefix + "Сервер перезагрузится через 15 секунд.");
            p.getInventory().clear();
        }
        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(TNTTagPlugin.prefix + "Сервер перезагрузится через 10 секунд.");
                            }
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(TNTTagPlugin.prefix + "Сервер перезагрузится через 5 секунд.");
                            }
                            Thread.sleep(5000);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendMessage(TNTTagPlugin.prefix + "Сервер перезагружается.");
                            }
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        TNTTagPlugin.getInstance().sendStats();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        sendUpdatesToLobby();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    }
                }).start();
    }

    public void killPlayer(Player p) {
        if ((this.status == GameStatus.INGAME || this.status == GameStatus.PRE_GAME) && this.players.containsKey(p.getName().toLowerCase())) {
            TNTPlayer pl = this.players.get(p.getName().toLowerCase());
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (!p1.getName().equals(p.getName())) {
                    p1.sendMessage(TNTTagPlugin.prefix + pl.getColorName() + " взорвался.");
                }
            }
            pl.sendMessage(TNTTagPlugin.prefix + "Ты взорвался!");
            pl.getStats().setInGameTime(System.currentTimeMillis() / 1000L - this.timeStart);
            pl.getBukkitModel().closeInventory();
            pl.getBukkitModel().getInventory().clear();
            this.players.remove(pl.getName().toLowerCase());
            this.spectators.put(pl.getName().toLowerCase(), pl);
            pl.setSpectator(true);
            pl.getBukkitModel().teleport(getSpawn());

            for (TNTPlayer p1 : this.players.values()) {
                p1.addExp(EconomicSettings.anotherDie);
                p1.sendMessage(TNTTagPlugin.prefixMoney + "+" + EconomicSettings.anotherDie + " опыта.");
            }

            if (getActivePlayers() < 2) {
                TNTTagPlugin.game.endGame();
            }
        } else {
            p.teleport(getLobby());
        }
        sendUpdatesToLobby();
    }

    public Location getLobby() {
        return this.lobby;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getActivePlayers() {
        int i = 0;
        for (TNTPlayer p : this.players.values()) {
            if (!p.isSpectator())
                i++;
        }
        return i;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public TNTPlayer getPlayer(String name) {
        if (this.players.containsKey(name.toLowerCase()))
            return this.players.get(name.toLowerCase());
        if (this.spectators.containsKey(name.toLowerCase()))
            return this.spectators.get(name.toLowerCase());
        return null;
    }

    public void addSpectator(TNTPlayer p) {
        this.spectators.put(p.getName(), p);
    }

    public boolean isSpectator(TNTPlayer p) {
        return this.spectators.containsKey(p.getName());
    }

    public void addPlayer(TNTPlayer p) {
        this.players.put(p.getName().toLowerCase(), p);
    }

    public HashMap<String, TNTPlayer> getPlayers() {
        return players;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public Collection<TNTPlayer> getActivePlayersArray() {
        Collection<TNTPlayer> i = new ArrayList<TNTPlayer>();
        for (TNTPlayer p : this.players.values()) {
            if (!p.isSpectator())
                i.add(p);
        }
        return i;
    }

    public Collection<TNTPlayer> getSpectatorsArray() {
        return this.spectators.values();
    }

    public void removeSpectator(TNTPlayer p) {
        if (p.isSpectator())
            this.spectators.remove(p.getName());
    }

    public void removePlayer(TNTPlayer p) {
        if (this.players.containsKey(p.getName()))
            this.players.remove(p.getName());
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public void broadcast(String m) {
        for (Player p : Bukkit.getOnlinePlayers())
            p.sendMessage(m);
    }
}
