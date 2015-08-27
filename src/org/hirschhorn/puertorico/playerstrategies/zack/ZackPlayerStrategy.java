package org.hirschhorn.puertorico.playerstrategies.zack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

public class ZackPlayerStrategy extends DefaultPlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new ZackPlayerStrategy();
  }
  
  @Override
  public MayorAction doMayor(GameState gameState, int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy, List<Good> plantationsAllowedToOccupy) {
    MayorAction action =  super.doMayor(gameState, colonistsAllowedToOccupy,
        buildingToOccupiedCountAllowed, quarriesAllowedToOccupy,
        plantationsAllowedToOccupy);
    if (gameState.getPlayerState(gameState.getCurrentPlayerToDoAction()).doesPlayerHaveOccupiedBuilding(BuildingType.Fortress)){
      Map<BuildingType, Integer> buildingToOccupiedCount = new HashMap<>(action.getBuildingToOccupiedCount());
      int quarries = action.getOccupiedQuarryCount();
      List<Good> plantations = action.getOccupiedPlantations();
      Integer count = buildingToOccupiedCount.get(BuildingType.Fortress);
      if (count == null || count == 0){
        if (quarries > 0){
          quarries--;
        } else if (!plantations.isEmpty()) {
          plantations.remove(0);
        } else {
          for (BuildingType buildingType : buildingToOccupiedCount.keySet()){
            if (buildingToOccupiedCount.get(buildingType) > 0) {
              buildingToOccupiedCount.put(buildingType, buildingToOccupiedCount.get(buildingType) - 1);
            }
          }
        }
        buildingToOccupiedCount.put(BuildingType.Fortress, 1);
      }
      action = new MayorAction(
          plantations,
          buildingToOccupiedCount,
          quarries);
    }
    return action;
  }

  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    int money = gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole()).getMoney();
    int quarriesOwned = gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole()).getOccupiedQuarryCount();
    int moneyOnBuilder = gameState.getMoneyOnRole(Role.Builder);
    int spendingPower = money + quarriesOwned + moneyOnBuilder;
    if (availableRoles.contains(Role.Builder) && spendingPower >= 9) {
      return Role.Builder;
    }
    if (availableRoles.contains(Role.Mayor) && gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole()).doesPlayerHaveOccupiedBuilding(BuildingType.Fortress)){
      return Role.Mayor;
    }
    Role role = chooseRoleWithMostMoney(gameState);
    if (availableRoles.contains(role)) {
      return role;
    }
    return availableRoles.iterator().next();
  }
  
//  @Override
//  public BuilderAction doBuilder(GameState gameState,
//      Set<BuildingType> buildingsAllowedToBuy) {
//    BuildingType buildingToBuy = null;
//    if (buildingsAllowedToBuy.contains(BuildingType.Fortress)) {
//      buildingToBuy = BuildingType.Fortress;
//    }
//    return new BuilderAction(buildingToBuy);
//  }
//  

  private Role chooseRoleWithMostMoney(GameState gameState) {
    Role roleWithMostMoney = null;
    for (Role role : gameState.getAvailableRoles()){
      if (roleWithMostMoney == null || gameState.getMoneyOnRole(role) > gameState.getMoneyOnRole(roleWithMostMoney)){
        roleWithMostMoney = role;
      }
    }
    return roleWithMostMoney;
  }
  
}
