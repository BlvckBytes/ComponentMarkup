/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter.tag;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.cml.TextWithSubViews;
import at.blvckbytes.component_markup.markup.interpreter.InterpreterTestsBase;
import at.blvckbytes.component_markup.markup.interpreter.JsonArrayBuilder;
import at.blvckbytes.component_markup.markup.interpreter.JsonObjectBuilder;
import org.junit.jupiter.api.Test;

public class SeparateTagTests extends InterpreterTestsBase {

  @Test
  public void shouldSeparatePlainTextValues() {
    JsonObjectBuilder expectedResult = new JsonObjectBuilder()
      .string("text", "A;B;C;D");

    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={;}",
        "  value={}",
        "  value={A}",
        "  value={}",
        "  value={B}",
        "  value={C}",
        "  value={}",
        "  value={}",
        "  value={D}",
        "  value={}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      expectedResult
    );

    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={;}",
        "  value={A}",
        "  value={}",
        "  value={B}",
        "  value={C}",
        "  value={}",
        "  value={}",
        "  value={D}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      expectedResult
    );
  }

  @Test
  public void shouldSeparateRichTextValues() {
    JsonObjectBuilder expectedResult = new JsonObjectBuilder()
      .string("text", "")
      .array("extra", extra -> (
        extra
          .object(item -> (
            item
              .string("text", "A")
              .string("color", "green")
          ))
          .object(item -> (
            item
              .string("text", ";")
              .string("color", "gray")
          ))
          .object(item -> (
            item
              .string("text", "B")
              .string("color", "aqua")
          ))
          .object(item -> (
            item
              .string("text", ";")
              .string("color", "gray")
          ))
          .object(item -> (
            item
              .string("text", "C")
              .string("color", "red")
          ))
          .object(item -> (
            item
              .string("text", ";")
              .string("color", "gray")
          ))
          .object(item -> (
            item
              .string("text", "D")
              .string("color", "gold")
          ))
      ));

    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={<gray>;}",
        "  value={}",
        "  value={<green>A}",
        "  value={}",
        "  value={<aqua>B}",
        "  value={<red>C}",
        "  value={}",
        "  value={}",
        "  value={<gold>D}",
        "  value={}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      expectedResult
    );

    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={<gray>;}",
        "  value={<green>A}",
        "  value={}",
        "  value={<aqua>B}",
        "  value={<red>C}",
        "  value={}",
        "  value={}",
        "  value={<gold>D}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.CHAT,
      expectedResult
    );
  }

  @Test
  public void shouldAllowForComponentBreaks() {
    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={<gray>;}",
        "  value={First line<br/><br/>Third line}",
        "  value={Third line continuation<br/>}",
        "  value={Fourth line}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(line -> (
          line
            .string("text", "First line")
        ))
        .object(line -> (
          line
            .string("text", "")
        ))
        .object(line -> (
          line
            .string("text", "")
            .array("extra", extra ->(
              extra
                .object(item -> (
                  item
                    .string("text", "Third line")
                ))
                .object(item -> (
                  item
                    .string("text", ";")
                    .string("color", "gray")
                ))
                .object(item -> (
                  item
                    .string("text", "Third line continuation")
                ))
            ))
        ))
        .object(line -> (
          line
            .string("text", "")
            .array("extra", extra ->(
              extra
                .object(item -> (
                  item
                    .string("text", ";")
                    .string("color", "gray")
                ))
                .object(item -> (
                  item
                    .string("text", "Fourth line")
                ))
            ))
        ))
    );
  }

  @Test
  public void shouldAllowForComponentBreakOnlyValue() {
    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={<gray>;}",
        "  value={A}",
        "  value={<br/>}",
        "  value={B}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonArrayBuilder()
        .object(line -> (
          line
            .string("text", "A")
        ))
        .object(line -> (
          line
            .string("text", "")
            .array("extra", extra ->(
              extra
                .object(item -> (
                  item
                    .string("text", ";")
                    .string("color", "gray")
                ))
                .object(item -> (
                  item
                    .string("text", "B")
                ))
            ))
        ))
    );
  }

  @Test
  public void shouldRenderEmptyAttributeOnNoContentIfProvided() {
    makeCase(
      new TextWithSubViews(
        "<separate",
        "  empty={No content rendered}",
        "  separator={,}",
        "  value={<container *if='false'>A}",
        "  value={<container *if='false'>B}",
        "  value={<container *if='false'>C}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonObjectBuilder()
        .string("text", "No content rendered")
    );

    makeCase(
      new TextWithSubViews(
        "<separate",
        "  separator={,}",
        "  value={<container *if='false'>A}",
        "  value={<container *if='false'>B}",
        "  value={<container *if='false'>C}",
        "/>"
      ),
      new InterpretationEnvironment(),
      SlotType.ITEM_LORE,
      new JsonObjectBuilder()
        .string("text", "")
    );
  }
}
