package fr.irwin.uge.utils;

import net.dv8tion.jda.api.entities.MessageReaction;

public class EmotesUtils {

    public static final String RER = "<:rer:752563057917689906>";
    public static final String RER_A = "<:rer_a:752563057321967647>";
    public static final String RER_E = "<:rer_e:752563056776708137>";

    public static final String YES = "<:yes:514172065788461068>";
    public static final String NO = "<:no:514172078153400335>";

    public static String getEmote(MessageReaction.ReactionEmote emote) {
        try {
            return emote.getEmoji();
        } catch (IllegalStateException ex) {
            return "<:" + emote.getAsReactionCode() + '>';
        }
    }

}
