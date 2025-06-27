package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.ast.*;
import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import at.blvckbytes.component_markup.expression.tokenizer.PrefixOperator;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpressionInterpreter {

  private static final Map<Class<?>, PublicFieldMap> publicFieldsByClass = new HashMap<>();

  private final Logger logger;

  public ExpressionInterpreter(Logger logger) {
    this.logger = logger;
  }

  public @Nullable Object interpret(ExpressionNode expression, InterpretationEnvironment environment) {
    if (expression instanceof TerminalNode)
      return ((TerminalNode) expression).getValue(environment);

    ValueInterpreter valueInterpreter = environment.getValueInterpreter();

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

        default:
          logger.log(Level.WARNING, "Unimplemented prefix-operator: " + prefixOperator);
          return null;
      }
    }

    if (expression instanceof InfixOperationNode) {
      InfixOperationNode node = (InfixOperationNode) expression;
      Object lhsValue = interpret(node.lhs, environment);
      Object rhsValue = interpret(node.rhs, environment);

      InfixOperator infixOperator = node.operatorToken.operator;
      ArithmeticOperator arithmeticOperator = ArithmeticOperator.fromInfix(infixOperator);

      if (arithmeticOperator != null) {
        Number lhs = valueInterpreter.asLongOrDouble(lhsValue);
        Number rhs = valueInterpreter.asLongOrDouble(rhsValue);

        if (lhs instanceof Long && rhs instanceof Long)
          return arithmeticOperator.longOperation(lhs.longValue(), rhs.longValue());

        return arithmeticOperator.doubleOperation(lhs.doubleValue(), rhs.doubleValue());
      }

      switch (infixOperator) {
        case CONCATENATION:
          return valueInterpreter.asString(lhsValue) + valueInterpreter.asString(rhsValue);

        case GREATER_THAN_OR_EQUAL:
        case LESS_THAN_OR_EQUAL:
          if (checkEquality(lhsValue, rhsValue))
            return true;

        case GREATER_THAN:
        case LESS_THAN: {
          Number lhs = valueInterpreter.asLongOrDouble(lhsValue);
          Number rhs = valueInterpreter.asLongOrDouble(rhsValue);

          int comparisonResult;

          if (lhs instanceof Long && rhs instanceof Long)
            comparisonResult = ((Long) lhs).compareTo((Long) rhs);
          else
            comparisonResult = Double.compare(lhs.doubleValue(), rhs.doubleValue());

          return (
            infixOperator == InfixOperator.GREATER_THAN || infixOperator == InfixOperator.GREATER_THAN_OR_EQUAL
              ? comparisonResult > 0
              : comparisonResult < 0
          );
        }

        case EQUAL_TO:
          return checkEquality(lhsValue, rhsValue);

        case NOT_EQUAL_TO:
          return !checkEquality(lhsValue, rhsValue);

        case RANGE: {
          long lowerBound = valueInterpreter.asLong(lhsValue);
          long upperBound = valueInterpreter.asLong(rhsValue);
          List<Long> result = new ArrayList<>();

          if (upperBound > lowerBound) {
            for (long index = lowerBound; index <= upperBound; ++index)
              result.add(index);
          }

          return result;
        }

        case MEMBER:
          return performSubscripting(lhsValue, rhsValue, environment);

        case CONJUNCTION:
          return valueInterpreter.asBoolean(lhsValue) && valueInterpreter.asBoolean(rhsValue);

        case DISJUNCTION:
          return valueInterpreter.asBoolean(lhsValue) || valueInterpreter.asBoolean(rhsValue);

        case NULL_COALESCE:
          return lhsValue != null ? lhsValue : rhsValue;

        default:
          logger.log(Level.WARNING, "Unimplemented infix-operator: " + infixOperator);
          return null;
      }
    }

    if (expression instanceof SubscriptingNode) {
      SubscriptingNode node = (SubscriptingNode) expression;
      Object lhs = interpret(node.lhs, environment);
      Object rhs = interpret(node.rhs, environment);
      return performSubscripting(lhs, rhs, environment);
    }

    if (expression instanceof IfElseNode) {
      IfElseNode node = (IfElseNode) expression;

      if (valueInterpreter.asBoolean(interpret(node.condition, environment)))
        return interpret(node.branchTrue, environment);

      return interpret(node.branchFalse, environment);
    }

    if (expression instanceof SubstringNode) {
      SubstringNode node = (SubstringNode) expression;

      String stringValue = valueInterpreter.asString(interpret(node.operand, environment));

      Object lowerBound = interpret(node.lowerBound, environment);
      Object upperBound = interpret(node.upperBound, environment);

      return performSubstring(
        stringValue,
        lowerBound == null ? null : valueInterpreter.asLong(lowerBound),
        upperBound == null ? null : valueInterpreter.asLong(upperBound)
      );
    }

    if (expression instanceof ArrayNode) {
      ArrayNode node = (ArrayNode) expression;

      List<Object> result = new ArrayList<>();

      for (ExpressionNode item : node.items)
        result.add(interpret(item, environment));

      return result;
    }

    logger.log(Level.WARNING, "Unimplemented node: " + expression.getClass());
    return null;
  }

  private @Nullable Object performSubscripting(@Nullable Object source, @Nullable Object key, InterpretationEnvironment environment) {
    if (source == null)
      return null;

    if (source instanceof Map<?, ?>)
      return ((Map<?, ?>) source).get(key);


    if (source instanceof List<?>) {
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
      int length = Array.getLength(source);
      int index = (int) environment.getValueInterpreter().asLong(key);

      if (index < 0 || index >= length)
        return null;

      return Array.get(source, index);
    }

    if (source instanceof String) {
      String string = (String) source;
      int index = (int) environment.getValueInterpreter().asLong(key);

      if (index < 0 || index >= string.length())
        return null;

      return String.valueOf(string.charAt(index));
    }

    if (key == null)
      return null;

    String stringKey = environment.getValueInterpreter().asString(key);
    Field field = publicFieldsByClass.computeIfAbsent(source.getClass(), PublicFieldMap::new).locateField(stringKey);

    if (field == null)
      return null;

    try {
      return field.get(source);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not access field " + field, e);
      return null;
    }
  }

  private String performSubstring(String input, @Nullable Long lowerIndex, @Nullable Long upperIndex) {
    StringBuilder result = new StringBuilder();
    long stringLength = input.length();

    if (lowerIndex != null) {
      if (lowerIndex < 0) {
        lowerIndex = Math.abs(lowerIndex);

        if (lowerIndex >= stringLength)
          lowerIndex = stringLength - 1;

        result.append(input, (int) (stringLength - 1 - lowerIndex), (int) stringLength);
        result.reverse();
      }
    }

    if (upperIndex != null) {
      if (upperIndex < 0) {
        upperIndex = Math.abs(upperIndex);

        if (upperIndex >= stringLength)
          upperIndex = stringLength - 1;

        result.append(input, 0, upperIndex.intValue());
      }
    }

    if (lowerIndex != null && upperIndex != null) {
      if (lowerIndex >= 0 && upperIndex >= 0 && lowerIndex.compareTo(upperIndex) <= 0) {
        if (upperIndex >= stringLength)
          upperIndex = stringLength - 1;

        result.append(input, lowerIndex.intValue(), upperIndex.intValue() + 1);
      }
    }

    return result.toString();
  }

  private boolean checkEquality(@Nullable Object lhsValue, @Nullable Object rhsValue) {
    if (lhsValue == null && rhsValue == null)
      return true;

    if (lhsValue == null || rhsValue == null)
      return false;

    return lhsValue.equals(rhsValue);
  }

  private Number flipSignOf(Number input) {
    if (input instanceof Double)
      return input.doubleValue() * -1;

    if (input instanceof Long)
      return input.longValue() * -1;

    return flipSignOf(input.longValue());
  }
}
