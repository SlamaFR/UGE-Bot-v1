package fr.irwin.uge.utils;

import com.notkamui.keval.Keval;
import com.notkamui.keval.KevalDSLException;
import com.notkamui.keval.KevalInvalidSymbolException;
import com.notkamui.keval.KevalZeroDivisionException;

public final class KevalUtils
{
    private static final Keval KEVAL_INSTANCE;

    static {
        Keval keval;
        try {
            keval = new Keval()
                    .withDefault()
                    // Constants
                    .withConstant("PHI", (1 + Math.sqrt(5)) / 2)
                    // Functions
                    .withFunction("max", 2, args -> Math.max(args[0], args[1]))
                    .withFunction("min", 2, args -> Math.min(args[0], args[1]))
                    .withFunction("sqrt", 1, args -> Math.sqrt(args[0]))
                    .withFunction("cos", 1, args -> Math.cos(args[0]))
                    .withFunction("sin", 1, args -> Math.sin(args[0]))
                    .withFunction("tan", 1, args -> Math.tan(args[0]))
                    .withFunction("acos", 1, args -> Math.acos(args[0]))
                    .withFunction("asin", 1, args -> Math.asin(args[0]))
                    .withFunction("atan", 1, args -> Math.atan(args[0]))
                    .withFunction("rand", 0, args -> Math.random())
                    .withFunction("floor", 1, args -> Math.floor(args[0]))
                    .withFunction("ceil", 1, args -> Math.ceil(args[0]))
                    .withFunction("log", 1, args -> Math.log10(args[0]))
                    .withFunction("logB", 1, args -> Math.log(args[0]) / Math.log(2))
                    .withFunction("ln", 1, args -> Math.log(args[0]));
        } catch (KevalDSLException e) {
            keval = null;
            System.err.println("Keval could be initialized: " + e.getMessage());
        }
        KEVAL_INSTANCE = keval;
    }

    public static Double eval(String expr) throws KevalInvalidSymbolException, KevalZeroDivisionException {
        if (KEVAL_INSTANCE == null) return null;
        return KEVAL_INSTANCE.eval(expr);
    }
}
