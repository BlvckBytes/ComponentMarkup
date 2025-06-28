package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;

import java.util.*;

public class InterceptorStack {

  private static class InterceptorEntry {
    InterpreterInterceptor interceptor;
    Stack<EnumSet<InterceptionFlag>> flagStack;

    InterceptorEntry(InterpreterInterceptor interceptor) {
      this.interceptor = interceptor;
      this.flagStack = new Stack<>();
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
    List<EnumSet<InterceptionFlag>> flagsList = new ArrayList<>();

    boolean skip = false;

    for (int i = 0; i < interceptorStack.size(); ++i) {
      InterceptorEntry entry = interceptorStack.get(i);
      EnumSet<InterceptionFlag> flags = entry.interceptor.interceptInterpretation(node, interpreter);

      if (flags.contains(InterceptionFlag.SKIP_PROCESSING)) {
        while (interceptorStack.size() > i + 1)
          interceptorStack.pop().interceptor.onSkippedByOther(node, interpreter);

        skip = true;
        break;
      }

      flagsList.add(flags);
    }

    for (int i = 0; i < interceptorStack.size(); ++i)
      interceptorStack.get(i).flagStack.push(flagsList.get(i));

    return skip;
  }

  public void handleAfter(MarkupNode node) {
    for (Iterator<InterceptorEntry> iterator = interceptorStack.iterator(); iterator.hasNext();) {
      InterceptorEntry entry = iterator.next();
      EnumSet<InterceptionFlag> flags = entry.flagStack.pop();

      if (flags.contains(InterceptionFlag.CALL_AFTER))
        entry.interceptor.afterInterpretation(node, interpreter);

      if (entry.flagStack.isEmpty())
        iterator.remove();
    }
  }
}
