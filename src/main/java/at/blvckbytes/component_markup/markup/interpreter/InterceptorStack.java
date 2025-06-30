package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

import java.util.*;

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

  public void add(InterpreterInterceptor interceptor) {
    interceptorStack.add(new InterceptorEntry(interceptor));
  }

  public boolean handleBeforeAndGetIfSkip(MarkupNode node) {
    boolean skip = false;

    for (int i = 0; i < interceptorStack.size(); ++i) {
      InterceptorEntry entry = interceptorStack.get(i);
      InterceptionResult result = entry.interceptor.interceptInterpretation(node, interpreter);

      if (result == InterceptionResult.DO_NOT_PROCESS) {
        while (interceptorStack.size() > i + 1)
          interceptorStack.pop().interceptor.onSkippedByParent(node, interpreter);

        skip = true;
        break;
      }

      entry.resultStack.push(result);
    }

    return skip;
  }

  public void handleAfter(MarkupNode node) {
    for (Iterator<InterceptorEntry> iterator = interceptorStack.iterator(); iterator.hasNext();) {
      InterceptorEntry entry = iterator.next();
      InterceptionResult result = entry.resultStack.pop();

      if (result == InterceptionResult.DO_PROCESS_AND_CALL_AFTER)
        entry.interceptor.afterInterpretation(node, interpreter);

      if (entry.resultStack.isEmpty())
        iterator.remove();
    }
  }
}
