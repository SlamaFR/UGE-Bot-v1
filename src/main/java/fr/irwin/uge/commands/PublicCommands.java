package fr.irwin.uge.commands;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PublicCommands {

    @Command(name = "ticket")
    private void ticket(Guild guild, TextChannel textChannel, Member member, Message message) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        if (TicketManager.getTicketManager(textChannel).openTicket(member)) {
            message.addReaction(EmotesUtils.YES.replace(">", "")).queue();
        } else {
            message.addReaction(EmotesUtils.NO.replace(">", "")).queue();
        }
    }

    @Command(name = "table", aliases = {"tab"})
    private void table(TextChannel textChannel, Message message) {
        List<String> args = StringUtils.splitArgs(message.getContentRaw());
        args.remove(0);

        if (args.isEmpty()) {
            textChannel.sendMessage(
                    new EmbedBuilder()
                            .setTitle("Générateur de tableaux ASCII")
                            .setDescription("Cette commande permet de générer rapidement des tableaux avec des caractères ASCII.")
                            .addField("Utilisation", "`!table <Ligne 1> <Ligne 2> ... <Ligne N>`", false)
                            .addField("Syntaxe", "Une ligne est divisée en colonnes par le caractère `;`. " +
                                    "Si votre ligne contient un ou plusieurs espace, il faut l'encadrer avec des " +
                                    "guillemets pour éviter des comportements innatendus.", false)
                            .addField("Exemple", "`!table \";Colonne 1;Colonne 2\" \"Ligne 1;Val 1;Val 2\" ou;comme;ceci`\n" +
                                    "```\n" +
                                    "+---------+-----------+-----------+\n" +
                                    "|         | Colonne 1 | Colonne 2 |\n" +
                                    "+---------+-----------+-----------+\n" +
                                    "| Ligne 1 | Val 1     | Val 2     |\n" +
                                    "+---------+-----------+-----------+\n" +
                                    "| ou      | comme     | ceci      |\n" +
                                    "+---------+-----------+-----------+\n" +
                                    "```", false)
                            .build()
            ).queue();
            return;
        }

        SimpleTable s = SimpleTable.of();
        for (String row : args) {
            s.nextRow();
            for (String col : row.split(";", -1)) {
                s.nextCell().addLine(" " + col + " ");
            }
        }

        GridTable g = s.toGrid();
        g = Border.of(Border.Chars.of('+', '-', '|')).apply(g);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            Util.print(g, ps);
            textChannel.sendMessage("```\n" + baos.toString(utf8) + "```").queue();
            ps.close();
            baos.close();
        } catch (IOException e) {
            MessageUtils.sendErrorMessage(textChannel, "Une erreur d'encodage est survenue !");
        }
    }
}
