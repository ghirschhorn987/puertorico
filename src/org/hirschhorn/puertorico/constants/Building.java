package org.hirschhorn.puertorico.constants;

import java.util.HashSet;
import java.util.Set;

public class Building {
  private static Set<Building> allBuildings = createAllBuildings();
  
  private BuildingType buildingType;
  private BuildingProductionType buildingProductionType;
  private Good productionGood;
  private BuildingAllowedColonists allowedColonists;
  private int cost;
  private BuildingQuarryDiscount quaryDiscount;
  private BuildingVictoryPoints victoryPoints;
  private BuildingIslandSpacesFilled islandSpacesFilled; //amount of spaces the building takes up on a players board (1 or 2).
  private int countInGame;
  
  private Building(
      BuildingType buildingType,
      BuildingProductionType buildingProductionType,
      Good productionGood,
      BuildingAllowedColonists allowedColonists,
      int cost,
      BuildingQuarryDiscount quarryDiscount,
      BuildingVictoryPoints victoryPoints,
      BuildingIslandSpacesFilled islandSpacesFilled,
      int countInGame) {
    super();
    this.buildingType = buildingType;
    this.buildingProductionType = buildingProductionType;
    this.productionGood = productionGood;
    this.allowedColonists = allowedColonists;
    this.cost = cost;
    this.quaryDiscount = quarryDiscount;
    this.victoryPoints = victoryPoints;
    this.islandSpacesFilled = islandSpacesFilled;
    this.countInGame = countInGame;
  }
    
  private static Set<Building> createAllBuildings() {
    Set<Building> buildings = new HashSet<>();
    buildings.add(Building.createOneQuarryProductionBuilding(BuildingType.SmallIndigoPlant, Good.Indigo, BuildingAllowedColonists.ONE, 1, 4));
    buildings.add(Building.createOneQuarryProductionBuilding(BuildingType.SmallSugarMill, Good.Sugar, BuildingAllowedColonists.ONE, 2, 4));
    buildings.add(Building.createTwoQuarryProductionBuilding(BuildingType.LargeIndigoPlant, Good.Indigo, BuildingAllowedColonists.THREE, 3, 3));
    buildings.add(Building.createTwoQuarryProductionBuilding(BuildingType.LargeSugarMill, Good.Sugar, BuildingAllowedColonists.THREE, 3, 3));
    buildings.add(Building.createThreeQuarryProductionBuilding(BuildingType.TobbacoPlant, Good.Tobacco, BuildingAllowedColonists.THREE, 5, 3));
    buildings.add(Building.createThreeQuarryProductionBuilding(BuildingType.CoffeStorage, Good.Coffee, BuildingAllowedColonists.TWO, 6, 3));

    buildings.add(Building.createOneQuarryVioletBuilding(BuildingType.SmallMarket, 1));
    buildings.add(Building.createOneQuarryVioletBuilding(BuildingType.Hacienda, 2));
    buildings.add(Building.createOneQuarryVioletBuilding(BuildingType.ConstructionHut, 2));
    buildings.add(Building.createOneQuarryVioletBuilding(BuildingType.SmallWarehouse, 3));

    buildings.add(Building.createTwoQuarryVioletBuilding(BuildingType.Hospice, 4));
    buildings.add(Building.createTwoQuarryVioletBuilding(BuildingType.Office, 5));
    buildings.add(Building.createTwoQuarryVioletBuilding(BuildingType.LargeMarket, 5));
    buildings.add(Building.createTwoQuarryVioletBuilding(BuildingType.LargeWharehouse, 6));
    
    buildings.add(Building.createThreeQuarryVioletBuilding(BuildingType.Factory, 7));
    buildings.add(Building.createThreeQuarryVioletBuilding(BuildingType.University, 8));
    buildings.add(Building.createThreeQuarryVioletBuilding(BuildingType.Harbor, 8));
    buildings.add(Building.createThreeQuarryVioletBuilding(BuildingType.Wharf, 9));

    buildings.add(Building.createFourQuarryVioletBuilding(BuildingType.GuildHall, 10));
    buildings.add(Building.createFourQuarryVioletBuilding(BuildingType.Residence, 10));
    buildings.add(Building.createFourQuarryVioletBuilding(BuildingType.Fortress, 10));
    buildings.add(Building.createFourQuarryVioletBuilding(BuildingType.CustomsHouse, 10));
    buildings.add(Building.createFourQuarryVioletBuilding(BuildingType.CityHall, 10));
    
    return buildings;
  }
  
  public static Building createOneQuarryProductionBuilding(
      BuildingType buildingType,
      Good productionGood,
      BuildingAllowedColonists allowedColonists,
      int cost,
      int countInGame) {
    return new Building(buildingType, BuildingProductionType.Production, productionGood, allowedColonists, cost, BuildingQuarryDiscount.ONE, BuildingVictoryPoints.ONE, BuildingIslandSpacesFilled.ONE, countInGame);
  }
  
  public static Building createTwoQuarryProductionBuilding(
      BuildingType buildingType,
      Good productionGood,
      BuildingAllowedColonists allowedColonists,
      int cost,
      int countInGame) {
    return new Building(buildingType, BuildingProductionType.Production, productionGood, allowedColonists, cost, BuildingQuarryDiscount.TWO, BuildingVictoryPoints.TWO, BuildingIslandSpacesFilled.ONE, countInGame);
  }
  
  public static Building createThreeQuarryProductionBuilding(
      BuildingType buildingType,
      Good productionGood,
      BuildingAllowedColonists allowedColonists,
      int cost,
      int countInGame) {
    return new Building(buildingType, BuildingProductionType.Production, productionGood, allowedColonists, cost, BuildingQuarryDiscount.THREE, BuildingVictoryPoints.THREE, BuildingIslandSpacesFilled.ONE, countInGame);
  }
  
  public static Building createOneQuarryVioletBuilding(
      BuildingType buildingType,
      int cost) {
    return new Building(buildingType, BuildingProductionType.Violet, null, BuildingAllowedColonists.ONE, cost, BuildingQuarryDiscount.ONE, BuildingVictoryPoints.ONE, BuildingIslandSpacesFilled.ONE, 2);
  }
  
  public static Building createTwoQuarryVioletBuilding(
      BuildingType buildingType,
      int cost) {
    return new Building(buildingType, BuildingProductionType.Violet, null, BuildingAllowedColonists.ONE, cost, BuildingQuarryDiscount.TWO, BuildingVictoryPoints.TWO, BuildingIslandSpacesFilled.ONE, 2);
  }
  
  public static Building createThreeQuarryVioletBuilding(
      BuildingType buildingType,
      int cost) {
    return new Building(buildingType, BuildingProductionType.Violet, null, BuildingAllowedColonists.ONE, cost, BuildingQuarryDiscount.THREE, BuildingVictoryPoints.THREE, BuildingIslandSpacesFilled.ONE, 2);
  }
  
  public static Building createFourQuarryVioletBuilding(
      BuildingType buildingType,
      int cost) {
    return new Building(buildingType, BuildingProductionType.Violet, null, BuildingAllowedColonists.ONE, cost, BuildingQuarryDiscount.FOUR, BuildingVictoryPoints.FOUR, BuildingIslandSpacesFilled.TWO, 1);
  }

  public static Building getBuildingFromType(BuildingType buildingType) {
    for (Building building : allBuildings) {
      if (building.getBuildingType().equals(buildingType)) {
        return building;
      }
    }
    throw new IllegalArgumentException("Building not found: " + buildingType);
  }
  
  public BuildingType getBuildingType() {
    return buildingType;
  }

  public BuildingProductionType getBuildingProductionType() {
    return buildingProductionType;
  }

  public Good getProductionGood() {
    return productionGood;
  }

  public int getAllowedColonists() {
    return allowedColonists.getAllowedColonistsValue();
  }

  public int getCost() {
    return cost;
  }

  public int getQuarryDiscount() {
    return quaryDiscount.getQuarryDiscountValue();
  }

  public int getVictoryPoints() {
    return victoryPoints.getVictoryPointsValue();
  }

  public int getIslandSpacesFilled() {
    return islandSpacesFilled.getIslandSpacesFilledValue();
  }

  public int getCountInGame() {
    return countInGame;
  }
  
  public boolean isProduction() {
    return getBuildingProductionType().equals(BuildingProductionType.Production);
  }
  
  public boolean isViolet() {
    return getBuildingProductionType().equals(BuildingProductionType.Violet);
  }
  public boolean isLargeProductionBuilding() {
    if (allowedColonists.getAllowedColonistsValue() > 1 && isProduction()) {
      return true;
    }
    return false;
  }

  public boolean isSmallProductionBuilding() {
    if (isProduction() && allowedColonists.getAllowedColonistsValue() == 1){
      return true;
    }
    return false;
  }

}
