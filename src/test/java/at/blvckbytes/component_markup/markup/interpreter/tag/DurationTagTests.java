/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import at.blvckbytes.component_markup.markup.interpreter.JsonObjectBuilder;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.platform.SlotType;
import at.blvckbytes.component_markup.test_utils.Environment;
import org.junit.jupiter.api.Test;

public class DurationTagTests extends InterpreterTestsBase {

  // TODO: This could use a few more detailed cases

  @Test
  public void shouldRenderWithDefaultFormat() {
    TextWithSubViews text = new TextWithSubViews(
      "<duration value=98000 units=\"s\" />"
    );

    makeCase(
      text,
      new Environment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "98s")
    );

    text = new TextWithSubViews(
      "<duration value=98000 units=\"ms\" />"
    );

    makeCase(
      text,
      new Environment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "1m 38s")
    );
  }
}
