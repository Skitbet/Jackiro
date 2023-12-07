package com.skitbet.jackiro.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandOption {

    private final String name;
    private final String description;
    private final boolean required;
    private final OptionType type;

    public CommandOption(String name, String description, boolean required, OptionType type) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public OptionType getType() {
        return type;
    }
}
