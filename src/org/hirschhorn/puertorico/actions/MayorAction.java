package org.hirschhorn.puertorico.actions;

import java.util.List;
import java.util.Map;

import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;

public class MayorAction implements Action {
  private List<Good> occupiedPlantations;
  private Map<BuildingType, Integer> buildingToOccupiedCount;
  private int occupiedQuarryCount;
  
  public MayorAction(
      List<Good> occupiedPlantations,
      Map<BuildingType, Integer> buildingToOccupiedCount,
      int occupiedQuarryCount) {
    super();
    this.occupiedPlantations = occupiedPlantations;
    this.buildingToOccupiedCount = buildingToOccupiedCount;
    this.occupiedQuarryCount = occupiedQuarryCount;
  }

  public List<Good> getOccupiedPlantations() {
    return occupiedPlantations;
  }

  public Map<BuildingType, Integer> getBuildingToOccupiedCount() {
    return buildingToOccupiedCount;
  }
  
  public int getOccupiedQuarryCount() {
    return occupiedQuarryCount;
  }
  
  public String toString() {
    int colonistsOnBuildings = 0;
    for (Integer count : getBuildingToOccupiedCount().values()) {
      colonistsOnBuildings += count;
    }
    return "Placed colonists on buildings: " + colonistsOnBuildings + ", on plantations: " + occupiedPlantations.size() + ", on quarries: " + occupiedQuarryCount; 
  }   
  
}
