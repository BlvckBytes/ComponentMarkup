package at.blvckbytes.component_markup.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private @Nullable T cache;

  public CachedSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (cache == null)
      cache = supplier.get();

    return cache;
  }
}
