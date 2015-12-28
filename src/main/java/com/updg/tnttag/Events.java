package com.updg.tnttag;

import com.updg.CR_API.APIPlugin;
import com.updg.CR_API.Events.BungeeReturnIdEvent;
import com.updg.CR_API.Events.LobbyUpdateCheckEvent;
import com.updg.CR_API.Models.APIPlayer;
import com.updg.CR_API.Utils.FireworkEffectPlayer;
import com.updg.tnttag.Models.TNTPlayer;
import com.updg.tnttag.Models.enums.GameStatus;
import com.updg.CR_API.Utils.StringUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;

/**
 * Created by Alex
 * Date: 17.06.13  19:46
 */
public class Events implements Listener {

    int tid = 0;
    int count = 10;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        Player user = event.getPlayer();
        TNTPlayer p = TNTTagPlugin.game.getPlayer(user.getName());
        if (p == null) {
            p = new TNTPlayer(user);
            if (TNTTagPlugin.game.getStatus() == GameStatus.WAITING) {
                if (TNTTagPlugin.game.getActivePlayers() < TNTTagPlugin.game.getMaxPlayers())
                    TNTTagPlugin.game.addPlayer(p);
                else
                    TNTTagPlugin.game.addSpectator(p);
            } else {
                TNTTagPlugin.game.addSpectator(p);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        e.getPlayer().getInventory().clear();
        final TNTPlayer p = TNTTagPlugin.game.getPlayer(e.getPlayer().getName());
        e.getPlayer().teleport(TNTTagPlugin.game.getLobby());
        if (TNTTagPlugin.game.getStatus() != GameStatus.WAITING) {
            p.sendMessage(TNTTagPlugin.prefix + "Игра уже началась.");
        } else {
            if (p.isSpectator()) {
                p.sendMessage(TNTTagPlugin.prefix + "В игре нет свободных мест. Вы зашли как налюбдающий!");
            } else {
                e.setJoinMessage(TNTTagPlugin.prefix + e.getPlayer().getName() + " вошел на арену. " + Bukkit.getOnlinePlayers().length + "/" + TNTTagPlugin.game.getMinPlayers());
                if (TNTTagPlugin.game.isAbleToStart()) {
                    TNTTagPlugin.game.preGame();
                } else {
                    TNTTagPlugin.game.sendUpdatesToLobby();
                    e.getPlayer().sendMessage(TNTTagPlugin.prefix + "Игра начнется когда наберется " + TNTTagPlugin.game.getMinPlayers() + " " + StringUtil.plural(TNTTagPlugin.game.getMinPlayers(), "игрок", "игрока", "игроков"));
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if (TNTTagPlugin.game.getPlayers().containsKey(e.getPlayer().getName())) {
            TNTTagPlugin.game.killPlayer(e.getPlayer());
        }
        TNTPlayer p = TNTTagPlugin.game.getPlayer(e.getPlayer().getName());
        if (p != null && TNTTagPlugin.game.getStatus() == GameStatus.WAITING) {
            TNTTagPlugin.game.removePlayer(p);
            TNTTagPlugin.game.removeSpectator(p);
        }
        TNTTagPlugin.game.sendUpdatesToLobby();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            e.setCancelled(true);
            p.setHealth(p.getMaxHealth());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (TNTTagPlugin.game.getStatus() == GameStatus.INGAME) {
            if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
                e.setCancelled(true);
                TNTPlayer d = TNTTagPlugin.game.getPlayer(((Player) e.getDamager()).getName());
                TNTPlayer v = TNTTagPlugin.game.getPlayer(((Player) e.getEntity()).getName());
                if (d.isThing() && !v.isThing()) {
                    v.nowThing();
                    d.noMoreThing();
                    FireworkEffectPlayer boom = new FireworkEffectPlayer();
                    FireworkEffect fe = FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.GREEN).build();
                    Location targetLoc = v.getBukkitModel().getLocation().add(0, 3, 0);
                    try {
                        boom.playFirework(targetLoc, fe);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onChangeHunger(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onGetID(BungeeReturnIdEvent e) {
        TNTTagPlugin.game.getPlayer(e.getUsername().toLowerCase()).setId(e.getId());
    }

    @EventHandler
    public void onNeedUpdate(LobbyUpdateCheckEvent e) {
        TNTTagPlugin.game.sendUpdatesToLobby();
    }

    @EventHandler
    public void inv(InventoryMoveItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (!e.getPlayer().isOp() || e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        TNTPlayer p = TNTTagPlugin.game.getPlayer(e.getPlayer().getName().toLowerCase());
        APIPlayer apiPlayer = APIPlugin.getPlayer(e.getPlayer().getName());
        String msg = apiPlayer.getPrefix() + ChatColor.RESET + apiPlayer.getNickColor() + e.getPlayer().getDisplayName() + ChatColor.RESET + apiPlayer.getColonColor() + ": " + ChatColor.RESET + apiPlayer.getMessageColor() + e.getMessage();
        if (TNTTagPlugin.game.getStatus() == GameStatus.INGAME) {
            if (p.isSpectator()) {
                for (TNTPlayer i : TNTTagPlugin.game.getSpectatorsArray()) {
                    i.getBukkitModel().sendMessage(msg);
                }
            } else {
                for (TNTPlayer i : TNTTagPlugin.game.getActivePlayersArray()) {
                    i.getBukkitModel().sendMessage(msg);
                }
                for (TNTPlayer i : TNTTagPlugin.game.getSpectatorsArray()) {
                    i.getBukkitModel().sendMessage(msg);
                }
            }
        } else {
            for (TNTPlayer i : TNTTagPlugin.game.getActivePlayersArray()) {
                i.getBukkitModel().sendMessage(msg);
            }
            for (TNTPlayer i : TNTTagPlugin.game.getSpectatorsArray()) {
                i.getBukkitModel().sendMessage(msg);
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().clear();
    }
}
