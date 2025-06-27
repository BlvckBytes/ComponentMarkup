package at.blvckbytes.component_markup.util;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Jsonifiable {

  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().create();

  public JsonObject jsonify() {
    return jsonify(this);
  }

  public static JsonObject jsonify(Object instance) {
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
          throw new IllegalStateException("Could not access field " + fieldName + " of " + currentClass, e);
        }
      }

      for (Method method : currentClass.getDeclaredMethods()) {
        if (Modifier.isStatic(method.getModifiers()))
          continue;

        if (!method.isAnnotationPresent(JsonifyGetter.class))
          continue;

        if (method.getParameterCount() > 0)
          throw new IllegalStateException("Can only call getters which require no arguments");

        String methodName = method.getName();

        try {
          method.setAccessible(true);
          result.add(methodName, jsonifyObject(method.getReturnType(), method.invoke(instance)));
        } catch (Exception e) {
          throw new IllegalStateException("Could not access method " + methodName + " of " + currentClass, e);
        }
      }

      currentClass = currentClass.getSuperclass();
    } while (currentClass != null && currentClass != Object.class);

    return result;
  }

  private static JsonElement jsonifyObject(@Nullable Class<?> type, @Nullable Object item) {
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

  @Override
  public String toString() {
    return gsonInstance.toJson(jsonify());
  }

  public static String toString(Object item) {
    return gsonInstance.toJson(Jsonifiable.jsonify(item));
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
