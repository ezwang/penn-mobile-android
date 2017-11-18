package com.pennapps.labs.pennmobile.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Machines {

    @SerializedName("dryers")
    @Expose
    private Dryers dryers;
    @SerializedName("washers")
    @Expose
    private Washers washers;

    public Dryers getDryers() {
        return dryers;
    }

    public Washers getWashers() {
        return washers;
    }
}