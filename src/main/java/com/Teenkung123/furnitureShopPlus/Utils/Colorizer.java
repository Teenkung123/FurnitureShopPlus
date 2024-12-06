package com.Teenkung123.furnitureShopPlus.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Colorizer {

    public static Component colorize(String text) {
        return MiniMessage.miniMessage().deserialize(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

}
