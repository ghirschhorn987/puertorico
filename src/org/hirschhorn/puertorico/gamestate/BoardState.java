package org.hirschhorn.puertorico.gamestate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hirschhorn.puertorico.Player;
import org.hirschhorn.puertorico.Rules;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;

public class BoardState {
  private static Logger logger = Logger.getLogger(BoardState.class.getName());
  
  private Map<BuildingType, Integer> buildingsToAvailableCount;
  
  private List<Role> currentRoundRolesPlayed;
  private Map<Role, Integer> roleToAccumulatedMoney;

  private int supplyVictoryPoints;
  private int supplyColonists;
  private int shipColonists;
  private Map<Good, Integer> goodtoAvailableCount;
  
  private List<ShipState> shipStates;
  private TradingHouse tradingHouse;
  private PlantationsState plantationsState;

  public BoardState(BoardState boardState) {
    buildingsToAvailableCount = new HashMap<>(boardState.buildingsToAvailableCount);
    currentRoundRolesPlayed = new ArrayList<>(boardState.currentRoundRolesPlayed);
    roleToAccumulatedMoney = new HashMap<>(boardState.roleToAccumulatedMoney);
    supplyVictoryPoints = boardState.supplyVictoryPoints;
    supplyColonists = boardState.supplyColonists;
    shipColonists = boardState.shipColonists;
    goodtoAvailableCount = new HashMap<>(boardState.goodtoAvailableCount);
    shipStates = new ArrayList<>();
    for (ShipState shipState : boardState.shipStates) {
      shipStates.add(new ShipState(shipState));
    }
    tradingHouse = new TradingHouse(boardState.tradingHouse);
    plantationsState = new PlantationsState(boardState.plantationsState);
  }
  
  public BoardState(Rules rules, int playerCount, List<Role> availableRoles) {
    buildingsToAvailableCount = rules.getBuildingsToCount();
    currentRoundRolesPlayed = new ArrayList<>();
    roleToAccumulatedMoney = new HashMap<>();
    for (Role role : availableRoles) {
      roleToAccumulatedMoney.put(role, 0);
    }
    
    supplyVictoryPoints = rules.getSupplyVictoryPoints(playerCount);
    supplyColonists = rules.getSupplyColonists(playerCount);
    shipColonists = rules.getShipColonists(playerCount);
    goodtoAvailableCount = rules.getGoodToAvailableCount();
    
    initializeShipStates(playerCount);
    tradingHouse = new TradingHouse();
    initializePlantationsState(playerCount, rules.getAllPlantations());
  }
  
  private void initializeShipStates(int playerCount) {
    shipStates = new ArrayList<>();
    switch(playerCount) {
      case 3:
        shipStates.add(new ShipState(4));
        shipStates.add(new ShipState(5));
        shipStates.add(new ShipState(6));
        break;
      case 4:
        shipStates.add(new ShipState(5));
        shipStates.add(new ShipState(6));
        shipStates.add(new ShipState(7));
        break;
      case 5:
        shipStates.add(new ShipState(6));
        shipStates.add(new ShipState(7));
        shipStates.add(new ShipState(8));
        break;
      default:
        throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
  }

  private void initializePlantationsState(int playerCount, List<Good> allPlantations) {
    plantationsState = new PlantationsState(playerCount, allPlantations);
    logger.fine("allPlantations: " + allPlantations.size() + " " + allPlantations);
  }

  public int getSupplyColonists() {
    return supplyColonists;
  }

  public int getSupplyVictoryPoints() {
    return supplyVictoryPoints;
  }
  
  public List<Role> getCurrentRoundRolesPlayed() {
    return new ArrayList<>(currentRoundRolesPlayed);
  }

  public Map<Role, Integer> getRoleToAccumulatedMoney() {
    return roleToAccumulatedMoney;
  }

  public int getShipColonists() {
    return shipColonists;
  }

  public int getAvailableCount(Good good) {
    return goodtoAvailableCount.get(good);
  }

  public void addOneMoneyToRole(Role role) {
    int money = roleToAccumulatedMoney.get(role);
    roleToAccumulatedMoney.put(role, money + 1);
  }
  
  public void takeAllMoneyFromRole (Role role) {
    roleToAccumulatedMoney.put(role, 0);
  }
  
  public boolean hasMoneyOnRole(Role role) {
    if (getRoleToAccumulatedMoney().get(role) == 0) {
      return false;
    } else {
      return true;
    }
  }
  
  public int getMoneyOnRole(Role role){
    return getRoleToAccumulatedMoney().get(role);
  }
  
  public void setSupplyColonists(int supplyColonists) {
    this.supplyColonists = supplyColonists;
  }

  public void setShipColonists(int shipColonists) {
    this.shipColonists = shipColonists;
  }

  public int takeGoodsFromSupply(Good good, int requested) {
    if (requested <= goodtoAvailableCount.get(good)){
      int newGoodCount = goodtoAvailableCount.get(good) - requested;
      goodtoAvailableCount.put(good, newGoodCount);
      return requested;
    } else if (goodtoAvailableCount.get(good) == 0) {
      return 0;
    } else if (requested > goodtoAvailableCount.get(good)){
      goodtoAvailableCount.put(good, 0);
      return goodtoAvailableCount.get(good);
    } else {
      throw new IllegalStateException("Should not get here.");
    }
  }

  
  public void addGoodsToSupply(Good good, int numberOfGoods) {
    int newGoodCount = goodtoAvailableCount.get(good) + numberOfGoods;
    goodtoAvailableCount.put(good, newGoodCount);
  }
  
  public void addGoodsToShip(int chosenShipSize, Good goodToShip, int amountOfGoods) {
    ShipState shipState = getShipState(chosenShipSize);
    if (shipState.getGoodType() == null) {
      shipState.setGoodType(goodToShip);
    } else {
      if (!shipState.getGoodType().equals(goodToShip)) {
        throw new IllegalArgumentException("Trying to ship " + goodToShip + " on a ship of type " + shipState.getGoodType());
      }
    }
    int newFilledCount = getShipState(chosenShipSize).getFilledCount() + amountOfGoods;
    getShipState(chosenShipSize).setFilledCount(newFilledCount);
  }

  // TODO: change this to private and have callers ask this class for requested ship state info. 
  //   Prevents anyone from accidentally changing ship state.
  public ShipState getShipState(int chosenShipSize) {
    for (ShipState shipState : shipStates) {
      if (shipState.getCapacity() == chosenShipSize) {
        return shipState;
      }
    }
    throw new IllegalArgumentException("Ship not found of size: " + chosenShipSize);
  }

  // TODO: change this to private and have callers ask this class for requested ship state info
  //   Prevents anyone from accidentally changing ship state.
  public ShipState getShipStateForGood(Good good) {
    for (ShipState shipState : shipStates) {
      if (shipState.getGoodType() != null && shipState.getGoodType().equals(good)) {
        return shipState;
      }
    }
    return null;
  }
  
  // TODO: change this to private and have callers ask this class for requested ship state info
  //   Prevents anyone from accidentally changing ship state.
  public ShipState getLargestEmptyShipState() {
    ShipState largest = null;
    for (ShipState shipState : shipStates) {
      if (shipState.isEmpty()) {
        if (largest == null || shipState.getCapacity() > largest.getCapacity()) {
          largest = shipState;
        }
      }
    }
    return largest;
  }
  
  public void takeVictoryPointsFromSupply(int count) {
    supplyVictoryPoints = supplyVictoryPoints - count;
  }

  public void removeAvailableBuilding(BuildingType buildingType) {
    int newCount = buildingsToAvailableCount.get(buildingType) - 1;
    buildingsToAvailableCount.put(buildingType, newCount);
  }

  public void addColonistToSupply() {
    supplyColonists = supplyColonists + 1;
  }

  public void addGoodToTradingHouse(Good chosenGood) {
    tradingHouse.addGood(chosenGood);
  }

  public void removeQuarryFromSupply() {
    plantationsState.removeQuarry();
  }

  public Good removePlantationFromHidden() {
    return plantationsState.removeHiddenPlantation();
  }

  public boolean removePlantationFromUncovered(Good plantation) {
    return plantationsState.removeUncoveredPlantation(plantation);
  }

  public void transferPlantationFromUncoveredToDiscarded() {
    plantationsState.transferPlantationsFromUncoveredToDiscarded();
  }

  public void rebuildUncoveredPlantations(List<Player> players) {
    plantationsState.rebuildUncoveredPlantations(players);
  }

  public List<Good> getUncoveredPlantations() {
    return plantationsState.getUncoveredPlantations();
  }

  public List<Good> getAllGoodsInTradingHouse() {
    return tradingHouse.getAllGoods();
  }

  public Set<Good> getUniqueGoodsInTradingHouse() {
    return tradingHouse.getUniqueGoods();
  }

  public Set<BuildingType> getAvailableBuildings() {
    Set<BuildingType> availableBuildings = new HashSet<>();
    for (Map.Entry<BuildingType, Integer> entry : buildingsToAvailableCount.entrySet()) {
      BuildingType buildingType = entry.getKey();
      int availableCount = entry.getValue();
      if (availableCount > 0) {
        availableBuildings.add(buildingType);
      }
    }
    return availableBuildings;
  }

  public Set<Good> getGoodsAlreadyBeingShipped() {
    Set<Good> goodsAlreadyBeingShipped = new HashSet<>();
    for (ShipState shipState : shipStates) {
      if (shipState.getGoodType() != null) {
        goodsAlreadyBeingShipped.add(shipState.getGoodType());
      }
    }
    return goodsAlreadyBeingShipped;
  }
  
  public Set<ShipState> getEmptyShipStatesWithCapacityAtLeast(int goodCount) {
    Set<ShipState> emptyShips = new HashSet<>();
    for (ShipState shipState : shipStates) {
      if (shipState.getFilledCount() == 0 && shipState.getCapacity() >= goodCount) {
        emptyShips.add(shipState);
      }
    }
    return emptyShips;
  }

  public Map<Integer, Good> getFilledShipSizeToGoodType() {
    Map<Integer, Good> shipSizeToGoodType = new HashMap<>();
    for (ShipState shipState : shipStates) {
      if (shipState.isFull()) {
        shipSizeToGoodType.put(shipState.getCapacity(), shipState.getGoodType());
      }
    }
    return shipSizeToGoodType;
  }
  
  public void clearShip(Integer shipSize) {
    ShipState shipState = getShipState(shipSize);
    shipState.setFilledCount(0);
    shipState.setGoodType(null);
  }

  public boolean hasNonFullShipForGood(Good good) {
    if (getLargestEmptyShipState() != null) {
      return true;
    }

    ShipState shipState = getShipStateForGood(good);
    if (shipState != null && !shipState.isFull()) {
      return true;
    }
    
    return false;
  }

  public List<Good> getCoveredPlantations() {
    return plantationsState.getAllCoveredPlantations();
  }

  public List<Good> getDiscardedPlanatations() {
    return plantationsState.getDiscardedPlantations();
  }

  public void clearTradingHouse() {
    tradingHouse.clearGoods();
  }

}