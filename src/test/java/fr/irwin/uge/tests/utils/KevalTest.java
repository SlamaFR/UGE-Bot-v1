package fr.irwin.uge.tests.utils;

import com.notkamui.keval.Keval;
import com.notkamui.keval.KevalDSLException;
import com.notkamui.keval.KevalInvalidSymbolException;
import com.notkamui.keval.KevalZeroDivisionException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class KevalTest {
    @Test
    public void kevalTest() throws KevalDSLException, KevalInvalidSymbolException, KevalZeroDivisionException {
        var test = new Keval()
            .withDefault()
            .eval("3+5");

        Assertions.assertThat(test).isEqualTo(8);
    }
}
