package at.blvckbytes.component_markup.markup.parser.token;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalToken extends Token {

  private @Nullable List<HierarchicalToken> children;

  public HierarchicalToken(TokenType type, int beginIndex, char value) {
    super(type, beginIndex, value);
  }

  public HierarchicalToken(TokenType type, int beginIndex, String value) {
    super(type, beginIndex, value);
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

  // TODO: Add test-cases for both hierarchical and sequential tokens

  public static void toSequence(List<HierarchicalToken> hierarchicalTokens, SequenceTokenConsumer output) {
    for (HierarchicalToken hierarchicalToken : hierarchicalTokens)
      appendTokenAndChildren(hierarchicalToken, output);
  }

  private static void appendTokenAndChildren(HierarchicalToken parentToken, SequenceTokenConsumer output) {
    if (parentToken.children == null || parentToken.children.isEmpty()) {
      output.handle(parentToken.type, parentToken.beginIndex, parentToken.value);
      return;
    }

    HierarchicalToken firstChild = parentToken.children.get(0);

    if (firstChild.beginIndex > parentToken.beginIndex) {
      output.handle(
        parentToken.type,
        parentToken.beginIndex,
        parentToken.value.substring(0, firstChild.beginIndex - parentToken.beginIndex)
      );
    }

    HierarchicalToken previousToken = null;

    for (HierarchicalToken childToken : parentToken.children) {
      if (previousToken != null) {
        int previousEnd = previousToken.beginIndex + previousToken.value.length() - 1;
        if (childToken.beginIndex - previousEnd > 1) {
          output.handle(
            parentToken.type,
            previousEnd + 1,
            parentToken.value.substring(
              previousEnd - parentToken.beginIndex + 1,
              childToken.beginIndex - parentToken.beginIndex
            )
          );
        }
      }

      appendTokenAndChildren(childToken, output);

      previousToken = childToken;
    }

    HierarchicalToken lastChild = parentToken.children.get(parentToken.children.size() - 1);

    int lastChildEnd = lastChild.beginIndex + lastChild.value.length() - 1;
    int parentEnd = parentToken.beginIndex + parentToken.value.length() - 1;

    if (lastChildEnd < parentEnd) {
      output.handle(
        parentToken.type,
        lastChildEnd + 1,
        parentToken.value.substring(lastChildEnd - parentToken.beginIndex + 1)
      );
    }
  }
}
