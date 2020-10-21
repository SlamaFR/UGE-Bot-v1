package fr.irwin.uge.internals;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.irwin.uge.UGEBot;
import fr.irwin.uge.config.Config;
import fr.irwin.uge.utils.RedisUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class AutoRole {

    private final long guildId;
    private final long textChannelId;
    private final String title;
    private final String description;
    private final Map<String, String> roles;
    private transient final Collection<Role> rolesCollection;
    private final boolean rolesList;
    private final int maxRoles;

    public AutoRole(@NotNull TextChannel textChannel, String title, String description, Map<String, String> roles, boolean rolesList, int maxRoles) {
        this.guildId = textChannel.getGuild().getIdLong();
        this.textChannelId = textChannel.getIdLong();
        this.title = title;
        this.description = description;
        this.roles = roles;
        this.rolesCollection = new HashSet<>();
        this.rolesList = rolesList;
        this.maxRoles = maxRoles;

        fillRolesCollection();
    }

    public AutoRole(@NotNull TextChannel textChannel, Config.Guild.AutoRole config) {
        this(textChannel, config.title, config.description, config.roles, config.rolesList, config.maxRoles);
    }

    /* Don't mind this atrocity, only for deserialization purpose. */
    @JsonCreator
    public AutoRole(@JsonProperty("guildId") long guildId, @JsonProperty("textChannelId") long textChannelId,
                    @JsonProperty("title") String title, @JsonProperty("description") String description,
                    @JsonProperty("roles") Map<String, String> roles, @JsonProperty("rolesList") boolean rolesList,
                    @JsonProperty("maxRoles") int maxRoles) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.title = title;
        this.description = description;
        this.roles = roles;
        this.rolesList = rolesList;
        this.maxRoles = maxRoles;
        this.rolesCollection = new HashSet<>();

        fillRolesCollection();
    }

    public void send() {
        final MessageEmbed messageEmbed = getMessageEmbed();
        if (messageEmbed == null) return;

        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return;

        final Message message = textChannel.sendMessage(messageEmbed).complete();
        start(message);
        RedisUtils.addAutoRole(textChannel.getGuild(), message.getIdLong(), this);
    }

    public boolean restore(String messageId) {
        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) return false;

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) return false;

        try {
            final Message message = textChannel.retrieveMessageById(messageId).complete();
            this.start(message);
            RedisUtils.addAutoRole(textChannel.getGuild(), message.getIdLong(), this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void start(Message m) {
        roles.keySet().stream().sorted().map(e -> e.replace(">", "")).forEach(e -> m.addReaction(e).queue());
        new EventWaiter.Builder(GuildMessageReactionAddEvent.class,
                e -> e.getMessageIdLong() == m.getIdLong() && !e.getUser().isBot() &&
                        (RolesUtils.commonRoles(e.getMember().getRoles(), rolesCollection) < maxRoles || RolesUtils.isAdmin(e.getMember())),
                (e, ew) -> {
                    String emote;
                    try {
                        emote = e.getReactionEmote().getEmoji();
                    } catch (IllegalStateException ex) {
                        emote = "<:" + e.getReactionEmote().getAsReactionCode() + '>';
                    }
                    if (emote.equals("❌") && RolesUtils.isAdmin(e.getMember())) {
                        ew.close();
                        RedisUtils.removeAutoRole(e.getGuild(), m.getIdLong());
                        return;
                    }
                    Role role = m.getGuild().getRoleById(roles.get(emote));
                    if (role != null) {
                        m.getGuild().addRoleToMember(e.getMember(), role).complete();
                    } else {
                        ew.close();
                        m.delete().queue();
                    }
                })
                .autoClose(false)
                .build();
    }

    private void fillRolesCollection() {
        for (Map.Entry<String, String> entry : roles.entrySet()) {
            Role role = UGEBot.JDA().getRoleById(entry.getValue());
            if (role == null) {
                return;
            }
            rolesCollection.add(role);
        }
    }

    private MessageEmbed getMessageEmbed() {
        final String roleList = formatRoleList();
        if (roleList == null) return null;

        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description);
        if (rolesList) {
            builder.addField("Liste des rôles", roleList, false);
        }
        return builder.build();
    }

    private String formatRoleList() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : this.roles.entrySet()) {
            Role role = UGEBot.JDA().getRoleById(entry.getValue());
            if (role == null) {
                return null;
            }
            if (stringBuilder.length() > 0) stringBuilder.append("\n\n");
            stringBuilder.append(String.format("%s : `%s`", entry.getKey(), role.getName()));
        }
        return stringBuilder.toString();
    }

}
