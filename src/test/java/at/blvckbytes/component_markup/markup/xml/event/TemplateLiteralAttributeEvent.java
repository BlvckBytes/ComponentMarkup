/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.xml.event;

import at.blvckbytes.component_markup.expression.ast.TerminalNode;
import at.blvckbytes.component_markup.util.InputView;

public class TemplateLiteralAttributeEvent implements XmlEvent {

  public final InputView name;
  public final String nameBuildResult;
  public final TerminalNode value;

  public TemplateLiteralAttributeEvent(InputView name, TerminalNode value, String nameBuildResult) {
    this.name = name;
    this.nameBuildResult = nameBuildResult;
    this.value = value;
  }
}
