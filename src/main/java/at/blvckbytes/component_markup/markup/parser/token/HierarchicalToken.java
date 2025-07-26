package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalToken extends Token {

  private @Nullable List<HierarchicalToken> children;

  public HierarchicalToken(TokenType type, StringView value) {
    super(type, value);
  }

  public HierarchicalToken addChild(HierarchicalToken token) {
    if (children == null)
      children = new ArrayList<>();

    children.add(token);
    return this;
  }

  public @Nullable List<HierarchicalToken> getChildren() {
    return children;
  }

  public static void toSequence(List<HierarchicalToken> hierarchicalTokens, SequenceTokenConsumer output) {
    for (HierarchicalToken hierarchicalToken : hierarchicalTokens)
      appendTokenAndChildren(hierarchicalToken, output);
  }

  private static void appendTokenAndChildren(HierarchicalToken parentToken, SequenceTokenConsumer output) {
    if (parentToken.children == null || parentToken.children.isEmpty()) {
      output.handle(parentToken.type, parentToken.value);
      return;
    }

    HierarchicalToken firstChild = parentToken.children.get(0);

    if (firstChild.beginIndex > parentToken.beginIndex)
      output.handle(parentToken.type, parentToken.value.buildSubViewAbsolute(0, firstChild.beginIndex));

    HierarchicalToken previousToken = null;

    for (HierarchicalToken childToken : parentToken.children) {
      if (previousToken != null) {
        if (childToken.beginIndex - previousToken.endIndex > 1) {
          StringView newValue = parentToken.value.buildSubViewAbsolute(previousToken.endIndex + 1, childToken.beginIndex);
          output.handle(parentToken.type, newValue);
        }
      }

      appendTokenAndChildren(childToken, output);

      previousToken = childToken;
    }

    HierarchicalToken lastChild = parentToken.children.get(parentToken.children.size() - 1);

    if (lastChild.endIndex < parentToken.endIndex)
      output.handle(parentToken.type, parentToken.value.buildSubViewAbsolute(lastChild.endIndex + 1));
  }
}
