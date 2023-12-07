package com.skitbet.jackiro.listeners;

import com.skitbet.jackiro.constants.MessageConstants;
import com.skitbet.jackiro.guild.GuildData;
import com.skitbet.jackiro.guild.GuildDataHandler;
import com.skitbet.jackiro.util.ButtonMenuBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.logging.Logger;

public class GuildListeners extends ListenerAdapter {

    private final GuildDataHandler guildDataHandler;


    public GuildListeners() {
        this.guildDataHandler = GuildDataHandler.get();
    }

    @Override
    public void onGenericGuild(GenericGuildEvent genericGuildEvent) {

        if (genericGuildEvent instanceof GuildJoinEvent ||
                genericGuildEvent instanceof GuildLeaveEvent ||
                genericGuildEvent instanceof GuildMemberJoinEvent ||
                genericGuildEvent instanceof GuildMemberRemoveEvent ||
                genericGuildEvent instanceof GuildMemberUpdateEvent)
        {
            Guild guild = genericGuildEvent.getGuild();
            String guildID = guild.getId();
            GuildData guildData = guildDataHandler.getGuildData(guildID);

            if (guildData == null) {
                guildData = new GuildData(guildID, guild.getMemberCount());
                Logger.getLogger("jackiro").info("I must have joined a new guild=" + guildID + " while offline, adding them to my brain.");

                ButtonMenuBuilder welcomeMenu = new ButtonMenuBuilder(guild.getDefaultChannel().asTextChannel())
                        .setContext(MessageConstants.getIntroMessage(guild))
                        .addButton(Button.danger("deleteMSG", "Delete this Message!"))
                        .onButtonClick((event, id) -> {
                            switch (id) {
                                case "deleteMSG" -> {
                                    event.getMessage().delete().queue();
                                    event.getHook().deleteOriginal().queue();
                                }
                            }
                        });

                welcomeMenu.send();
            }

            guildDataHandler.saveGuildData(guildData);
        }
    }
}
