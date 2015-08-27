package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.TradingAndWharfPlayerStrategy;
import org.junit.Assert;
import org.junit.Test;

public class GameExecutorTest {

  private GameExecutor gameExecutor;  

  private List<PlayerStrategy> playerStrategies;
  private Rules rules;
  private GameState gameState;

  /**
   * Set up common test fields: #playerStrategies, #rules, #gameState.
   * 
   * This method is intentionally not marked as @Before to allow individual test methods to 
   * override default values. Test methods can do this overriding by specifying values for one
   * or more of the fields BEFORE calling this method.
   * 
   * IMPORTANT! If #gameState is overridden, then both #playerStrategies and #rules should also be
   * overridden, as #gameState depends on them. Failure to do so will produce unreliable results.
   * 
   * IMPORTANT! Overriding values after calling this method will produce unreliable results.
   */  
  private void setUpGameState() {
    if (playerStrategies == null || playerStrategies.isEmpty()) {
      playerStrategies = new ArrayList<PlayerStrategy>();
      playerStrategies.add(new TradingAndWharfPlayerStrategy());
      playerStrategies.add(new TradingAndWharfPlayerStrategy());
      playerStrategies.add(new TradingAndWharfPlayerStrategy());
      playerStrategies.add(new TradingAndWharfPlayerStrategy());
    }
    
    if (rules == null) {
      rules = new Rules();
    }

    if (gameState == null) {
      gameState = new GameState(rules, playerStrategies);
    }

    gameExecutor = new GameExecutor(rules, gameState);
  }
  
  @Test
  public void mayorShouldDistributeColonists() {
    // Including the one for privilege
    // Set up
    setUpGameState(); 
    gameState.setColonistsOnShip(5);

    // Verify state before
    for (Player player : gameState.getPlayers()) {
      PlayerState playerState = gameState.getPlayerState(player);
      Assert.assertEquals(0, playerState.getAllColonists());
    }
    Assert.assertEquals(5, gameState.getColonistsOnShipCount());
    Assert.assertEquals(75, gameState.getSupplyColonists());
    
    // Do action
    gameExecutor.processRoleChoice(Role.Mayor);
    
    // Verify state after    
    List<Player> players = gameState.getPlayersStartingAtCurrentPlayerToChooseRole();
    Assert.assertEquals(3, gameState.getPlayerState(players.get(0)).getAllColonists()); 
    Assert.assertEquals(1, gameState.getPlayerState(players.get(1)).getAllColonists()); 
    Assert.assertEquals(1, gameState.getPlayerState(players.get(2)).getAllColonists()); 
    Assert.assertEquals(1, gameState.getPlayerState(players.get(3)).getAllColonists());
    Assert.assertEquals(0, gameState.getColonistsOnShipCount());
    Assert.assertEquals(74, gameState.getSupplyColonists());
  }
  
  @Test
  public void playCaptainShouldShipAllPossible() {
    // Set up
    setUpGameState(); 
    Player player = gameState.getCurrentPlayerToChooseRole();
    gameState.transferGoodsFromSupplyToPlayer(Collections.nCopies(6, Good.Corn), player);
    gameState.transferGoodsFromPlayerToShip(player, Good.Corn, 5, 5);
    // Verify state before
    Assert.assertEquals(1, gameState.getPlayerState(player).getGoodCount(Good.Corn));     

    // Do action
    gameExecutor.playTurn(Role.Captain);
    
    // Verify state after
    Assert.assertEquals(1, gameState.getPlayerState(player).getGoodCount(Good.Corn));    
  }
  
  @Test
  public void playCaptainShouldNotLoopForeverIfAvailableWharfIsNotUsed() {
    // Set up
    PlayerStrategy strategy = new DefaultPlayerStrategy() {
      @Override
      public CaptainAction doCaptain(GameState gameState,
          Map<Good, Set<Integer>> goodsToShipSizesAllowedToShipOn,
          Set<Good> goodsAllowedToShipOnWharf) {
        boolean isUsingWharf = false;
        return new CaptainAction(0, Good.Corn, isUsingWharf);
      }
    };
    playerStrategies = Collections.nCopies(4, strategy);
    setUpGameState();
    
    Player player = gameState.getCurrentPlayerToChooseRole();
    gameState.transferBuildingFromSupplyToPlayer(BuildingType.Wharf, player);
    Map<BuildingType, Integer> buildingTypeToCount = new HashMap<>();
    buildingTypeToCount.put(BuildingType.Wharf, 1);
    gameState.occupyPlayerBuildings(buildingTypeToCount, player);
    gameState.transferGoodsFromSupplyToPlayer(Collections.nCopies(6, Good.Corn), player);
    gameState.transferGoodsFromPlayerToShip(player, Good.Corn, 5, 5);
    // Verify state before
    Assert.assertEquals(1, gameState.getPlayerState(player).getGoodCount(Good.Corn));     

    // Do action
    gameExecutor.playTurn(Role.Captain);
    
    // Verify state after
    Assert.assertEquals(1, gameState.getPlayerState(player).getGoodCount(Good.Corn));    
  }
}
