package org.hirschhorn.puertorico.playerstrategies.zack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.actions.TraderAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.dad.BuildingCornQuarry;

public class ZackStragety extends DefaultPlayerStrategy {

  private PlayerState getRolePs(GameState gameState){
    return gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole());
  }
  
  private PlayerState getActionPs(GameState gameState){
    return gameState.getPlayerState(gameState.getCurrentPlayerToDoAction());
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    PlayerState ps = getRolePs(gameState);
    Role role = super.chooseRole(gameState, availableRoles);
    if (!haveNoGoods(gameState, ps) && availableRoles.contains(Role.Captain)){
      role = Role.Captain;
    }
    if (canChooseCorn(gameState, ps) && availableRoles.contains(Role.Settler)){
      role = Role.Settler;
    }
    if (haveNoGoods(gameState, ps) && availableRoles.contains(Role.Craftsman)){
      role = Role.Craftsman;
    }
    return role; 
  }

  private boolean haveNoGoods(GameState gameState, PlayerState ps) {
    if (ps.getGoods().isEmpty()){
      return true;
    }
    return false;
  }

  private boolean canShipCorn(GameState gameState, PlayerState ps) {
    if (!haveNoGoods(gameState, ps) && gameState.getShipStateForGood(Good.Corn) != null) {
      if (gameState.getLargestEmptyShipState() != null){
      return true;
      }
      if (gameState.getShipStateForGood(Good.Corn).getFilledCount() < gameState.getShipStateForGood(Good.Corn).getCapacity()
          || gameState.getEmptyShipStatesWithCapacityAtLeast(0).size() > 1){
        return true;
      }
    }
    return false;
  }

  private boolean canChooseCorn(GameState gameState, PlayerState ps) {
    return gameState.getUncoveredPlantations().contains(Good.Corn);
  }

  @Override
  public SettlerAction doSettler(GameState gameState,
      Set<Good> plantationsAllowedToChoose, boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
      Good plantation = null;
      plantation = plantationsAllowedToChoose.iterator().next();
      if (plantationsAllowedToChoose.contains(Good.Corn)){
        plantation = Good.Corn;
      }
      return new SettlerAction (plantation, 
                                false, 
                                false);
  }
    

  @Override
  public CaptainAction doCaptain(GameState gameState,
      Map<Good, Set<Integer>> goodsToShipSizesAllowedToShipOn,
      Set<Good> goodsAllowedToShipOnWharf) {
    // TODO program wharf into this
    Good good = super.doCaptain(gameState, goodsToShipSizesAllowedToShipOn, goodsAllowedToShipOnWharf).getGoodToShip();
    int shipSize = super.doCaptain(gameState, goodsToShipSizesAllowedToShipOn, goodsAllowedToShipOnWharf).getChosenShipSize();
    if (goodsToShipSizesAllowedToShipOn.get(Good.Corn) != null){
      for (Integer size : goodsToShipSizesAllowedToShipOn.get(Good.Corn)) {
        shipSize = 0;
        if (size > shipSize){
          shipSize = size;
        }
        good = Good.Corn;
      }
    };
    CaptainAction action = new CaptainAction(shipSize, good, false);
    return action;
  }
  
  public MayorAction doMayor(GameState gameState, int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy, List<Good> plantationsAllowedToOccupy) {
      List<Good> plantations = new ArrayList<>();
      List<Good> plantationsNotOccupied = new ArrayList<>(getActionPs(gameState).getAllPlantations());
      Map<BuildingType, Integer> buildingToOccupiedCount = new HashMap<>();
      int quarries = 0;
      int quarriesNotOccupied = quarriesAllowedToOccupy;
      int loops = 0;
      while (colonistsAllowedToOccupy != 0 && loops < 100){;
        if (loops == 10 || loops == 50){
          System.out.println("loops reached 10 or 50");
        }
        if (loops == 100){
          System.out.println("loops reached limit:100");
        }
        if (buildingToOccupiedCountAllowed.keySet().contains(BuildingType.CustomsHouse)){
          buildingToOccupiedCount.put(BuildingType.CustomsHouse, 1);
          colonistsAllowedToOccupy--;
          loops++;
        } else 
          if (plantationsNotOccupied.contains(Good.Corn)){
          plantations.add(Good.Corn);
          plantationsNotOccupied.remove(Good.Corn);
          colonistsAllowedToOccupy--;
          loops++;
        } else if (!plantationsNotOccupied.isEmpty() && colonistsAllowedToOccupy != 0){
          plantations.add(plantationsNotOccupied.get(0));
          plantationsNotOccupied.remove(0);
          colonistsAllowedToOccupy--;
          loops++;
        } else if (!getActionPs(gameState).getAllBuildings().isEmpty()){
          buildingToOccupiedCount.put(getActionPs(gameState).getAllBuildings().get(0), 1);
          colonistsAllowedToOccupy--;
          loops++;
        } else if (quarriesNotOccupied != 0){
          quarries++;
          quarriesNotOccupied--;
          colonistsAllowedToOccupy--;
          loops++;
        } else {
          loops++;
        }
      }
      MayorAction action = new MayorAction(
          plantations,
          buildingToOccupiedCount,
          quarries);
    return action;
  }

  @Override
  public BuilderAction doBuilder(GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
      BuildingType buildingType = null;
      if (buildingsAllowedToBuy.contains(BuildingType.CustomsHouse)){
        buildingType = BuildingType.CustomsHouse;
      }
      if (getActionPs(gameState).getAllBuildings().contains(BuildingType.CustomsHouse) && !buildingsAllowedToBuy.isEmpty()){
        buildingType = buildingsAllowedToBuy.iterator().next();
      }
      BuilderAction action = new BuilderAction(buildingType);
    return action;
  }

//  @Override
//  public BuilderAction doBuilder(GameState gameState,
//      Set<BuildingType> buildingsAllowedToBuy){
//        BuildingType buildingToBuy = super.doBuilder(gameState, buildingsAllowedToBuy).getBuildingToBuy();
//        if (buildingToBuy != null && Building.getBuildingFromType(buildingToBuy).getIslandSpacesFilled() == 2) {
//          if (buildingsAllowedToBuy.contains(BuildingType.CustomsHouse)){
//            buildingToBuy = BuildingType.CustomsHouse;
//          }
//        }
//        BuilderAction action = new BuilderAction(buildingToBuy);
//        return action;
//  }
//  
//  
  
  
  @Override
  public TraderAction doTrader(GameState gameState,
      Set<Good> goodsAllowedToTrade) {
    Good good = null;
    if (!goodsAllowedToTrade.isEmpty()){
      for (Good candidateGood : goodsAllowedToTrade){
        if (candidateGood.equals(Good.Sugar)){
          if (!good.equals(Good.Tobacco) || !good.equals(Good.Coffee)){
          good = Good.Sugar;
          } else if (candidateGood.equals(Good.Tobacco)){
            if (!good.equals(Good.Coffee)){
            good = Good.Tobacco;
            }else if (candidateGood.equals(Good.Coffee)){
              good.equals(Good.Coffee);
            }
          }
        }
      }
    }
    TraderAction action = new TraderAction(good);
    return action;
  }
}
