package fr.irwin.uge.utils;

import java.util.List;

public final class SwearUtils {

    private static final List<String> swearslist = List.of(
            "Bon ca suffit maintenant",
            "Cesse de nous importuner",
            "Si seulement ça nous intéressait",
            "Boooooooring",
            "Hop hop hop bannez le moi celui la"
    );

    public static String getSwear(){
        return swearslist.get((int)(Math.random()*101 % 5));
    }
}
