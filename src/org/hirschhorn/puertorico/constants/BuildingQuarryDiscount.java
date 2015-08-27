package org.hirschhorn.puertorico.constants;

public enum BuildingQuarryDiscount {
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4);
  
  private int quarryDiscountValue;
  
  BuildingQuarryDiscount(int quarryDiscountValue) {
    this.quarryDiscountValue = quarryDiscountValue;
  }
  
  public int getQuarryDiscountValue() {
    return quarryDiscountValue;
  }
}
