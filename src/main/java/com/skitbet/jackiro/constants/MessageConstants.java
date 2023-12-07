package com.skitbet.jackiro.constants;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Date;

public class MessageConstants {

    public static MessageEmbed getIntroMessage(Guild guild) {
        return new EmbedBuilder()
                .setColor(ColorConstants.AQUA)
                .setTitle(":wave: • Thank you for inviting me!")
                .setDescription("Greetings! I've just arrived and I'm ready to be configured.\n\n" +
                        "You can get started with the following commands:\n" +
                        "- Use `/help` to view all available commands.\n" +
                        "- Execute `/setup` to configure your server within Discord itself!.\n" +
                        "\n" +
                        "Prefer a web interface? Visit our site to configure there!\n" +
                        "\n" +
                        "Need assistance or want to connect with the community?\n" +
                        "- **Support Server**: [Coming Soon]\n" +
                        "- **Website**: [Coming Soon]")
                .setTimestamp(new Date().toInstant())
                .setThumbnail(guild.getSelfMember().getEffectiveAvatarUrl())
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }

    public static MessageEmbed getErrorEmbed(String message) {
        return new EmbedBuilder()
                .setTitle(":x: • " + message)
                .setColor(ColorConstants.ERROR_RED)
                .setTimestamp(new Date().toInstant())
                .build();
    }

    public static MessageEmbed getSuccessEmbed(String message) {
        return new EmbedBuilder()
                .setDescription(":white_check_mark: • " + message)
                .setColor(ColorConstants.SUCCESS_GREEN)
                .setTimestamp(new Date().toInstant())
                .build();
    }
}
