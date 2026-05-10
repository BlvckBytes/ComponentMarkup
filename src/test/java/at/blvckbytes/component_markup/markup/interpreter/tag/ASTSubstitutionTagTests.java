package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import at.blvckbytes.component_markup.markup.interpreter.JsonObjectBuilder;
import org.junit.jupiter.api.Test;

public class ASTSubstitutionTagTests extends InterpreterTestsBase {

  @Test
  public void shouldSubstituteMarkupNodeAndIntroduceAttributesAsLetBindings() {
    makeCase(
      new TextWithSubViews(
        "<container",
        " *let-my_component={ <red>Hello there, {first} {last}! }",
        ">",
        "  <$my_component first='Markus' last='Persson' />",
        "  ;",
        "  <$my_component first='Jens' last='Bergensten' />"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "")
        .array("extra", extra -> (
          extra
            .object(item -> (
              item
                .string("text", "Hello there, Markus Persson!")
                .string("color", "red")
            ))
            .object(item -> (
              item
                .string("text", ";")
            ))
            .object(item -> (
              item
                .string("text", "Hello there, Jens Bergensten!")
                .string("color", "red")
            ))
          ))
    );
  }

  @Test
  public void shouldIntroduceLetBindingsWithProperCasing() {
    makeCase(
      new TextWithSubViews(
        "<container",
        " *let-my_component={ My number is {my_number} }",
        ">",
        "  <$my_component [my-number]='5' />",
        "  ;",
        "  <$my_component my-number=21 />"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "My number is 5;My number is 21")
    );
  }
}
