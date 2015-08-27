package org.hirschhorn.puertorico;

import java.util.Comparator;
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
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

import com.google.common.base.Objects;

public class Player {
  private int position;
  private PlayerStrategy strategy;
  
  public static Comparator<Player> getStrategyNameComparator() {
    return new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
        return ((Player)o1).getStrategy().getClass().getName().compareTo(((Player)o2).getStrategy().getClass().getName());
      }
    };
  }
  
  public Player(Player player) {    
    position = player.getPosition();
    strategy = player.getStrategy();;
  }
  
  public Player(int position, PlayerStrategy strategy) {
    this.strategy = strategy;
    this.position = position;
  }
  
  public PlayerStrategy getStrategy() {
    return strategy.getCopy();
  }

  public int getPosition() {
    return position;
  }
  
  public Role chooseRole(GameState gameState, List<Role> availableRoles){
    return strategy.chooseRole(gameState, availableRoles);
  }

  public SettlerAction doSettler(
      GameState gameState,
      Set<Good> plantationsAllowedToChoose,
      boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation){
    return strategy.doSettler(gameState, plantationsAllowedToChoose, allowedToChooseQuarry, allowedToUseHaciendaBuildingToGetExtraPlantation);
  }
  
  public MayorAction doMayor(
      GameState gameState,
      int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy,
      List<Good> plantationsAllowedToOccupy) {  
    return strategy.doMayor(
        gameState,
        colonistsAllowedToOccupy,
        buildingToOccupiedCountAllowed,
        quarriesAllowedToOccupy,
        plantationsAllowedToOccupy);
  }
  
  public boolean declineExtraColonistPrivilegeForMayor(GameState gameState) {
    return strategy.declineExtraColonistPrivilegeForMayor(gameState);
  }

  public BuilderAction doBuilder(
      GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
    return strategy.doBuilder(gameState, buildingsAllowedToBuy);
  }

  public CraftsmanAction doCraftsman(
      GameState gameState,
      Set<Good> goodsAllowedToChooseAsPrivilege) {
    return strategy.doCraftsman(gameState, goodsAllowedToChooseAsPrivilege);
  }

  public TraderAction doTrader(
      GameState gameState,
      Set<Good> goodsAllowedToTrade) {
    return strategy.doTrader(gameState, goodsAllowedToTrade);
  }

  public CaptainAction doCaptain(
      GameState gameState,
      Map<Good, Set<Integer>> allowedGoodsToAllowedShipSizes,
      Set<Good> goodsAllowedToShipOnWharf) {
    return strategy.doCaptain(gameState, allowedGoodsToAllowedShipSizes, goodsAllowedToShipOnWharf);
  }

  public List<Good> chooseGoodsToKeepAfterCaptain(
      GameState gameState,
      Map<Good, Integer> goodsAllowedToKeep, int goodTypesAllowedToKeepMoreThanOneOf) {
    return strategy.chooseGoodsToKeepAfterCaptain(
        gameState, goodsAllowedToKeep, goodTypesAllowedToKeepMoreThanOneOf);
  }

  public String toString() {
    return "Player " + getPosition() + ": " + getStrategyNameSimple();
  }

  public String getStrategyNameSimple() {
    return strategy.getClass().getSimpleName();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(position, strategy);
  }

  @Override
  public boolean equals(Object obj) {
    Player other = (Player) obj;
    return (position == other.position && Objects.equal(strategy, other.strategy));
  }
    
}
