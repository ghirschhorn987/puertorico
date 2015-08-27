package org.hirschhorn.puertorico.actions;

import org.hirschhorn.puertorico.constants.Good;

public class SettlerAction implements Action  {
  private Good chosenPlantation;
  private boolean useHaciendaBuildingToGetExtraPlantation;
  private boolean chooseQuarryInstead;

  public SettlerAction(
      Good chosenPlantation,
      boolean chooseQuarryInstead,
      boolean useHaciendaBuildingToGetExtraPlantation){
    this.chosenPlantation = chosenPlantation;
    this.chooseQuarryInstead = chooseQuarryInstead;
    this.useHaciendaBuildingToGetExtraPlantation = useHaciendaBuildingToGetExtraPlantation;
  }
  
  public Good getChosenPlantation(){
    return chosenPlantation;
  }

  public boolean isChooseQuarryInstead() {
    return chooseQuarryInstead;
  }

  public boolean isUseHaciendaBuildingToGetExtraPlantation() {
    return useHaciendaBuildingToGetExtraPlantation;
  }
 
  public String toString() {
     return "Chose " + (chooseQuarryInstead ? "quarry" : chosenPlantation);
  }
}