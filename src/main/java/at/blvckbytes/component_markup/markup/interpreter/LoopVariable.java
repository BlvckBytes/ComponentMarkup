package at.blvckbytes.component_markup.markup.interpreter;

public class LoopVariable {

  private final int size;

  public int index;
  public boolean isFirst;
  public boolean isLast;
  public boolean isEven;
  public boolean isOdd;

  public LoopVariable(int size) {
    this.size = size;
  }

  public void setIndex(int index) {
    this.index = index;
    isEven = index % 2 == 0;
    isOdd = !isEven;
    isFirst = index == 0;
    isLast = index == size - 1;
  }
}
