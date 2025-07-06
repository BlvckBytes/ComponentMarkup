package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.AsciiOptimizedCharMap;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public abstract class TagRegistry {

  private static class CharTree {
    final @Nullable CharTree parent;
    final List<TagDefinition> members = new ArrayList<>();
    final AsciiOptimizedCharMap<CharTree> charMap = new AsciiOptimizedCharMap<>();

    CharTree(@Nullable CharTree parent) {
      this.parent = parent;
    }
  }

  private final CharTree charTree;
  private final int maxCharTreeDepth;

  public TagRegistry() {
    this(2);
  }

  public TagRegistry(int maxCharTreeDepth) {
    this.charTree = new CharTree(null);
    this.maxCharTreeDepth = maxCharTreeDepth;
  }

  public @Nullable TagDefinition locateTag(String nameLower) {
    int maxLength = Math.min(maxCharTreeDepth, nameLower.length());
    CharTree innermostTree = charTree;

    for (int charIndex = 0; charIndex < maxLength; ++charIndex) {
      char currentChar = nameLower.charAt(charIndex);
      CharTree nextTree = innermostTree.charMap.get(currentChar);

      if (nextTree == null)
        break;

      innermostTree = nextTree;
    }

    CharTree currentTree = innermostTree;

    while (currentTree != null) {
      List<TagDefinition> candidates = new ArrayList<>();

      for (TagDefinition member : currentTree.members) {
        try {
          if (member.matchName(nameLower))
            candidates.add(member);
        } catch (Throwable thrownError) {
          LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to match via " + member.getClass().getName() + "#matchName", thrownError);
        }
      }

      int candidateCount = candidates.size();

      if (candidateCount == 0) {
        currentTree = currentTree.parent;
        continue;
      }

      if (candidateCount != 1)
        candidates.sort(Comparator.comparingInt(item -> item.tagPriority.ordinal()));

      return candidates.get(0);
    }

    return null;
  }

  protected void register(TagDefinition tag) {
    for (String staticPrefix : tag.staticPrefixes) {
      int maxLength = Math.min(maxCharTreeDepth, staticPrefix.length());
      CharTree currentTree = charTree;

      for (int charIndex = 0; charIndex < maxLength; ++charIndex) {
        char currentChar = staticPrefix.charAt(charIndex);
        CharTree nextTree = currentTree.charMap.get(currentChar);

        if (nextTree == null) {
          nextTree = new CharTree(currentTree);
          currentTree.charMap.put(currentChar, nextTree);
        }

        currentTree = nextTree;
      }

      currentTree.members.add(tag);
    }
  }
}
