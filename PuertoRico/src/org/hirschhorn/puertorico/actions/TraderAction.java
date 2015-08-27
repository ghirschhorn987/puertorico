package org.hirschhorn.puertorico.actions;

import org.hirschhorn.puertorico.constants.Good;

public class TraderAction implements Action {
  private Good chosenGood;
  

  public TraderAction(Good chosenGood){
    this.chosenGood = chosenGood;
  }
  
  public Good getChosenGood() {
    return chosenGood;
  }
  
  public String toString() {
    return "Traded " + chosenGood;
  }
}
