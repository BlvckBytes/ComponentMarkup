package at.blvckbytes.component_markup.interpreter;

public class LoopVariable {

  private final int size;

  private int index;
  private boolean is_first;
  private boolean is_last;
  private boolean is_even;
  private boolean is_odd;

  public LoopVariable(int size) {
    this.size = size;
  }

  public void setIndex(int index) {
    is_even = index % 2 == 0;
    is_odd = !is_even;
    is_first = index == 0;
    is_last = index == size - 1;
  }
}
