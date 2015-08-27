package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RulesTest {

  private Rules rules;
  
  private GameState gameState;

  @Before
  public void setUp() {
    rules = new Rules();

    // playerStrategies is needed only to create initial GameState. Not used after.
    List<PlayerStrategy> playerStrategies = new ArrayList<PlayerStrategy>();
    playerStrategies.add(new DefaultPlayerStrategy());
    playerStrategies.add(new DefaultPlayerStrategy());
    playerStrategies.add(new DefaultPlayerStrategy());
    playerStrategies.add(new DefaultPlayerStrategy());

    gameState = new GameState(rules, playerStrategies);
  }
  
  @Test
  public void craftsmanShouldGiveGoodsIfPlayerHasOccupiedPlantation() {
    // Set up 
    Player player = gameState.getCurrentPlayerToChooseRole();
    PlayerState playerState = gameState.getPlayerState(player);
    playerState.addPlantationToOccupiedList(Good.Corn);

    // Verify state before
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(10, gameState.getAvailableCount(Good.Corn));
    Assert.assertEquals(0, playerState.getGoodCount(Good.Corn));
    
    // Do action
    rules.givePlayerGoodsDuringCraftsman(gameState, player);
    
    // Verify state after    
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(9, gameState.getAvailableCount(Good.Corn));
    Assert.assertEquals(1, playerState.getGoodCount(Good.Corn));    
  }

  @Test
  public void captainShouldShipGoodsIfSpaceOnShip() {
    // Set up
    Player player = gameState.getCurrentPlayerToChooseRole();
    PlayerState playerState = gameState.getPlayerState(player);
    gameState.transferGoodsFromSupplyToPlayer(Good.Corn, 1, player);

    // Verify state before
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(9, gameState.getAvailableCount(Good.Corn));
    Assert.assertEquals(1, playerState.getGoodCount(Good.Corn));
    Assert.assertEquals(new HashSet<Good>(), gameState.getGoodsAlreadyBeingShipped());
    Assert.assertNull(gameState.getShipStateForGood(Good.Corn));
    Assert.assertEquals(0, playerState.getVictoryPointChips());
    
    // Do action
    boolean useWharf = false;
    CaptainAction captainAction = new CaptainAction(6, Good.Corn, useWharf);
    rules.executeCaptainAction(gameState, captainAction, player);
    
    // Verify state after    
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(9, gameState.getAvailableCount(Good.Corn));    
    Assert.assertEquals(0, playerState.getGoodCount(Good.Corn));
    HashSet<Good> goodList = new HashSet<Good>();
    goodList.add(Good.Corn);
    Assert.assertEquals(goodList, gameState.getGoodsAlreadyBeingShipped());
    Assert.assertEquals(6, gameState.getShipStateForGood(Good.Corn).getCapacity());
    Assert.assertEquals(1, gameState.getShipStateForGood(Good.Corn).getFilledCount());
    Assert.assertEquals(1, playerState.getVictoryPointChips());
  }

  @Test
  public void captainUsingWharfShouldNotChangeShipState() {
    // Set up
    Player player = gameState.getCurrentPlayerToChooseRole();
    PlayerState playerState = gameState.getPlayerState(player);
    gameState.transferGoodsFromSupplyToPlayer(Good.Corn, 1, player);

    // Verify state before
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(9, gameState.getAvailableCount(Good.Corn));
    Assert.assertEquals(1, playerState.getGoodCount(Good.Corn));
    Assert.assertEquals(new HashSet<Good>(), gameState.getGoodsAlreadyBeingShipped());
    Assert.assertNull(gameState.getShipStateForGood(Good.Corn));
    Assert.assertEquals(0, playerState.getVictoryPointChips());
    
    // Do action
    boolean useWharf = true;
    CaptainAction captainAction = new CaptainAction(6, Good.Corn, useWharf);
    rules.executeCaptainAction(gameState, captainAction, player);
    
    // Verify state after    
    playerState = gameState.getPlayerState(player);
    Assert.assertEquals(9, gameState.getAvailableCount(Good.Corn));    
    Assert.assertEquals(0, playerState.getGoodCount(Good.Corn));
    HashSet<Good> goods = new HashSet<Good>();
    goods.add(Good.Corn);
    Assert.assertEquals(goods, gameState.getGoodsAlreadyBeingShipped());
    Assert.assertEquals(6, gameState.getShipStateForGood(Good.Corn).getCapacity());
    Assert.assertEquals(1, gameState.getShipStateForGood(Good.Corn).getFilledCount());
    Assert.assertEquals(1, playerState.getVictoryPointChips());
  }
  
}
