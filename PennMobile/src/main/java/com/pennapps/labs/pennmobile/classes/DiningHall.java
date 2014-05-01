package com.pennapps.labs.pennmobile.classes;

import java.util.HashMap;

public class DiningHall {

    private String name;
    private boolean open;
    private HashMap<String, String> dinnerMenu;
    private HashMap<String, String> lunchMenu;

    public DiningHall(String name, boolean open) {
        this.name = name;
        this.open = open;
    }

    public String getName() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setDinnerMenu(HashMap<String, String> dinnerMenu) {
        this.dinnerMenu = dinnerMenu;
    }

    public void setLunchMenu(HashMap<String, String> lunchMenu) {
        this.lunchMenu = lunchMenu;
    }
}