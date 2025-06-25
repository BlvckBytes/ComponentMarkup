package at.blvckbytes.component_markup.util;

import com.google.gson.*;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Jsonifiable {

  public JsonObject jsonify() {
    JsonObject result = new JsonObject();

    Class<?> currentClass = getClass();

    result.addProperty("className", currentClass.getName());

    do {
      for (Field field : currentClass.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()))
          continue;

        if (field.isAnnotationPresent(JsonifyIgnore.class))
          continue;

        String fieldName = field.getName();
        JsonElement overrideRepresentation = overrideJsonRepresentation(fieldName);

        if (overrideRepresentation != null) {
          result.add(fieldName, overrideRepresentation);
          continue;
        }

        try {
          field.setAccessible(true);
          result.add(fieldName, jsonifyObject(field.getType(), field.get(this)));
        } catch (Exception e) {
          throw new IllegalStateException("Could not access field " + fieldName + " of " + currentClass, e);
        }
      }

      currentClass = currentClass.getSuperclass();
    } while (currentClass != null && currentClass != Object.class);

    return result;
  }

  protected @Nullable JsonElement overrideJsonRepresentation(String field) {
    return null;
  }

  private JsonElement jsonifyObject(@Nullable Class<?> type, @Nullable Object item) {
    if (item == null) {
      if (type != null && (Collection.class.isAssignableFrom(type) || type.isArray()))
        return new JsonArray();

      if (type != null && Map.class.isAssignableFrom(type))
        return new JsonObject();

      return JsonNull.INSTANCE;
    }

    if (item.getClass().isArray()) {
      JsonArray result = new JsonArray();
      int arrayLength = Array.getLength(item);

      for (int index = 0; index < arrayLength; ++index)
        result.add(jsonifyObject(null, Array.get(item, index)));

      return result;
    }

    if (item instanceof Jsonifiable)
      return ((Jsonifiable) item).jsonify();

    if (item instanceof AExpression)
      return new JsonPrimitive(((AExpression) item).expressionify());

    if (item instanceof Number)
      return new JsonPrimitive((Number) item);

    if (item instanceof String)
      return new JsonPrimitive((String) item);

    if (item instanceof Boolean)
      return new JsonPrimitive((Boolean) item);

    if (item instanceof Enum<?>)
      return new JsonPrimitive(((Enum<?>) item).name());

    if (item instanceof List<?>) {
      JsonArray result = new JsonArray();

      for (Object listItem : ((List<?>) item))
        result.add(jsonifyObject(null, listItem));

      return result;
    }

    throw new IllegalStateException("Don't know how to stringify " + item.getClass().getSimpleName());
  }
}
