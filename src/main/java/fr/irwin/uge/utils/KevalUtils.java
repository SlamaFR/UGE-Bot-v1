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
                    .withFunction(
                            "max",
                            2,
                            args -> Math.max(args[0], args[1])
                    ).withFunction(
                            "min",
                            2,
                            args -> Math.min(args[0], args[1])
                    ).withFunction(
                            "sqrt",
                            1,
                            args -> Math.sqrt(args[0])
                    );
        } catch (KevalDSLException e) {
            keval = null;
            System.err.println("Keval could be initialized: " + e.getMessage());
        }
        KEVAL_INSTANCE = keval;
    }

    public static double eval(String expr) throws KevalInvalidSymbolException, KevalZeroDivisionException {
        return KEVAL_INSTANCE.eval(expr);
    }
}
