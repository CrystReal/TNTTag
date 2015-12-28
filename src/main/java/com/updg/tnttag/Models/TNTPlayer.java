package com.updg.tnttag.Models;

import com.updg.CR_API.APIPlugin;
import com.updg.CR_API.Bungee.Bungee;
import com.updg.CR_API.DataServer.DSUtils;
import com.updg.tnttag.TNTTagPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Created by Alex
 * Date: 15.12.13  13:37
 */
public class TNTPlayer {
    private int id;
    private String name;

    private Player bukkitModel;
    private TNTPlayerStats stats;

    private double exp = 0;

    private boolean wasInGame = false;
    private boolean thing = false;

    public TNTPlayer(Player p) {
        this.setBukkitModel(p);
        this.name = p.getName();
        this.stats = new TNTPlayerStats();
        this.getIdFromBungee();
    }

    private void getIdFromBungee() {
        Bungee.isLogged(getBukkitModel(), getName());
    }

    public Player getBukkitModel() {
        return bukkitModel;
    }

    public void setBukkitModel(Player bukkitModel) {
        this.bukkitModel = bukkitModel;
    }

    public TNTPlayerStats getStats() {
        return stats;
    }

    public void sendMessage(String s) {
        getBukkitModel().sendMessage(s);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getColorName() {
        return APIPlugin.getPlayer(getName()).getNickColor() + getName() + ChatColor.RESET;
    }

    public void setSpectator(boolean b) {
        if (b) {
            this.hidePlayer();
            this.bukkitModel.setAllowFlight(true);
            removePotionEffects();
            this.getBukkitModel().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
            this.getBukkitModel().getInventory().clear();
            this.getBukkitModel().getInventory().setHelmet(null);
        } else {
            this.showPlayer();
            this.bukkitModel.setAllowFlight(false);
        }
    }

    private void hidePlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(this.bukkitModel);
        }
    }

    private void showPlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(this.bukkitModel);
        }
    }

    public boolean isSpectator() {
        return TNTTagPlugin.game.isSpectator(this);
    }


    public double getExp() {
        String[] out = DSUtils.getExpAndMoney(getBukkitModel());
        this.setExp(Double.parseDouble(out[0]));
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void withdrawExp(double v) {
        String[] out = DSUtils.withdrawPlayerExpAndMoney(getBukkitModel(), v, 0);
        this.setExp(Double.parseDouble(out[0]));
    }

    public void addExp(double v) {
        String[] out = DSUtils.addPlayerExpAndMoney(getBukkitModel(), v, 0);
        this.setExp(Double.parseDouble(out[0]));
    }

    public boolean wasInGame() {
        return wasInGame;
    }

    public void setWasInGame(boolean wasInGame) {
        this.wasInGame = wasInGame;
    }

    public boolean isThing() {
        return thing;
    }

    public void nowThing() {
        this.thing = true;
        removePotionEffects();
        //this.getBukkitModel().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        this.getBukkitModel().setWalkSpeed(0.6F);
        TNTTagPlugin.game.broadcast(TNTTagPlugin.prefix + getColorName() + " теперь водит!");
        this.getBukkitModel().getInventory().clear();
        this.getBukkitModel().getInventory().setHelmet(new ItemStack(Material.TNT));
        this.getBukkitModel().getInventory().addItem(new ItemStack(Material.TNT));
    }

    public void noMoreThing() {
        this.thing = false;
        removePotionEffects();
        //this.getBukkitModel().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        this.getBukkitModel().setWalkSpeed(0.4F);
        this.getBukkitModel().getInventory().clear();
        this.getBukkitModel().getInventory().setHelmet(null);
    }

    private void removePotionEffects() {
        /*for (PotionEffect p : this.getBukkitModel().getActivePotionEffects()) {
            this.getBukkitModel().removePotionEffect(p.getType());
        } */
        this.getBukkitModel().getActivePotionEffects().clear();
    }


}
