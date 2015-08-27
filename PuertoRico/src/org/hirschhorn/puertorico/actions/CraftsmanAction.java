package org.hirschhorn.puertorico.actions;

import org.hirschhorn.puertorico.constants.Good;

public class CraftsmanAction implements Action  {
  private Good chosenGoodForPrivilege;
  
  public Good getChosenGoodForPrivilge(){
    return chosenGoodForPrivilege;
  }
  public CraftsmanAction(Good chosenGoodForPrivilege){
    this.chosenGoodForPrivilege = chosenGoodForPrivilege;
  }
  
  public String toString() {
    return "Chose extra good for Craftsman: " + chosenGoodForPrivilege; 
  }
}
