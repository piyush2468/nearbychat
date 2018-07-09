package com.rndtechnosoft.fynder.model;

/**
 * Created by Ravi on 11/16/2016.
 */

public class Item {
    public final String text;
    public final int index;
    public Item(String text, Integer index) {
        this.text = text;
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }
}
