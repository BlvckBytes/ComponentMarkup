package at.blvckbytes.component_markup.markup.parser.token;

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
    for (int childBeginIndex = parent.value.startInclusive + 1; childBeginIndex < parent.value.endExclusive; ++childBeginIndex) {
      HierarchicalToken child = tokenByCharIndex[childBeginIndex];

      if (child == null)
        continue;

      childBeginIndex = collectChildrenAndGetNextIndex(child);

      parent.addChild(child);
    }

    return parent.value.endExclusive - 1;
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
        int columnCounter = 1;

        for (int i = 0; i < currentBeginIndex; ++i) {
          if (input.contents.charAt(i) == '\n') {
            ++lineCounter;
            columnCounter = 0;
          }

          ++columnCounter;
        }

        throw new IllegalStateException("Missing token on line " + lineCounter + " column " + columnCounter + " (" + currentBeginIndex + "/" + (tokenByCharIndex.length - 1) + "; char '" + input.contents.charAt(currentBeginIndex) + "')");
      }

      currentBeginIndex = collectChildrenAndGetNextIndex(currentToken);
      result.add(currentToken);
    }
  }

  public List<HierarchicalToken> getResult() {
    return result;
  }

  public void emitCharToken(int position, TokenType type) {
    emitToken(type, input.buildSubViewAbsolute(position, position + 1));
  }

  public void emitToken(TokenType type, StringView value) {
    validateTokenIndex(type, value);
    tokenByCharIndex[value.startInclusive] = new HierarchicalToken(type, value);
  }

  private void validateTokenIndex(TokenType type, StringView value) {
    if (tokenByCharIndex == null)
      throw new IllegalStateException("Do not emit tokens before calling TokenOutput#onInitialization");

    if (value.startInclusive < 0 || value.startInclusive >= tokenByCharIndex.length)
      throw new IllegalStateException("Encountered out-of-range token \"" + type + "\" at start-inclusive " + value.startInclusive);

    if (value.endExclusive < 0 || value.endExclusive > tokenByCharIndex.length)
      throw new IllegalStateException("Encountered out-of-range token \"" + type + "\" at end-exclusive " + value.endExclusive);

    if (value.isEmpty())
      throw new IllegalStateException("Encountered empty token \"" + type + "\" at start-inclusive " + value.startInclusive);
  }
}
