package at.blvckbytes.component_markup.markup.interpreter;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class InterpretationResult {

  private final List<Object> components;
  // TODO: Add paths of deferred nodes

  public InterpretationResult(List<Object> components) {
    this.components = Collections.unmodifiableList(components);
  }

  public List<Object> resolveDeferred(@Nullable Object recipient) {
    // TODO: Implement resolving deferred nodes
    return components;
  }
}
