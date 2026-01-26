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

public class DurationTagTests extends InterpreterTestsBase {

  // TODO: This could use a few more detailed cases

  @Test
  public void shouldRenderWithDefaultRenderer() {
    TextWithSubViews text = new TextWithSubViews(
      "<duration value=98000 units=\"s\" />"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "98s")
    );

    text = new TextWithSubViews(
      "<duration value=98000 units=\"ms\" />"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1m 38s")
    );
  }

  @Test
  public void shouldNotRenderFractionalWithDefaultRenderer() {
    TextWithSubViews text = new TextWithSubViews(
      "<duration",
      "  [value]='(60 + 30.5) * 1000'",
      "  units='ms'",
      "/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1m 30s")
    );
  }

  @Test
  public void shouldRenderFractionalWithCustomRenderer() {
    TextWithSubViews text = new TextWithSubViews(
      "<duration",
      "  [value]='(60 + 30.5) * 1000'",
      "  units='ms'",
      "  unit-renderer={",
      "    <number *if='is_fractional' [value]='value' format='0.00' locale='de_DE' />",
      "    <container *else &value />",
      "    {unit}",
      "  }",
      "/>"
    );

    makeCase(
      text,
      new InterpretationEnvironment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1m 30,50s")
    );
  }
}
