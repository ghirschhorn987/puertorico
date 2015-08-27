package org.hirschhorn.puertorico.gamestate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hirschhorn.puertorico.Rules;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

public class PlayerState {
  private int victoryPointChips;
  private int victoryPointBonus;
  private int money;
  private List<Good> goods;
  
  private Map<BuildingType, Integer> buildingToOccupiedCount;

  private Map<Good, Integer> occupiedPlantationsToCount;
  private Map<Good, Integer> unoccupiedPlantationsToCount;
  private int occupiedQuarryCount;
  private int unoccupiedQuarryCount;
  
  private int colonistsOnSanJuan;

  private boolean hasUsedWharf;

  public PlayerState(PlayerState playerState) {
    victoryPointChips = playerState.victoryPointChips;
    victoryPointBonus = playerState.victoryPointBonus;
    money = playerState.money;
    goods = new ArrayList<>(playerState.goods);
    buildingToOccupiedCount = new HashMap<>(playerState.buildingToOccupiedCount);
    occupiedPlantationsToCount = new HashMap<>(playerState.occupiedPlantationsToCount);
    unoccupiedPlantationsToCount = new HashMap<>(playerState.unoccupiedPlantationsToCount);
    occupiedQuarryCount = playerState.occupiedQuarryCount;
    unoccupiedQuarryCount = playerState.unoccupiedQuarryCount;
    colonistsOnSanJuan = playerState.colonistsOnSanJuan;
    hasUsedWharf = playerState.hasUsedWharf;
  }
  
  public PlayerState(Rules rules, int playerCount, int playerPosition) {
    victoryPointChips = 0;
    victoryPointBonus = 0;
    money = rules.getInitialPlayerMoney(playerCount);
    goods = new ArrayList<>();
    buildingToOccupiedCount = new HashMap<>();
    
    occupiedPlantationsToCount = rules.createOccupiedPlantationsToCount();
    unoccupiedPlantationsToCount = rules.createUnoccupiedPlantationsToCount();
    addUnoccupiedPlantation(rules.getInitialPlayerPlantations(playerCount, playerPosition));
    occupiedQuarryCount = 0;
    unoccupiedQuarryCount = 0;
    colonistsOnSanJuan = 0;
    
    hasUsedWharf = false;
  }
    
  public List<Good> getGoods() {
    return new ArrayList<>(goods);
  }

  public void setColonistsOnSanJuan(int colonists) {
    this.colonistsOnSanJuan = colonists;
  }
  
  public int getVictoryPointChips() {
    return victoryPointChips;
  }

  public int getVictoryPointBonus() {
    return victoryPointBonus;
  }

  public int getVictoryPoints() {
    return getVictoryPointChips() + getVictoryPointBonus();
  }

  public int getMoney() {
    return money;
  }

  public Set<BuildingType> getOccupiedBuildings() {
    Set<BuildingType> occupiedBuildings = new HashSet<>();
    for (Map.Entry<BuildingType, Integer> entry : buildingToOccupiedCount.entrySet()) {
      BuildingType building = entry.getKey();
      int count = entry.getValue();
      if (count > 0) {
        occupiedBuildings.add(building);
      }
    }
    return occupiedBuildings;
  }

  public List<BuildingType> getUnoccupiedBuildings() {
    List<BuildingType> unoccupiedBuildings = new ArrayList<>();
    for (Map.Entry<BuildingType, Integer> entry : buildingToOccupiedCount.entrySet()) {
      BuildingType building = entry.getKey();
      int count = entry.getValue();
      if (count == 0) {
        unoccupiedBuildings.add(building);
      }
    }
    return unoccupiedBuildings;
  }
  
  public List<BuildingType> getAllLargeBuildings() {
    List<BuildingType> buildings = getAllBuildings();
    Iterator<BuildingType> iter = buildings.iterator();
    while (iter.hasNext()) {
      Building building = Building.getBuildingFromType(iter.next());
      if (building.getIslandSpacesFilled() != 2) {
        iter.remove();
      }
    }
    return buildings;
  }
  
  public List<BuildingType> getAllLargeBuildingsOccupied() {
    List<BuildingType> buildings = getAllBuildings();
    Iterator<BuildingType> iter = buildings.iterator();
    while (iter.hasNext()) {
      Building building = Building.getBuildingFromType(iter.next());
      if (building.getIslandSpacesFilled() != 2 || !getOccupiedBuildings().contains(building.getBuildingType())) {
        iter.remove();
      }
    }
    return buildings;
  }
  
  public List<Good> getOccupiedPlantations() {
    List<Good> occupiedPlantations = new ArrayList<>();
    for (Entry<Good, Integer> entry : occupiedPlantationsToCount.entrySet()) {
      Good plantation = entry.getKey();
      Integer count = entry.getValue();
      occupiedPlantations.addAll(Collections.nCopies(count, plantation));
    }
    return occupiedPlantations;
  }

  public List<Good> getUnoccupiedPlantations() {
    List<Good> unoccupiedPlantations = new ArrayList<>();
    for (Entry<Good, Integer> entry : unoccupiedPlantationsToCount.entrySet()) {
      Good plantation = entry.getKey();
      Integer count = entry.getValue();
      unoccupiedPlantations.addAll(Collections.nCopies(count, plantation));
    }
    return unoccupiedPlantations;
  }

  public int getOccupiedQuarryCount() {
    return occupiedQuarryCount;
  }

  public int getUnoccupiedQuarryCount() {
    return unoccupiedQuarryCount;
  }

  public int getColonistsOnSanJuan() {
    return colonistsOnSanJuan;
  }

  public boolean hasUsedWharf() {
    return hasUsedWharf;
  }

  public void setHasUsedWharf(boolean hasUsedWharf) {
    this.hasUsedWharf = hasUsedWharf;
  }
  
  public int getOccupiedCountOfBuilding(BuildingType building) {
    return buildingToOccupiedCount.get(building);
  }

  public int getGoodCount(Good goodToCount) {
    int count = 0;
    for (Good good : goods) {
      if (good.equals(goodToCount)) {
        count++;
      }
    }
    return count;
  }

  public void removeGoods(Good good, int count) {
    for (int x=0; x<count; x++) {
      goods.remove(good);
    }
  }

  public void addVictoryPointChips(int count) {
    victoryPointChips = victoryPointChips + count;
  }
  
  public void addVictoryPointBonus(int count) {
    victoryPointBonus = victoryPointBonus + count;
  }

  public void addBuilding(BuildingType building) {
    buildingToOccupiedCount.put(building, 0);
    if (doesPlayerHaveOccupiedBuilding(BuildingType.University)){
      buildingToOccupiedCount.put(building, 1);
    }
  }

  public void removeMoney(int count) {
    if (count > money) {
      throw new IllegalArgumentException("Removing " + count + " money but player only has " +  money + " money.");
    }
    money = money - count;
  }

  public void takeAwayColonist() {
    colonistsOnSanJuan = colonistsOnSanJuan - 1;
  }

  public void occupyPlantations(List<Good> plantations) {
    for (Good plantation : plantations){
      addPlantationToOccupiedList(plantation);
      removePlantationFromUnoccupiedMap(plantation);
    }
  }

  public void removePlantationFromUnoccupiedMap(Good plantation) {
    int newValue = unoccupiedPlantationsToCount.get(plantation) - 1;
    unoccupiedPlantationsToCount.put(plantation, newValue);
  }


  public void occupyBuildings(Map<BuildingType, Integer> map) {
    buildingToOccupiedCount.putAll(map);
  }

  public void occupyQuarries(int quarryCount) {
    occupiedQuarryCount = quarryCount;
  }

  public void moveAllColonistsToSanJuan() {
    colonistsOnSanJuan = getTotalColonistCount();
    occupiedQuarryCount = 0;
    for (Entry <Good, Integer> entry: occupiedPlantationsToCount.entrySet()){
      Good plantation = entry.getKey();
      Integer count = entry.getValue();
      Integer newCount = count + unoccupiedPlantationsToCount.get(plantation);
      unoccupiedPlantationsToCount.put(plantation, newCount);
    }
    for (Good plantation : occupiedPlantationsToCount.keySet()){
      occupiedPlantationsToCount.put(plantation, 0);
    }
    for (BuildingType building : buildingToOccupiedCount.keySet()) {
      buildingToOccupiedCount.put(building, 0);
    }
  }

  private int getTotalColonistCount() {
    int total = colonistsOnSanJuan + occupiedQuarryCount + getOccupiedPlantations().size();
    for (Integer value : buildingToOccupiedCount.values()) {
      total = total + value;
    }    
    return total;
  }

  public void addMoney(int value) {
    money = money + value;
  }

  public void addQuarry() {
    unoccupiedQuarryCount++;
  }

  public void addUnoccupiedPlantation(Good plantation) {
      int newValue = unoccupiedPlantationsToCount.get(plantation) + 1; 
      unoccupiedPlantationsToCount.put(plantation, newValue);
  }
  
  public boolean doesPlayerHaveOccupiedBuilding(BuildingType buildingType) {
    for (BuildingType buildingTypeList : getOccupiedBuildings()){
      if (buildingTypeList.equals(buildingType)) {
        return true;
      }
    }
    return false;
  }

  public void addPlantationToOccupiedList(Good plantation) {
    int newValue = occupiedPlantationsToCount.get(plantation) + 1; 
    occupiedPlantationsToCount.put(plantation, newValue);
  }


  public List<BuildingType> getAllBuildings() {
    List<BuildingType> allBuildings = new ArrayList<>(getOccupiedBuildings());
    allBuildings.addAll(getUnoccupiedBuildings());
    return allBuildings;
  }
    
  public int getAllPlantationsCount() {
    return getAllPlantations().size();
  }

  public List<Good> getAllPlantations() {
    List<Good> allPlantations = new ArrayList<>();
    for (Entry<Good, Integer> entry : occupiedPlantationsToCount.entrySet()) {
      Good plantation = entry.getKey();
      Integer count = entry.getValue();
      allPlantations.addAll(Collections.nCopies(count, plantation));
    }
    for (Entry<Good, Integer> entry : unoccupiedPlantationsToCount.entrySet()) {
      Good plantation = entry.getKey();
      Integer count = entry.getValue();
      allPlantations.addAll(Collections.nCopies(count, plantation));
    }
    return allPlantations;
  }


  public int getAllColonists() {
    int colonists = colonistsOnSanJuan;
    for (Integer count : buildingToOccupiedCount.values()) {
      colonists += count;
    }
    return colonists;
  }


  public void removeColonistsFromSanJuan(int count) {
    colonistsOnSanJuan = colonistsOnSanJuan - count;
  }


  public boolean isThereSpaceOnIsland() {
    if (getUnoccupiedPlantations().size() + getOccupiedPlantations().size() + unoccupiedQuarryCount + occupiedQuarryCount >= 12){
      return false;
    } else {
    return true;
    }
  }

  public void addGoods(Good good, int count) {
    goods.addAll(Collections.nCopies(count, good));
  }

  public void removeAllGoods() {
    goods.clear();
  }

  public boolean hasGoods() {
    return !goods.isEmpty();
  }

}
