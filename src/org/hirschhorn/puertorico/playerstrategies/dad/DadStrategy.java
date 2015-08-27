package org.hirschhorn.puertorico.playerstrategies.dad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

public class DadStrategy extends DefaultPlayerStrategy {

  private List<Role> roleChoices;
  private List<BuildingCornQuarry> mayorBuildingCornQuarries;
  
  @Override
  public PlayerStrategy getCopy() {
    return new DadStrategy(new ArrayList<>(roleChoices), new ArrayList<>(mayorBuildingCornQuarries));
  }
  
  public static DadStrategy getDefaultDadStrategy() {
    List<Role> roleChoices = Arrays.asList(
        Role.Settler,
        Role.Mayor,
        Role.Builder,
        Role.Captain,
        Role.Trader,
        Role.Craftsman,
        Role.Prospector);
    
    List<BuildingCornQuarry> mayorBuildingCornQuarries = new ArrayList<>();
    mayorBuildingCornQuarries.addAll(Collections.nCopies(11, new BuildingCornQuarry(BuildingCornQuarry.Type.Corn, null)));
    mayorBuildingCornQuarries.addAll(Collections.nCopies(4, new BuildingCornQuarry(BuildingCornQuarry.Type.Quarry, null)));
    for (BuildingType buildingType : BuildingType.values()) {
      mayorBuildingCornQuarries.add(new BuildingCornQuarry(BuildingCornQuarry.Type.Building, buildingType));
    }
    
    return new DadStrategy(roleChoices, mayorBuildingCornQuarries);
  }
  
  public DadStrategy(
      List<Role> roleChoices,
      List<BuildingCornQuarry> mayorBuildingCornQuarries) {
    this.roleChoices = roleChoices;
    this.mayorBuildingCornQuarries = mayorBuildingCornQuarries;
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    int maxMoney = 0;
    for (Role role : availableRoles) {
      maxMoney = Math.max(maxMoney, gameState.getMoneyOnRole(role));
    }
    
    // Get available role with most money
    for (Role role : roleChoices) {
      if (availableRoles.contains(role) && gameState.getMoneyOnRole(role) == maxMoney) {
        return role;
      }
    }
    throw new IllegalStateException("Should not be here.");
  }
  
  @Override
  public MayorAction doMayor(
      GameState gameState,
      int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy,
      List<Good> plantationsAllowedToOccupy) {

    int colonistsRemaining = colonistsAllowedToOccupy;

    Map<BuildingType, Integer> buildingToOcccupiedCount = new HashMap<>();
    List<Good> occupiedPlantations = new ArrayList<>();
    int occupiedQuarryCount = 0;
    
    for (BuildingCornQuarry buildingCornQuarry : mayorBuildingCornQuarries) {
      if (colonistsRemaining > 0) {
        switch (buildingCornQuarry.getType()) {
          case Building:
            BuildingType buildingType = buildingCornQuarry.getBuildingType();
            Integer countAllowed = buildingToOccupiedCountAllowed.get(buildingType);
            if (countAllowed != null) {
              Building building = Building.getBuildingFromType(buildingType);
              if (building.isProduction()) {
                // Only use production building if you can produce the Good.
                Good goodType = building.getProductionGood();
                int unoccupiedIslandSpaces = getRemainingPlantationsAllowedToOccupy(goodType, plantationsAllowedToOccupy, occupiedPlantations);
                if (colonistsRemaining < 2 || unoccupiedIslandSpaces <= 0) {
                  break;  
                }
              }
              Integer currentCount = buildingToOcccupiedCount.get(buildingType);
              if (currentCount == null) {
                currentCount = 0;
              }
              if (currentCount < countAllowed) {
                colonistsRemaining--;
                buildingToOcccupiedCount.put(buildingType, currentCount + 1);
                if (building.isProduction()) {
                  Good goodType = building.getProductionGood();
                  colonistsRemaining--;
                  occupiedPlantations.add(goodType);
                }
              }
            }
            break;
          case Corn:
            Good goodType = Good.Corn;
            int unoccupiedIslandSpaces = getRemainingPlantationsAllowedToOccupy(goodType, plantationsAllowedToOccupy, occupiedPlantations);
            if (unoccupiedIslandSpaces > 0) {
              colonistsRemaining--;
              occupiedPlantations.add(goodType);
            }
            break;
          case Quarry:
            if (quarriesAllowedToOccupy < occupiedQuarryCount) {
              colonistsRemaining--;
              occupiedQuarryCount++;
            }
            break;
          default:
            throw new IllegalStateException("Unknown BuildingCornQuarry type: " + buildingCornQuarry.getType());
        }
      }
    }    
    return new MayorAction(occupiedPlantations, buildingToOcccupiedCount, occupiedQuarryCount);
  }

  @Override
  public BuilderAction doBuilder(
      GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {    

    BuilderAction builderAction = super.doBuilder(gameState, buildingsAllowedToBuy);
    Set<BuildingType> buildingsNotToBuy = new HashSet<>(Arrays.asList(
        BuildingType.University));

    PlayerState ps = gameState.getPlayerState(gameState.getCurrentPlayerToDoAction());
    
    if (buildingsAllowedToBuy.contains(BuildingType.Wharf)) {
      builderAction = new BuilderAction(BuildingType.Wharf);
    } else {
      int money = ps.getMoney();
      int quarriesOwned = ps.getOccupiedQuarryCount();
      int wharfCost = Building.getBuildingFromType(BuildingType.Wharf).getCost();
      if (wharfCost - money - quarriesOwned < 2) {
        builderAction = new BuilderAction(null);      
      } else if (builderAction.getBuildingToBuy() != null) {
        if (buildingsNotToBuy.contains(builderAction.getBuildingToBuy())) {
          builderAction = new BuilderAction(null);
        }
      }
    }
    return builderAction;
  }

  private int getRemainingPlantationsAllowedToOccupy(
      Good goodType,
      Collection<Good> plantationsAllowedToOccupy,
      Collection<Good> occupiedPlantations) {
    int countAllowed = Collections.frequency(plantationsAllowedToOccupy, goodType);
    int currentCount = Collections.frequency(occupiedPlantations, goodType);
    return countAllowed - currentCount;
  }

}
