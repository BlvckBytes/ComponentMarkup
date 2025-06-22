package at.blvckbytes.component_markup.ast.node.content;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.LetBinding;
import at.blvckbytes.component_markup.parser.ParserChildItem;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ContentNode extends AstNode implements ParserChildItem {

  public ContentNode(
    CursorPosition position,
    @Nullable List<LetBinding> letBindings
  ) {
    super(position, null, letBindings);
  }
}
