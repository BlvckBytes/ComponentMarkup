/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */
package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;

import java.util.EnumSet;
import java.util.logging.Level;

public enum PlatformWarning {
  MALFORMED_MATERIAL("This string does not represent a valid material-value"),
  MALFORMED_ENTITY_TYPE("This string does not represent a valid entity-type-value"),
  MALFORMED_FONT_NAME("This font-name is malformed"),
  MALFORMED_URL("This url is malformed"),
  MALFORMED_PAGE_VALUE("This string does not represent a valid page-value"),
  ;

  private static final ThreadLocal<EnumSet<PlatformWarning>> localSets = ThreadLocal.withInitial(() -> EnumSet.noneOf(PlatformWarning.class));

  public final String message;

  PlatformWarning(String message) {
    this.message = message;
  }

  @SuppressWarnings("unused")
  public static void emit(PlatformWarning warning) {
    localSets.get().add(warning);
  }

  public static void logIfEmitted(PlatformWarning warning, Runnable handler) {
    if (localSets.get().contains(warning))
      handler.run();
  }

  public static void logIfEmitted(PlatformWarning warning, InputView position, String value) {
    if (!localSets.get().contains(warning))
      return;

    for (String line : ErrorScreen.make(position, warning.message + ": \"" + value + "\""))
      LoggerProvider.log(Level.WARNING, line, false);
  }

  public static void clear() {
    localSets.get().clear();
  }
}
