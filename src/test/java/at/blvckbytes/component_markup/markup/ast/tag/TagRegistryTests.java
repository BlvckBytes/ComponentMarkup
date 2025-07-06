package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

public class TagRegistryTests {

  // TODO: This "suite" could use a bit more attention, ^^"

  private static class DummyTag extends TagDefinition {

    Predicate<String> matcher;

    public DummyTag(Predicate<String> matcher, TagPriority priority, String... prefixes) {
      super(prefixes, TagClosing.OPEN_CLOSE, priority);
      this.matcher = matcher;
    }

    @Override
    public boolean matchName(String tagNameLower) {
      return matcher.test(tagNameLower);
    }

    @Override
    public @NotNull MarkupNode createNode(@NotNull String tagNameLower, @NotNull CursorPosition position, @Nullable AttributeMap attributes, @Nullable List<LetBinding> letBindings, @Nullable List<MarkupNode> children) {
      throw new UnsupportedOperationException();
    }
  }

  private static final TagRegistry registry = new TagRegistry(5) {};

  private static final DummyTag POUND_A = new DummyTag(name -> name.startsWith("#aa"), TagPriority.NORMAL, "#aa");
  private static final DummyTag POUND_B = new DummyTag(name -> name.startsWith("#aa") && name.length() > 3, TagPriority.HIGH, "#aa");

  static {
    registry.register(POUND_A);
    registry.register(POUND_B);
  }

  @Test
  public void shouldSelectBasedOnPriorityAndPrefixes() {
    Assertions.assertEquals(POUND_A, registry.locateTag("#aa"));
    Assertions.assertEquals(POUND_B, registry.locateTag("#aab"));
  }
}
