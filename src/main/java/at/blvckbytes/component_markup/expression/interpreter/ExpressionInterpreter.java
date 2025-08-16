/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.token.IdentifierToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.InfixOperatorToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.StringToken;
import at.blvckbytes.component_markup.expression.tokenizer.token.TerminalToken;
import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import at.blvckbytes.component_markup.util.DeepIterator;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ExpressionInterpreter {

  private static final double DOUBLE_EQUALITY_THRESHOLD = .001;

  private static final Map<String, Pattern> patternCache = new HashMap<>();

  private ExpressionInterpreter() {}

  public static @Nullable Object interpret(@Nullable ExpressionNode expression, InterpretationEnvironment environment) {
    if (expression == null)
      return null;

    if (expression instanceof TerminalNode)
      return ((TerminalNode) expression).getValue(environment);

    ValueInterpreter valueInterpreter = environment.getValueInterpreter();

    if (expression instanceof TransformerNode) {
      TransformerNode node = (TransformerNode) expression;
      return node.transformer.transform(interpret(node.wrapped, environment), environment);
    }

    if (expression instanceof PrefixOperationNode) {
      PrefixOperationNode node = (PrefixOperationNode) expression;
      Object operandValue = interpret(node.operand, environment);

      if (operandValue == null)
        return null;

      PrefixOperator prefixOperator = node.operatorToken.operator;

      switch (prefixOperator) {
        case NEGATION:
          return !valueInterpreter.asBoolean(operandValue);

        case FLIP_SIGN:
          return flipSignOf(valueInterpreter.asLongOrDouble(operandValue));

        case LOWER_CASE:
          return valueInterpreter.asString(operandValue).toLowerCase(Locale.ROOT);

        case UPPER_CASE:
          return valueInterpreter.asString(operandValue).toUpperCase(Locale.ROOT);

        case TITLE_CASE:
          return toTitleCase(valueInterpreter.asString(operandValue));

        case TOGGLE_CASE:
          return toggleCase(valueInterpreter.asString(operandValue));

        case SLUGIFY:
          return slugify(valueInterpreter.asString(operandValue));

        case ASCIIFY:
          return asciify(valueInterpreter.asString(operandValue));

        case TRIM:
          return valueInterpreter.asString(operandValue).trim();

        case REVERSE:
          return new StringBuilder(valueInterpreter.asString(operandValue)).reverse().toString();

        case LONG:
          return valueInterpreter.asLong(operandValue);

        case DOUBLE:
          return valueInterpreter.asDouble(operandValue);

        case ROUND:
        case CEIL:
        case FLOOR: {
          Number numericValue = valueInterpreter.asLongOrDouble(operandValue);

          if (numericValue instanceof Long)
            return numericValue;

          double doubleValue = numericValue.doubleValue();

          switch (prefixOperator) {
            case ROUND:
              return (double) Math.round(doubleValue);
            case FLOOR:
              return Math.floor(doubleValue);
            case CEIL:
              return Math.ceil(doubleValue);
          }
        }

        case MIN:
        case MAX: {
          if (!(operandValue instanceof Iterable<?>))
            return operandValue;

          Number result = null;

          for (Iterator<Number> it = new DeepIterator<>((Iterable<?>) operandValue, valueInterpreter::asLongOrDouble); it.hasNext();) {
            Number number = it.next();

            if (result == null || ((compareNumbers(number, result) > 0) ^ (prefixOperator == PrefixOperator.MIN)))
              result = number;
          }

          return result;
        }

        case SUM:
        case AVG: {
          if (!(operandValue instanceof Iterable<?>))
            return operandValue;

          double accumulator = 0;
          int memberCount = 0;

          for (Iterator<Number> it = new DeepIterator<>((Iterable<?>) operandValue, valueInterpreter::asLongOrDouble); it.hasNext();) {
            accumulator += it.next().doubleValue();
            ++memberCount;
          }

          if (memberCount == 0)
            return 0;

          if (memberCount == 1 || prefixOperator == PrefixOperator.SUM)
            return accumulator;

          return accumulator / memberCount;
        }

        default:
          LoggerProvider.log(Level.WARNING, "Unimplemented prefix-operator: " + prefixOperator);
          return null;
      }
    }

    if (expression instanceof InfixOperationNode) {
      InfixOperationNode node = (InfixOperationNode) expression;
      Object lhsValue = interpret(node.lhs, environment);

      InfixOperator infixOperator = node.operatorToken.operator;
      ArithmeticOperator arithmeticOperator = ArithmeticOperator.fromInfix(infixOperator);

      if (infixOperator == InfixOperator.MEMBER) {
        Object rhsValue = null;
        boolean isRhsIdentifier = false;

        if (node.rhs instanceof TerminalNode) {
          TerminalToken terminalToken = ((TerminalNode) node.rhs).token;

          if (terminalToken instanceof IdentifierToken) {
            rhsValue = ((IdentifierToken) terminalToken).identifier;
            isRhsIdentifier = true;
          }
        }

        if (rhsValue == null)
          rhsValue = interpret(node.rhs, environment);

        return performSubscripting(node.operatorToken, lhsValue, rhsValue, isRhsIdentifier, environment);
      }

      Object rhsValue = interpret(node.rhs, environment);

      if (arithmeticOperator != null) {
        Number lhs = valueInterpreter.asLongOrDouble(lhsValue);
        Number rhs = valueInterpreter.asLongOrDouble(rhsValue);

        if ((lhs instanceof Double || lhs instanceof Float) || (rhs instanceof Double || rhs instanceof Float))
          return arithmeticOperator.doubleOperation(lhs.doubleValue(), rhs.doubleValue());

        return arithmeticOperator.longOperation(lhs.longValue(), rhs.longValue());
      }

      switch (infixOperator) {
        case CONCATENATION:
          return valueInterpreter.asString(lhsValue) + valueInterpreter.asString(rhsValue);

        case REGEX_SPLIT:
        case SPLIT: {
          String input = valueInterpreter.asString(lhsValue);
          String delimiter = rhsValue == null ? "" : valueInterpreter.asString(rhsValue);

          Pattern pattern;

          try {
            if (infixOperator == InfixOperator.SPLIT)
              pattern = Pattern.compile(delimiter, Pattern.LITERAL);
            else
              pattern = patternCache.computeIfAbsent(delimiter, Pattern::compile);
          } catch (Throwable e) {
            for (String line : ErrorScreen.make(node.rhs.getFirstMemberPositionProvider(), "Encountered malformed pattern: \"" + delimiter + "\""))
              LoggerProvider.log(Level.WARNING, line, false);

            return Collections.emptyList();
          }

          return Arrays.asList(pattern.split(input));
        }

        case REPEAT: {
          String input = valueInterpreter.asString(lhsValue);
          int count = (int) valueInterpreter.asLong(rhsValue);

          StringBuilder result = new StringBuilder(input.length() * count);

          for (int i = 0; i < count; ++i)
            result.append(input);

          return result.toString();
        }

        case GREATER_THAN_OR_EQUAL:
        case LESS_THAN_OR_EQUAL:
          if (checkEquality(lhsValue, rhsValue, valueInterpreter))
            return true;

        case GREATER_THAN:
        case LESS_THAN: {
          int comparisonResult = compareNumbers(
            valueInterpreter.asLongOrDouble(lhsValue),
            valueInterpreter.asLongOrDouble(rhsValue)
          );

          return (
            infixOperator == InfixOperator.GREATER_THAN || infixOperator == InfixOperator.GREATER_THAN_OR_EQUAL
              ? comparisonResult > 0
              : comparisonResult < 0
          );
        }

        case EQUAL_TO:
          return checkEquality(lhsValue, rhsValue, valueInterpreter);

        case NOT_EQUAL_TO:
          return !checkEquality(lhsValue, rhsValue, valueInterpreter);

        case RANGE: {
          long lowerBound = valueInterpreter.asLong(lhsValue);
          long upperBound = valueInterpreter.asLong(rhsValue);
          List<Long> result = new ArrayList<>();

          if (upperBound >= lowerBound) {
            for (long index = lowerBound; index <= upperBound; ++index)
              result.add(index);
          }

          return result;
        }

        case CONJUNCTION:
          return valueInterpreter.asBoolean(lhsValue) && valueInterpreter.asBoolean(rhsValue);

        case DISJUNCTION:
          return valueInterpreter.asBoolean(lhsValue) || valueInterpreter.asBoolean(rhsValue);

        case FALLBACK:
          return lhsValue != null ? lhsValue : rhsValue;

        case SUBSCRIPTING:
          return performSubscripting(node.operatorToken, lhsValue, rhsValue, false, environment);

        case IN:
          return checkContains(rhsValue, lhsValue, valueInterpreter);

        case MATCHES_REGEX: {
          String input = valueInterpreter.asString(lhsValue);
          String patternString = rhsValue == null ? "" : valueInterpreter.asString(rhsValue);

          Pattern pattern;

          try {
            pattern = patternCache.computeIfAbsent(patternString, Pattern::compile);
          } catch (Throwable e) {
            for (String line : ErrorScreen.make(node.rhs.getFirstMemberPositionProvider(), "Encountered malformed pattern: \"" + patternString + "\""))
              LoggerProvider.log(Level.WARNING, line, false);

            return false;
          }

          return pattern.matcher(input).find();
        }

        default:
          LoggerProvider.log(Level.WARNING, "Unimplemented infix-operator: " + infixOperator);
          return null;
      }
    }

    if (expression instanceof BranchingNode) {
      BranchingNode node = (BranchingNode) expression;

      if (valueInterpreter.asBoolean(interpret(node.condition, environment)))
        return interpret(node.branchTrue, environment);

      return interpret(node.branchFalse, environment);
    }

    if (expression instanceof SubstringNode) {
      SubstringNode node = (SubstringNode) expression;

      return performSubstring(
        valueInterpreter.asString(interpret(node.operand, environment)),
        node, environment
      );
    }

    if (expression instanceof ArrayNode) {
      ArrayNode node = (ArrayNode) expression;

      List<Object> result = new ArrayList<>();

      for (ExpressionNode item : node.items)
        result.add(interpret(item, environment));

      return result;
    }

    if (expression instanceof MapNode) {
      MapNode node = (MapNode) expression;

      Map<String, Object> result = new LinkedHashMap<>();

      for (Map.Entry<String, ExpressionNode> item : node.items.entrySet())
        result.put(item.getKey(), interpret(item.getValue(), environment));

      return result;
    }

    LoggerProvider.log(Level.WARNING, "Unimplemented node: " + expression.getClass());
    return null;
  }

  private static int compareNumbers(Number a, Number b) {
    if (a instanceof Double || b instanceof Double)
      return Double.compare(a.doubleValue(), b.doubleValue());

    return Long.compare(a.longValue(), b.longValue());
  }

  private static @Nullable Object performSubscripting(
    InfixOperatorToken operatorToken,
    @Nullable Object source,
    @Nullable Object key,
    boolean isKeyIdentifierName,
    InterpretationEnvironment environment
  ) {
    if (source == null)
      return null;

    if (source instanceof Map<?, ?>) {
      if (isKeyIdentifierName) {
        for (String line : ErrorScreen.make(operatorToken.raw, "Could not locate field \"" + key + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      return ((Map<?, ?>) source).get(key);
    }

    if (source instanceof List<?>) {
      if (isKeyIdentifierName) {
        for (String line : ErrorScreen.make(operatorToken.raw, "Could not locate field \"" + key + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      List<?> list = (List<?>) source;
      int index = (int) environment.getValueInterpreter().asLong(key);
      int listSize = list.size();

      if (index < 0) {
        if (Math.abs(index) > listSize)
          return null;

        index += listSize;
      }

      if (index < 0 || index >= listSize)
        return null;

      return list.get(index);
    }

    if (source.getClass().isArray()) {
      if (isKeyIdentifierName) {
        for (String line : ErrorScreen.make(operatorToken.raw, "Could not locate field \"" + key + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      int length = Array.getLength(source);
      int index = (int) environment.getValueInterpreter().asLong(key);

      if (index < 0 || index >= length)
        return null;

      return Array.get(source, index);
    }

    if (source instanceof String) {
      if (isKeyIdentifierName) {
        for (String line : ErrorScreen.make(operatorToken.raw, "Could not locate field \"" + key + "\""))
          LoggerProvider.log(Level.WARNING, line, false);

        return null;
      }

      String string = (String) source;
      int index = (int) environment.getValueInterpreter().asLong(key);

      if (index < 0 || index >= string.length())
        return null;

      return String.valueOf(string.charAt(index));
    }

    if (key == null)
      return null;

    String stringKey = environment.getValueInterpreter().asString(key);

    if (source instanceof DirectFieldAccess) {
      Object accessResult = ((DirectFieldAccess) source).accessField(stringKey);

      if (accessResult != DirectFieldAccess.UNKNOWN_FIELD_SENTINEL)
        return accessResult;
    }

    LoggerProvider.log(Level.WARNING, "Don't know how to access field " + stringKey + " of " + source.getClass());
    return null;
  }

  private static @Nullable String extractStringTerminal(@Nullable ExpressionNode node) {
    if (node instanceof TerminalNode) {
      TerminalNode terminal = (TerminalNode) node;

      if (terminal.token instanceof StringToken)
        return (String) terminal.token.getPlainValue();
    }

    return null;
  }

  private static @Nullable Long decideSubstringIndex(
    String input,
    InterpretationEnvironment environment,
    ExpressionNode node,
    boolean firstIndex
  ) {
    if (node == null)
      return null;

    String delimiter = extractStringTerminal(node);

    if (delimiter != null) {
      int index;

      if (firstIndex) {
        index = input.indexOf(delimiter);

        if (index < 0)
          index = 0;
      }

      else {
        index = input.lastIndexOf(delimiter);

        if (index < 0)
          index = input.length() - 1;
      }

      return (long) index;
    }

    return environment.getValueInterpreter().asLong(interpret(node, environment));
  }

  private static String performSubstring(String input, SubstringNode node, InterpretationEnvironment environment) {
    int len = input.length();

    if (len == 0)
      return "";

    @Nullable Long lowerBound = decideSubstringIndex(input, environment, node.lowerBound, true);
    @Nullable Long upperBound = decideSubstringIndex(input, environment, node.upperBound, false);

    long beginIndex = (lowerBound == null) ? 0 : (lowerBound < 0 ? len + lowerBound : lowerBound);
    long endIndex = (upperBound == null) ? len - 1 : (upperBound < 0 ? len + upperBound : upperBound);
    boolean wereNegative = beginIndex < 0 && endIndex < 0;

    beginIndex = clampZeroAndMax(beginIndex, len);
    endIndex = clampZeroAndMax(endIndex, len - 1);

    if (beginIndex > endIndex || (wereNegative && beginIndex == endIndex && beginIndex == 0))
      return "";

    return input.substring((int) beginIndex, (int) endIndex + 1);
  }

  private static long clampZeroAndMax(long value, long max) {
    return Math.max(0, Math.min(max, value));
  }

  private static boolean checkContains(@Nullable Object lhsValue, @Nullable Object rhsValue, ValueInterpreter valueInterpreter) {
    if (lhsValue == null && rhsValue == null)
      return true;

    if (lhsValue == null || rhsValue == null)
      return false;

    return valueInterpreter.asString(lhsValue).contains(valueInterpreter.asString(rhsValue));
  }

  private static boolean checkEquality(@Nullable Object lhsValue, @Nullable Object rhsValue, ValueInterpreter valueInterpreter) {
    if (lhsValue == null && rhsValue == null)
      return true;

    if (lhsValue == null || rhsValue == null)
      return false;

    if (lhsValue instanceof Number || rhsValue instanceof Number) {
      Number lhsNumber = valueInterpreter.asLongOrDouble(lhsValue);
      Number rhsNumber = valueInterpreter.asLongOrDouble(rhsValue);

      if (lhsNumber instanceof Double || rhsNumber instanceof Double)
        return Math.abs(lhsNumber.doubleValue() - rhsNumber.doubleValue()) < DOUBLE_EQUALITY_THRESHOLD;

      return lhsNumber.longValue() == rhsNumber.longValue();
    }

    return lhsValue.equals(rhsValue);
  }

  private static Number flipSignOf(Number input) {
    if (input instanceof Double)
      return input.doubleValue() * -1;

    if (input instanceof Long)
      return input.longValue() * -1;

    return flipSignOf(input.longValue());
  }

  private static String toTitleCase(String input) {
    LoggerProvider.log(Level.WARNING, "title() has not yet been implemented");
    return input;
  }

  private static String toggleCase(String input) {
    StringBuilder result = new StringBuilder(input.length());

    for (int index = 0; index < input.length(); ++index) {
      char currentChar = input.charAt(index);

      if (Character.isUpperCase(currentChar))
        currentChar = Character.toLowerCase(currentChar);

      if (Character.isLowerCase(currentChar))
        currentChar = Character.toUpperCase(currentChar);

      result.append(currentChar);
    }

    return result.toString();
  }

  private static String slugify(String input) {
    LoggerProvider.log(Level.WARNING, "slugify() has not yet been implemented");
    return input;
  }

  private static String asciify(String input) {
    LoggerProvider.log(Level.WARNING, "asciify() has not yet been implemented");
    return input;
  }
}
