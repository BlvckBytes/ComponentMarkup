/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.markup.xml.TextWithSubViews;
import at.blvckbytes.component_markup.platform.AnsiStyleColor;
import at.blvckbytes.component_markup.platform.ComponentConstructor;
import at.blvckbytes.component_markup.platform.PackedColor;
import at.blvckbytes.component_markup.platform.SlotType;
import at.blvckbytes.component_markup.test_utils.renderer.ChatRenderer;
import at.blvckbytes.component_markup.util.LoggerProvider;
import at.blvckbytes.component_markup.util.InputView;
import com.google.gson.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;

public abstract class InterpreterTestsBase {

  private static final Gson gsonInstance = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
  private static final ComponentConstructor componentConstructor = new JsonComponentConstructor();


  @SuppressWarnings("SameParameterValue")
  protected void makeRecordedCase(
    TextWithSubViews input,
    InterpretationEnvironment environment,
    SlotType slot
  ) {
    makeRecordedCase(input, environment, slot, null);
  }

  @SuppressWarnings("SameParameterValue")
  protected void makeRecordedCase(
    TextWithSubViews input,
    InterpretationEnvironment environment,
    SlotType slot,
    @Nullable String nameSuffix
  ) {
    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

    String testCaseName = null;

    for (StackTraceElement stackTraceElement : stacktrace) {
      String methodName = stackTraceElement.getMethodName();
      try {
        Method method = getClass().getDeclaredMethod(methodName);

        if (!method.isAnnotationPresent(Test.class))
          continue;

        testCaseName = methodName;
        break;
      } catch (NoSuchMethodException ignored) {
      } catch (Exception e) {
        LoggerProvider.log(Level.SEVERE, "Could not access method", e);
      }
    }

    if (testCaseName == null)
      throw new IllegalStateException("Could not determine a name for this test-case");

    if (nameSuffix != null)
      testCaseName += "_" + nameSuffix;

    File resourcesFolder = Paths.get("src", "test", "resources").toFile();
    File caseFolder = new File(resourcesFolder, Paths.get("interpreter", testCaseName).toString());

    if (!caseFolder.exists()) {
      if (!caseFolder.mkdirs())
        throw new IllegalStateException("Could not create folders for " + caseFolder);
    }

    File componentsFile = new File(caseFolder, "components.json");

    MarkupNode actualNode;

    try {
      actualNode = MarkupParser.parse(InputView.of(input.text), BuiltInTagRegistry.INSTANCE);
    } catch (MarkupParseException e) {
      System.out.println(String.join("\n", e.makeErrorScreen()));
      Assertions.fail("Threw an error:", e);
      return;
    }

    List<Object> components = MarkupInterpreter.interpret(
      componentConstructor,
      environment,
      null,
      componentConstructor.getSlotContext(slot),
      actualNode
    ).unprocessedComponents;

    JsonArray actualArray = new JsonArray();

    for (Object component : components)
      actualArray.add((JsonElement) component);

    String actualJson = gsonInstance.toJson(actualArray);

    if (componentsFile.exists()) {
      LoggerProvider.log(Level.INFO, "Found data for case " + testCaseName, false);

      String expectedJson;

      try (
        FileReader fileReader = new FileReader(componentsFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader)
      ) {
        StringJoiner result = new StringJoiner("\n");

        String line;

        while ((line = bufferedReader.readLine()) != null)
          result.add(line);

        expectedJson = result.toString();
      } catch (Exception e) {
        Assertions.fail("Could not read stored components:", e);
        return;
      }

      Assertions.assertEquals(
        gsonInstance.toJson(sortKeysRecursively(gsonInstance.fromJson(expectedJson, JsonElement.class))),
        gsonInstance.toJson(sortKeysRecursively(gsonInstance.fromJson(actualJson, JsonElement.class)))
      );
      return;
    }

    LoggerProvider.log(Level.INFO, "Writing initial data for case " + testCaseName, false);

    try (
      FileWriter writer = new FileWriter(componentsFile)
    ) {
      writer.write(actualJson);
    } catch (Exception e) {
      Assertions.fail("Could not write components:", e);
    }

    try {
      File renderFile = new File(caseFolder, "render.png");
      BufferedImage image = ChatRenderer.render(components, componentConstructor.getSlotContext(slot));
      ImageIO.write(image, "png", renderFile);
    } catch (Exception e) {
      Assertions.fail("Could not render/write image:", e);
    }
  }

  protected JsonElement sortKeysRecursively(JsonElement input) {
    if (input instanceof JsonArray) {
      JsonArray jsonArray = (JsonArray) input;

      for (int index = 0; index < jsonArray.size(); ++index) {
        jsonArray.set(index, sortKeysRecursively(jsonArray.get(index)));
      }

      return jsonArray;
    }

    if (input instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) input;
      JsonObject result = new JsonObject();

      List<String> jsonKeys = new ArrayList<>(jsonObject.keySet());
      jsonKeys.sort(String::compareTo);

      for (String key : jsonKeys) {
        JsonElement value = sortKeysRecursively(jsonObject.get(key));

        if (value instanceof JsonPrimitive && key.equals("color")) {
          AnsiStyleColor ansiColor = AnsiStyleColor.fromNameLowerOrNull(value.getAsString().toLowerCase());

          if (ansiColor != null)
            value = new JsonPrimitive(PackedColor.asNonAlphaHex(ansiColor.packedColor));
        }

        result.add(key, value);
      }

      return result;
    }

    if (input instanceof JsonPrimitive || input instanceof JsonNull)
      return input;

    throw new IllegalStateException("Unaccounted-for json-element: " + input.getClass());
  }

  protected void makeCase(
    TextWithSubViews input,
    InterpretationEnvironment baseEnvironment,
    SlotType slot,
    JsonBuilder expectedResult
  ) {
    MarkupNode actualNode;

    try {
      actualNode = MarkupParser.parse(InputView.of(input.text), BuiltInTagRegistry.INSTANCE);
    } catch (MarkupParseException e) {
      System.out.println(String.join("\n", e.makeErrorScreen()));
      Assertions.fail("Threw an error:", e);
      return;
    }

    JsonElement expectedJson;

    if (expectedResult instanceof JsonObjectBuilder) {
      JsonArray array = new JsonArray();
      array.add(expectedResult.build());
      expectedJson = array;
    }
    else if (expectedResult instanceof JsonArrayBuilder)
      expectedJson = expectedResult.build();
    else
      throw new IllegalStateException("Unknown json-builder: " + expectedResult.getClass());

    List<Object> resultItems = MarkupInterpreter.interpret(
      componentConstructor,
      baseEnvironment,
      null,
      componentConstructor.getSlotContext(slot),
      actualNode
    ).unprocessedComponents;

    JsonArray actualJson = new JsonArray();

    for (Object resultItem : resultItems)
      actualJson.add((JsonElement) resultItem);

    Assertions.assertEquals(
      gsonInstance.toJson(sortKeysRecursively(expectedJson)),
      gsonInstance.toJson(sortKeysRecursively(actualJson))
    );
  }

}
