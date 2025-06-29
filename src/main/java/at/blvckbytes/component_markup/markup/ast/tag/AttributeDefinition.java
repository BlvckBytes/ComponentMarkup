package at.blvckbytes.component_markup.markup.ast.tag;

import at.blvckbytes.component_markup.markup.ast.tag.attribute.Attribute;

import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class AttributeDefinition {

  public static final Pattern NAME_PATTERN = Pattern.compile("^[a-z]+(-[a-z]+)*$");

  public final String name;
  public final EnumSet<AttributeFlag> flags;
  public final Class<? extends Attribute> valueType;

  protected AttributeDefinition(String name, Class<? extends Attribute> valueType, AttributeFlag... flags) {
    this.name = name.toLowerCase();

    if (!NAME_PATTERN.matcher(this.name).matches())
      throw new IllegalStateException("Malformed attribute-name; please adhere to " + NAME_PATTERN.pattern());

    if (this.name.startsWith("let-"))
      throw new IllegalStateException("The let- namespace is reserved as to bind variables with");

    if (this.name.equals("let"))
      throw new IllegalStateException("Do not use let as an attribute, as it is easily confused with let-binding");

    if (this.name.startsWith("for-"))
      throw new IllegalStateException("The for- namespace is reserved as to pass parameters to loops");

    this.flags = flags.length == 0 ? EnumSet.noneOf(AttributeFlag.class) : EnumSet.of(flags[0], flags);
    this.valueType = valueType;
  }

  public boolean matches(Attribute attribute) {
    return this.valueType.isInstance(attribute) && this.name.equals(attribute.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (!getClass().isInstance(other)) return false;
    AttributeDefinition that = (AttributeDefinition) other;
    return Objects.equals(name, that.name) && Objects.equals(flags, that.flags);
  }
}
