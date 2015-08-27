package org.hirschhorn.puertorico.constants;

public enum BuildingVictoryPoints {
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4);
  
  private int victoryPointsValue;
  
  BuildingVictoryPoints(int victoryPointsValue) {
    this.victoryPointsValue = victoryPointsValue;
  }
  
  public int getVictoryPointsValue() {
    return victoryPointsValue;
  }
}
