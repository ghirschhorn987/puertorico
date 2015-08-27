package org.hirschhorn.puertorico.actions;

import org.hirschhorn.puertorico.constants.BuildingType;

public class BuilderAction implements Action {
  private BuildingType buildingToBuy;

  public BuilderAction(BuildingType buildingToBuy) {
    super();
    this.buildingToBuy = buildingToBuy;
  }

  public BuildingType getBuildingToBuy() {
    return buildingToBuy;
  }
  
  public String toString() {
    return "Bought building: " + buildingToBuy;
  }
}
