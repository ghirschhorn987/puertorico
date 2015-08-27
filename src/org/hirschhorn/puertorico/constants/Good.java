package org.hirschhorn.puertorico.constants;

public enum Good {
  Corn(0),
  Indigo(1),
  Sugar(2),
  Tobacco(3),
  Coffee(4);
  
  private Good(int value) {
    this.value = value;
  }
  
  private int value;
  
  public int getValue() {
    return value;
  }
}
