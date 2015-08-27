package org.hirschhorn.puertorico.playerstrategies.dad;

import org.hirschhorn.puertorico.constants.BuildingType;

public class BuildingCornQuarry {
  public static enum Type {
    Building,
    Corn,
    Quarry;
  }
  
  private Type type;
  private BuildingType buildingType;
  
  public BuildingCornQuarry(Type type, BuildingType buildingType) {
    this.type = type;
    this.buildingType = buildingType;
    
    if (type != Type.Building && buildingType != null) {
      throw new IllegalArgumentException("Building type specified, but type is not BUILDING. type=" + type + ", buildingType=" + buildingType);
    }
  }

  public Type getType() {
    return type;
  }

  public BuildingType getBuildingType() {
    return buildingType;
  }
}
