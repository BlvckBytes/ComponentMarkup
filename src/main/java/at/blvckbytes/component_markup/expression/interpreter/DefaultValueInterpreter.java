/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class DefaultValueInterpreter implements ValueInterpreter {

  // Let's make string-valued boolean-literals be compatible with YAMLs boolean-specification, seeing how I
  // prefer to completely avoid YAML-typing by stringify-ing all scalar values and running them through my parser.
  // See: https://yaml.org/type/bool.html
  private static final List<String> BOOLEAN_TRUE_LITERALS = Arrays.asList("true", "yes", "y", "on");
  private static final List<String> BOOLEAN_FALSE_LITERALS = Arrays.asList("false", "no", "n", "off");

  @Override
  public long asLong(@Nullable Object value) {
    return asLongOrDouble(value).longValue();
  }

  @Override
  public double asDouble(@Nullable Object value) {
    return asLongOrDouble(value).doubleValue();
  }

  @Override
  public @NotNull Number asLongOrDouble(@Nullable Object value) {
    if (value == null)
      return 0L;

    if (value instanceof Number) {
      if (value instanceof Double || value instanceof Float)
        return ((Number) value).doubleValue();

      return ((Number) value).longValue();
    }

    if (value instanceof Boolean)
      return ((Boolean) value) ? 1 : 0;

    String stringValue = asString(value);

    try {
      return Long.parseLong(stringValue);
    } catch (NumberFormatException ignored) {}

    try {
      return Double.parseDouble(stringValue);
    } catch (NumberFormatException ignored) {}

    return 0L;
  }

  @Override
  public boolean asBoolean(@Nullable Object value) {
    if (value == null)
      return false;

    if (value instanceof Number)
      return ((Number) value).intValue() != 0;

    if (value instanceof Boolean)
      return ((Boolean) value);

    if (value instanceof Collection)
      return !((Collection<?>) value).isEmpty();

    if (value instanceof Map)
      return !((Map<?, ?>) value).isEmpty();

    if (value.getClass().isArray())
      return Array.getLength(value) != 0;

    if (value instanceof String) {
      String stringValue = (String) value;

      if (BOOLEAN_TRUE_LITERALS.stream().anyMatch(stringValue::equalsIgnoreCase))
        return true;

      if (BOOLEAN_FALSE_LITERALS.stream().anyMatch(stringValue::equalsIgnoreCase))
        return false;

      return !stringValue.trim().isEmpty();
    }

    // All other, not specifically accounted-for types simply evaluate to true
    // if they're present, no matter their actual value.
    return true;
  }

  private @Nullable String asStringOrNull(@Nullable Object value) {
    if (value == null)
      return null;

    if (value instanceof Iterable<?>) {
      StringBuilder result = new StringBuilder("[");

      for (Object o : (Iterable<?>) value) {
        if (result.length() > 1)
          result.append(", ");

        result.append(asStringOrNull(o));
      }

      return result.append(']').toString();
    }

    if (value.getClass().isArray()) {
      int arrayLength = Array.getLength(value);
      StringBuilder result = new StringBuilder("[");

      for (int i = 0; i < arrayLength; ++i) {
        if (i != 0)
          result.append(", ");

        result.append(asStringOrNull(Array.get(value, i)));
      }

      return result.append(']').toString();
    }

    if (value instanceof Map<?, ?>)
      return asStringOrNull(((Map<?, ?>) value).entrySet());

    if (value instanceof Map.Entry<?, ?>) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
      return asStringOrNull(entry.getKey()) + "=" + asStringOrNull(entry.getValue());
    }

    return value.toString();
  }

  @Override
  public @NotNull String asString(@Nullable Object value) {
    String result = asStringOrNull(value);

    if (result == null)
      return "null";

    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull List<Object> asList(@Nullable Object value) {
    // This language cannot mutate data - pass lists through untouched, by reference.
    if (value instanceof List<?>)
      return (List<Object>) value;

    // Keep this as a fast-path for everything that can internally be mem-copied.
    if (value instanceof Collection<?>)
      return new ArrayList<>((Collection<?>) value);

    // And go with element-by-element extraction, including possibly required resizing, otherwise.
    if (value instanceof Iterable<?>) {
      List<Object> result = new ArrayList<>();

      for (Object item : (Iterable<?>) value)
        result.add(item);

      return result;
    }

    if (value instanceof Map<?, ?>)
      return new ArrayList<>(((Map<?, ?>) value).keySet());

    List<Object> result = new ArrayList<>();
    result.add(value);
    return result;
  }
}
