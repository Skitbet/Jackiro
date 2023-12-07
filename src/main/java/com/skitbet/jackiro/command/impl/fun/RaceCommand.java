package com.skitbet.jackiro.command.impl.fun;

import com.skitbet.jackiro.command.CommandRegistar;
import com.skitbet.jackiro.command.annotation.Command;
import com.skitbet.jackiro.command.annotation.options.UserOpt;
import com.skitbet.jackiro.constants.ColorConstants;
import com.skitbet.jackiro.module.Module;
import com.skitbet.jackiro.util.MathUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RaceCommand extends CommandRegistar {

    @Command(name = "race", description = "Race another member of the server!", module = Module.FUN, cooldown = 4)
    public static void onRaceCommand(SlashCommandInteractionEvent event, @UserOpt(name = "opponent", description = "User to race")User opponent) {

        HashMap<User, Integer> scores = new HashMap<>();
        scores.put(event.getUser(), 100);
        scores.put(opponent, 100);


        HashMap<User, List<RandomEvents>> events = new HashMap<>();
        events.put(event.getUser(), new ArrayList<RandomEvents>());
        events.put(opponent, new ArrayList<RandomEvents>());

        for (RandomEvents value : RandomEvents.values()) {

            if (MathUtil.percentageChance(value.getChances())) {
                scores.put(event.getUser(), Math.max(0, scores.get(event.getUser()) - value.getRemoveAmount()));
                events.get(event.getUser()).add(value);
            }

            if (MathUtil.percentageChance(value.getChances())) {
                scores.put(opponent, Math.max(0, scores.get(opponent) - value.getRemoveAmount()));
                events.get(opponent).add(value);
            }

        }

        User winner = null;
        boolean tied = false;

        if (scores.get(opponent) > scores.get(event.getUser())) {
            winner = opponent;
        }else if (scores.get(opponent) < scores.get(event.getUser())){
            winner = event.getUser();
        }else{
            tied = true;
        }

        StringBuilder builder = new StringBuilder();
        events.forEach((member1, randomEvents) -> {
            for (RandomEvents randomEvent : randomEvents) {
                String formatted = randomEvent.getMessage().replaceAll("%name%", member1.getEffectiveName());
                builder.append(formatted + "\n");
            }
        });

        if (scores.get(event.getUser()) == 100) {
            builder.append(event.getUser().getAsMention() + " has reached the finish line.\n");
        }

        if (scores.get(opponent) == 100) {
            builder.append(opponent.getAsMention() + " has reached the finish line.");
        }

        String message;
        if (tied) {
            message = "The race was a tie!";
        }else{
            message = "**" + winner.getName() + "**" + " has won the race!";
        }

        StringBuilder raceStringBuilder = new StringBuilder();
        raceStringBuilder.append(message);
        raceStringBuilder.append("\n\n");
        raceStringBuilder.append(event.getUser().getName() + " - " + (winner == event.getUser() ? "1st" : "2nd") + " - " + scores.get(event.getUser()) + "%");
        raceStringBuilder.append("\n");
        String track1 = "";
        for (int senderIndex = -1; senderIndex <= 100; senderIndex++) {
            if (senderIndex == 100) {
                if (scores.get(event.getUser()) == 100) {
                    track1 += ":blue_car::checkered_flag:";
                    track1 = track1.substring(1);
                    continue;
                }
                track1 += ":checkered_flag:";
                continue;
            }
            if (senderIndex == scores.get(event.getUser())) {
                track1 += ":blue_car:";
                continue;
            }
            track1 += " ";
        }
        raceStringBuilder.append(track1);
        raceStringBuilder.append("\n");
        raceStringBuilder.append(opponent.getName() + " - " + (winner == opponent ? "1st" : "2nd") + " - " + scores.get(opponent) + "%");
        raceStringBuilder.append("\n");
        String track2 = "";
        for (int userIndex = -1; userIndex <= 100; userIndex++) {
            if (userIndex == 100) {
                if (scores.get(opponent) == 100) {
                    track2 += ":red_car::checkered_flag:";
                    track2 = track2.substring(1);
                    continue;
                }
                track2 += ":checkered_flag:";
                continue;
            }
            if (userIndex == scores.get(opponent)) {
                track2 += ":red_car:";
                continue;
            }
            track2 += " ";
        }
        raceStringBuilder.append(track2);


        event.replyEmbeds(new EmbedBuilder()
                        .setDescription(builder.toString())
                        .setColor(ColorConstants.AQUA)
                        .build()
        ).setEphemeral(true).setContent(raceStringBuilder.toString()).queue();


    }

    public enum RandomEvents {
        CHECK_ENGINE_LIGHT(30, 0.6D, ":warning: %name%'s check engine light came on."),
        OIL_LEAK(35, 0.5D, ":oil: %name%'s oil started leaking."),
        TIRE_BLOW_OUT(50, 0.2D, ":wheel: %name% had a tire blow out."),
        POLICE(100, 0.05D, ":rotating_light: %name% was caught by the police."),
        RAN_OUT_OF_GAS(100, 0.1D, ":fuelpump: %name% ran out of gas."),
        ;

        private final int removeAmount;
        private final double chances;
        private final String message;

        RandomEvents(int removeAmount, double chances, String message) {
            this.removeAmount = removeAmount;
            this.chances = chances;
            this.message = message;
        }

        public int getRemoveAmount() {
            return new Random().nextInt(removeAmount / 2, removeAmount);
        }

        public double getChances() {
            return chances;
        }

        public String getMessage() {
            return message;
        }
    }

}
