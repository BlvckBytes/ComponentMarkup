package at.blvckbytes.component_markup.ast.tag;

import java.util.regex.Pattern;

public class AttributeDefinition {

  public static final Pattern NAME_PATTERN = Pattern.compile("^[a-z]+(-[a-z]+)*$");

  public final String name;
  public final AttributeType type;
  public final boolean multiValue;
  public final boolean mandatory;

  public AttributeDefinition(
    String name,
    AttributeType type,
    boolean multiValue,
    boolean mandatory
  ) {
    if (!NAME_PATTERN.matcher(name).matches())
      throw new IllegalStateException("Malformed attribute-name; please adhere to " + NAME_PATTERN.pattern());

    if (name.startsWith("let-"))
      throw new IllegalStateException("The let- namespace is reserved as to bind variables with");

    if (name.equals("let"))
      throw new IllegalStateException("Do not use let as an attribute, as it is easily confused with let-binding");

    if (name.startsWith("for-"))
      throw new IllegalStateException("The for- namespace is reserved as to pass parameters to loops");

    this.name = name;
    this.type = type;
    this.multiValue = multiValue;
    this.mandatory = mandatory;
  }
}
