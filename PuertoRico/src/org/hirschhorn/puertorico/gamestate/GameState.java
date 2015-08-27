package org.hirschhorn.puertorico.gamestate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hirschhorn.puertorico.GameStats;
import org.hirschhorn.puertorico.Player;
import org.hirschhorn.puertorico.ResultsUI.Statistic;
import org.hirschhorn.puertorico.Rules;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

import com.google.common.annotations.VisibleForTesting;

public class GameState {
  private static Logger logger = Logger.getLogger(GameState.class.getName());  
  
  // Fixed for entire game
  private List<Player> players;
  private List<Role> allRoles;
  
  // State changes during game
  private BoardState boardState;
  private List<PlayerState> playerStates;
  private List<Role> availableRoles;
  private int governorPlayerNumber;
  private int currentRolePlayerNumber;
  private int currentActionPlayerNumber;
  private boolean allPlayersHaveDoneActionInCurrentTurn;

  // Experimental
  private GameStats gameStats;
  
  public GameState(GameState gameState) {
    players = new ArrayList<>();
    for (Player player : gameState.players) {
      players.add(new Player(player));
    }
    
    allRoles = new ArrayList<>(gameState.allRoles);
    availableRoles = new ArrayList<>(gameState.availableRoles);
    
    boardState = new BoardState(gameState.boardState);
    
    playerStates = new ArrayList<>();
    for (PlayerState playerState : gameState.playerStates) {
      playerStates.add(new PlayerState(playerState));
    }
    
    governorPlayerNumber = gameState.governorPlayerNumber;
    currentRolePlayerNumber = gameState.currentRolePlayerNumber;
    currentActionPlayerNumber = gameState.currentActionPlayerNumber;
    allPlayersHaveDoneActionInCurrentTurn = gameState.allPlayersHaveDoneActionInCurrentTurn;
    
    gameStats = new GameStats(gameState.gameStats);    
  }
  
  public GameState(Rules rules, List<PlayerStrategy> playerStrategies) {
    // Fixed for entire game
    initializePlayers(playerStrategies);
    int playerCount = getPlayerCount();
    allRoles = Collections.unmodifiableList(rules.getRolesForGame(playerCount));

    // State changes during game
    gameStats = new GameStats();
    playerStates = new ArrayList<>();
    for (int playerPosition = 0; playerPosition < playerCount; playerPosition++){
      playerStates.add(new PlayerState(rules, playerCount, playerPosition));
    }
    playerStates = Collections.unmodifiableList(playerStates);
    availableRoles = new ArrayList<>(allRoles);
    boardState = new BoardState(rules, playerCount, availableRoles);
    governorPlayerNumber = 0;
    currentRolePlayerNumber = 0;
    currentActionPlayerNumber = 0;
    allPlayersHaveDoneActionInCurrentTurn = true;
  }

  private void initializePlayers(List<PlayerStrategy> playerStrategies) {
    players = new ArrayList<Player>();
    for (int x = 0; x < playerStrategies.size(); x++){
      PlayerStrategy playerStrategy = playerStrategies.get(x);
      Player player = new Player(x, playerStrategy);
      players.add(player);
    }    
  }

  private int getPlayerCount() {
    return players.size();
  }
  
  public GameStats getGameStats() {
    return new GameStats(gameStats);  
  }
  
  public Number getPlayerStatistic(
      String strategyNameSimple,
      Statistic statistic) {
    return gameStats.getPlayerStatistic(strategyNameSimple, statistic);
  }
  
  public void incrementPlayerStatistic(
      String strategyNameSimple,
      Statistic statistic,
      Number value) {
    gameStats.incrementPlayerStatistic(strategyNameSimple, statistic, value);
  }
  
  public void transferQuarryFromSupplyToPlayer(Player player) {
    boardState.removeQuarryFromSupply();
    getPlayerState(player).addQuarry();
  }

  public void transferRandomPlantationFromHiddenToPlayer(Player player) {
    Good plantation = boardState.removePlantationFromHidden();
    getPlayerState(player).addUnoccupiedPlantation(plantation);
  }

  public void transferPlantationFromUncoveredToPlayer(Player player, Good chosenPlantation) {
    boolean removed = boardState.removePlantationFromUncovered(chosenPlantation);
    if (!removed) {
      throw new IllegalArgumentException("Cannot remove planation that is not there: " + chosenPlantation);
    }
    getPlayerState(player).addUnoccupiedPlantation(chosenPlantation);
  }

  public List<Player> getPlayers() {
    return players;
  }

  public int getGovernorPlayerNumber() {
    return governorPlayerNumber;
  }

  public int getCurrentRolePlayerNumer() {
    return currentRolePlayerNumber;
  }
  
  public Player getCurrentPlayerToChooseRole() {
    return players.get(currentRolePlayerNumber);
  }

  public void transferGoodFromPlayerToTradingHouse(Good chosenGood, Player player) {
    getPlayerState(player).removeGoods(chosenGood, 1);
    boardState.addGoodToTradingHouse(chosenGood);
  }

  public void transferMoneyFromSupplyToPlayer(int value, Player player) {
    getPlayerState(player).addMoney(value);
    // No need to remove money from supply -- assume supply has infinite
    
    gameStats.incrementPlayerStatistic(
        player.getStrategyNameSimple(),
        Statistic.CumulativeMoneyReceived,
        value);
  }

  public void occupyPlayerBuildings(Map<BuildingType, Integer> map, Player player) {
    getPlayerState(player).occupyBuildings(map);    
  }

 
  public void occupyPlayerPlantation(List<Good> occupiedPlantations, Player player) {
    getPlayerState(player).occupyPlantations(occupiedPlantations);
  }

  public void occupyPlayerQuarries(int occupiedQuarryCount, Player player) {
    getPlayerState(player).occupyQuarries(occupiedQuarryCount);
  } 

  public void takeAwayExtraColonistGivenAsPrivilege(Player player) {
    getPlayerState(player).takeAwayColonist();
    boardState.addColonistToSupply();
  }

  public int getOccupiedQuarryCount(Player player) {
    return getPlayerState(player).getOccupiedQuarryCount();
  }

  public void removePlayerMoney(int count, Player player) {
    getPlayerState(player).removeMoney(count);
  }

  public void transferBuildingFromSupplyToPlayer(BuildingType building, Player player) {
    boardState.removeAvailableBuilding(building);
    getPlayerState(player).addBuilding(building);
  }

  public void transferVictoryPointsFromSupplyToPlayer(Player player, int count) {
    boardState.takeVictoryPointsFromSupply(count);
    getPlayerState(player).addVictoryPointChips(count);
    logger.fine("Transfering " + count + " victory points from supply to player " + player.getPosition());    
  }

  public void removeGoodsFromPlayer(Player player, Good good, int count) {
    getPlayerState(player).removeGoods(good, count);
  }

  public List<Good> getPlayerGoods(Player player) {
    return new ArrayList<>(getPlayerState(player).getGoods());
  }
  
  public int getGoodCount(Player player, Good good) {
    return getPlayerState(player).getGoodCount(good);
  }

  public int getShipFilledCount(int chosenShipSize) {
    return boardState.getShipState(chosenShipSize).getFilledCount();
  }

  public void addGoodsToSupply(Good good, int numberOfGoods) {
    boardState.addGoodsToSupply(good, numberOfGoods);
  }
  
  public void addGoodsToShip(int chosenShipSize, Good goodToShip, int amountOfGoods) {
    boardState.addGoodsToShip(chosenShipSize, goodToShip, amountOfGoods);
  }

  public int transferGoodsFromSupplyToPlayer(List<Good> goods, Player player) {
    int goodsTaken = 0;
    for (Good good : goods) {
      goodsTaken += transferGoodsFromSupplyToPlayer(good, 1, player);
    }
    return goodsTaken;
  }
   
  public int transferGoodsFromSupplyToPlayer(Good good, int count, Player player) {
    int goodsTransferred = boardState.takeGoodsFromSupply(good, count);
    if (goodsTransferred > 0) {
      getPlayerState(player).addGoods(good, goodsTransferred);
    }

    gameStats.incrementPlayerStatistic(
        player.getStrategyNameSimple(),
        Statistic.CumulativeGoodsProduced,
        goodsTransferred);
    
    return goodsTransferred;
  }

  public void transferGoodsFromPlayerToSupply(Player player, List<Good> goods) {
    for (Good good : goods) {
      transferGoodsFromPlayerToSupply(player, good, 1);
    }
  }
  
  public void transferAllGoodsFromPlayerToSupply(Player player) {
    for (Good good : getPlayerGoods(player)){
      boardState.addGoodsToSupply(good, 1);
    }
    getPlayerState(player).removeAllGoods();
  }

  public void updateCurrentPlayerToChooseRole() {
    currentRolePlayerNumber++;
    if (currentRolePlayerNumber >= players.size()){
      currentRolePlayerNumber = 0;
    }
    
  }

  public List<Player> getPlayersStartingAtCurrentPlayerToChooseRole() {
    List<Player> players = new ArrayList<>();
    players.addAll(getPlayers().subList(currentRolePlayerNumber, getPlayers().size()));
    players.addAll(getPlayers().subList(0, currentRolePlayerNumber));
    return players;
  }

  public Player getCurrentPlayerToDoAction() {
    int position = currentActionPlayerNumber;
    return players.get(position);
  }

  public void updateCurrentPlayerToDoAction() {
    currentActionPlayerNumber++;
    if (currentActionPlayerNumber >= players.size()){
      currentActionPlayerNumber = 0;
    }
  }

  public void transferGoodsFromFullShipsToBoard() {
    Map<Integer, Good> shipSizeToGood = boardState.getFilledShipSizeToGoodType();
    for (Map.Entry<Integer, Good> entry : shipSizeToGood.entrySet()) {
      Integer size = entry.getKey();
      Good good = entry.getValue();
      addGoodsToSupply(good, size);
      boardState.clearShip(size);
    }
  }

  public int getSupplyVictoryPoints() {
    return boardState.getSupplyVictoryPoints();
  }

  public int getSupplyColonists() {
    int supplyColonists = boardState.getSupplyColonists();
    return supplyColonists;
  }

  public List<PlayerState> getPlayerStates() {
    return new ArrayList<>(playerStates);
  }

  public List<Role> getAvailableRoles() {
    return new ArrayList<>(availableRoles);
  }

  public int getAvailableCount(Good good) {
    return boardState.getAvailableCount(good);
  }
  
  public void updateGovernor() {
    governorPlayerNumber++;
    if (governorPlayerNumber >= players.size()){
      governorPlayerNumber = 0;
    }
  }

  public void addCoinsToUnusedRoles() {
    for (Role role : availableRoles) {
      boardState.addOneMoneyToRole(role);
    }
  }

  public void resetRoles() {
    availableRoles = new ArrayList<>(allRoles); 
  }

  public void takeAwayFromAvailableRolesList(Role role) {
    availableRoles.remove(role);
  }

  public boolean hasMoneyOnRole(Role role) {
    return boardState.hasMoneyOnRole(role);
  }
  
  public int getMoneyOnRole(Role role){
    return boardState.getMoneyOnRole(role);
  }

  public void takeAllMoneyFromRole(Role role) {
    boardState.takeAllMoneyFromRole(role);
  }

  public boolean playerHasGoods(Player player) {
    if (getPlayerState(player).hasGoods()) {
      return true;
    }
    return false;
  }
  
  public PlayerState getPlayerState (Player player) {
    // TODO: return copy instead of actual player state. But before doing,
    //   need to fix a lot of code that depends on this not being a copy
    return playerStates.get(player.getPosition());
  }

  public boolean hasOccupiedWharf(Player player) {
    for (BuildingType building : getPlayerState(player).getOccupiedBuildings()) {
      if (building.equals(BuildingType.Wharf)) {
        return true;
      }
    }
    return false;
  }

  public Set<Good> getTypesOfGoodsPlayerHas(Player player) {
    return new HashSet<>(getPlayerGoods(player));
  }

  public boolean hasUsedWharf(Player player) {
    return getPlayerState(player).hasUsedWharf();
  }

  public void setHasUsedWharf(Player player, boolean hasUsedWharf) {
    getPlayerState(player).setHasUsedWharf(hasUsedWharf);
  }

  public int getOccupiedColonistCount(BuildingType building, Player player) {
    return getPlayerState(player).getOccupiedCountOfBuilding(building);
  }

  public int getOccupiedPlantationCount(Good good, List<Good> plantations) {
    List<Good> goods = new ArrayList<Good>();
    for (Good plantation : plantations) {
      if (plantation.equals(good)) {
        goods.add(plantation);    
      }
    }
    return goods.size();
  }

  public int getColonistsOnShipCount() {
    return boardState.getShipColonists();
  }

  public void transferColonistsFromShipToPlayer(Player player, int count) {
    int newColonistPlayerCount = getPlayerState(player).getColonistsOnSanJuan() + count;
    getPlayerState(player).setColonistsOnSanJuan(newColonistPlayerCount);
    int newColonistShipCount = boardState.getShipColonists() - count;
    boardState.setShipColonists(newColonistShipCount);
  }

  public void transferColonistFromSupplyToPlayer(Player player) {
    int newColonistPlayerCount = getPlayerState(player).getColonistsOnSanJuan() + 1;
    getPlayerState(player).setColonistsOnSanJuan(newColonistPlayerCount);
    int newColonistSupplyCount = boardState.getSupplyColonists() - 1;
    boardState.setSupplyColonists(newColonistSupplyCount);
    logger.fine("Colonists that should be in supply" + newColonistSupplyCount);
    logger.fine("Colonists that actually in supply" + boardState.getSupplyColonists());
  }

  public void moveAllColonistsToSanJuan(Player player) {
    getPlayerState(player).moveAllColonistsToSanJuan();
  }

  public void transferPlantationsFromUncoveredToDiscarded() {
    boardState.transferPlantationFromUncoveredToDiscarded();
  }

  public void rebuildUncoveredPlantations() {
    boardState.rebuildUncoveredPlantations(players); 
  }
  
  public boolean doesPlayerHaveOccupiedBuiling (BuildingType buildingType, Player player){
    return getPlayerState(player).doesPlayerHaveOccupiedBuilding(buildingType);
  }

  public void transferOccupiedPlantationFromUncoveredPlantationsToPlayer(Player player, Good chosenPlantation) {
    boardState.removePlantationFromUncovered(chosenPlantation);
    getPlayerState(player).addPlantationToOccupiedList(chosenPlantation);
    int newSupplyColonists = boardState.getSupplyColonists() - 1;
    boardState.setSupplyColonists(newSupplyColonists);
  }

  public List<Good> getUncoveredPlantations() {
    return boardState.getUncoveredPlantations();
  }

  public boolean getAllPlayersHaveDoneActionInCurrentTurn() {
    return allPlayersHaveDoneActionInCurrentTurn;
  }

  public boolean setAllPlayersHaveDoneActionInCurrentTurn(boolean value) {
    return allPlayersHaveDoneActionInCurrentTurn = value;
  }

  public Set<Good> getGoodsAlreadyBeingShipped() {
    return boardState.getGoodsAlreadyBeingShipped();
  }

  public List<Good> getAllGoodsInTradingHouse() {
    return boardState.getAllGoodsInTradingHouse();
  }

  public Set<Good> getUniqueGoodsInTradingHouse() {
    return boardState.getUniqueGoodsInTradingHouse();
  }

  public Set<BuildingType> getAvailableBuildings() {
    return boardState.getAvailableBuildings();
  }

  // TODO: remove methods that return ShipState.  See TO DO in BoardState
  public ShipState getShipStateForGood(Good good) {
    return boardState.getShipStateForGood(good);
  }
  
  // TODO: remove methods that return ShipState.  See TO DO in BoardState
  public Set<ShipState> getEmptyShipStatesWithCapacityAtLeast(int goodCount) {
    return boardState.getEmptyShipStatesWithCapacityAtLeast(goodCount);
  }

  // TODO: remove methods that return ShipState.  See TO DO in BoardState
  public ShipState getLargestEmptyShipState() {
    return boardState.getLargestEmptyShipState();
  }

  public void setCurrentPlayerToChooseRoleToBeGovernor() {
    currentRolePlayerNumber = governorPlayerNumber;    
  }

  public void setCurrentPlayerToDoActionToBeCurrentRolePlayer() {
    currentActionPlayerNumber = currentRolePlayerNumber;    
  }

  public boolean hasAtLeastOneEmptyShip() {
    return getLargestEmptyShipState() != null;
  }

  public boolean hasNonFullShipForGood(Good good) {
    return boardState.hasNonFullShipForGood(good);
  }

  public void transferColonistsFromSupplyToShip(int count) {
    boardState.setSupplyColonists(getSupplyColonists() - count);
    boardState.setShipColonists(boardState.getShipColonists() + count);
  }

  public void removeColonistsFromSanJuan(int count, Player player) {
    getPlayerState(player).removeColonistsFromSanJuan(count);
  }

  public List<Good> getCoveredPlantation() {
    return boardState.getCoveredPlantations();
  }

  public List<Good> getDiscardedPlantations() {
    return boardState.getDiscardedPlanatations();
  }

  public void transferGoodsFromTradingHouseToSupply() {
    for (Good good : boardState.getAllGoodsInTradingHouse()) {
      addGoodsToSupply(good, 1);      
    }
    boardState.clearTradingHouse();
  }

  @VisibleForTesting
  public void setColonistsOnShip(int colonists) {
    boardState.setShipColonists(colonists);
  }

  public void transferGoodsFromPlayerToSupply(Player player, Good goodType, int amount) {
    removeGoodsFromPlayer(player, goodType, amount);
    addGoodsToSupply(goodType, amount);
  }

  public void transferGoodsFromPlayerToShip(Player player, Good goodType, int amount, int shipSize) {
    removeGoodsFromPlayer(player, goodType, amount);
    addGoodsToShip(shipSize, goodType, amount);
  }

  public Player getPlayerWithMostVictoryPoints() {
    Player highestPlayer = null;
    int highestVictoryPoints = 0;
    for (Player player : getPlayers()) {
      PlayerState playerState = getPlayerState(player);
      if (playerState.getVictoryPoints() > highestVictoryPoints) {
        highestVictoryPoints = playerState.getVictoryPoints();
        highestPlayer = player;
      }
    }
    return highestPlayer;
  }

  public List<Role> getAllRoles() {
    return new ArrayList<>(allRoles);
  }

  public int getPlayerMoney(Player player) {
    return getPlayerState(player).getMoney();
  }

}
