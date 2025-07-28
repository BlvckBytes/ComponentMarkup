package at.blvckbytes.component_markup.markup.ast.node.control;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhenMatchingMap {

  private final Map<String, MarkupNode> caseByValue = new HashMap<>();

  // Only kept for test-matching, as to compare against real views
  // This is not ideal, but I need to move on now...
  private final List<StringView> views = new ArrayList<>();

  public @Nullable MarkupNode put(StringView value, MarkupNode node) {
    views.add(value);
    return caseByValue.put(value.buildString().toLowerCase(), node);
  }

  public @Nullable MarkupNode get(String valueLower) {
    return caseByValue.get(valueLower);
  }

  public boolean isEmpty() {
    return caseByValue.isEmpty();
  }
}
