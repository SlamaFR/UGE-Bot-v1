package fr.irwin.uge.managers;

import com.google.common.base.MoreObjects;
import fr.irwin.uge.UGEBot;
import fr.irwin.uge.internals.TaskScheduler;
import fr.irwin.uge.utils.ChannelUtils;
import fr.irwin.uge.utils.MailUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class MailManager
{
    public static final String SEPARATOR = "---------------------------------------------------------------------";
    private static final Logger LOGGER = LoggerFactory.getLogger(MailManager.class);

    private Folder inbox;

    public MailManager() throws MessagingException
    {
        this.inbox = getStore().getFolder("INBOX");
        this.inbox.open(Folder.READ_ONLY);
        this.inbox.addConnectionListener(new ConnectionAdapter()
        {
            @Override
            public void disconnected(ConnectionEvent e)
            {
                super.disconnected(e);
                try
                {
                    LOGGER.info("Keeping mail server connection alive");
                    inbox = getStore().getFolder("INBOX");
                    inbox.open(Folder.READ_ONLY);
                }
                catch (MessagingException ex)
                {
                    LOGGER.warn("[UNSTABLE] MailManager failed!", ex);
                }
            }
        });

        TaskScheduler.scheduleRepeating(new Runnable()
        {
            private int lastCount = inbox.getMessageCount();

            @Override
            public void run()
            {
                try
                {
                    int count = inbox.getMessageCount();
                    int delta = count - lastCount;
                    if (delta > 0)
                    {
                        Message[] messages = inbox.getMessages(lastCount + 1, count);
                        for (Message message : messages)
                        {
                            LOGGER.info("[MAIL] Received a new mail!");
                            if (!MailUtils.isFromMoodle(message))
                            {
                                LOGGER.info("[MAIL] Not from Moodle, aborting...");
                                continue;
                            }
                            LOGGER.info("[MAIL] Mail is from Moodle!");
                            dispatchMessage(message);
                        }
                    }
                    lastCount = count;
                }
                catch (FolderClosedException e)
                {
                    try
                    {
                        LOGGER.info("Keeping mail server connection alive");
                        inbox.open(Folder.READ_ONLY);
                    }
                    catch (MessagingException ex)
                    {
                        LOGGER.warn("[UNSTABLE] MailManager failed!", ex);
                    }
                }
                catch (StoreClosedException e)
                {
                    try
                    {
                        inbox = getStore().getFolder("INBOX");
                    }
                    catch (MessagingException ex)
                    {
                        LOGGER.warn("[UNSTABLE] MailManager failed!", ex);
                    }
                }
                catch (MessagingException | IOException e)
                {
                    LOGGER.warn("[MAIL][Unstable] Mail failed to parse/read!", e);
                }
                catch (Exception e)
                {
                    LOGGER.warn("[MAIL][Unstable] Mail dispatch failed!", e);
                }
            }
        }, 120 * 1000);
    }

    private void dispatchMessage(Message message) throws IOException, MessagingException
    {
        String content = MailUtils.extractContent(message);
        String courseId = MailUtils.getCourseId(message);
        if (courseId == null)
        {
            LOGGER.info("[MAIL] No course id detected, aborting...");
            return;
        }
        LOGGER.info("[MAIL] Course id detected! ({})", courseId);

        String senderName = MailUtils.getSenderName(message).trim();
        String courseName = MailUtils.getCourseName(message);

        TextChannel textChannel = null;
        Color color = null;
        String avatarUrl = null;
        for (Guild guild : UGEBot.JDA().getGuilds())
        {
            System.out.println("Checking " + guild);
            textChannel = ChannelUtils.getCourseChannel(guild, courseId);
            if (textChannel != null)
            {
                if (!senderName.isEmpty())
                {
                    List<Member> members = guild.retrieveMembersByPrefix(senderName, 1).get();
                    if (!members.isEmpty())
                    {
                        color = members.get(0).getColor();
                        avatarUrl = members.get(0).getUser().getAvatarUrl();
                    }
                }
                break;
            }
        }
        if (textChannel == null)
        {
            LOGGER.info("[MAIL] No text channel found, aborting...");
            return;
        }

        LOGGER.info("[MAIL] Sender name: {}", senderName);
        LOGGER.info("[MAIL] Course name: {}", courseName);
        LOGGER.info("[MAIL] Sending e-Learning announcement!");
        textChannel.sendMessage(new EmbedBuilder().setTitle(MoreObjects.firstNonNull(courseName, "Annonce"))
                .setAuthor(senderName, null, avatarUrl).setDescription(content).setColor(color)
                .setFooter("Via e-Learning").build()).queue();
    }

    private Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", UGEBot.config().mail.protocol), UGEBot.config().mail.host);
        properties.put(String.format("mail.%s.port", UGEBot.config().mail.protocol),
                String.valueOf(UGEBot.config().mail.port));
        properties.put(String.format("mail.%s.starttls.enable", UGEBot.config().mail.protocol), "true");
        return properties;
    }

    private Store getStore() throws MessagingException
    {
        Session emailSession = Session.getDefaultInstance(getProperties());
        Store store = emailSession.getStore(String.format("%ss", UGEBot.config().mail.protocol));
        store.connect(UGEBot.config().mail.host, UGEBot.config().mail.user, UGEBot.config().mail.password);
        return store;
    }
}
