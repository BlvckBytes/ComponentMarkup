/*
 * Copyright (c) 2025, BlvckBytes
 * SPDX-License-Identifier: MIT
 */

package at.blvckbytes.component_markup.expression.interpreter;

import at.blvckbytes.component_markup.expression.tokenizer.InfixOperator;
import org.jetbrains.annotations.Nullable;

public enum ArithmeticOperator {

  ADDITION {
    @Override
    public double doubleOperation(double a, double b) {
      return a + b;
    }

    @Override
    public long longOperation(long a, long b) {
      return a + b;
    }
  },

  SUBTRACTION {
    @Override
    public double doubleOperation(double a, double b) {
      return a - b;
    }

    @Override
    public long longOperation(long a, long b) {
      return a - b;
    }
  },

  MULTIPLICATION {
    @Override
    public double doubleOperation(double a, double b) {
      return a * b;
    }

    @Override
    public long longOperation(long a, long b) {
      return a * b;
    }
  },

  DIVISION {
    @Override
    public double doubleOperation(double a, double b) {
      return b == 0 ? a : a / b;
    }

    @Override
    public long longOperation(long a, long b) {
      return b == 0 ? a : a / b;
    }
  },

  MODULO {
    @Override
    public double doubleOperation(double a, double b) {
      return b == 0 ? a : a % b;
    }

    @Override
    public long longOperation(long a, long b) {
      return b == 0 ? a : a % b;
    }
  },

  EXPONENTIATION {
    @Override
    public double doubleOperation(double a, double b) {
      return Math.pow(a, b);
    }

    @Override
    public long longOperation(long a, long b) {
      return (long) Math.pow((double) a, (double) b);
    }
  },
  ;

  public abstract double doubleOperation(double a, double b);
  public abstract long longOperation(long a, long b);

  public static @Nullable ArithmeticOperator fromInfix(InfixOperator operator) {
    switch(operator) {
      case ADDITION:
        return ADDITION;
      case SUBTRACTION:
        return SUBTRACTION;
      case MULTIPLICATION:
        return MULTIPLICATION;
      case DIVISION:
        return DIVISION;
      case MODULO:
        return MODULO;
      case EXPONENTIATION:
        return EXPONENTIATION;
    }
    return null;
  }
}
