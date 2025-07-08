package at.blvckbytes.component_markup.util;

import at.blvckbytes.component_markup.markup.ast.tag.ExpressionList;
import at.blvckbytes.component_markup.markup.ast.tag.MarkupList;
import at.blvckbytes.component_markup.util.json.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Jsonifiable {

  public _JsonObject jsonify() {
    return jsonify(this);
  }

  public static _JsonObject jsonify(Object instance) {
    _JsonObject result = new _JsonObject();

    Class<?> currentClass = instance.getClass();

    result.add("className", new _JsonString(currentClass.getName()));

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
          LoggerProvider.get().log(Level.WARNING, "Could not access field " + fieldName + " of " + currentClass, e);
        }
      }

      for (Method method : currentClass.getDeclaredMethods()) {
        if (Modifier.isStatic(method.getModifiers()))
          continue;

        Class<? extends Annotation> annotationClass = JsonifyGetter.class;

        if (!method.isAnnotationPresent(annotationClass))
          continue;

        if (method.getParameterCount() > 0)
          LoggerProvider.get().log(Level.WARNING, "Method " + method + " requires parameters and thus isn't a valid " + annotationClass);

        String methodName = method.getName();

        try {
          method.setAccessible(true);
          result.add(methodName, jsonifyObject(method.getReturnType(), method.invoke(instance)));
        } catch (Exception e) {
          LoggerProvider.get().log(Level.WARNING, "Could not access method " + methodName + " of " + currentClass, e);
        }
      }

      currentClass = currentClass.getSuperclass();
    } while (currentClass != null && currentClass != Object.class);

    return result;
  }

  private static _JsonElement jsonifyObject(@Nullable Class<?> type, @Nullable Object item) {
    if (item == null) {
      if (type != null && (Collection.class.isAssignableFrom(type) || type.isArray()))
        return new _JsonArray();

      if (type != null && Map.class.isAssignableFrom(type))
        return new _JsonObject();

      return _JsonString.NULL;
    }

    if (item.getClass().isArray()) {
      _JsonArray result = new _JsonArray();
      int arrayLength = Array.getLength(item);

      for (int index = 0; index < arrayLength; ++index)
        result.add(jsonifyObject(null, Array.get(item, index)));

      return result;
    }

    if (item instanceof Jsonifiable)
      return ((Jsonifiable) item).jsonify();

    if (item instanceof Number)
      return new _JsonNumber((Number) item);

    if (item instanceof String)
      return new _JsonString((String) item);

    if (item instanceof Character)
      return new _JsonString(String.valueOf(item));

    if (item instanceof Boolean)
      return new _JsonBoolean((Boolean) item);

    if (item instanceof Enum<?>)
      return new _JsonString(((Enum<?>) item).name());

    if (item instanceof Collection<?>) {
      _JsonArray result = new _JsonArray();

      for (Object listItem : ((List<?>) item))
        result.add(jsonifyObject(null, listItem));

      return result;
    }

    if (item instanceof ExpressionList)
      return jsonifyObject(null, ((ExpressionList) item).get(null));

    if (item instanceof MarkupList)
      return jsonifyObject(null, ((MarkupList) item).get(null));

    if (item instanceof Map<?, ?>) {
      _JsonObject result = new _JsonObject();

      for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet())
        result.add(String.valueOf(entry.getKey()), jsonifyObject(null, entry.getValue()));

      return result;
    }

    LoggerProvider.get().log(Level.WARNING, "Don't know how to stringify " + item.getClass());

    return _JsonString.NULL;
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
