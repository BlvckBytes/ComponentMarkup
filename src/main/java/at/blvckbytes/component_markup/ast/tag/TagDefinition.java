package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.attribute.*;
import at.blvckbytes.component_markup.xml.CursorPosition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TagDefinition {

  public static final AttributeDefinition[] NO_ATTRIBUTES = new AttributeDefinition[0];

  public abstract boolean matchName(String tagName);

  public abstract TagClosing getClosing();

  public abstract TagPriority getPriority();

  public abstract AttributeDefinition[] getAttributes();

  public abstract AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute<?>> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  );

  protected @Nullable String tryGetStringAttribute(String name, List<Attribute<?>> attributes) {
    return tryGetAttribute(name, StringAttribute.class, attributes);
  }

  protected String getStringAttribute(String name, List<Attribute<?>> attributes) {
    return getAttribute(name, StringAttribute.class, attributes);
  }

  protected @Nullable Long tryGetLongAttribute(String name, List<Attribute<?>> attributes) {
    return tryGetAttribute(name, LongAttribute.class, attributes);
  }

  protected Long getLongAttribute(String name, List<Attribute<?>> attributes) {
    return getAttribute(name, LongAttribute.class, attributes);
  }

  protected @Nullable Double tryGetDoubleAttribute(String name, List<Attribute<?>> attributes) {
    return tryGetAttribute(name, DoubleAttribute.class, attributes);
  }

  protected Double getDoubleAttribute(String name, List<Attribute<?>> attributes) {
    return getAttribute(name, DoubleAttribute.class, attributes);
  }

  protected @Nullable Boolean tryGetBooleanAttribute(String name, List<Attribute<?>> attributes) {
    return tryGetAttribute(name, BooleanAttribute.class, attributes);
  }

  protected Boolean getBooleanAttribute(String name, List<Attribute<?>> attributes) {
    return getAttribute(name, BooleanAttribute.class, attributes);
  }

  protected @Nullable AstNode tryGetSubtreeAttribute(String name, List<Attribute<?>> attributes) {
    return tryGetAttribute(name, SubtreeAttribute.class, attributes);
  }

  protected AstNode getSubtreeAttribute(String name, List<Attribute<?>> attributes) {
    return getAttribute(name, SubtreeAttribute.class, attributes);
  }

  protected <T> @Nullable T tryGetAttribute(String name, Class<? extends Attribute<T>> type, List<Attribute<?>> attributes) {
    Attribute<?> result = null;

    for (Attribute<?> attribute : attributes) {
      if (!attribute.name.equalsIgnoreCase(name))
        continue;

      result = attribute;
      break;
    }

    if (result == null)
      return null;

    if (!type.isInstance(result))
      throw new IllegalStateException("Expected attribute \"" + name + "\" to be a " + type.getSimpleName());

    return type.cast(result).getValue();
  }

  protected <T> T getAttribute(String name, Class<? extends Attribute<T>> type, List<Attribute<?>> attributes) {
    Attribute<?> result = null;

    for (Attribute<?> attribute : attributes) {
      if (!attribute.name.equalsIgnoreCase(name))
        continue;

      result = attribute;
      break;
    }

    if (result == null)
      throw new IllegalStateException("Required absent attribute \"" + name + "\"");

    if (!type.isInstance(result))
      throw new IllegalStateException("Expected attribute \"" + name + "\" to be a " + type.getSimpleName());

    return type.cast(result).getValue();
  }

  protected List<String> getStringAttributes(String name, List<Attribute<?>> attributes) {
    return getAttributes(name, StringAttribute.class, attributes);
  }

  protected List<Long> getLongAttributes(String name, List<Attribute<?>> attributes) {
    return getAttributes(name, LongAttribute.class, attributes);
  }

  protected List<Double> getDoubleAttributes(String name, List<Attribute<?>> attributes) {
    return getAttributes(name, DoubleAttribute.class, attributes);
  }

  protected List<Boolean> getBooleanAttributes(String name, List<Attribute<?>> attributes) {
    return getAttributes(name, BooleanAttribute.class, attributes);
  }

  protected List<AstNode> getSubtreeAttributes(String name, List<Attribute<?>> attributes) {
    return getAttributes(name, SubtreeAttribute.class, attributes);
  }

  protected <T> List<T> getAttributes(String name, Class<? extends Attribute<T>> type, List<Attribute<?>> attributes) {
    List<T> result = new ArrayList<>();

    for (Attribute<?> attribute : attributes) {
      if (!attribute.name.equalsIgnoreCase(name))
        continue;

      if (!type.isInstance(attribute))
        throw new IllegalStateException("Expected attribute \"" + name + "\" to be a " + type.getSimpleName());

      result.add(type.cast(attribute).getValue());
    }

    return result;
  }
}
