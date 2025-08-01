/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag.built_in.click;

public class ToClipboardTag extends ClickTag {

  public ToClipboardTag() {
    super(ClickAction.COPY_TO_CLIPBOARD, "to-clipboard");
  }
}
