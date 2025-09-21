/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.cml.event;

import at.blvckbytes.component_markup.util.InputView;

public class TextEvent implements CmlEvent {

  public final InputView text;
  public final String textBuildResult;

  public TextEvent(InputView text, String textBuildResult) {
    this.text = text;
    this.textBuildResult = textBuildResult;
  }
}
