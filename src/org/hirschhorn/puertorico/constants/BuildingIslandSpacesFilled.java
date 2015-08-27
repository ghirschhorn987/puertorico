package org.hirschhorn.puertorico.constants;

public enum BuildingIslandSpacesFilled {
  ONE(1),
  TWO(2);
  
  private int islandSpacesFilledValue;
  
  BuildingIslandSpacesFilled(int islandSpacesFilledValue) {
    this.islandSpacesFilledValue = islandSpacesFilledValue;
  }
  
  public int getIslandSpacesFilledValue() {
    return islandSpacesFilledValue;
  }
}
