package org.hirschhorn.puertorico.playerstrategies;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.actions.CraftsmanAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.actions.TraderAction;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;

public interface PlayerStrategy {

  PlayerStrategy getCopy();
  
  Role chooseRole(
      GameState gameState,
      List<Role> availableRoles);
  
  SettlerAction doSettler(
      GameState gameState,
      Set<Good> plantationsAllowedToChoose,
      boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation);

  MayorAction doMayor(
      GameState gameState,
      int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy,
      List<Good> plantationsAllowedToOccupy);
  
  boolean declineExtraColonistPrivilegeForMayor(GameState gameState);

  BuilderAction doBuilder(
      GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy);

  CraftsmanAction doCraftsman(
      GameState gameState,
      Set<Good> goodsAllowedToChooseAsPrivilege);

  TraderAction doTrader(
      GameState gameState,
      Set<Good> goodsAllowedToTrade);

  CaptainAction doCaptain(
      GameState gameState,
      Map<Good, Set<Integer>> allowedGoodsToAllowedShipSizes,
      Set<Good> goodsAllowedToShipOnWharf);
  
  /**
   * Player can always keep 1 Good of any one Good type from #goodsAllowedToKeep.
   * 
   * If #goodTypesAllowedToKeepMoreThanOneOf is greater than 0, then a player can keep all Goods
   * of a specific Good type in #goodsAllowedToKeep for that many Good types. These Goods are
   * in addition to the 1 Good a player can always keep.
   *  
   * @param gameState
   * @param goodsAllowedToKeep
   * @param goodTypesAllowedToKeepMoreThanOneOf
   * @return
   */
  List<Good> chooseGoodsToKeepAfterCaptain(
      GameState gameState,
      Map<Good, Integer> goodsAllowedToKeep,
      int goodTypesAllowedToKeepMoreThanOneOf);
}
