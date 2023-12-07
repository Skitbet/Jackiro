package com.skitbet.jackiro.command;

import com.skitbet.jackiro.module.Module;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandInfo {
    private final String name;
    private final String description;
    private final List<CommandOption> options;
    private final List<Permission> permissions;
    private final Module module;
    private final Method method;
    private final Object object;
    private final int cooldown;

    public CommandInfo(String name, String description, List<CommandOption> options, List<Permission> permissions, Module module, Method method, Object object, int cooldown) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.permissions = permissions;
        this.module = module;
        this.method = method;
        this.object = object;
        this.cooldown = cooldown;
    }

    public void execute(SlashCommandInteractionEvent event) throws InvocationTargetException, IllegalAccessException {
        List<Object> arguments = new ArrayList<>();
        arguments.add(event);
        for (CommandOption option : options) {
            OptionType type = option.getType();
            String optionName = option.getName();

            if (event.getOption(optionName) == null) {
                if (option.getType() == OptionType.BOOLEAN) {
                    arguments.add(false);
                    continue;
                }
                arguments.add(null);
                continue;
            }

            switch (type) {
                case STRING -> arguments.add(event.getOption(optionName).getAsString());
                case BOOLEAN -> arguments.add(event.getOption(optionName).getAsBoolean());
                case USER -> arguments.add(event.getOption(optionName).getAsUser());
            }
        }
        method.invoke(object, arguments.toArray());
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Module getModule() {
        return module;
    }

    public int getCooldown() {
        return cooldown;
    }
}
