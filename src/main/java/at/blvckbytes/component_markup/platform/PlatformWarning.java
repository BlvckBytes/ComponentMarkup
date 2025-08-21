/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */
package at.blvckbytes.component_markup.platform;

import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.logging.Level;

public enum PlatformWarning {
  MALFORMED_MATERIAL(args -> "The string \"" + args[0] + " does not represent a valid material-value"),
  MALFORMED_ENTITY_TYPE(args -> "The string \"" + args[0] + "\" does not represent a valid entity-type-value"),
  MALFORMED_FONT_NAME(args -> "The font-name \"" + args[0] + "\" is malformed"),
  MALFORMED_URL(args -> "The url \"" + args[0] + "\" is malformed"),
  MALFORMED_PAGE_VALUE(args -> "The string \"" + args[0] + "\" does not represent a valid page-value"),
  MISSING_SCORE_TYPE(args -> "The score \"" + args[0] + "\" requires an explicit type"),
  MALFORMED_SCORE_TYPE(args -> "The string \"" + args[0] + "\" does not represent a valid score-type"),
  UNKNOWN_OBJECTIVE(args -> "The objective \"" + args[0] + "\" is not known to the server"),
  ;

  private static final ThreadLocal<EnumSet<PlatformWarning>> localSets = ThreadLocal.withInitial(() -> EnumSet.noneOf(PlatformWarning.class));

  // TODO: This, and at all other sites, really shouldn't be a bare array (as to avoid crashing over OOR indices)
  public final Function<String[], String> messageBuilder;

  PlatformWarning(Function<String[], String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }

  @SuppressWarnings("unused")
  public static void emit(PlatformWarning warning) {
    localSets.get().add(warning);
  }

  public static void callIfEmitted(PlatformWarning warning, Runnable handler) {
    if (localSets.get().contains(warning))
      handler.run();
  }

  public static void logIfEmitted(PlatformWarning warning, InputView position, String... values) {
    if (!localSets.get().contains(warning))
      return;

    for (String line : ErrorScreen.make(position, warning.messageBuilder.apply(values)))
      LoggerProvider.log(Level.WARNING, line, false);
  }

  public static void clear() {
    localSets.get().clear();
  }
}
