package at.blvckbytes.component_markup.markup.parser.token;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalToken {

  public final TokenType type;
  public final int beginIndex;
  public final String value;

  private @Nullable List<HierarchicalToken> children;

  public HierarchicalToken(TokenType type, int beginIndex, char value) {
    this(type, beginIndex, String.valueOf(value));
  }

  public HierarchicalToken(TokenType type, int beginIndex, String value) {
    this.type = type;
    this.beginIndex = beginIndex;
    this.value = value;
  }

  public void addChild(HierarchicalToken token) {
    if (children == null)
      children = new ArrayList<>();

    children.add(token);
  }

  public @Nullable List<HierarchicalToken> getChildren() {
    return children;
  }

  // TODO: Add test-cases for both hierarchical and sequential tokens

  public static void toSequence(List<HierarchicalToken> hierarchicalTokens, SequenceTokenConsumer output) {
    for (HierarchicalToken parentToken : hierarchicalTokens) {
      if (parentToken.children == null || parentToken.children.isEmpty()) {
        output.handle(parentToken.type, parentToken.beginIndex, parentToken.value);
        continue;
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
              previousEnd,
              parentToken.value.substring(
                previousEnd - parentToken.beginIndex + 1,
                childToken.beginIndex - parentToken.beginIndex
              )
            );
          }
        }

        output.handle(
          childToken.type,
          childToken.beginIndex,
          childToken.value
        );

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
}
