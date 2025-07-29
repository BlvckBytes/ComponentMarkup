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

    if (firstChild.value.startInclusive > parentToken.value.startInclusive)
      output.handle(parentToken.type, parentToken.value.buildSubViewAbsolute(parentToken.value.startInclusive, firstChild.value.startInclusive));

    HierarchicalToken previousChild = null;

    for (HierarchicalToken currentChild : parentToken.children) {
      // <...-----------parent_token-------------...>
      // prev_end_index -vv- new_begin_index
      //  <previous_child><new_token><current_child>
      //              new_end_index-^^- curr_begin_index
      if (previousChild != null) {
        if (currentChild.value.startInclusive - (previousChild.value.endExclusive - 1) > 1) {
          output.handle(
            parentToken.type,
            parentToken.value.buildSubViewAbsolute(
              previousChild.value.endExclusive,
              currentChild.value.startInclusive
            )
          );
        }
      }

      appendTokenAndChildren(currentChild, output);

      previousChild = currentChild;
    }

    HierarchicalToken lastChild = parentToken.children.get(parentToken.children.size() - 1);

    if (lastChild.value.endExclusive < parentToken.value.endExclusive) {
      output.handle(
        parentToken.type,
        parentToken.value.buildSubViewAbsolute(lastChild.value.endExclusive, parentToken.value.endExclusive)
      );
    }
  }
}
