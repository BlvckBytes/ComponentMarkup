package at.blvckbytes.component_markup.markup.parser.token;

import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

public class TokenOutput {

  private @Nullable HierarchicalToken[] tokenByCharIndex;
  private String input;
  private final List<HierarchicalToken> result = new ArrayList<>();
  private boolean hasEnded;

  public final EnumSet<OutputFlag> outputFlags;

  public TokenOutput(EnumSet<OutputFlag> outputFlags) {
    this.outputFlags = outputFlags;
  }

  public void onInitialization(String input) {
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

  private void throwMissingTokenError(int index) {
    int lineCounter = 1;
    int charCounter = 0;

    for (int i = 0; i < index; ++i) {
      if (input.charAt(i) == '\n') {
        ++lineCounter;
        charCounter = 0;
      }

      ++charCounter;
    }

    throw new IllegalStateException("Missing token for index line " + lineCounter + " column " + charCounter + " (" + index + "/" + (tokenByCharIndex.length - 1) + ")");
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
        throwMissingTokenError(currentBeginIndex);
        continue;
      }

      currentBeginIndex = collectChildrenAndGetNextIndex(currentToken);
      result.add(currentToken);
    }
  }

  public List<HierarchicalToken> getResult() {
    return result;
  }

  public void emitToken(int beginIndex, TokenType type, char value) {
    emitToken(beginIndex, type, String.valueOf(value));
  }

  public void emitToken(int beginIndex, TokenType type, String value) {
    if (tokenByCharIndex == null)
      throw new IllegalStateException("Do not emit tokens before calling TokenOutput#onInitialization");

    if (beginIndex < 0 || beginIndex >= tokenByCharIndex.length) {
      LoggerProvider.log(Level.WARNING, "Encountered out-of-range token \"" + value + "\" at index " + beginIndex);
      return;
    }

    tokenByCharIndex[beginIndex] = new HierarchicalToken(type, beginIndex, value);
  }
}
