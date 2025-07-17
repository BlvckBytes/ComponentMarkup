package at.blvckbytes.component_markup.test_utils;

import at.blvckbytes.component_markup.markup.ast.tag.ExpressionList;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupList;
import at.blvckbytes.component_markup.util.JsonifyGetter;
import at.blvckbytes.component_markup.util.JsonifyIgnore;
import at.blvckbytes.component_markup.util.LoggerProvider;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Jsonifier {

  private static final Gson GSON_INSTANCE = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

  public static String jsonify(@Nullable Object instance) {
    return GSON_INSTANCE.toJson(_jsonify(instance));
  }

  private static JsonElement _jsonify(@Nullable Object instance) {
    if (instance == null)
      return JsonNull.INSTANCE;

    JsonObject result = new JsonObject();

    Class<?> currentClass = instance.getClass();

    result.addProperty("className", currentClass.getName());

    do {
      if (isJavaStandardClass(currentClass))
        break;

      for (Field field : currentClass.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()))
          continue;

        if (field.isAnnotationPresent(JsonifyIgnore.class))
          continue;

        String fieldName = field.getName();

        try {
          field.setAccessible(true);
          result.add(fieldName, jsonifyObject(field.getType(), field.get(instance)));
        } catch (Exception e) {
          LoggerProvider.log(Level.WARNING, "Could not access field " + fieldName + " of " + currentClass, e);
        }
      }

      for (Method method : currentClass.getDeclaredMethods()) {
        if (Modifier.isStatic(method.getModifiers()))
          continue;

        Class<? extends Annotation> annotationClass = JsonifyGetter.class;

        if (!method.isAnnotationPresent(annotationClass))
          continue;

        if (method.getParameterCount() > 0)
          LoggerProvider.log(Level.WARNING, "Method " + method + " requires parameters and thus isn't a valid " + annotationClass);

        String methodName = method.getName();

        try {
          method.setAccessible(true);
          result.add(methodName, jsonifyObject(method.getReturnType(), method.invoke(instance)));
        } catch (Exception e) {
          LoggerProvider.log(Level.WARNING, "Could not access method " + methodName + " of " + currentClass, e);
        }
      }

      currentClass = currentClass.getSuperclass();
    } while (currentClass != null && currentClass != Object.class);

    return result;
  }

  private static JsonElement jsonifyObject(@Nullable Class<?> fieldType, @Nullable Object item) {
    if (item == null) {
      if (fieldType != null && (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()))
        return new JsonArray();

      if (fieldType != null && Map.class.isAssignableFrom(fieldType))
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

    if (item instanceof Number)
      return new JsonPrimitive((Number) item);

    if (item instanceof String)
      return new JsonPrimitive((String) item);

    if (item instanceof Character)
      return new JsonPrimitive(String.valueOf(item));

    if (item instanceof Boolean)
      return new JsonPrimitive((Boolean) item);

    if (item instanceof Enum<?>)
      return new JsonPrimitive(((Enum<?>) item).name());

    if (item instanceof Collection<?>) {
      JsonArray result = new JsonArray();

      for (Object listItem : ((List<?>) item))
        result.add(jsonifyObject(null, listItem));

      return result;
    }

    if (item instanceof ExpressionList)
      return jsonifyObject(null, ((ExpressionList) item).get(null));

    if (item instanceof MarkupList)
      return jsonifyObject(null, ((MarkupList) item).get(null));

    if (item instanceof Map<?, ?>) {
      JsonObject result = new JsonObject();

      for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet())
        result.add(String.valueOf(entry.getKey()), jsonifyObject(null, entry.getValue()));

      return result;
    }

    return _jsonify(item);
  }

  private static boolean isJavaStandardClass(Class<?> clazz) {
    String packageName = clazz.getPackage().getName();

    return (
      packageName.startsWith("java.") ||
        packageName.startsWith("javax.") ||
        packageName.startsWith("jdk.") ||
        packageName.startsWith("sun.") ||
        packageName.startsWith("com.sun.") ||
        packageName.startsWith("org.w3c.dom.") ||
        packageName.startsWith("org.xml.sax.")
    );
  }
}
