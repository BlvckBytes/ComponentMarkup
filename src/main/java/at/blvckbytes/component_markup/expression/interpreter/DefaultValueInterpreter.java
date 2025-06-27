package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class DefaultValueInterpreter implements ValueInterpreter {

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

    if (value instanceof Number)
      return (Number) value;

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
  public @NotNull Boolean asBoolean(@Nullable Object value) {
    if (value == null)
      return false;

    if (value instanceof Number)
      return ((Number) value).intValue() != 0;

    if (value instanceof Boolean)
      return ((Boolean) value);

    String stringValue;

    if (value instanceof String)
      stringValue = (String) value;
    else
      stringValue = asStringOrNull(value);

    if (stringValue == null)
      return false;

    switch (stringValue) {
      case "true":
        return true;
      case "false":
        return false;
      default:
        return stringValue.isEmpty();
    }
  }

  private @Nullable String asStringOrNull(@Nullable Object value) {
    if (value == null)
      return null;

    if (value instanceof Iterable<?>) {
      Iterator<?> iterator = ((Iterable<?>) value).iterator();

      if (!iterator.hasNext())
        return null;

      return asStringOrNull(iterator.next());
    }

    if (value.getClass().isArray()) {
      if (Array.getLength(value) == 0)
        return null;

      return asStringOrNull(Array.get(value, 0));
    }

    if (value instanceof Map<?, ?>)
      return asStringOrNull(((Map<?, ?>) value).entrySet());

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
    if (value instanceof List<?>)
      return (List<Object>) value;

    if (value instanceof Collection<?>)
      return new ArrayList<>((Collection<?>) value);

    if (value instanceof Map<?, ?>)
      return new ArrayList<>(((Map<?, ?>) value).keySet());

    List<Object> result = new ArrayList<>();
    result.add(value);
    return result;
  }
}
