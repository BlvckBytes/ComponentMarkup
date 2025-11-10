/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */
package at.blvckbytes.component_markup.constructor;

import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.MessagePlaceholders;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.logging.Level;

public enum ConstructorWarning {
  MALFORMED_MATERIAL(args -> "The string \"" + args.get(0) + " does not represent a valid material-value"),
  MALFORMED_ENTITY_TYPE(args -> "The string \"" + args.get(0) + "\" does not represent a valid entity-type-value"),
  MALFORMED_FONT_NAME(args -> "The font-name \"" + args.get(0) + "\" is malformed"),
  MALFORMED_URL(args -> "The url \"" + args.get(0) + "\" is malformed"),
  MALFORMED_PAGE_VALUE(args -> "The string \"" + args.get(0) + "\" does not represent a valid page-value"),
  ;

  private static final ThreadLocal<EnumSet<ConstructorWarning>> localSets = ThreadLocal.withInitial(() -> EnumSet.noneOf(ConstructorWarning.class));

  public final Function<MessagePlaceholders, String> messageBuilder;

  ConstructorWarning(Function<MessagePlaceholders, String> messageBuilder) {
    this.messageBuilder = messageBuilder;
  }

  @SuppressWarnings("unused")
  public static void emit(ConstructorWarning warning) {
    localSets.get().add(warning);
  }

  public static void callIfEmitted(ConstructorWarning warning, Runnable handler) {
    if (localSets.get().contains(warning))
      handler.run();
  }

  public static void logIfEmitted(ConstructorWarning warning, InputView position, String... messagePlaceholders) {
    if (!localSets.get().contains(warning))
      return;

    for (String line : ErrorScreen.make(position, warning.messageBuilder.apply(new MessagePlaceholders(messagePlaceholders))))
      LoggerProvider.log(Level.WARNING, line, false);
  }

  public static void clear() {
    localSets.get().clear();
  }
}
