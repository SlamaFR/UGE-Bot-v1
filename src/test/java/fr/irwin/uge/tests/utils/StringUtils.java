package fr.irwin.uge.tests.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StringUtils
{
    @Test
    public void testCapitalizedString()
    {
        String string = "John Doe";
        boolean isCapitalized = fr.irwin.uge.utils.StringUtils.isCapitalized(string);
        Assertions.assertThat(isCapitalized).isTrue();
    }

    @Test
    public void testNonCapitalizedString()
    {
        String string = "john DOE";
        boolean isCapitalized = fr.irwin.uge.utils.StringUtils.isCapitalized(string);
        Assertions.assertThat(isCapitalized).isFalse();
    }

    @Test
    public void testCapitalizingString()
    {
        String string = "john doe";
        String capitalized = fr.irwin.uge.utils.StringUtils.capitalizeString(string);
        Assertions.assertThat(capitalized).isEqualTo("John Doe");
    }

    @Test
    public void testSplitingSimpleArgs()
    {
        String string = "this is a test";
        List<String> args = fr.irwin.uge.utils.StringUtils.splitArgs(string);
        List<String> expectedResult = Arrays.asList("this", "is", "a", "test");
        Assertions.assertThat(args).isEqualTo(expectedResult);
    }

    @Test
    public void testSplitingComplexArgs()
    {
        String string = "this \"is a\" test";
        List<String> args = fr.irwin.uge.utils.StringUtils.splitArgs(string);
        List<String> expectedResult = Arrays.asList("this", "is a", "test");
        Assertions.assertThat(args).isEqualTo(expectedResult);
    }

    @Test
    public void testSplitingSimpleArgsEscapingDoubleQuotes()
    {
        String string = "this \\\"is\\\" a test";
        List<String> args = fr.irwin.uge.utils.StringUtils.splitArgs(string);
        List<String> expectedResult = Arrays.asList("this", "\"is\"", "a", "test");
        Assertions.assertThat(args).isEqualTo(expectedResult);
    }

    @Test
    public void testSplitingComplexArgsEscapingDoubleQuotes()
    {
        String string = "this \"is \\\"sort of\\\" a\" test";
        List<String> args = fr.irwin.uge.utils.StringUtils.splitArgs(string);
        List<String> expectedResult = Arrays.asList("this", "is \"sort of\" a", "test");
        Assertions.assertThat(args).isEqualTo(expectedResult);
    }
}
