package com.skitbet.jackiro.guild;

import com.skitbet.jackiro.JackiroBot;
import com.skitbet.jackiro.module.Module;
import org.bson.BSON;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.HashMap;
import java.util.Map;

public class GuildData {

    private String guildId;
    private boolean isSetup;
    private int memberCount;
    private Map<Module, Boolean> modules;

    public GuildData(String guildId, int memberCount) {
        this.guildId = guildId;
        this.isSetup = false;
        this.memberCount = memberCount;
        this.modules = initializeDefaultModules();
    }

    public GuildData(String guildId, boolean isSetup, int memberCount, Map<Module, Boolean> modules) {
        this.guildId = guildId;
        this.isSetup = isSetup;
        this.memberCount = memberCount;
        this.modules = modules;
    }

    private Map<Module, Boolean> initializeDefaultModules() {
        Map<Module, Boolean> defaultModules = new HashMap<>();
        for (Module value : Module.values()) {
            defaultModules.put(value, true);
        }
        return defaultModules;
    }

    public Document toDocument() {
        if (JackiroBot.get().getJda().getGuildById(guildId) != null) {
            this.memberCount = JackiroBot.get().getJda().getGuildById(guildId).getMemberCount();
        }
        Document document = new Document("_id", guildId)
                .append("setup", isSetup)
                .append("members", memberCount);

        Document modulesDocument = new Document();
        for (Module module : modules.keySet()) {
            if (!module.isSaveable()) {
                continue;
            }
            modulesDocument.append(module.getID(), modules.get(module));
        }
        document.append("modules", modulesDocument);

        return document;
    }

    public static GuildData fromDocument(Document document) {
        Map<Module, Boolean> modules = new HashMap<>();

        Document modulesDocument = document.get("modules", Document.class);
        if (modulesDocument != null) {
            for (Module module : Module.values()) {
                if (!module.isSaveable()) {
                    continue;
                }
                modules.put(module, modulesDocument.getBoolean(module.getID(), true));
            }
        }

        return new GuildData(
                document.getString("_id"),
                document.getBoolean("setup"),
                document.getInteger("members"),
                modules
        );
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public Map<Module, Boolean> getModules() {
        return modules;
    }

    public void setModules(Map<Module, Boolean> modules) {
        this.modules = modules;
    }

    public boolean isModuleEnabled(Module module) {
        return modules.getOrDefault(module, true);
    }

    public void setModuleEnabled(Module module, boolean enabled) {
        modules.put(module, enabled);
    }
}
