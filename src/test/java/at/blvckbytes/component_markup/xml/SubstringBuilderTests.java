package at.blvckbytes.component_markup.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstringBuilderTests {

  @Test
  public void shouldCollapseNewlineTrailingWhitespace() {
    String input = "Hello\n  world\n     test";
    SubstringBuilder builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello world test", builder.build(true));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("Hello\n  world\n     test", builder.build(false));

    input = "\n test";
    builder = new SubstringBuilder(input);

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("\n test", builder.build(false));

    builder.setStartInclusive(0);
    builder.setEndExclusive(input.length());
    assertEquals("test", builder.build(true));
  }
}
