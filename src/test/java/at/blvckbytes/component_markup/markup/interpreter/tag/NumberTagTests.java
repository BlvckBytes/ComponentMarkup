/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import at.blvckbytes.component_markup.markup.interpreter.JsonObjectBuilder;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.constructor.SlotType;
import org.junit.jupiter.api.Test;

public class NumberTagTests extends InterpreterTestsBase {

  @Test
  public void shouldCastValueToInteger() {
    TextWithSubViews text = new TextWithSubViews(
      "<number value=15.12345 int/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "15")
    );
  }

  @Test
  public void shouldTakeAbsoluteValue() {
    TextWithSubViews text = new TextWithSubViews("<number value=-15 abs/>");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "15")
    );

    text = new TextWithSubViews("<number value=-15.12345 abs/>");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "15.12345")
    );
  }

  @Test
  public void shouldApplyFormat() {
    TextWithSubViews text = new TextWithSubViews("<number value=5.555 format=\"00.00\" locale=\"en_US\"/>");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "05.56")
    );

    text = new TextWithSubViews("<number value=5.555 format=\"00.00\" locale=\"de_AT\"/>");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "05,56")
    );
  }

  @Test
  public void shouldApplyFormatWithRounding() {
    TextWithSubViews text = new TextWithSubViews("<number value=5.555 format=\"00.00\" locale=\"en_US\" rounding=\"HALF_DOWN\"/>");

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "05.55")
    );
  }
}
