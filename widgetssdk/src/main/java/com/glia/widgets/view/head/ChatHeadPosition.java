package com.glia.widgets.view.head;

import androidx.core.util.Pair;

public class ChatHeadPosition {
    private Integer posX;
    private Integer posY;

    public void set(Integer x, Integer y) {
        posX = x;
        posY = y;
    }

    public Pair<Integer, Integer> get() {
        return new Pair<>(posX, posY);
    }

    public static ChatHeadPosition getInstance() {
        return new ChatHeadPosition();
    }
}
