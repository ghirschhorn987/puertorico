package org.hirschhorn.puertorico.playerstrategies.ariella;

import java.util.List;
import java.util.Set;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.RandomRolePlayerStrategy;

public class AriellaPlayerStrategy extends RandomRolePlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new AriellaPlayerStrategy();
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    PlayerState playerState = gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole());
    if ( availableRoles.contains(Role.Trader)) {
      if (playerState.getGoodCount(Good.Coffee) > 0) {
        if (playerState.doesPlayerHaveOccupiedBuilding(BuildingType.Office)) {
          return Role.Trader;
        } else if (!gameState.getUniqueGoodsInTradingHouse().contains(Good.Coffee)) {
          return Role.Trader;
        }
      }
    }
    return super.chooseRole(gameState, availableRoles);
  }

  @Override
  public BuilderAction doBuilder(GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
    if (buildingsAllowedToBuy.contains(BuildingType.CoffeStorage)) {
      return new BuilderAction(BuildingType.CoffeStorage);
    } else if (buildingsAllowedToBuy.contains(BuildingType.Office)) {
      return new BuilderAction(BuildingType.Office);
    } else if (buildingsAllowedToBuy.contains(BuildingType.LargeMarket)) {
      return new BuilderAction(BuildingType.LargeMarket);      
    } else if (buildingsAllowedToBuy.contains(BuildingType.SmallMarket)) {
      return new BuilderAction(BuildingType.SmallMarket);      
    } else {
      return new BuilderAction(null);
    }
  }

  @Override
  public SettlerAction doSettler(
      GameState gameState,
      Set<Good> plantationsAllowedToChoose,
      boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
    if (plantationsAllowedToChoose.contains(Good.Coffee)) {
      return new SettlerAction(Good.Coffee, false, false);
    }
    return super.doSettler(gameState, plantationsAllowedToChoose, allowedToChooseQuarry, allowedToUseHaciendaBuildingToGetExtraPlantation);
  }

}
