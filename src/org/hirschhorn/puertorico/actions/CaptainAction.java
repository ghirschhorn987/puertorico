package org.hirschhorn.puertorico.actions;

import org.hirschhorn.puertorico.constants.Good;

public class CaptainAction implements Action {
  private Good goodToShip;
  private int chosenShipSize;
  private boolean isUsingWharf;
  
  public CaptainAction(int chosenShipSize, Good goodToShip, boolean isUsingWharf) {
    super();
    this.goodToShip = goodToShip;
    this.chosenShipSize = chosenShipSize;
    this.isUsingWharf = isUsingWharf;
  }

  public Good getGoodToShip() {
    return goodToShip;
  }

  public int getChosenShipSize() {
    return chosenShipSize;
  }

  public boolean isUsingWharf() {
    return isUsingWharf;
  }
  
  public String toString() {
    if (isUsingWharf) {
      return "Shipped goods of type: " + goodToShip + " on wharf: " + isUsingWharf; 
    } else {
      return "Shipped goods of type: " + goodToShip + " on ship size: " + chosenShipSize; 
    }
  } 
}
