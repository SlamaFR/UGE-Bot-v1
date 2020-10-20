package fr.irwin.uge.commands.core;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.AdminCommands;
import fr.irwin.uge.commands.InternalCommands;
import fr.irwin.uge.commands.PublicCommands;
import fr.irwin.uge.commands.TeacherCommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created on 15/03/2018.
 *
 * @version 2.0
 */
public class CommandMap {

    private final Map<String, BotCommand> commands = new HashMap<>();
    private final List<BotCommand> registry = new ArrayList<>();

    public CommandMap() {
        registerCommand(new InternalCommands());
        registerCommand(new AdminCommands());
        registerCommand(new TeacherCommands());
        registerCommand(new PublicCommands());
    }

    public static String getTag() {
        return "!";
    }

    private Object[] getCommand(String command) {
        String[] commandSplit = command.split(" ");
        String[] args = new String[commandSplit.length - 1];
        System.arraycopy(commandSplit, 1, args, 0, commandSplit.length - 1);
        BotCommand botCommand = commands.get(commandSplit[0]);
        return new Object[]{botCommand, args};
    }

    private void registerCommand(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command command = method.getAnnotation(Command.class);
                method.setAccessible(true);
                BotCommand botCommand;
                botCommand = new BotCommand(command.name(), object, method);
                commands.put(command.name(), botCommand);
                Arrays.stream(command.aliases()).forEach(alias -> commands.put(alias, botCommand));
                if (!registry.contains(botCommand)) registry.add(botCommand);
            }
        }
    }

    public void commandConsole(String command) {
        Object[] object = getCommand(command);
        if (object[0] == null) {
            System.out.println("Unknown command.");
            return;
        }
        BotCommand cmd = (BotCommand) object[0];
        try {
            execute(cmd, command, (String[]) object[1], null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandUser(String command, Message message) {
        Object[] object = getCommand(command);
        if (object[0] == null) return;
        BotCommand cmd = (BotCommand) object[0];
        try {
            execute(cmd, command, (String[]) object[1], message);
        } catch (Exception e) {
            System.out.printf("The %s command failed\n", cmd.getName());
            e.printStackTrace();
        }
    }

    private void execute(BotCommand botCommand, String command, String[] args, Message message) throws Exception {
        Parameter[] parameters = botCommand.getMethod().getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == String[].class)
                objects[i] = args;
            else if (parameters[i].getType() == User.class)
                objects[i] = message == null ? null : message.getAuthor();
            else if (parameters[i].getType() == BotCommand.class)
                objects[i] = botCommand;
            else if (parameters[i].getType() == TextChannel.class)
                objects[i] = message == null ? null : message.getTextChannel();
            else if (parameters[i].getType() == PrivateChannel.class)
                objects[i] = message == null ? null : message.getPrivateChannel();
            else if (parameters[i].getType() == Guild.class)
                objects[i] = message == null ? null : message.getGuild();
            else if (parameters[i].getType() == String.class)
                objects[i] = command;
            else if (parameters[i].getType() == Message.class)
                objects[i] = message;
            else if (parameters[i].getType() == JDA.class)
                objects[i] = UGEBot.JDA();
            else if (parameters[i].getType() == Member.class)
                objects[i] = message == null ? null : message.getMember();
            else if (parameters[i].getType() == MessageChannel.class)
                objects[i] = message == null ? null : message.getChannel();
        }
        botCommand.getMethod().invoke(botCommand.getObject(), objects);
    }

}
