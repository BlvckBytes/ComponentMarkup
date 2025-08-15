/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PublicFieldMapTests {

  static class ExampleClass {
    public static final String test1 = "";
    private static final int test2 = 5;
    private final long test3 = 3;
    public final boolean test4 = true;
    private double test5 = 3.1415;
    public String test6 = "hello";
    public double test7 = 8.8;
  }

  @Test
  public void shouldTransformToSnakeCase() {
    PublicFieldMap fieldMap = new PublicFieldMap(ExampleClass.class);
    ExampleClass instance = new ExampleClass();

    Assertions.assertEquals("hello_world", fieldMap.toSnakeCase("_12helloWorld"));
    Assertions.assertEquals("my_example12_variable", fieldMap.toSnakeCase("myExample12Variable"));
    Assertions.assertNull(fieldMap.locateField("test1"));
    Assertions.assertNull(fieldMap.locateField("test2"));
    Assertions.assertNull(fieldMap.locateField("test3"));
    assertFieldValue(instance, fieldMap.locateField("test4"), true);
    Assertions.assertNull(fieldMap.locateField("test5"));
    assertFieldValue(instance, fieldMap.locateField("test6"), "hello");
    assertFieldValue(instance, fieldMap.locateField("test7"), 8.8);
  }

  private void assertFieldValue(Object instance, @Nullable Field field, Object expectedValue) {
    Assertions.assertNotNull(field);
    Assertions.assertDoesNotThrow(() -> {
      Assertions.assertEquals(expectedValue, field.get(instance));
    });
  }

  private void assertMethodValue(Object instance, @Nullable Method method, Object expectedValue) {
    Assertions.assertNotNull(method);
    Assertions.assertDoesNotThrow(() -> {
      Assertions.assertEquals(expectedValue, method.invoke(instance));
    });
  }
}
