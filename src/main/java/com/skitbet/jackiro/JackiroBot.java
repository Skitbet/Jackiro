package com.skitbet.jackiro;

import com.mongodb.internal.connection.CommandMessage;
import com.skitbet.jackiro.command.CommandHandler;
import com.skitbet.jackiro.database.DatabaseManager;
import com.skitbet.jackiro.guild.GuildDataHandler;
import com.skitbet.jackiro.listeners.GuildListeners;
import com.skitbet.jackiro.task.StatusRepeatingTask;
import com.skitbet.jackiro.util.ButtonMenuBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;

public class JackiroBot {

    private JDA jda;

    // Managers
    private DatabaseManager databaseManager;
    private GuildDataHandler guildDataHandler;
    private CommandHandler commandHandler;

    // Tasks
    private StatusRepeatingTask statusTask;

    // Instance
    private static JackiroBot instance;
    public static JackiroBot get() {
        return instance;
    }


    public JackiroBot(String[] launchArgs) {
        instance = this;

        JDABuilder jdaBuilder = JDABuilder.createDefault(launchArgs[0])
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);

        new Thread(() -> {

            databaseManager = DatabaseManager.get();
            databaseManager.setup(launchArgs[1]);

            try {
                this.jda = jdaBuilder.build();
                this.jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("startup sounds."));
                this.jda.awaitReady(); // wait until the bot is actually be logged in before setting up everything else
                this.setup();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).run();
    }

    private void setup() {

        List<Guild> guilds = this.jda.getGuilds();

        guildDataHandler = GuildDataHandler.get();
        guildDataHandler.setup(guilds);

        commandHandler = CommandHandler.get();
        commandHandler.setup(guilds);

        this.jda.addEventListener(ButtonMenuBuilder.LISTENER);
        this.jda.addEventListener(new GuildListeners());

        this.statusTask = new StatusRepeatingTask(jda);
        this.statusTask.start();
    }

    public JDA getJda() {
        return jda;
    }
}
