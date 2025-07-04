package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ValueInterpreter {

  long asLong(@Nullable Object value);

  double asDouble(@Nullable Object value);

  @NotNull Number asLongOrDouble(@Nullable Object value);

  boolean asBoolean(@Nullable Object value);

  @NotNull String asString(@Nullable Object value);

  @NotNull List<Object> asList(@Nullable Object value);

}
