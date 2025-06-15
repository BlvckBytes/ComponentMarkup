package at.blvckbytes.component_markup.ast.tag;

import at.blvckbytes.component_markup.ast.node.AstNode;
import at.blvckbytes.component_markup.ast.tag.attribute.*;
import at.blvckbytes.component_markup.xml.CursorPosition;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class TagDefinition {

  public static final AttributeDefinition[] NO_ATTRIBUTES = new AttributeDefinition[0];

  private final AttributeDefinition[] attributes;
  public final Collection<String> staticPrefixes;

  protected TagDefinition(AttributeDefinition[] attributes, String[] staticPrefixes) {
    this.attributes = attributes;
    this.staticPrefixes = Arrays.asList(staticPrefixes);
  }

  public @Nullable AttributeDefinition getAttribute(String attributeName) {
    for (AttributeDefinition attribute : attributes) {
      if (attribute.name.equalsIgnoreCase(attributeName))
        return attribute;
    }

    return null;
  }

  // TODO: tagName should be called tagNameLower
  public abstract boolean matchName(String tagName);

  // TODO: These really don't need to be methods, do they? Have them as public final properties.

  public abstract TagClosing getClosing();

  public abstract TagPriority getPriority();

  // TODO: tagName should be called tagNameLower
  public abstract AstNode construct(
    String tagName,
    CursorPosition position,
    List<Attribute> attributes,
    List<LetBinding> letBindings,
    List<AstNode> children
  );

  protected static AstNode findSubtreeAttribute(String name, List<Attribute> attributes) {
    AstNode value = tryFindSubtreeAttribute(name, attributes);

    if (value == null)
      throw new IllegalStateException("Required attribute '" + name + "' to be present");

    return value;
  }

  protected static @Nullable AstNode tryFindSubtreeAttribute(String name, List<Attribute> attributes) {
    Attribute attribute = tryFindAttribute(name, attributes);

    if (attribute == null)
      return null;

    if (attribute instanceof SubtreeAttribute)
      return ((SubtreeAttribute) attribute).value;

    throw new IllegalStateException("Required attribute '" + name + "' to be of type subtree");
  }

  protected static AExpression findExpressionAttribute(String name, List<Attribute> attributes) {
    AExpression value = tryFindExpressionAttribute(name, attributes);

    if (value == null)
      throw new IllegalStateException("Required attribute '" + name + "' to be present");

    return value;
  }

  protected static @Nullable AExpression tryFindExpressionAttribute(String name, List<Attribute> attributes) {
    Attribute attribute = tryFindAttribute(name, attributes);

    if (attribute == null)
      return null;

    if (attribute instanceof ExpressionAttribute)
      return ((ExpressionAttribute) attribute).value;

    throw new IllegalStateException("Required attribute '" + name + "' to be of type expression");
  }

  private static @Nullable Attribute tryFindAttribute(String name, List<Attribute> attributes) {
    for (Attribute attribute : attributes) {
      if (attribute.name.equalsIgnoreCase(name))
        return attribute;
    }

    return null;
  }

  protected static List<AExpression> findExpressionAttributes(String name, List<Attribute> attributes) {
    List<AExpression> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!attribute.name.equalsIgnoreCase(name))
        continue;

      if (!(attribute instanceof ExpressionAttribute))
        throw new IllegalStateException("Required attribute '" + name + "' to be of type expression");

      result.add(((ExpressionAttribute) attribute).value);
    }

    return result;
  }

  protected static List<AstNode> findSubtreeAttributes(String name, List<Attribute> attributes) {
    List<AstNode> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!attribute.name.equalsIgnoreCase(name))
        continue;

      if (!(attribute instanceof SubtreeAttribute))
        throw new IllegalStateException("Required attribute '" + name + "' to be of type subtree");

      result.add(((SubtreeAttribute) attribute).value);
    }

    return result;
  }
}
