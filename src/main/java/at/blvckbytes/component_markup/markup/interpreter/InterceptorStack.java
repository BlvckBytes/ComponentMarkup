package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.util.LoggerProvider;

import java.util.*;
import java.util.logging.Level;

public class InterceptorStack {

  private static class InterceptorEntry {
    InterpreterInterceptor interceptor;
    Stack<InterceptionResult> resultStack;

    InterceptorEntry(InterpreterInterceptor interceptor) {
      this.interceptor = interceptor;
      this.resultStack = new Stack<>();
    }
  }

  private final Interpreter interpreter;
  private final Stack<InterceptorEntry> interceptorStack;

  public InterceptorStack(Interpreter interpreter) {
    this.interpreter = interpreter;
    this.interceptorStack = new Stack<>();
  }

  private InterceptionResult callInterceptor(InterpreterInterceptor interceptor, MarkupNode node) {
    try {
      return interceptor.interceptInterpretation(node, interpreter);
    } catch (Throwable thrownError) {
      String className = interceptor.getClass().getName();
      LoggerProvider.log(Level.SEVERE, "An error occurred while trying to call " + className + "#interceptInterpretation", thrownError);
      return InterceptionResult.DO_PROCESS;
    }
  }

  public boolean handleBeforeAndGetIfSkip(MarkupNode node) {
    InterceptionResult result;

    // Call interceptors of parent-nodes first, as they take precedence
    for (InterceptorEntry entry : interceptorStack) {
      result = callInterceptor(entry.interceptor, node);

      if (result == InterceptionResult.DO_NOT_PROCESS)
        return true;

      entry.resultStack.push(result);
    }

    // The current node does not partake in intercepting and thus has no say on skipping
    if (!(node instanceof InterpreterInterceptor))
      return false;

    InterpreterInterceptor interceptor = (InterpreterInterceptor) node;

    result = callInterceptor(interceptor, node);

    // If the current node skips itself, do not add it to the stack, as there's no "after"
    if (result == InterceptionResult.DO_NOT_PROCESS) {

      // Notify parents with their prior result, such that they can manage their internal state-stack
      for (InterceptorEntry entry : interceptorStack) {
        InterceptionResult priorResult = entry.resultStack.pop();
        entry.interceptor.onSkippedByChild(node, interpreter, priorResult);
      }

      return true;
    }

    InterceptorEntry entry = new InterceptorEntry(interceptor);

    entry.resultStack.push(result);
    interceptorStack.add(entry);

    return false;
  }

  public void handleAfter(MarkupNode node) {
    for (Iterator<InterceptorEntry> iterator = interceptorStack.iterator(); iterator.hasNext();) {
      InterceptorEntry entry = iterator.next();
      InterceptionResult result = entry.resultStack.pop();

      if (result == InterceptionResult.DO_PROCESS_AND_CALL_AFTER) {
        try {
          entry.interceptor.afterInterpretation(node, interpreter);
        } catch (Throwable thrownError) {
          String className = entry.interceptor.getClass().getName();
          LoggerProvider.log(Level.SEVERE, "An error occurred while trying to call " + className + "#afterInterpretation", thrownError);
        }
      }

      if (entry.resultStack.isEmpty())
        iterator.remove();
    }
  }
}
