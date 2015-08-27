package org.hirschhorn.puertorico.playerstrategies.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hirschhorn.puertorico.Player;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.RandomRolePlayerStrategy;

public class SamSecondStrategy extends RandomRolePlayerStrategy {

  @Override
  public SettlerAction doSettler(GameState gameState,
      Set<Good> plantationsAllowedToChoose, boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
    boolean usingHacienda = false;
    Good chosenPlantation = null;
    if (allowedToUseHaciendaBuildingToGetExtraPlantation == true) {
      usingHacienda = true;
    }
    List<Good> prioritizedPlantations = new ArrayList<>();
    prioritizedPlantations.add(Good.Corn);
    if (plantationsAllowedToChoose.contains(Good.Corn)){
      chosenPlantation = Good.Corn;
    } else {
      chosenPlantation = plantationsAllowedToChoose.iterator().next();
    }
    
    return new SettlerAction(chosenPlantation, false, usingHacienda);
  }

  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    if (canImproveGoodProductionWithSettler(gameState) && availableRoles.contains(Role.Settler)){
      return Role.Settler;
    }
    //TODO change to can ship the most goods
    if ((hasTheMostGoods(gameState) || gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole()).getGoods().size() >=5) && availableRoles.contains(Role.Captain)){
      return Role.Captain;
    }
    if (canProduceTheMostGoods(gameState) && availableRoles.contains(Role.Craftsman)){
      return Role.Craftsman;
    }
    if (canImproveGoodProductionWithBuilder(gameState) && availableRoles.contains(Role.Builder)){
      return Role.Builder;
    }
    return roleWithMostMoney(gameState, availableRoles);
  }

  private boolean canImproveGoodProductionWithBuilder(GameState gameState) {
    PlayerState playerState = gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole());
    for (Good plantation : playerState.getAllPlantations()) {
      if (plantation.equals(Good.Corn)) {
      } else {
        int buildingCount = 0;
        int plantationCount = Collections.frequency(playerState.getAllPlantations(), plantation);
        for (BuildingType buildingType : playerState.getAllBuildings()) {
          Building building = Building.getBuildingFromType(buildingType);
          if (building.isProduction() && building.getProductionGood().equals(plantation)) {
            if (building.isLargeProductionBuilding() && !building.getProductionGood().equals(Good.Coffee)) {
               buildingCount =+ 3;
            } else if (building.isLargeProductionBuilding() && building.getProductionGood().equals(Good.Coffee)){
               buildingCount =+ 2;
            } else {
               buildingCount =+1;
            }
          }
        }
        if (plantationCount > buildingCount) {
          return true;
        }
      }
    }
    return false;
  }

  
  private boolean hasTheMostGoods(GameState gameState) {
    int mostGoods = gameState.getPlayerState(gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(0)).getGoods().size();
    Player playerWithMostGoods = gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(0);
    for (Player player : gameState.getPlayersStartingAtCurrentPlayerToChooseRole()){
      if (gameState.getPlayerState(player).getGoods().size() > mostGoods) {
        mostGoods = gameState.getPlayerState(player).getGoods().size();
        playerWithMostGoods = player;
      }
    }
    if (playerWithMostGoods.equals(gameState.getCurrentPlayerToChooseRole())) {
      return true;
    }
    return false;
  }

  private boolean canProduceTheMostGoods(GameState gameState) {
    int mostGoods = getGoodsAbleToProduce(gameState, gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(0)).size();
    Player playerWithMostGoods = gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(0);
    for (int x = 0; x < gameState.getPlayersStartingAtCurrentPlayerToChooseRole().size(); x++){
      Player player = gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(x);
      if (getGoodsAbleToProduce(gameState, player).size() > mostGoods) {
        mostGoods = getGoodsAbleToProduce(gameState, player).size();
        playerWithMostGoods = player;
      }
    }
    if (playerWithMostGoods.equals(gameState.getCurrentPlayerToChooseRole())) {
      return true;
    }
    return false;
  }
  
  private List<Good> getGoodsAbleToProduce(GameState gameState, Player player) {
    List<Good> plantations = new ArrayList<>(gameState.getPlayerState(player).getOccupiedPlantations()); 
    List<Good> goods = new ArrayList<>();
    for (BuildingType buildingType : gameState.getPlayerState(player).getOccupiedBuildings()) {
      Building building = Building.getBuildingFromType(buildingType);
      if (building.isProduction()) {
        int colonists = gameState.getOccupiedColonistCount(buildingType, player);
        Good good = building.getProductionGood();
        int plantationsOfSpecificGood = gameState.getOccupiedPlantationCount(good, plantations);
        int goodsAbleToProduce = Math.min(plantationsOfSpecificGood, colonists);
        for (int x=0; x < goodsAbleToProduce; x++) {
          goods.add(good);
          plantations.remove(good);
        }
      } 
    }
    for (Good good : gameState.getPlayerState(player).getOccupiedPlantations()) {
      if (good.equals(Good.Corn)) {
        goods.add(good);
      }
    }
    return goods;
  }

  private Role roleWithMostMoney(GameState gameState, List<Role> availableRoles) {
    Role role = availableRoles.get(0);
    int highestValue = gameState.getMoneyOnRole(availableRoles.get(0));
    for (int x = 0; x < availableRoles.size(); x++) {
      Role role1 = availableRoles.get(x);
      if (gameState.getMoneyOnRole(role1) > highestValue) {
        highestValue = gameState.getMoneyOnRole(role1);
        role = role1;
      }
    }
    return role;
  }

  private boolean canImproveGoodProductionWithSettler(GameState gameState) {
    if (gameState.getUncoveredPlantations().contains(Good.Corn)){
      return true;
    }
    // TODO also add if have building but no plantation
    return false;
  }
  

}
