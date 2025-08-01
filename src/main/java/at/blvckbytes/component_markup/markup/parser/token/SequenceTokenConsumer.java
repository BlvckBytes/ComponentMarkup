/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.StringView;

@FunctionalInterface
public interface SequenceTokenConsumer {

  void handle(TokenType type, StringView value);

}
