package at.blvckbytes.component_markup.markup.ast.tag.built_in.player_name;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.*;
import at.blvckbytes.component_markup.markup.xml.CursorPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerNameTag extends TagDefinition {

  public PlayerNameTag() {
    super(TagClosing.SELF_CLOSE, TagPriority.NORMAL);
  }

  @Override
  public boolean matchName(String tagNameLower) {
    return tagNameLower.equals("player-name");
  }

  @Override
  public @NotNull MarkupNode createNode(
    @NotNull String tagNameLower,
    @NotNull CursorPosition position,
    @NotNull AttributeMap attributes,
    @Nullable List<LetBinding> letBindings,
    @Nullable List<MarkupNode> children
  ) {
    return new PlayerNameNode(
      attributes.getOptionalExpressionNode("display-name"),
      attributes.getOptionalMarkupNode("representation"),
      position, letBindings
    );
  }
}
