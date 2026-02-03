package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import at.blvckbytes.component_markup.markup.interpreter.JsonObjectBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ColorTagTests extends InterpreterTestsBase {

  @Test
  public void shouldUseStaticColorString() {
    makeCase(
      new TextWithSubViews(
        "<color value='red'>Hello, world"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world")
        .string("color", "red")
    );
  }

  @Test
  public void shouldUseDynamicColorString() {
    makeCase(
      new TextWithSubViews(
        "<color [value]='color'>Hello, world"
      ),
      new InterpretationEnvironment()
        .withVariable("color", "aqua"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world")
        .string("color", "aqua")
    );
  }

  @Test
  public void shouldUseDynamicColorShorthand() {
    makeCase(
      new TextWithSubViews(
        "<color &lut.main>Hello, world"
      ),
      new InterpretationEnvironment()
        .withVariable("lut", Collections.singletonMap("main", "light_purple")),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world")
        .string("color", "light_purple")
    );
  }

  @Test
  public void shouldUseDynamicColorShorthandWithUnderscore() {
    makeCase(
      new TextWithSubViews(
        "<color &main_color>Hello, world"
      ),
      new InterpretationEnvironment()
        .withVariable("main_color", "light_purple"),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "Hello, world")
        .string("color", "light_purple")
    );
  }
}
