package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.StringPosition;
import at.blvckbytes.component_markup.util.StringView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TokenOutput {

  private @Nullable HierarchicalToken[] tokenByCharIndex;
  private StringView input;
  private final List<HierarchicalToken> result = new ArrayList<>();
  private boolean hasEnded;

  public final EnumSet<OutputFlag> outputFlags;

  public TokenOutput(EnumSet<OutputFlag> outputFlags) {
    this.outputFlags = outputFlags;
  }

  public void onInitialization(StringView input) {
    if (tokenByCharIndex != null)
      throw new IllegalStateException("Do not call TokenOutput#onInitialization more than once");

    tokenByCharIndex = new HierarchicalToken[input.length()];
    this.input = input;
  }

  private int collectChildrenAndGetNextIndex(HierarchicalToken parent) {
    for (int childBeginIndex = parent.beginIndex + 1; childBeginIndex <= parent.endIndex; ++childBeginIndex) {
      HierarchicalToken child = tokenByCharIndex[childBeginIndex];

      if (child == null)
        continue;

      childBeginIndex = collectChildrenAndGetNextIndex(child);

      parent.addChild(child);
    }

    return parent.endIndex;
  }

  public void onInputEnd() {
    if (hasEnded)
      throw new IllegalStateException("Do not call TokenOutput#onInputEnd more than once");

    if (tokenByCharIndex == null)
      throw new IllegalStateException("Do not call TokenOutput#onInputEnd before calling TokenOutput#onInitialization");

    hasEnded = true;

    for (int currentBeginIndex = 0; currentBeginIndex < tokenByCharIndex.length; ++currentBeginIndex) {
      HierarchicalToken currentToken = tokenByCharIndex[currentBeginIndex];

      if (currentToken == null) {
        int lineCounter = 1;
        int charCounter = 0;

        for (int i = 0; i < currentBeginIndex; ++i) {
          if (input.contents.charAt(i) == '\n') {
            ++lineCounter;
            charCounter = 0;
          }

          ++charCounter;
        }

        throw new IllegalStateException("Missing token for index line " + lineCounter + " column " + charCounter + " (" + currentBeginIndex + "/" + (tokenByCharIndex.length - 1) + ")");
      }

      currentBeginIndex = collectChildrenAndGetNextIndex(currentToken);
      result.add(currentToken);
    }
  }

  public List<HierarchicalToken> getResult() {
    return result;
  }

  public void emitCharToken(StringPosition position, TokenType type) {
    emitToken(type, position.rootView.buildSubViewAbsolute(position.charIndex, position.charIndex));
  }

  public void emitToken(TokenType type, StringView value) {
    int tokenIndex = value.startInclusive.charIndex;

    validateTokenIndex(type, tokenIndex);

    tokenByCharIndex[tokenIndex] = new HierarchicalToken(type, value);
  }

  private void validateTokenIndex(TokenType type, int index) {
    if (tokenByCharIndex == null)
      throw new IllegalStateException("Do not emit tokens before calling TokenOutput#onInitialization");

    if (index < 0 || index >= tokenByCharIndex.length)
      throw new IllegalStateException("Encountered out-of-range token \"" + type + "\" at index " + index);
  }
}
