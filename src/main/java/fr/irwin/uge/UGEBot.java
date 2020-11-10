package fr.irwin.uge;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.irwin.uge.commands.core.CommandMap;
import fr.irwin.uge.config.Config;
import fr.irwin.uge.managers.MailManager;
import fr.irwin.uge.redis.Redis;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.security.auth.login.LoginException;
import java.util.*;

/**
 * Created on 04/10/2018.
 */
public class UGEBot implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UGEBot.class);
    private static UGEBot instance;

    private final JDA jda;
    private final CommandMap commandMap;
    private final Scanner scanner;
    private final Map<String, String> games;
    private final Random generator;

    private Config config;
    private boolean running;

    public UGEBot(String configPath) throws LoginException, JsonProcessingException
    {
        instance = this;
        commandMap = new CommandMap();
        config = Config.parseFile(configPath);

        Redis.instance();

        jda = JDABuilder
                .createDefault(config.token)
                .addEventListeners(new EventListener(commandMap))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();
        try
        {
            jda.awaitReady();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        try
        {
            LOGGER.info("Connecting to mail server...");
            new MailManager();
            LOGGER.info("Successfully connected to mail server!");
        }
        catch (MessagingException e)
        {
            LOGGER.error("Failed to start mail manager!", e);
        }

        games = config.presenceMessages;
        generator = new Random();
        scanner = new Scanner(System.in);

        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Object[] keys = games.keySet().toArray();
                String key = (String) keys[generator.nextInt(keys.length)];
                try
                {
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf(games.get(key)), key));
                }
                catch (IllegalArgumentException e)
                {
                    LOGGER.warn("Activity \"{}\" has unknown type ({})", key, games.get(key));
                    jda.getPresence().setActivity(Activity.of(Activity.ActivityType.DEFAULT, key));
                }
            }
        }, 0, 10 * 60 * 1000);
    }

    public static void main(String[] args)
    {
        String configPath = "./config.json";

        if (args.length > 0)
        {
            configPath = args[0];
        }

        try
        {
            new Thread(new UGEBot(configPath)).start();
        }
        catch (LoginException e)
        {
            LOGGER.error("Provided token is incorrect!");
            System.exit(1);
        }
        catch (JsonProcessingException e)
        {
            LOGGER.error("Error occurred whilst parsing config.json!", e);
            System.exit(1);
        }
    }

    public static UGEBot instance()
    {
        return instance;
    }

    public static Config config()
    {
        return instance().config;
    }

    public static JDA JDA()
    {
        return instance().jda;
    }

    @Override
    public void run()
    {
        running = true;
        while (running)
        {
            if (scanner.hasNextLine())
            {
                commandMap.commandConsole(scanner.nextLine());
            }
        }
        LOGGER.info("Stopping...");
        scanner.close();
        LOGGER.info("Closing Discord connection...");
        jda.shutdownNow();
        Redis.close();
        LOGGER.info("Stopped.");
        System.exit(0);
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }

    public void reloadConfig()
    {
        try
        {
            config = Config.parseFile("./config.json");
            LOGGER.info("Successfully reloaded config!");
        }
        catch (JsonProcessingException e)
        {
            LOGGER.error("Error whilst reloading config! Check your file.");
        }
    }
}
