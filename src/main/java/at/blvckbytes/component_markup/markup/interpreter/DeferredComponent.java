package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

public interface DeferredComponent {

  @Nullable Object renderDeferredComponent(@Nullable Object recipient);

}
