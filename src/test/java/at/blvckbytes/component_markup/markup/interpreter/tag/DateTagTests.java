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

public class DateTagTests extends InterpreterTestsBase {

  @Test
  public void shouldRenderWithDefaultFormat() {
    TextWithSubViews text = new TextWithSubViews(
      "<date [value]=\"1754662942 * 1000\" />"
    );

    makeCase(
      text,
      new Environment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "2025-08-08 16:22:22")
    );
  }

  @Test
  public void shouldRenderWithCustomFormat() {
    TextWithSubViews text = new TextWithSubViews(
      "<date [value]=\"1754533342 * 1000\" format=\"dd.MM.yyyy HH:mm:ss\" />"
    );

    makeCase(
      text,
      new Environment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "07.08.2025 04:22:22")
    );
  }

  @Test
  public void shouldRenderInCustomZone() {
    TextWithSubViews text = new TextWithSubViews(
      "<date [value]=\"1754439794 * 1000\" zone=\"BST\" />"
    );

    makeCase(
      text,
      new Environment(),
      SlotType.CHAT,
      new JsonObjectBuilder()
        .string("text", "2025-08-06 06:23:14")
    );
  }
}
