package at.blvckbytes.component_markup.expression.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PublicFieldMapTests {

  static class ExampleClass {
    public static final String test1 = "";
    private static final int test2 = 5;
    private final long test3 = 3;
    public final boolean test4 = true;
    private double test5 = 3.1415;
    public String test6 = "";
    public double test7 = 8.8;
  }

  @Test
  public void shouldTransformToSnakeCase() {
    PublicFieldMap fieldMap = new PublicFieldMap(ExampleClass.class);

    Assertions.assertEquals("hello_world", fieldMap.toSnakeCase("_12helloWorld"));
    Assertions.assertEquals("my_example12_variable", fieldMap.toSnakeCase("myExample12Variable"));
    Assertions.assertNull(fieldMap.locateField("test1"));
    Assertions.assertNull(fieldMap.locateField("test2"));
    Assertions.assertNull(fieldMap.locateField("test3"));
    Assertions.assertNotNull(fieldMap.locateField("test4"));
    Assertions.assertNull(fieldMap.locateField("test5"));
    Assertions.assertNotNull(fieldMap.locateField("test6"));
    Assertions.assertNotNull(fieldMap.locateField("test7"));
  }
}
