package com.skitbet.jackiro.command;

import com.skitbet.jackiro.JackiroBot;
import com.skitbet.jackiro.command.annotation.Command;
import com.skitbet.jackiro.command.annotation.options.Bool;
import com.skitbet.jackiro.command.annotation.options.Text;
import com.skitbet.jackiro.command.annotation.options.UserOpt;
import com.skitbet.jackiro.command.impl.fun.RaceCommand;
import com.skitbet.jackiro.command.impl.mod.ReSyncCommands;
import com.skitbet.jackiro.constants.MessageConstants;
import com.skitbet.jackiro.guild.GuildData;
import com.skitbet.jackiro.guild.GuildDataHandler;
import com.skitbet.jackiro.module.Module;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Import statements...

public class CommandHandler extends ListenerAdapter {

    // Command-related data structures
    private HashMap<String, CommandInfo> commandMethods;
    private HashMap<User, CommandInfo> cooldowns;
    private List<CommandRegistar> commandClasses;

    // Synchronization-related data structures and executor services
    private Queue<Guild> syncQueue;
    private ScheduledExecutorService syncExecutor;
    private CountDownLatch syncLatch;

    // Cooldown executor service
    private ScheduledExecutorService cooldownExecutor;

    // Singleton instance
    private static CommandHandler instance;

    // Singleton instance retrieval method
    public static CommandHandler get() {
        return instance == null ? instance = new CommandHandler() : instance;
    }

    // Initialization method
    public void setup(List<Guild> guilds) {
        // Event listener registration
        JackiroBot.get().getJda().addEventListener(this);

        // Initialization of data lists
        this.commandMethods = new HashMap<>();
        this.commandClasses = new ArrayList<>();

        // Registration of predefined command classes
        this.commandClasses.add(new RaceCommand());
        this.commandClasses.add(new ReSyncCommands());

        // setup cool down vars
        this.cooldowns = new HashMap<>();
        this.cooldownExecutor = Executors.newSingleThreadScheduledExecutor();

        // setup sync vars
        this.syncQueue = new LinkedList<>();
        this.syncExecutor = Executors.newSingleThreadScheduledExecutor();
        this.syncLatch = new CountDownLatch(1);

        // Synchronize all guilds at the start
        for (Guild guild : guilds) {
            syncGuild(guild);
        }

        // Schedule periodic synchronization
        syncExecutor.scheduleAtFixedRate(this::processNextSyncGuild, 0, 2, TimeUnit.SECONDS);
    }

    // Add guild to sync queue and return its position in the queue
    public int syncGuild(Guild guild) {
        syncQueue.add(guild);
        return syncQueue.size();
    }

    // Process the next guild in the sync queue
    public void processNextSyncGuild() {
        Guild guild = syncQueue.poll();
        if (guild != null) {
            syncLatch = new CountDownLatch(1);
            handleSync(guild);
            try {
                syncLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    // Handle synchronization for a specific guild
    public void handleSync(Guild guild) {
        GuildData data = GuildDataHandler.get().getGuildData(guild.getId());
        for (CommandRegistar commandClass : this.commandClasses) {
            registerCommand(commandClass, guild, data);
        }
        removedDisabledGuilds(guild, data);
        syncLatch.countDown();
    }

    // Register commands for a specific guild
    private void registerCommand(Object instance, Guild guild, GuildData data) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                // Extract command information from annotations
                Command slashCommand = method.getAnnotation(Command.class);
                Module commandModule = slashCommand.module();

                // Check if the module is enabled for the guild
                if (commandModule != Module.NONE && !data.isModuleEnabled(commandModule)) {
                    continue;
                }

                // Create command data and options
                CommandDataImpl commandData = createCommandData(slashCommand);
                List<CommandOption> options = createCommandOptions(method, commandData);

                // Create command information object
                CommandInfo commandInfo = new CommandInfo(
                        slashCommand.name(),
                        slashCommand.description(),
                        options,
                        Arrays.asList(slashCommand.permissions()),
                        slashCommand.module(),
                        method,
                        instance.getClass(),
                        slashCommand.cooldown()
                );

                // Store command information
                commandMethods.put(slashCommand.name(), commandInfo);
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.permissions()));

                try {
                    // Register the command with the guild
                    guild.upsertCommand(commandData).queue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Remove disabled commands for a specific guild
    private void removedDisabledGuilds(Guild guild, GuildData data) {
        guild.retrieveCommands().queue(commands -> {
            List<String> commandsToDelete = new ArrayList<>();

            for (Map.Entry<String, CommandInfo> entry : commandMethods.entrySet()) {
                CommandInfo info = entry.getValue();
                Module module = info.getModule();

                for (net.dv8tion.jda.api.interactions.commands.Command discordCommand : commands) {
                    if (discordCommand.getName().equalsIgnoreCase(info.getName()) &&
                            discordCommand.getDescription().equalsIgnoreCase(info.getDescription())) {
                        if (module != Module.NONE && !data.isModuleEnabled(module)) {
                            commandsToDelete.add(discordCommand.getId());
                        }
                        break;
                    }
                }
            }

            // Delete commands asynchronously with a delay
            deleteCommands(guild, commandsToDelete, 0, 1000);
        });
    }

    // Asynchronously delete commands with a delay
    private void deleteCommands(Guild guild, List<String> commandsToDelete, int index, long delay) {
        if (index < commandsToDelete.size()) {
            String commandId = commandsToDelete.get(index);
            guild.deleteCommandById(commandId).queue(
                    success -> {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Recursively delete the next command
                        deleteCommands(guild, commandsToDelete, index + 1, delay);
                    },
                    failure -> {
                        // Retry with an increased delay on failure
                        deleteCommands(guild, commandsToDelete, index, delay * 2);
                    }
            );
        }
    }

    // Handle slash command interactions
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        if (commandMethods.containsKey(commandName)) {
            CommandInfo commandInfo = commandMethods.get(commandName);
            try {
                // Check cooldown
                User user = event.getUser();
                if (cooldowns.containsKey(user) && cooldowns.get(user).getName().equalsIgnoreCase(commandName)) {
                    event.replyEmbeds(MessageConstants.getErrorEmbed("Please wait before using this command again.")).queue();
                    return;
                }

                // Execute the command
                commandInfo.execute(event);

                // Start cooldown
                if (commandInfo.getCooldown() > 0) {
                    cooldowns.put(event.getUser(), commandInfo);

                    cooldownExecutor.schedule(() -> {
                        cooldowns.remove(event.getUser());
                    }, commandInfo.getCooldown(), TimeUnit.SECONDS);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Create CommandDataImpl object from Command annotation
    private CommandDataImpl createCommandData(Command slashCommand) {
        return new CommandDataImpl(slashCommand.name(), slashCommand.description());
    }

    // Create CommandOption list from method parameters and CommandDataImpl
    private List<CommandOption> createCommandOptions(Method method, CommandDataImpl commandData) {
        List<CommandOption> options = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            // Determine option type based on annotations
            OptionType optionType = determineOptionType(parameter);
            if (optionType != null) {
                CommandOption commandOption = createCommandOption(parameter, optionType);
                options.add(commandOption);

                // Add the option to CommandDataImpl
                commandData.addOption(optionType, commandOption.getName(), commandOption.getDescription(), commandOption.isRequired());
            }
        }

        return options;
    }

    // Determine OptionType based on parameter annotations
    private OptionType determineOptionType(Parameter parameter) {
        if (parameter.isAnnotationPresent(Text.class)) {
            return OptionType.STRING;
        } else if (parameter.isAnnotationPresent(Bool.class)) {
            return OptionType.BOOLEAN;
        } else if (parameter.isAnnotationPresent(UserOpt.class)) {
            return OptionType.USER;
        }

        return null;
    }

    // Create CommandOption object based on parameter annotations
    private CommandOption createCommandOption(Parameter parameter, OptionType optionType) {
        String name = "";
        String description = "";
        boolean required = false;

        // Extract annotation values
        if (parameter.isAnnotationPresent(Text.class)) {
            Text textCMD = parameter.getAnnotation(Text.class);
            name = textCMD.name();
            description = textCMD.description();
            required = textCMD.required();
        } else if (parameter.isAnnotationPresent(Bool.class)) {
            Bool boolCMD = parameter.getAnnotation(Bool.class);
            name = boolCMD.name();
            description = boolCMD.description();
            required = boolCMD.required();
        } else if (parameter.isAnnotationPresent(UserOpt.class)) {
            UserOpt userOptCMD = parameter.getAnnotation(UserOpt.class);
            name = userOptCMD.name();
            description = userOptCMD.description();
            required = userOptCMD.required();
        }

        return new CommandOption(name, description, required, optionType);
    }

    public Queue<Guild> getSyncQueue() {
        return syncQueue;
    }
}
