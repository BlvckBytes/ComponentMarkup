package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class PublicFieldMap {

  private final Map<String, Field> fieldByIdentifier;

  public PublicFieldMap(Class<?> clazz) {
    this.fieldByIdentifier = new HashMap<>();

    while (clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        int modifiers = field.getModifiers();

        if (Modifier.isStatic(modifiers))
          continue;

        if (!Modifier.isPublic(modifiers))
          continue;

        fieldByIdentifier.put(toSnakeCase(field.getName()), field);
      }

      clazz = clazz.getSuperclass();
    }
  }

  public @Nullable Field locateField(String name) {
    if (name.isEmpty() || Character.isDigit(name.charAt(0)))
      return null;

    return fieldByIdentifier.get(toSnakeCase(name));
  }

  public String toSnakeCase(String identifier) {
    StringBuilder result = new StringBuilder(identifier.length());

    int length = identifier.length();

    char lastChar = 0;

    for (int index = 0; index < length; ++index) {
      char currentChar = identifier.charAt(index);

      if (currentChar == '_') {
        if (result.length() == 0)
          continue;

        if (lastChar == '_')
          continue;

        result.append(currentChar);
        lastChar = currentChar;
        continue;
      }

      boolean isDigit = currentChar >= '0' && currentChar <= '9';

      if (isDigit && result.length() == 0)
        continue;

      boolean isLower = isDigit || currentChar >= 'a' && currentChar <= 'z';
      boolean isUpper = currentChar >= 'A' && currentChar <= 'Z';

      if (!(isLower || isUpper))
        continue;

      if (isUpper) {
        if (lastChar != 0 && lastChar != '_')
          result.append('_');

        currentChar = Character.toLowerCase(currentChar);
      }

      result.append(currentChar);
      lastChar = currentChar;
    }

    return result.toString();
  }
}
