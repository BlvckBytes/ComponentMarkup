package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public abstract class TagRegistry {

  protected final Set<Class<?>> registeredClasses;
  protected final List<TagDefinition> tagDefinitions;

  public TagRegistry() {
    this.tagDefinitions = new ArrayList<>();
    this.registeredClasses = new HashSet<>();
  }

  public @Nullable TagDefinition locateTag(String nameLower) {
    List<TagDefinition> candidates = new ArrayList<>();

    for (TagDefinition definition : tagDefinitions) {
      try {
        if (definition.matchName(nameLower))
          candidates.add(definition);
      } catch (Throwable thrownError) {
        LoggerProvider.get().log(Level.SEVERE, "An error occurred while trying to match via " + definition.getClass() + "#matchName", thrownError);
      }
    }

    int candidateCount = candidates.size();

    if (candidateCount == 0)
      return null;

    if (candidateCount != 1)
      candidates.sort(Comparator.comparingInt(item -> item.tagPriority.ordinal()));

    return candidates.get(0);
  }

  protected void register(TagDefinition tag) {
    if (!registeredClasses.add(tag.getClass()))
      throw new IllegalStateException("Duplicate tag-registration for " + tag.getClass());

    tagDefinitions.add(tag);
  }
}
