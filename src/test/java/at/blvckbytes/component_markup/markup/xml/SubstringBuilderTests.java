package at.blvckbytes.component_markup.markup.xml;

import at.blvckbytes.component_markup.markup.xml.StringBuilderMode;
import at.blvckbytes.component_markup.markup.xml.SubstringBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstringBuilderTests {

  @Test
  public void shouldCollapseNewlineTrailingWhitespace() {
    String input = "Hello\n  world\n     test";
    SubstringBuilder builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello world test", builder.build(StringBuilderMode.TEXT_MODE));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello\n  world\n     test", builder.build(StringBuilderMode.NORMAL_MODE));

    input = "\n test";
    builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("\n test", builder.build(StringBuilderMode.NORMAL_MODE));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("test", builder.build(StringBuilderMode.TEXT_MODE));
  }

  @Test
  public void shouldTrimTrailingSpaces() {
    String input = "Hello,  ";
    SubstringBuilder builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello,  ", builder.build(StringBuilderMode.TEXT_MODE));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello,", builder.build(StringBuilderMode.TEXT_MODE_TRIM_TRAILING_SPACES));

    input = "Hello,  \\";
    builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello,  \\", builder.build(StringBuilderMode.TEXT_MODE));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello,  ", builder.build(StringBuilderMode.TEXT_MODE_TRIM_TRAILING_SPACES));
  }
}
