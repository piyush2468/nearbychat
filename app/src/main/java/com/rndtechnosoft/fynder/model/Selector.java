package com.rndtechnosoft.fynder.model;

/**
 * Created by Ravi on 3/14/2017.
 */

public class Selector {
    private int id;
    private String name;
    private String key;
    private boolean selected;

    public Selector(){
    }

    public Selector(int id, String name){
        this.id = id;
        this.name = name;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

