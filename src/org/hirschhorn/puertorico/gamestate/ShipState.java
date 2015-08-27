package org.hirschhorn.puertorico.gamestate;

import org.hirschhorn.puertorico.constants.Good;

public class ShipState {
  private int capacity;
  private int filledCount;
  private Good goodType;
  
  public ShipState(ShipState shipState) {
    capacity = shipState.capacity;
    filledCount = shipState.filledCount;
    goodType = shipState.goodType;
  }
  
  public ShipState(int capacity) {
    this.goodType = null;
    this.capacity = capacity;
    this.filledCount = 0;
  }
  
  public int getCapacity() {
    return capacity;
  }
  
  public int getFilledCount() {
    return filledCount;
  }
  
  public Good getGoodType() {
    return goodType;
  }

  public void setFilledCount(int filledCount) {
    this.filledCount = filledCount;
  }
  
  public void setGoodType(Good goodType) {
    this.goodType = goodType;
  }
  
  public boolean isEmpty() {
    return filledCount == 0;
  }
  
  public boolean isFull() {
    return filledCount == capacity;
  }
  
}
