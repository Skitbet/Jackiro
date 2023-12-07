package com.skitbet.jackiro.module;

public enum Module {
    FUN("Fun", true),
    MUSIC("Music", true),
    NONE("none", false);

    private final String name;
    private final boolean saveable;

    Module(String name, boolean saveable) {
        this.name = name;
        this.saveable = saveable;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return name.toLowerCase().replace(" ", "_");
    }

    public static Module getModuleByID(String id) {
        for (Module value : values()) {
            if (value.getID().equalsIgnoreCase(id)) {
                return value;
            }
        }
        return null;
    }

    public boolean isSaveable() {
        return saveable;
    }
}
