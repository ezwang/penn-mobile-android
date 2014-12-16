package com.pennapps.labs.pennmobile.classes;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adel on 12/16/14.
 * Class for Person from Directory
 * Will replace Person class at some point
 */
public class DirectoryPerson {
    @SerializedName("person_id")
    public String id;
    @SerializedName("list_name")
    public String name;
    @SerializedName("list_affiliation")
    public String affiliation;
    @SerializedName("list_email")
    public String email;
    @SerializedName("list_organization")
    public String organization;
    @SerializedName("list_phone")
    public String phone;
    @SerializedName("list_title_or_major")
    public String title_or_major;
}
