package at.blvckbytes.component_markup.markup.interpreter;

public class LoopVariable {

  public int index;
  public final int length;
  public boolean isFirst;
  public boolean isLast;
  public boolean isEven;
  public boolean isOdd;

  public LoopVariable(int length) {
    this.length = length;
  }

  public void setIndex(int index) {
    this.index = index;
    isEven = index % 2 == 0;
    isOdd = !isEven;
    isFirst = index == 0;
    isLast = index == length - 1;
  }
}
