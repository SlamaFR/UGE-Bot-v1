package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.internals.ASCIITable;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class PublicCommands
{

    @Command(name = "ticket")
    private void ticket(Guild guild, TextChannel textChannel, Member member, Message message)
    {
        if (guild == null)
        {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId()))
        {
            return;
        }

        if (!TicketManager.hasOpenTickedManager(textChannel))
        {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        if (TicketManager.getTicketManager(textChannel).openTicket(member))
        {
            message.addReaction(EmotesUtils.YES.replace(">", "")).queue();
        }
        else
        {
            message.addReaction(EmotesUtils.NO.replace(">", "")).queue();
        }
    }

    @Command(name = "table", aliases = {"tab"})
    private void table(TextChannel textChannel, Message message)
    {
        List<String> args = StringUtils.splitArgs(message.getContentRaw());
        args.remove(0);

        if (args.isEmpty())
        {
            textChannel
                    .sendMessage(new EmbedBuilder()
                            .setTitle("Générateur de tableaux ASCII")
                            .setDescription(
                                    "Cette commande permet de générer rapidement des tableaux avec des caractères ASCII.")
                            .addField("Utilisation", "`!table <Ligne 1> <Ligne 2> ... <Ligne N>`", false)
                            .addField("Syntaxe", """
                                                 Une ligne est divisée en colonnes par le caractère `;`.
                                                 Si une ligne contient un ou plusieurs espaces, il faut l'encadrer avec des guillemets pour éviter des comportements inattendus.
                                                 Pour dessiner une case vide, sans les bordures, la cellule ne doit contenir que des espaces.
                                                 """, false)
                            .addField("Exemple 1", """
                                                             `!table ";Colonne 1;Colonne 2" "Ligne 1;Val 1;Val 2" ou;comme;ceci`
                                                             ```
                                                   ┌─────────┬───────────┬───────────┐
                                                   │         │ Colonne 1 │ Colonne 2 │
                                                   ├─────────┼───────────┼───────────┤
                                                   │ Ligne 1 │ Val 1     │ Val 2     │
                                                   ├─────────┼───────────┼───────────┤
                                                   │ ou      │ comme     │ ceci      │
                                                   └─────────┴───────────┴───────────┘
                                                   ```
                                                   """, false)
                            .addField("Exemple 2", """
                                                             `!table " ;Colonne 1;Colonne 2" "Ligne 1;Val 1;Val 2" ou;comme;ceci`
                                                             ```
                                                             ┌───────────┬───────────┐
                                                             │ Colonne 1 │ Colonne 2 │
                                                   ┌─────────┼───────────┼───────────┤
                                                   │ Ligne 1 │ Val 1     │ Val 2     │
                                                   ├─────────┼───────────┼───────────┤
                                                   │ ou      │ comme     │ ceci      │
                                                   └─────────┴───────────┴───────────┘
                                                   ```
                                                   """, false)
                            .build())
                    .queue();
            return;
        }

        ASCIITable table = ASCIITable.of();
        for (String row : args)
        {
            table.nextRow();
            for (String col : row.split(";", -1))
            {
                if (col.isEmpty())
                {
                    table.nextCell().blank();
                }
                else
                {
                    table.nextCell().setText(col.replace("`", ""));
                }
            }
        }

        if (table.isEmpty())
        {
            MessageUtils.sendErrorMessage(textChannel, "Le tableau est vide.");
        }
        else
        {
            String string = table.toString();
            if (string.length() > 1990) {
                MessageUtils.sendErrorMessage(textChannel, "Le tableau est trop grand.");
            } else {
                textChannel.sendMessage("```\n" + string + "\n```").queue();
            }
        }
    }
}
