/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.util.logging;

import at.blvckbytes.component_markup.util.InputView;
import org.jetbrains.annotations.Nullable;

// We use a separate logger for interpreting-errors, since they are specific to user-input
// and thereby may be prefixed by the corresponding configuration-file-path by the API-consumer.
// In contrast, the GlobalLogger just redirects all internal errors to the same output.

public interface InterpreterLogger {

  default void logErrorScreen(InputView positionProvider, String message) {
    log(positionProvider, positionProvider.getPosition(), message, null);
  }

  default void logErrorScreen(InputView positionProvider, String message, Throwable e) {
    log(positionProvider, positionProvider.getPosition(), message, null);
  }

  void log(InputView view, int position, String message, @Nullable Throwable e);

}
