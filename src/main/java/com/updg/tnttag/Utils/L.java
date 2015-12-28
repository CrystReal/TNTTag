package com.updg.tnttag.Utils;

import com.updg.tnttag.TNTTagPlugin;

import java.util.logging.Level;

/**
 * Created by Alex
 * Date: 15.12.13  13:17
 */
public class L {
    public static void $(String str) {
        TNTTagPlugin.getInstance().getLogger().log(Level.INFO, str);
    }

    public static void $(Level l, String str) {
        TNTTagPlugin.getInstance().getLogger().log(l, str);
    }
}
