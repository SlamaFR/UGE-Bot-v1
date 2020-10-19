package fr.irwin.uge.commands.core;

import java.lang.reflect.Method;

/**
 * Created on 15/03/2018.
 */
public final class BotCommand {

    private final String name;
    private final String[] aliases;
    private final Object object;
    private final Method method;

    BotCommand(String name, String[] aliases, Object object, Method method) {
        this.name = name;
        this.aliases = aliases;
        this.object = object;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    Object getObject() {
        return object;
    }

    Method getMethod() {
        return method;
    }

}
