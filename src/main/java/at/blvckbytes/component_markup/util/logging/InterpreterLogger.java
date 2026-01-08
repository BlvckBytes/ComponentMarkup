/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util.logging;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

public interface InterpreterLogger {

  default void logErrorScreen(InputView positionProvider, String message) {
    log(positionProvider, positionProvider.getPosition(), message, null);
  }

  default void logErrorScreen(InputView positionProvider, String message, Throwable e) {
    log(positionProvider, positionProvider.getPosition(), message, null);
  }

  void log(InputView view, int position, String message, @Nullable Throwable e);

}
