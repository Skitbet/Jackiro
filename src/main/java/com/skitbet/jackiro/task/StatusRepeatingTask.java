package com.skitbet.jackiro.task;

import com.skitbet.jackiro.util.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.TimeUnit;

public class StatusRepeatingTask extends RepeatingTask {

    private int statusIndex = 0;

    private final JDA jda;

    public StatusRepeatingTask(JDA jda) {
        super(5, TimeUnit.SECONDS);
        this.jda = jda;
    }

    @Override
    protected void run() {
        switch (statusIndex) {
            case 0 -> {
                jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.competing("endurance tests."));
                statusIndex++;
            }
            case 1 -> {
                int totalMembers = BotUtils.getTotalMembers();
                jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching(totalMembers + " members"));
                statusIndex++;
            }
            case 2 -> {
                jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching(jda.getGuilds().size() + " guilds"));
                statusIndex = 0;
            }
        }
    }
}
