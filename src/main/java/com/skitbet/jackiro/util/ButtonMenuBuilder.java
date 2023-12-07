// Package declaration
package com.skitbet.jackiro.util;

// Import statements
import com.skitbet.jackiro.constants.MessageConstants;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// ButtonMenu class for handling button interactions
public class ButtonMenuBuilder {

    // Static listener for button clicks
    public static final ButtonClickListener LISTENER = new ButtonClickListener();

    // Instance variables
    private final MessageChannel channel;
    private final List<Button> buttons;
    private boolean ephermal = false;
    private MessageEmbed context;
    private BiConsumer<ButtonInteractionEvent, String> buttonClickCallback;

    /**
     * @param channel The message channel to send the button menu.
     * @param buttons The list of buttons to be included in the menu.
     */
    public ButtonMenuBuilder(MessageChannel channel, List<Button> buttons) {
        this.channel = channel;
        this.buttons = buttons;
        this.context = MessageConstants.getErrorEmbed("Not Configured");
        this.buttonClickCallback = null;
    }

    /**
     * @param channel The message channel to send the button menu.
     */
    public ButtonMenuBuilder(MessageChannel channel) {
        this.channel = channel;
        this.buttons = new ArrayList<>();
        this.context = MessageConstants.getErrorEmbed("Not Configured");
        this.buttonClickCallback = null;
    }

    /**
     * Sets the context (message embed) for the button menu.
     *
     * @param context The message embed to be set as the context.
     * @return This ButtonMenu instance for method chaining.
     */
    public ButtonMenuBuilder setContext(MessageEmbed context) {
        this.context = context;
        return this;
    }

    public ButtonMenuBuilder asEphermal() {
        this.ephermal = true;
        return this;
    }

    /**
     * Adds a single button to the button menu.
     *
     * @param button The button to be added.
     * @return This ButtonMenu instance for method chaining.
     */
    public ButtonMenuBuilder addButton(Button button) {
        this.buttons.add(button);
        return this;
    }

    /**
     * Adds a list of buttons to the button menu.
     *
     * @param buttons The list of buttons to be added.
     * @return This ButtonMenu instance for method chaining.
     */
    public ButtonMenuBuilder addButtons(List<Button> buttons) {
        this.buttons.addAll(buttons);
        return this;
    }

    /**
     * Sets a callback for handling button clicks in the button menu.
     *
     * @param callback The callback function to be executed on button click.
     * @return This ButtonMenu instance for method chaining.
     */
    public ButtonMenuBuilder onButtonClick(BiConsumer<ButtonInteractionEvent, String> callback) {
        this.buttonClickCallback = callback;
        return this;
    }

    /**
     * Sends the button menu to the specified message channel.
     * Registers the button click listener for further handling.
     */
    public void send() {
        ActionRow actionRow = ActionRow.of(buttons);

        MessageCreateAction createAction = channel.sendMessageEmbeds(context).setActionRow(actionRow.getComponents());
        createAction.queue(message -> LISTENER.registerMenu(message.getIdLong(), this::handleButtonClick));
    }

    /**
     * Sends the button menu as a reply to a command event.
     * Registers the button click listener for further handling.
     */
    public void send(SlashCommandInteractionEvent event) {
        ActionRow actionRow = ActionRow.of(buttons);

        if (event.isAcknowledged()) {
            event.getHook().editOriginalEmbeds(context).setActionRow(actionRow.getComponents())
                    .queue(message -> {
                        LISTENER.registerMenu(message.getIdLong(), this::handleButtonClick);
                    });
            return;
        } else {
            event.replyEmbeds(context).setActionRow(actionRow.getComponents())
                    .queue(interactionHook -> {
                        interactionHook.retrieveOriginal().queue(message -> {
                            LISTENER.registerMenu(message.getIdLong(), this::handleButtonClick);
                        });
                    });
        }
    }

    /**
     * Handles the button click event by deferring the reply and invoking the callback function.
     *
     * @param event The button interaction event.
     */
    private void handleButtonClick(ButtonInteractionEvent event) {
        event.deferReply(ephermal).queue();

        buttons.stream()
                .filter(button -> button.getId().equalsIgnoreCase(event.getComponent().getId()))
                .findFirst()
                .ifPresent(button -> {
                    if (buttonClickCallback != null) {
                        buttonClickCallback.accept(event, event.getComponentId());
                    }
                });
    }

    private static class ButtonClickListener extends ListenerAdapter {

        private final Map<Long, Consumer<ButtonInteractionEvent>> menuCallbacks = new HashMap<>();

        /**
         * Registers a menu and its associated callback function.
         *
         * @param messageId      The ID of the message containing the menu.
         * @param eventConsumer  The callback function to be executed on button click.
         */
        public void registerMenu(long messageId, Consumer<ButtonInteractionEvent> eventConsumer) {
            menuCallbacks.put(messageId, eventConsumer);
        }

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            long messageId = event.getMessageIdLong();
            if (menuCallbacks.containsKey(messageId)) {
                menuCallbacks.get(messageId).accept(event);
            }
        }
    }
}
