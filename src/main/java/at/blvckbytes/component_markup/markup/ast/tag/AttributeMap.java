package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.ExpressionAttribute;
import at.blvckbytes.component_markup.markup.ast.tag.attribute.MarkupAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AttributeMap {

  private final List<Attribute> attributes;

  public AttributeMap() {
    this.attributes = new ArrayList<>();
  }

  public void add(Attribute attribute) {
    this.attributes.add(attribute);
  }

  public @Nullable ExpressionNode firstExpressionOrNull(String name) {
    for (Attribute attribute : attributes) {
      if (!attribute.name.equals(name))
        continue;

      if (!(attribute instanceof ExpressionAttribute))
        continue;

      return ((ExpressionAttribute) attribute).value;
    }

    return null;
  }

  public List<ExpressionNode> expressions(String name) {
    List<ExpressionNode> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!attribute.name.equals(name))
        continue;

      if (!(attribute instanceof ExpressionAttribute))
        continue;

      result.add(((ExpressionAttribute) attribute).value);
    }

    return result;
  }

  public @Nullable MarkupNode firstMarkupOrNull(String name) {
    for (Attribute attribute : attributes) {
      if (!attribute.name.equals(name))
        continue;

      if (!(attribute instanceof MarkupAttribute))
        continue;

      return ((MarkupAttribute) attribute).value;
    }

    return null;
  }

  public List<MarkupNode> markups(String name) {
    List<MarkupNode> result = new ArrayList<>();

    for (Attribute attribute : attributes) {
      if (!attribute.name.equals(name))
        continue;

      if (!(attribute instanceof MarkupAttribute))
        continue;

      result.add(((MarkupAttribute) attribute).value);
    }

    return result;
  }
}
