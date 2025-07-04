package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TagDefinitionTests {

  private static class MyTag extends TagDefinition {

    protected MyTag(AttributeDefinition... attributes) {
      super(new String[] {}, TagClosing.OPEN_CLOSE, TagPriority.NORMAL, attributes);
    }

    @Override
    public boolean matchName(String tagNameLower) {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull MarkupNode createNode(
      @NotNull String tagNameLower,
      @NotNull CursorPosition position,
      @Nullable AttributeMap attributes,
      @Nullable List<LetBinding> letBindings,
      @Nullable List<MarkupNode> children
    ) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void shouldThrowOnCollidingAttributes() {
    IllegalStateException error;

    error = Assertions.assertThrows(IllegalStateException.class, () -> new MyTag(
      new MandatoryExpressionAttributeDefinition("my-attr"),
      new MarkupAttributeDefinition("my-attr")
    ));

    Assertions.assertEquals("Colliding attribute: my-attr", error.getMessage());
  }
}
