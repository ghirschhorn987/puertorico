package org.hirschhorn.puertorico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hirschhorn.puertorico.ResultsUI.Statistic;
import org.hirschhorn.puertorico.actions.Action;
import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.actions.CraftsmanAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.actions.ProspectorAction;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.actions.TraderAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;

import com.google.common.annotations.VisibleForTesting;

public class GameExecutor {
  private static final Logger logger = Logger.getLogger(GameExecutor.class.getName());
  
  private static final int CAPTAIN_EXTRA_VICTORY_POINT = 1;
  
  private GameState gameState;
  private Rules rules;
  private ActionValidator actionValidator;
  
  public GameExecutor(
      Rules rules,
      GameState gameState) {
    this.rules = rules;
    this.gameState = gameState;
    
    actionValidator = new ActionValidator();
  }
  
  @VisibleForTesting
  GameState getGameState() {
    return new GameState(gameState);
  }
  
  public void playGame() {
    logger.fine("Starting game");
    int round = 0;
    while (!rules.isGameOver(gameState)) {
      round++;
      logger.fine("====\nStarting round " + round);
      playRound();
      
      if (round == 12) {
        for (Player player : gameState.getPlayers()) {
          String strategyNameSimple = player.getStrategyNameSimple();
          int money = gameState.getPlayerStatistic(strategyNameSimple, Statistic.CumulativeMoneyReceived).intValue();
          gameState.incrementPlayerStatistic(strategyNameSimple, Statistic.MoneyAfterRound12, money);
        }
      }
    }
    logger.fine("====\nEnding game. Calculating winner.");
    giveBonusVictoryPoints();
    displayResults(false);
    logger.fine("====\nGame over. Rounds: " + round);
  }

  private void giveBonusVictoryPoints() {
    for (Player player : gameState.getPlayers()){
      rules.giveBonusVictoryPoints(gameState, player);
    }
  }

  private void displayResults(boolean includeGameState) {
    for (int x = 0; x < gameState.getPlayers().size(); x++) {
      Player player = gameState.getPlayers().get(x);
      PlayerState playerState = gameState.getPlayerState(player);
      logger.fine("" + x + ": " + playerState.getVictoryPoints());
    }
    
    if (includeGameState) {
      // TODO print gameState
    }
  }

  private void playRound() {
    logger.fine("Governor: Player " + gameState.getGovernorPlayerNumber());
    gameState.setCurrentPlayerToChooseRoleToBeGovernor();
    while (!rules.isRoundOver(gameState)) {
      Player player = gameState.getCurrentPlayerToChooseRole();
      
      Role role = player.chooseRole(getGameState(), gameState.getAvailableRoles());
      role = actionValidator.validateChooseRole(role, getGameState(), gameState.getAvailableRoles());
      
      logger.fine("---\nPlayer " + player.getPosition() + " chose role " + role);
      processRoleChoice(role);
      playTurn(role);
      gameState.updateCurrentPlayerToChooseRole();
    }
    endOfRound();
  }

  @VisibleForTesting
  void processRoleChoice(Role role) {
    Player playerChoosingRole = gameState.getCurrentPlayerToChooseRole();
    rules.transferMoneyFromRoleToPlayer(gameState, role, playerChoosingRole);
    switch (role) {
      case Settler:
        break;
      case Mayor:
        // Give ship supply to all players
        rules.givePlayersColonistsDuringMayor(gameState);
        // Give extra for privilege
        if (!playerChoosingRole.declineExtraColonistPrivilegeForMayor(getGameState())) {
          gameState.transferColonistFromSupplyToPlayer(playerChoosingRole);
        }
        break;
      case Builder:
        break;
      case Craftsman:
        for (Player player : gameState.getPlayersStartingAtCurrentPlayerToChooseRole()) {
          rules.givePlayerGoodsDuringCraftsman(gameState, player);
        }
        break;
      case Trader:
        break;
      case Captain:
        break;
      case Prospector:
        break;
      default:
        throw new RuntimeException("unrecognized role: " + role);
      }
  }

  private void endOfRound() {
    gameState.updateGovernor();
    gameState.addCoinsToUnusedRoles();
    gameState.resetRoles();
  }

  @VisibleForTesting
  void playTurn(Role role) {
    gameState.setAllPlayersHaveDoneActionInCurrentTurn(false);
    gameState.setCurrentPlayerToDoActionToBeCurrentRolePlayer();
    if (!role.equals(Role.Captain)) {
      while (!gameState.getAllPlayersHaveDoneActionInCurrentTurn()) {  
        playNonCaptainTurn(role);
      }
    } else {
      // Captain is different than other roles because players can do action more than once.
      playCaptainTurn();
    }
    endOfTurn(role);
  }
  
  private void playNonCaptainTurn(Role role) {
    Player player = gameState.getCurrentPlayerToDoAction();
    logger.fine("Player " + player.getPosition() + " taking action for " + role);
    PlayerState playerState = gameState.getPlayerState(player);
    Action action = null;
    switch (role) {
      case Settler:
        action = playSettler(player, playerState);
        break;
      case Mayor:
        action = playMayor(player);
        break;
      case Builder:
        action = playBuilder(player);
        break;
      case Craftsman:
        action = playCraftsman(player);
        break;
      case Trader:
        action = playTrader(player);
        break;
      case Prospector:
        action = playProspector(player);
        break;
      case Captain:
        throw new RuntimeException("Programming Error. Shouldnt Be Here: " + role);
      default:
        throw new RuntimeException("unrecognized role: " + role);
    }
    
    if (action == null) {
      logger.fine("Player " + player.getPosition() + " action for " + role + ": None.");
    } else {
      logger.fine("Player " + player.getPosition() + " action for " + role + ": " + action);
    }
    
    gameState.updateCurrentPlayerToDoAction();
    
    // If back to player who chose role, then all players have done action
    if (gameState.getCurrentPlayerToDoAction().equals(gameState.getCurrentPlayerToChooseRole())){
      gameState.setAllPlayersHaveDoneActionInCurrentTurn(true);
    }
  }

  private void playCaptainTurn() {
    boolean hasBeenGivenExtraVictoryPoint = false;
    int consecutivePlayersThatHaveNotDoneAction = 0;
    while (consecutivePlayersThatHaveNotDoneAction < gameState.getPlayers().size() + 1){
      Player player = gameState.getCurrentPlayerToDoAction();
      if (!canPlayerDoCaptainAction(player)) {
        consecutivePlayersThatHaveNotDoneAction++;
      } else {
        Map<Good, Set<Integer>> allowedGoodsToAllowedShipSizes = rules.getAllowedGoodsToAllowedShipSizes(gameState, player);
        Set<Good> goodsAllowedToShipOnWharf = rules.getGoodsAllowedToShipOnWharf(gameState, player);
        CaptainAction action = player.doCaptain(getGameState(), allowedGoodsToAllowedShipSizes, goodsAllowedToShipOnWharf);
        action = actionValidator.validateCaptainAction(action, getGameState(), allowedGoodsToAllowedShipSizes, goodsAllowedToShipOnWharf);

        int amountOfGoodsShipped = rules.executeCaptainAction(gameState, action, player);
        
        logger.fine(action.toString());
        
        if (player.equals(gameState.getCurrentPlayerToChooseRole()) && !hasBeenGivenExtraVictoryPoint) {
          gameState.transferVictoryPointsFromSupplyToPlayer(player, CAPTAIN_EXTRA_VICTORY_POINT);
          hasBeenGivenExtraVictoryPoint = true;
        }
        
        if (amountOfGoodsShipped == 0) {
          consecutivePlayersThatHaveNotDoneAction++;
        } else {
          consecutivePlayersThatHaveNotDoneAction = 0;
        }
      }
      gameState.updateCurrentPlayerToDoAction();      
    }  
  }
    
  private Action playProspector(Player player) {
    if (player.equals(gameState.getCurrentPlayerToChooseRole())) {
      rules.givePlayerMoneyForProspector(gameState, player);
    }
    return new ProspectorAction();
  }

  private Action playTrader(Player player) {
    Set<Good> goodsAllowedToTrade = rules.getGoodsAllowedToTrade(gameState, player);
    TraderAction action = player.doTrader(getGameState(), goodsAllowedToTrade);
    action = actionValidator.validateTraderAction(action, getGameState(), goodsAllowedToTrade);
    rules.executeTraderAction(gameState, action, player);
    return action;
  }

  private Action playCraftsman(Player player) {
    CraftsmanAction action = null;
    if (player.equals(gameState.getCurrentPlayerToChooseRole())) {
      Set<Good> goodsAllowedToChooseAsPrivilege = rules.getGoodsAllowedToChooseAsPrivilege(gameState, player);
      action = player.doCraftsman(getGameState(), goodsAllowedToChooseAsPrivilege);
      action = actionValidator.validateCraftsmanAction(action, getGameState(), goodsAllowedToChooseAsPrivilege);
      rules.executeCraftsmanAction(gameState, action, player);
    }
    return action;
  }

  private Action playBuilder(Player player) {
    Set<BuildingType> buildingsAllowedToBuy = rules.getBuildingsAllowedToBuy(gameState, player);
    BuilderAction action = player.doBuilder(getGameState(), buildingsAllowedToBuy);
    action = actionValidator.validateBuilderAction(action, getGameState(), buildingsAllowedToBuy);
    rules.executeBuilderAction(gameState, action, player);
    return action;
  }

  private Action playMayor(Player player) {
    PlayerState playerState = gameState.getPlayerState(player);
    int colonistsAllowedToOccupy = playerState.getAllColonists();
    Map<BuildingType, Integer> buildingToOccupiedCountAllowed = new HashMap<>();
    for (BuildingType buildingType : playerState.getAllBuildings()) {
      Building building = Building.getBuildingFromType(buildingType);
      buildingToOccupiedCountAllowed.put(buildingType, building.getAllowedColonists());
    }
    int quarriesAllowedToOccupy = playerState.getOccupiedQuarryCount() + playerState.getUnoccupiedQuarryCount();
    List<Good> plantationsAllowedToOccupy = playerState.getAllPlantations();
    logger.fine("plantationsAllowedToOccupy :" + plantationsAllowedToOccupy.size());
    logger.fine("colonistsAllowedToOccupy " + colonistsAllowedToOccupy);

    MayorAction action = player.doMayor(
        getGameState(),
        colonistsAllowedToOccupy,
        buildingToOccupiedCountAllowed,
        quarriesAllowedToOccupy,
        plantationsAllowedToOccupy);
    action = actionValidator.validateMayorAction(action, getGameState(), colonistsAllowedToOccupy, buildingToOccupiedCountAllowed, quarriesAllowedToOccupy, plantationsAllowedToOccupy);
    rules.executeMayorAction(gameState, action, player);

    return action;
  }

  private Action playSettler(Player player, PlayerState playerState) {
    Set<Good> plantationsAllowedToChoose = new HashSet<>(gameState.getUncoveredPlantations());
    boolean allowedToChooseQuarry =
        player.equals(gameState.getCurrentPlayerToChooseRole()) 
          || playerState.getOccupiedBuildings().contains(BuildingType.ConstructionHut);
    boolean allowedToUseHaciendaBuildingToGetExtraPlantation =
        playerState.getOccupiedBuildings().contains(BuildingType.Hacienda);
    SettlerAction action = player.doSettler(getGameState(), plantationsAllowedToChoose, allowedToChooseQuarry, allowedToUseHaciendaBuildingToGetExtraPlantation);
    action = actionValidator.validateSettlerAction(action, getGameState(), plantationsAllowedToChoose, allowedToChooseQuarry, allowedToUseHaciendaBuildingToGetExtraPlantation);
    rules.executeSettlerAction(gameState, action, player);
    return action;
  }
  
  private void endOfTurn(Role role) {
    switch(role) {
      case Settler:
        gameState.transferPlantationsFromUncoveredToDiscarded();
        gameState.rebuildUncoveredPlantations();
        break;
      case Mayor:
        rules.refillColonistShip(gameState);
        break;
      case Builder:
        break;
      case Craftsman:
        break;
      case Trader:
        if (gameState.getAllGoodsInTradingHouse().size() == 4) {
          gameState.transferGoodsFromTradingHouseToSupply();
        }
        break;
      case Prospector:
        break;
      case Captain:
        gameState.transferGoodsFromFullShipsToBoard();
      
        for (Player player : gameState.getPlayers()){
          Map<Good, Integer> goodsAllowedToKeep = rules.getGoodsAllowedToKeepAfterCaptain(gameState, player);
          int goodTypesAllowedToKeepMoreThanOneOf = rules.getGoodTypesAllowedToKeepAboveOneAfterCaptain(gameState, player);
          
          List<Good> goodsToKeep = player.chooseGoodsToKeepAfterCaptain(getGameState(), goodsAllowedToKeep, goodTypesAllowedToKeepMoreThanOneOf);
          goodsToKeep = actionValidator.validateChosenGoodsAfterCaptain(goodsToKeep, getGameState(), goodsAllowedToKeep, goodTypesAllowedToKeepMoreThanOneOf);
          
          rules.moveExtraGoodsFromPlayerToBoardForCaptain(gameState, player, goodsToKeep);
          gameState.getPlayerState(player).setHasUsedWharf(false);
        }      
        break;
      default:
        throw new RuntimeException("unrecognized role: " + role);
    }
    gameState.takeAwayFromAvailableRolesList(role);
  }

  private boolean canPlayerDoCaptainAction(Player player) {
    Map<Good, Set<Integer>> allowedGoodsToAllowedShipSizes = rules.getAllowedGoodsToAllowedShipSizes(gameState, player);
    Set<Good> goodsAllowedToShipOnWharf = rules.getGoodsAllowedToShipOnWharf(gameState, player);
    
    return !allowedGoodsToAllowedShipSizes.isEmpty() || !goodsAllowedToShipOnWharf.isEmpty();    
  }
  
}
