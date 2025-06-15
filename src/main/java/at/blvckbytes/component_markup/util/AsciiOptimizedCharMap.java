package at.blvckbytes.component_markup.util;

public class AsciiOptimizedCharMap<V> {

  private final Object[] asciiValues;
  private char[] otherKeys;
  private Object[] otherValues;
  private int otherSize;

  public AsciiOptimizedCharMap() {
    this.asciiValues = new Object[128];
    this.otherKeys = new char[8];
    this.otherValues = new Object[8];
    this.otherSize = 0;
  }

  public void put(char key, V value) {
    if (key < 128) {
      asciiValues[key] = value;
      return;
    }

    for (int i = 0; i < otherSize; i++) {
      if (otherKeys[i] == key) {
        otherValues[i] = value;
        return;
      }
    }

    if (otherSize == otherKeys.length)
      growOthers();

    otherKeys[otherSize] = key;
    otherValues[otherSize++] = value;
  }

  @SuppressWarnings("unchecked")
  public V get(char key) {
    if (key < 128)
      return (V) asciiValues[key];

    for (int i = 0; i < otherSize; i++) {
      if (otherKeys[i] == key)
        return (V) otherValues[i];
    }

    return null;
  }

  private void growOthers() {
    int newSize = otherKeys.length * 2;

    char[] newKeys = new char[newSize];
    System.arraycopy(otherKeys, 0, newKeys, 0, otherSize);

    Object[] newValues = new Object[newSize];
    System.arraycopy(otherValues, 0, newValues, 0, otherSize);

    this.otherKeys = newKeys;
    this.otherValues = newValues;
  }
}

