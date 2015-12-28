package com.updg.tnttag.Models;

/**
 * Created by Alex
 * Date: 15.12.13  13:40
 */
public class TNTPlayerStats {
    private boolean winner = false;
    private long inGameTime = 0;
    private int takeTNT = 0;
    private int catchTNT = 0;

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public long getInGameTime() {
        return inGameTime;
    }

    public void setInGameTime(long inGameTime) {
        this.inGameTime = inGameTime;
    }

    public void addTakeTNT() {
        this.takeTNT++;
    }

    public void addCatchTNT() {
        this.catchTNT++;
    }

    public int getTakeTNT() {
        return takeTNT;
    }

    public int getCatchTNT() {
        return catchTNT;
    }
}
