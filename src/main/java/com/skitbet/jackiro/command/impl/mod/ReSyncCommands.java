package com.skitbet.jackiro.command.impl.mod;

import com.skitbet.jackiro.command.CommandHandler;
import com.skitbet.jackiro.command.CommandRegistar;
import com.skitbet.jackiro.command.annotation.Command;
import com.skitbet.jackiro.constants.MessageConstants;
import com.skitbet.jackiro.util.ButtonMenuBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Date;

public class ReSyncCommands extends CommandRegistar {

    @Command(name = "resynccmds", description = "Resync this guilds slash commands.", permissions = { Permission.MANAGE_SERVER }, cooldown = 60)
    public static void onResyncCMD(SlashCommandInteractionEvent event) {
        int pos = CommandHandler.get().syncGuild(event.getGuild());

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(":white_check_mark: • This server is now queued for resyncing")
                .setDescription("Remember this can take from 5 seconds to 5 hours depending on how many people are trying to resyncing commands.")
                .addField("Position `(as of message sent)`:", String.valueOf(pos), true)
                .setTimestamp(new Date().toInstant())
                .setFooter(event.getGuild().getName(), event.getGuild().getIconUrl());

        new ButtonMenuBuilder(event.getChannel())
                .setContext(builder.build())
                .addButton(Button.primary("cancelqueue", "Cancel"))
                .asEphermal()

                .onButtonClick((btnEvent, btnId) -> {
                    if (btnId.equals("cancelqueue")) {
                        if (CommandHandler.get().getSyncQueue().contains(event.getGuild())) {
                            CommandHandler.get().getSyncQueue().remove(event.getGuild());
                            btnEvent.getMessage().delete().queue();
                        }else{
                            btnEvent.getHook().editOriginal("You have already been re-synced!").queue();
                            btnEvent.getMessage().delete().queue();
                        }
                    }
                })
                .send();

//        event.replyEmbeds(MessageConstants.getSuccessEmbed("This guild is now queued to be resync'ed. Remember this may take take a couple minutes.\nPosition as of sending this is " + pos)).setEphemeral(true).queue();
    }

}
