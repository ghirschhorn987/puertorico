package org.hirschhorn.puertorico.playerstrategies;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.actions.TraderAction;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;

public class TradingAndWharfPlayerStrategy extends DefaultPlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new TradingAndWharfPlayerStrategy();
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    int choice = (int) Math.floor(Math.random() * availableRoles.size());
    return availableRoles.get(choice);
  }
  
  @Override
  public TraderAction doTrader(GameState gameState, Set<Good> goodsAllowedToTrade) {
    Good good = null;
    if (!goodsAllowedToTrade.isEmpty()) {
      good = goodsAllowedToTrade.iterator().next();
    }  
    return new TraderAction(good);
  }

  @Override
  public CaptainAction doCaptain(GameState gameState,
      Map<Good, Set<Integer>> goodsToShipSizesAllowedToShipOn,
      Set<Good> goodsAllowedToShipOnWharf) {
    CaptainAction action = super.doCaptain(gameState, goodsToShipSizesAllowedToShipOn, goodsAllowedToShipOnWharf);
    return new CaptainAction(
        action.getChosenShipSize(),
        action.getGoodToShip(),
        true);
  }
  
}
