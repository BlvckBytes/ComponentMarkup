/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.tokenizer;

import at.blvckbytes.component_markup.expression.tokenizer.token.Token;
import at.blvckbytes.component_markup.util.InputView;

public interface EnumToken {

  Token create(InputView raw);

  int getLength();

}
