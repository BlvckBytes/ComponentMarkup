/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class CaptureLetBinding extends LetBinding {

  public final String bindingName;
  public final @Nullable Object capturedValue;

  public CaptureLetBinding(InputView capturedName, @Nullable Object capturedValue) {
    super(capturedName);

    this.bindingName = capturedName.buildString();
    this.capturedValue = capturedValue;
  }
}
