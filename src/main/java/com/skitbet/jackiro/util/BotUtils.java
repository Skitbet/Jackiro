package com.skitbet.jackiro.util;

import com.skitbet.jackiro.JackiroBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class BotUtils {
    public static int getTotalMembers() {
        int totalMembers = 0;
        for (Guild guild : JackiroBot.get().getJda().getGuilds()) {
            totalMembers += guild.getMemberCache().size();
        }
        return totalMembers;
    }
}
