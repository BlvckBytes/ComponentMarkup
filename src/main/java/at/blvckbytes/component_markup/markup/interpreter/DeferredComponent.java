package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DeferredComponent {

  @Nullable List<Object> renderDeferredComponent(@Nullable Object recipient);

}
