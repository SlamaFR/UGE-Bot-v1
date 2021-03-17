package fr.irwin.uge.utils;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.redis.Redis;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public final class SwearUtils {

/*    private static final List<String> swearslist = List.of(
            "Bon ca suffit maintenant",
            "Cesse de nous importuner",
            "Si seulement ça nous intéressait",
            "Boooooooring",
            "Hop hop hop bannez le moi celui la"
    );*/

    public static Role getOrCreateRole(Guild guild){
        Role role;
        if(Redis.instance().getBucket(guild.getId()+":SwearerRole").get() == null){
            role = guild.createRole().setName("Swearer").complete();
            Redis.instance().getBucket(guild.getId()+":SwearerRole").set(role.getIdLong());
        }
        else{
            role = guild.getRoleById(Redis.instance().getBucket(guild.getId()+":SwearerRole").get().toString());
            if (role == null){
                role = guild.createRole().setName("Swearer").complete();
                Redis.instance().getBucket(guild.getId()+":SwearerRole").set(role.getIdLong());

            }
        }
        return role;
    }

    public static String getSwear(){
        final List<String> swearingMessages = UGEBot.config().swearingMessages;
        return swearingMessages.get((int)(Math.random() * swearingMessages.size()));
    }
}
