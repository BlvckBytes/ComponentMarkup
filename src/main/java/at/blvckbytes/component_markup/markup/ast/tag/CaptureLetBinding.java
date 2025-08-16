/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public class CaptureLetBinding extends LetBinding {

  public final String capturedName;
  public final @Nullable Object capturedValue;

  public CaptureLetBinding(@Nullable Object capturedValue, String capturedName, InputView captureName) {
    super(captureName);

    this.capturedName = capturedName;
    this.capturedValue = capturedValue;
  }
}
