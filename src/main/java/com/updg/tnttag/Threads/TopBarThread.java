package com.updg.tnttag.Threads;

import com.updg.tnttag.TNTTagPlugin;
import com.updg.tnttag.Models.enums.GameStatus;
import com.updg.CR_API.Utils.StringUtil;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Alex
 * Date: 06.12.13  16:14
 */
public class TopBarThread extends Thread implements Runnable {
    public void run() {
        while (true) {
            try {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (TNTTagPlugin.game.getStatus() == GameStatus.WAITING) {
                        if (TNTTagPlugin.game.tillGame != TNTTagPlugin.game.tillGameDefault)
                            BarAPI.setMessage(p, ChatColor.GREEN + "До игры" + StringUtil.plural(TNTTagPlugin.game.tillGame, " осталась " + TNTTagPlugin.game.tillGame + " секунда", " осталось " + TNTTagPlugin.game.tillGame + " секунды", " осталось " + TNTTagPlugin.game.tillGame + " секунд") + ".", (float) TNTTagPlugin.game.tillGame / ((float) TNTTagPlugin.game.tillGameDefault / 100F));
                        else if (TNTTagPlugin.game.getActivePlayers() < TNTTagPlugin.game.getMinPlayers())
                            BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков.", (float) TNTTagPlugin.game.getActivePlayers() * ((float) TNTTagPlugin.game.getMinPlayers() / 100F));
                        else
                            BarAPI.setMessage(p, ChatColor.GREEN + "Ожидаем игроков.", 100F);
                    }
                    if (TNTTagPlugin.game.getStatus() == GameStatus.PRE_GAME) {
                        BarAPI.setMessage(p, ChatColor.RED + "Разбегайся! До резни" + StringUtil.plural(TNTTagPlugin.game.tillGame, " осталась " + TNTTagPlugin.game.tillGame + " секунда", " осталось " + TNTTagPlugin.game.tillGame + " секунды", " осталось " + TNTTagPlugin.game.tillGame + " секунд") + ".", (float) TNTTagPlugin.game.tillGame / (10F / 100F));
                    }
                    if (TNTTagPlugin.game.getStatus() == GameStatus.INGAME) {
                        if (TNTTagPlugin.game.isRelax) {
                            BarAPI.setMessage(p, ChatColor.GREEN + "Передышка", (float) TNTTagPlugin.game.gameTimer / ((float) TNTTagPlugin.game.totalGameTimer / 100F));
                        } else {
                            BarAPI.setMessage(p, ChatColor.GREEN + "Игра", (float) TNTTagPlugin.game.gameTimer / ((float) TNTTagPlugin.game.totalGameTimer / 100F));
                        }
                    }
                    if (TNTTagPlugin.game.getStatus() == GameStatus.POSTGAME) {
                        BarAPI.setMessage(p, ChatColor.AQUA + "Победил " + TNTTagPlugin.game.winner.getName(), 100F);
                    }
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
