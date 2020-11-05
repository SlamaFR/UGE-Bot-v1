package fr.irwin.uge.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class MessageUtils
{
    public static void sendErrorMessage(TextChannel textChannel, String error)
    {
        textChannel.sendMessage(new EmbedBuilder().setTitle("Erreur").setDescription(error).setColor(new Color(0xe74c3c)).build()).queue();
    }

    public static void sendWarningMessage(TextChannel textChannel, String error)
    {
        textChannel.sendMessage(new EmbedBuilder().setTitle("Attention").setDescription(error).setColor(new Color(0xf1c40f)).build()).queue();
    }

    public static void sendSuccessMessage(TextChannel textChannel, String error)
    {
        textChannel.sendMessage(new EmbedBuilder().setTitle("Succès").setDescription(error).setColor(new Color(0x2ecc71)).build()).queue();
    }
}
