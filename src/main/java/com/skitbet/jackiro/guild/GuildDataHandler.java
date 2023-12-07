package com.skitbet.jackiro.guild;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.skitbet.jackiro.JackiroBot;
import com.skitbet.jackiro.constants.MessageConstants;
import com.skitbet.jackiro.database.DatabaseManager;
import com.skitbet.jackiro.util.ButtonMenuBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GuildDataHandler {
    private Map<String, GuildData> guildDataMap;

    private DatabaseManager databaseManager;
    private MongoCollection<Document> guildCollection;

    private static GuildDataHandler instance;
    public static synchronized GuildDataHandler get() {
        return instance == null ? instance = new GuildDataHandler() : instance;
    }

    public void setup(List<Guild> guilds) {
        guildDataMap = new HashMap<>();

        this.databaseManager = DatabaseManager.get();

        guildCollection = this.databaseManager.getMongoDatabase().getCollection("guilds");

        new Thread(() -> {
            for (Guild guild : guilds) {
                String guildID = guild.getId();
                GuildData guildData = getGuildData(guildID);

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

                guild.loadMembers().get();
                saveGuildData(guildData);
            }
        }).run();
    }

    public void saveGuildData(GuildData guildData) {
        guildCollection.replaceOne(
                new Document("_id", guildData.getGuildId()),
                guildData.toDocument(),
                new UpdateOptions().upsert(true));
    }

    public GuildData getGuildData(String guildId) {
        Document document = guildCollection.find(new Document("_id", guildId)).first();
        return document != null ? GuildData.fromDocument(document) : null;
    }



}
