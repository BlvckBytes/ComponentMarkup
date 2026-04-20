/*
 * Copyright (c) 2026, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.InputView;

public interface TokenEmitter {

  void emitCharToken(int position, TokenType type);

  void emitToken(TokenType type, InputView value);

}
