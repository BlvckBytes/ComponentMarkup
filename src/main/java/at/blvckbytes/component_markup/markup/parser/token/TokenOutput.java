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
          if (input.charAt(i) == '\n') {
            ++lineCounter;
            charCounter = 0;
          }

          ++charCounter;
        }

        throw new IllegalStateException("Missing token for index line " + lineCounter + " column " + charCounter + " (" + currentBeginIndex + "/" + (tokenByCharIndex.length - 1) + ")");
      }

      int tokenLength = currentToken.value.length();

      if (tokenLength == 0) {
        LoggerProvider.log(Level.WARNING, "Encountered zero-length token of type " + currentToken.type + " at index " + currentBeginIndex);
        continue;
      }

      int tokenEndIndex = currentBeginIndex + (tokenLength - 1);

      if (tokenLength == 1) {
        result.add(currentToken);
        currentBeginIndex = tokenEndIndex;
        continue;
      }

      for (int childBeginIndex = currentBeginIndex + 1; childBeginIndex <= tokenEndIndex; ++childBeginIndex) {
        HierarchicalToken child = tokenByCharIndex[childBeginIndex];

        if (child == null)
          continue;

        currentToken.addChild(child);
      }

      result.add(currentToken);
      currentBeginIndex = tokenEndIndex;
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
