package fr.irwin.uge;

import fr.irwin.uge.commands.core.CommandMap;
import fr.irwin.uge.features.channel.TrafficNotifier;
import fr.irwin.uge.features.message.AutoRole;
import fr.irwin.uge.features.message.OrganizationDisplay;
import fr.irwin.uge.redis.Redis;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RedisUtils;
import fr.irwin.uge.utils.SwearUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created on 04/10/2018.
 */
public final class EventListener extends ListenerAdapter
{
    private final CommandMap commandMap;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public EventListener(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (event.getAuthor().isBot()) {
            return;
        }

<<<<<<< HEAD
        if (event.getMessage().getMember().getRoles().contains(
                event.getGuild().getRolesByName(String.valueOf(
                        Redis.instance().getBucket(event.getGuild().getId()+":SwearerRole").get()),true).get(0))){
                if(Math.random()  < .1){
                    event.getMessage().getChannel().sendMessage(SwearUtils.getSwear()).queue();
                }
        }
        if (event.getMessage().getContentRaw().startsWith(CommandMap.getTag()))
        {
=======
        if (event.getMessage().getContentRaw().startsWith(CommandMap.getTag())) {
>>>>>>> 1b403482c352aa749cf0d55b8e34ed08275e4130
            commandMap.commandUser(event.getMessage().getContentRaw().replaceFirst("!", ""), event.getMessage());
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        super.onReady(event);

        for (Guild guild : event.getJDA().getGuilds()) {
            Redis.instance().getMap(RedisUtils.getKey(guild, AutoRole.class)).forEach((msgId, o) -> {
                ((AutoRole) o).restore(String.valueOf(msgId), null);
            });

            Redis.instance().getMap(RedisUtils.getKey(guild, OrganizationDisplay.class)).forEach((msgId, o) -> {
                ((OrganizationDisplay) o).restore(String.valueOf(msgId), null);
            });
        }

        TrafficNotifier manager = RedisUtils.getObject(TrafficNotifier.class);
        if (manager != null) {
            TrafficNotifier.instance(manager);
            LOGGER.info("Restored TrafficNotifier!");
        }

        LOGGER.info("Ready!");
    }
}
