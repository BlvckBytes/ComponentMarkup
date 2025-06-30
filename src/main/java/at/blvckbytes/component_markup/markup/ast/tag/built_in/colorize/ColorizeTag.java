package at.blvckbytes.component_markup.markup.ast.tag.built_in.colorize;

import at.blvckbytes.component_markup.markup.ast.tag.*;

public abstract class ColorizeTag extends TagDefinition {

  private static final ExpressionAttributeDefinition ATTR_PHASE = new ExpressionAttributeDefinition("phase");
  private static final ExpressionAttributeDefinition ATTR_DEEP = new ExpressionAttributeDefinition("deep");
  private static final ExpressionAttributeDefinition ATTR_OVERRIDE_COLORS = new ExpressionAttributeDefinition("override-colors");
  private static final ExpressionAttributeDefinition ATTR_SKIP_WHITESPACE = new ExpressionAttributeDefinition("skip-whitespace");
  private static final ExpressionAttributeDefinition ATTR_SKIP_NON_TEXT = new ExpressionAttributeDefinition("skip-non-text");
  private static final ExpressionAttributeDefinition ATTR_MERGE_INNER = new ExpressionAttributeDefinition("merge-inner");

  protected ColorizeTag(String[] staticPrefixes, TagPriority tagPriority, AttributeDefinition... attributes) {
    super(
      staticPrefixes,
      TagClosing.OPEN_CLOSE,
      tagPriority,
      concatenate(attributes, new AttributeDefinition[] {
        ATTR_PHASE,
        ATTR_DEEP,
        ATTR_OVERRIDE_COLORS,
        ATTR_SKIP_WHITESPACE,
        ATTR_SKIP_NON_TEXT,
        ATTR_MERGE_INNER
      })
    );
  }

  protected ColorizeAttributes getBaseAttributes(AttributeMap attributes) {
    return new ColorizeAttributes(
      ATTR_PHASE.singleOrNull(attributes),
      ATTR_DEEP.singleOrNull(attributes),
      ATTR_OVERRIDE_COLORS.singleOrNull(attributes),
      ATTR_SKIP_WHITESPACE.singleOrNull(attributes),
      ATTR_SKIP_NON_TEXT.singleOrNull(attributes),
      ATTR_MERGE_INNER.singleOrNull(attributes)
    );
  }

  private static AttributeDefinition[] concatenate(AttributeDefinition[] a, AttributeDefinition[] b) {
    AttributeDefinition[] result = new AttributeDefinition[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }
}
