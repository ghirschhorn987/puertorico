package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;

import org.hirschhorn.puertorico.ResultsUI.Statistic;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.OrderedRolePlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.RandomRolePlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.ariella.AriellaPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.dad.DadStrategy;
import org.hirschhorn.puertorico.playerstrategies.sam.SamSecondStrategy;
import org.hirschhorn.puertorico.playerstrategies.sam.SamStrategy;
import org.hirschhorn.puertorico.playerstrategies.zack.ZackStragety;

import com.google.common.collect.Collections2;

public class PuertoRico extends Application {

  private static final Logger logger = Logger.getLogger(PuertoRico.class.getName());
    
  //TODO: Remove hacky way of using static to show results after learing more about JavaFX
  private static ResultsUI resultsUI;
  
  public static void main(String[] args) {
    PuertoRico puertoRico = new PuertoRico();
    puertoRico.run(args);
  }
  
  public void run(String[] args) {
    List<PlayerStrategy> strategies;
//    strategies = processArgs(args);
//    strategies = getRandomRoleStrategies();
//    strategies = getOrderedRoleStrategies();
//    strategies = getDadPlayerStrategies();
    strategies = getMixedPlayerStrategies();
    int playerCount = 4;
    int gamesPerSession = 1;
//    int gamesPerSession = 25;
    int maxTotalGames = 10000;
    play(strategies, playerCount, gamesPerSession, maxTotalGames, args);
  }
  
  private void play(
      List<PlayerStrategy> strategies,
      int playerCount,
      int gamesPerSession,
      int maxTotalGames,
      String[] fxArgs) {
    Rules rules = new Rules();
    List<GameResult> gameResults = new ArrayList<>();

    Comparator<PlayerStrategy> comparator = new Comparator<PlayerStrategy>() {
        @Override
        public int compare(PlayerStrategy o1, PlayerStrategy o2) {
          return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
      };
    Collection<List<PlayerStrategy>> strategiesPermuatations = Collections2.orderedPermutations(strategies, comparator);
    logger.info("Playing with " + strategiesPermuatations.size() + " permutations of " + strategies.size() + " strategies.");
    
    //for (int session = 0; session < numberOfSessions; session++) {
    //  if (shuffleBetweenSessions) {
    //    Collections.shuffle(strategies);
    //  }
    //  gameResults.addAll(playSession(session, gamesPerSession, strategies, rules));
    //}
    int session = 0;
    for (List<PlayerStrategy> strategiesPermutation : strategiesPermuatations) {
      List<PlayerStrategy> strategiesUsedInThisGame = strategiesPermutation.subList(0, playerCount);
      gameResults.addAll(playSession(session, gamesPerSession, strategiesUsedInThisGame, rules));
      session++;
      if (gameResults.size() > maxTotalGames) {
        logger.warning("Exceeeded max total games limit of " + maxTotalGames + ". Stopping after " + gameResults.size() + " games.");
        break;
      }
    }
    
    // Use JavaFX to show results
    resultsUI = new ResultsUI(gameResults);
    launch(fxArgs);
  }

  /**
   * Java FX method to show results
   */
  @Override
  public void start(Stage stage) throws Exception {
    resultsUI.displayCharts(stage);
  }  
  
  private List<GameResult> playSession(
      int session,
      int gamesInSession,
      List<PlayerStrategy> strategies,
      Rules rules) {

    List<GameResult> gameResults = new ArrayList<>();
    List<AtomicInteger> victories = new ArrayList<>();
    for (int i = 0; i < strategies.size(); i++) {
      victories.add(new AtomicInteger(0));
    }
    
    for (int game = 0; game < gamesInSession; game++) {
      GameState gameState = new GameState(rules, strategies);
      GameExecutor gameExecutor = new GameExecutor(rules, gameState);
      gameExecutor.playGame();

      //TODO: Add empty player results for players who are not playing so that graphs line up correctly
      GameResult gameResult = buildGameResult(game, gameState);

      gameResults.add(gameResult);
      
      Player player = gameState.getPlayerWithMostVictoryPoints();
      logger.fine("Session: " + session + " Game " + game + ": Player " + player.getStrategyNameSimple() + " wins with " + gameState.getPlayerState(player).getVictoryPoints());
      AtomicInteger playerWins = victories.get(player.getPosition());
      playerWins.incrementAndGet();
      logger.fine(player + " wins " + playerWins + " times");
    }
      
    logger.fine("=========================================");
    for (int i = 0; i < strategies.size(); i++) {
      PlayerStrategy strategy = strategies.get(i);
      logger.fine("Player " + strategy.getClass().getSimpleName()  + ": " + victories.get(i) + " wins");
    }
    
    return gameResults;
  }

  private GameResult buildGameResult(int game, GameState gameState) {    
    List<Integer> victoryPointList = new ArrayList<>();
    for (Player player : gameState.getPlayers()) {
      victoryPointList.add(gameState.getPlayerState(player).getVictoryPoints());
    }
    Collections.sort(victoryPointList);
    int firstPlace = victoryPointList.get(victoryPointList.size() - 1);
    int secondPlace = victoryPointList.get(victoryPointList.size() - 2);

    GameResult gameResult = new GameResult();
    for (Player player : gameState.getPlayers()) {
      PlayerState ps = gameState.getPlayerState(player);
      String strategyName = player.getStrategyNameSimple();
      PlayerResult playerResult = new PlayerResult();
      playerResult.setValue(ResultsUI.Statistic.Wins, game);
      playerResult.setValue(ResultsUI.Statistic.Position, player.getPosition());
      playerResult.setValue(ResultsUI.Statistic.VictoryPoints, ps.getVictoryPoints());
      playerResult.setValue(ResultsUI.Statistic.VictoryPointChips, ps.getVictoryPointChips());
      playerResult.setValue(ResultsUI.Statistic.VictoryPointBonus, ps.getVictoryPointBonus());
      playerResult.setValue(ResultsUI.Statistic.HasWharf, ps.getAllBuildings().contains(BuildingType.Wharf) ? 1 : 0);
      playerResult.setValue(ResultsUI.Statistic.HasHarbor, ps.getAllBuildings().contains(BuildingType.Harbor) ? 1 : 0);
      playerResult.setValue(ResultsUI.Statistic.LargeBuldings, ps.getAllLargeBuildings().size());
      playerResult.setValue(ResultsUI.Statistic.LargeBuildingsOccupied, ps.getAllLargeBuildingsOccupied().size());
      
      int cumulativeGoods = 0;
      int cumulativeMoney = 0;
      int moneyAfterRound8 = 0;
      PlayerStats playerStats = gameState.getGameStats().getPlayerStats(strategyName);
      if (playerStats != null) {
        cumulativeGoods = playerStats.getValue(Statistic.CumulativeGoodsProduced).intValue();
        cumulativeMoney = playerStats.getValue(Statistic.CumulativeMoneyReceived).intValue();
        moneyAfterRound8 = playerStats.getValue(Statistic.MoneyAfterRound8).intValue();
      }
      playerResult.setValue(ResultsUI.Statistic.CumulativeGoodsProduced, cumulativeGoods);        
      playerResult.setValue(ResultsUI.Statistic.CumulativeMoneyReceived, cumulativeMoney);        
      playerResult.setValue(ResultsUI.Statistic.MoneyAfterRound8, moneyAfterRound8);
      
      int victoryPoints = ps.getVictoryPoints();
      double victoryMarginPct;
      if (victoryPoints == firstPlace) {
        // How far ahead of second place?
        victoryMarginPct = (double)firstPlace / (double)secondPlace;
      } else {
        // How far behind winner?
        victoryMarginPct = (double)victoryPoints / (double)firstPlace;
      }
      playerResult.setValue(ResultsUI.Statistic.Wins, ps.getVictoryPoints() == firstPlace ? 1 : 0);
      playerResult.setValue(ResultsUI.Statistic.VictoryMarginPct, victoryMarginPct);
      
      gameResult.putPlayerResult(strategyName, playerResult);
    }
    
    return gameResult;
  }

  private List<PlayerStrategy> getRandomRoleStrategies() {
    return Collections.nCopies(4, (PlayerStrategy) new RandomRolePlayerStrategy());
  }

  private List<PlayerStrategy> getOrderedRoleStrategies() {
    return Collections.nCopies(4, (PlayerStrategy) new OrderedRolePlayerStrategy());
  }
  
  private List<PlayerStrategy> getDadPlayerStrategies() {
    return Collections.nCopies(4, (PlayerStrategy) DadStrategy.getDefaultDadStrategy());    
  }
  
  private List<PlayerStrategy> getMixedPlayerStrategies() {
    List<PlayerStrategy> strategies = new ArrayList<>();
//    strategies.add(new RandomRolePlayerStrategy());
    strategies.add(new RandomRolePlayerStrategy());
    strategies.add(new AriellaPlayerStrategy());
    strategies.add(DadStrategy.getDefaultDadStrategy());
    strategies.add(new ZackStragety());
//    strategies.add(new SamStrategy());
    strategies.add(new SamSecondStrategy());
    return strategies;
  }

  private List<PlayerStrategy> getPlayerStrategiesFromClassNames(List<String> classNames) {
    List<PlayerStrategy> strategies = new ArrayList<>();
    for (String className : classNames) {
      Class<?> strategyClass;
      try {
        strategyClass = Class.forName(className);
        Object strategy = strategyClass.newInstance();
        if (!(strategy instanceof PlayerStrategy)) {
          throw new IllegalArgumentException("Unrecogonized PlayerStrategy class: " + className);
        }
        strategies.add((PlayerStrategy) strategy);
      } catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return strategies;
  }
  
  private List<PlayerStrategy> processArgs(String[] args) {
    int playerCount = 4;
    List<PlayerStrategy> playerStrategies = new ArrayList<>();
    for (String arg : args) {
      String argName = arg.split("=")[0];
      String argValue = arg.split("=")[1];
      
      switch (argName.toLowerCase()) {
        case "strategies" :
          List<String> strategyNames = Arrays.asList(argValue.split(","));
          playerStrategies.addAll(getPlayerStrategiesFromClassNames(strategyNames));
          break;
        case "players" :
          playerCount = Integer.parseInt(argValue);
          break;
        default: 
          throw new IllegalArgumentException("Unrecogonized option: " + argName + "=" + argValue);
      }
    }
    
    // Add default strategies for remaining players if not enough strategies specified
    while (playerStrategies.size() < playerCount) {
      playerStrategies.add(new DefaultPlayerStrategy());
    }
    
    return playerStrategies;
  }
}
