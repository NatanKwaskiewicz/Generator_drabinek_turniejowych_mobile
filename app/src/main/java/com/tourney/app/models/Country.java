package com.tourney.app.models;

import com.google.gson.annotations.SerializedName;

public class Country {

    @SerializedName("cca2")
    private String code;

    @SerializedName("flag")
    private String flag;

    @SerializedName("name")
    private Name nameObject;

    public static class Name {
        @SerializedName("common")
        private String common;

        public String getCommon() {
            return common;
        }
    }

    public String getCode() {
        return code;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return nameObject != null ? nameObject.getCommon() : "";
    }

    public String getDisplayLabel() {
        return flag + " " + getName();
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}