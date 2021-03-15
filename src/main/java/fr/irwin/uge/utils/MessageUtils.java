package fr.irwin.uge.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public final class MessageUtils {
    public static void sendMessage(
        String message,
        Color color,
        TextChannel textChannel,
        String error
    ) {
        textChannel.sendMessage(
            new EmbedBuilder()
                .setTitle(message)
                .setDescription(error)
                .setColor(color)
                .build()
        ).queue();
    }

    public static void sendErrorMessage(TextChannel textChannel, String error) {
        sendMessage(
            "Erreur",
            new Color(0xe74c3c),
            textChannel,
            error
        );
    }

    public static void sendWarningMessage(TextChannel textChannel, String error) {
        sendMessage(
            "Attention",
            new Color(0xf1c40f),
            textChannel,
            error
        );
    }

    public static void sendSuccessMessage(TextChannel textChannel, String error) {
        sendMessage(
            "Succès",
            new Color(0x2ecc71),
            textChannel,
            error
        );
    }
}
