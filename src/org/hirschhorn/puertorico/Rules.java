package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hirschhorn.puertorico.actions.BuilderAction;
import org.hirschhorn.puertorico.actions.CaptainAction;
import org.hirschhorn.puertorico.actions.CraftsmanAction;
import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.actions.SettlerAction;
import org.hirschhorn.puertorico.actions.TraderAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.gamestate.ShipState;

public class Rules {
  private static Logger logger = Logger.getLogger(Rules.class.getName());
  
  private static final int PROSPECTOR_MONEY = 1;
  
  //TODO changed to public and static
  private Building getBuildingFromType(BuildingType buildingType) {
    return Building.getBuildingFromType(buildingType);
  }

  public void executeSettlerAction(GameState gameState,
      SettlerAction settlerAction, Player player) {
    if (settlerAction.isUseHaciendaBuildingToGetExtraPlantation()
        && gameState.getPlayerState(player).isThereSpaceOnIsland()) {
      gameState.transferRandomPlantationFromHiddenToPlayer(player);
      logger.fine("Recieved random plantation");
    }
    if (settlerAction.isChooseQuarryInstead()
        && gameState.getPlayerState(player).isThereSpaceOnIsland()) {
      gameState.transferQuarryFromSupplyToPlayer(player);
      logger.fine("Recieved Quarry");
    } else if (gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.Hospice) 
        && gameState.getPlayerState(player).isThereSpaceOnIsland()) {
      gameState.transferOccupiedPlantationFromUncoveredPlantationsToPlayer(player, settlerAction.getChosenPlantation());
      logger.fine("Recieved " + settlerAction.getChosenPlantation());
   } else if (gameState.getPlayerState(player).isThereSpaceOnIsland()){
      gameState.transferPlantationFromUncoveredToPlayer(player,
      settlerAction.getChosenPlantation());
      logger.fine("Recieved " + settlerAction.getChosenPlantation());
    }
  }  


  public void executeCraftsmanAction(GameState gameState, CraftsmanAction craftsmanAction, Player player) {
    if (player.equals(gameState.getCurrentPlayerToChooseRole())) {
      Good chosenGood = craftsmanAction.getChosenGoodForPrivilge();
      if (chosenGood != null) {
        gameState.transferGoodsFromSupplyToPlayer(chosenGood, 1, player);
      }
    }
  }

  public void givePlayerGoodsDuringCraftsman(GameState gameState, Player player) {
    List<Good> goodsPlayerReceived = new ArrayList<>();
    List<Good> goods = getGoodsAbleToProduce(gameState, player);
    logger.fine("Goods Player " + player.getPosition() + " Able To Produce: " + goods);
    gameState.transferGoodsFromSupplyToPlayer(goods, player);

    // For factory, only need types of good received, not total amount
    if (gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.Factory)) {
      Set<Good> goodTypes = new HashSet<>(goodsPlayerReceived);
      switch (goodTypes.size()){
      case 2:
        gameState.transferMoneyFromSupplyToPlayer(1, player);
        break;
      case 3:
        gameState.transferMoneyFromSupplyToPlayer(2, player);
        break;
      case 4:
        gameState.transferMoneyFromSupplyToPlayer(3, player);
        break;
      case 5:
        gameState.transferMoneyFromSupplyToPlayer(5, player);
        break;
      }
    }
  }

  public void executeTraderAction(GameState gameState,
      TraderAction traderAction, Player player) {
    Good chosenGood = traderAction.getChosenGood();
    if (chosenGood != null) {
      gameState.transferGoodFromPlayerToTradingHouse(chosenGood, player);
      gameState.transferMoneyFromSupplyToPlayer(chosenGood.getValue(), player);
      if (gameState.doesPlayerHaveOccupiedBuiling(BuildingType.SmallMarket, player) && chosenGood != null){
        gameState.transferMoneyFromSupplyToPlayer(1, player);
      } 
      if (gameState.doesPlayerHaveOccupiedBuiling(BuildingType.LargeMarket, player) && chosenGood != null){
        gameState.transferMoneyFromSupplyToPlayer(2, player);
      }
    }
  }

  public void givePlayerMoneyForProspector(GameState gameState, Player player) {
    gameState.transferMoneyFromSupplyToPlayer(PROSPECTOR_MONEY, player);
  }

  public void executeMayorAction(GameState gameState, MayorAction mayorAction, Player player) {
 //   logger.fine("Before " + gameState.getPlayerState(player).getOccupiedPlantations().size());
    gameState.moveAllColonistsToSanJuan(player);
 //   logger.fine("After Clearing " + gameState.getPlayerState(player).getOccupiedPlantations().size());
    gameState.occupyPlayerQuarries(mayorAction.getOccupiedQuarryCount(), player);
    gameState.removeColonistsFromSanJuan(mayorAction.getOccupiedQuarryCount(), player);
    gameState.occupyPlayerBuildings(mayorAction.getBuildingToOccupiedCount(), player);
    int colonistsOnBuildings = 0;
    for (Integer value : mayorAction.getBuildingToOccupiedCount().values()){
      colonistsOnBuildings = colonistsOnBuildings + value;
    }
    gameState.removeColonistsFromSanJuan(colonistsOnBuildings, player);
    gameState.occupyPlayerPlantation(mayorAction.getOccupiedPlantations(), player);
    gameState.removeColonistsFromSanJuan(mayorAction.getOccupiedPlantations().size(), player);
 //   logger.fine("After " + gameState.getPlayerState(player).getOccupiedPlantations().size());
  }
  
  public void executeBuilderAction(GameState gameState, BuilderAction builderAction, Player player) {
    BuildingType buildingToBuy = builderAction.getBuildingToBuy();
    if (buildingToBuy == null) {
      return;
    }
    
    Building building = getBuildingFromType(buildingToBuy);
    int buildingCost = building.getCost();
    int roleDiscount = 0;
    if (gameState.getCurrentPlayerToChooseRole().equals(player)) {
      roleDiscount = 1;
    };
    int quarryDiscount = Math.min(gameState.getOccupiedQuarryCount(player), building.getQuarryDiscount());
    int cost = buildingCost - roleDiscount - quarryDiscount;
    gameState.removePlayerMoney(cost, player);
    gameState.transferBuildingFromSupplyToPlayer(buildingToBuy, player);
 }

  public int executeCaptainAction(GameState gameState, CaptainAction captainAction, Player player) {
    Good goodToShip = captainAction.getGoodToShip();
    if (goodToShip == null) {
      // Nothing to do
      return 0;
    }
    
    int amountOfGoods;
    if (captainAction.isUsingWharf()) {
      amountOfGoods = gameState.getGoodCount(player, goodToShip);
      gameState.transferGoodsFromPlayerToSupply(player, goodToShip, amountOfGoods);
      gameState.setHasUsedWharf(player, true);
    } else {
      int amountLeftInShip = captainAction.getChosenShipSize() - gameState.getShipFilledCount(captainAction.getChosenShipSize());
      amountOfGoods = Math.min(gameState.getGoodCount(player, goodToShip), amountLeftInShip);
      gameState.transferGoodsFromPlayerToShip(player, goodToShip, amountOfGoods, captainAction.getChosenShipSize());
    }
    
    gameState.transferVictoryPointsFromSupplyToPlayer(player, amountOfGoods);  
    
    if (gameState.doesPlayerHaveOccupiedBuiling(BuildingType.Harbor, player)){
      gameState.transferVictoryPointsFromSupplyToPlayer(player, 1);
    }
    logger.fine("Player " + player.getPosition() + " shipped " + amountOfGoods + " " + captainAction.getGoodToShip());
    return amountOfGoods;
  } 
 
  public void moveExtraGoodsFromPlayerToBoardForCaptain(GameState gameState, Player player, List<Good> goodsToKeepInCaptain) {
    List<Good> goodsToTransfer = new ArrayList<>(gameState.getPlayerGoods(player));
    for (Good good : goodsToKeepInCaptain) {
      goodsToTransfer.remove(good);
    }
    gameState.transferGoodsFromPlayerToSupply(player, goodsToTransfer);
  }

  public List<Good> getGoodsAbleToProduce(GameState gameState, Player player) {
    List<Good> plantations = new ArrayList<>(gameState.getPlayerState(player).getOccupiedPlantations()); 
    List<Good> goods = new ArrayList<>();
    for (BuildingType buildingType : gameState.getPlayerState(player).getOccupiedBuildings()) {
      Building building = getBuildingFromType(buildingType);
      if (building.isProduction()) {
        int colonists = gameState.getOccupiedColonistCount(buildingType, player);
        Good good = building.getProductionGood();
        int plantationsOfSpecificGood = gameState.getOccupiedPlantationCount(good, plantations);
        int goodsAbleToProduce = Math.min(plantationsOfSpecificGood, colonists);
        for (int x=0; x < goodsAbleToProduce; x++) {
          goods.add(good);
          plantations.remove(good);
        }
      } 
    }
    for (Good good : gameState.getPlayerState(player).getOccupiedPlantations()) {
      if (good.equals(Good.Corn)) {
        goods.add(good);
      }
    }
    return goods;
  }


  public void givePlayersColonistsDuringMayor(GameState gameState) {
    int colonistsToGiveToAllPlayers = gameState.getColonistsOnShipCount()/gameState.getPlayers().size();
    for (Player player : gameState.getPlayers()){
      gameState.transferColonistsFromShipToPlayer(player, colonistsToGiveToAllPlayers);
      logger.fine("Player " + player.getPosition() + "got number of colonists:" + colonistsToGiveToAllPlayers);
    }
    
    for (int x = 0; x < gameState.getColonistsOnShipCount(); x++) {
      Player playerGive = gameState.getPlayersStartingAtCurrentPlayerToChooseRole().get(x);
      gameState.transferColonistsFromShipToPlayer(playerGive, 1);
      logger.fine("Player " + playerGive.getPosition() + "got number of colonists:" + 1);
    }
  }
  
  public List<Role> getRolesForGame(int playerCount){
    List<Role> list = new ArrayList<>();
    switch (playerCount){
    case 5:
      list.add(Role.Prospector);
    case 4:
      list.add(Role.Prospector);
    case 3:
      list.add(Role.Settler);
      list.add(Role.Mayor);
      list.add(Role.Builder);
      list.add(Role.Craftsman);
      list.add(Role.Trader);
      list.add(Role.Captain);  
      break;
    default:
      throw new RuntimeException("Illegal Player Count: " + playerCount);
    }
    return list;
  }

  public boolean isGameOver(GameState gameState) {
    boolean noMoreVictoryPoints = gameState.getSupplyVictoryPoints() <= 0;
    boolean noMoreSupplyColonists = gameState.getSupplyColonists() <= 0;
    boolean buildingSpaceFilledUp = isBuildingSpaceFilledUpForAnyPlayer(gameState);
    
    if (noMoreVictoryPoints || noMoreSupplyColonists || buildingSpaceFilledUp) {
      logger.fine(
          "Conditions met to end game. "
          + "  supplyVictoryPoints: " + gameState.getSupplyVictoryPoints()
          + "  supplyColonists: " + gameState.getSupplyColonists()
          + "  buildingSpaceFilledUp: " + buildingSpaceFilledUp);
      return true;
    }
    
    return false;
  }
  
  public boolean isBuildingSpaceFilledUpForAnyPlayer(GameState gameState) {
    for (PlayerState playerState : gameState.getPlayerStates()){
      int occupiedIslandSpaces = 0;
      for (BuildingType buildingType : playerState.getOccupiedBuildings()){
        Building building = getBuildingFromType(buildingType);
        occupiedIslandSpaces = occupiedIslandSpaces + building.getIslandSpacesFilled();
      }
      for (BuildingType buildingType : playerState.getUnoccupiedBuildings()){
        Building building = getBuildingFromType(buildingType);
        occupiedIslandSpaces = occupiedIslandSpaces + building.getIslandSpacesFilled();
      }
      if (occupiedIslandSpaces == 12){
        return true;
      }
    }
    return false;
  }
  
  public boolean isRoundOver(GameState gameState) {
    return gameState.getAvailableRoles().size() == 3;
  }


  public void transferMoneyFromRoleToPlayer(GameState gameState, Role role, Player player) {
    gameState.transferMoneyFromSupplyToPlayer(gameState.getMoneyOnRole(role), player);
    gameState.takeAllMoneyFromRole(role);
  }

  public void giveBonusVictoryPoints(GameState gameState, Player player) {
    int victoryPoints = determineVictoryPointBonus(gameState, player);
    gameState.getPlayerState(player).addVictoryPointBonus(victoryPoints);
    logger.fine("Giving player " + player.getPosition() + " " + victoryPoints + " bonus victory points.");
  }
  
  public int determineVictoryPointBonus(GameState gameState, Player player) {
    int victoryPoints = 0;
    PlayerState playerState = gameState.getPlayerState(player);
    
    // Get victory points from buildings, occupied or not
    for (BuildingType buildingType : playerState.getAllBuildings()){
      victoryPoints += getBuildingFromType(buildingType).getVictoryPoints();
    }
    
    // Get victory points from the five large violet buildings only if occupied
    for (BuildingType buildingType : playerState.getOccupiedBuildings()){
      switch (buildingType) {
      case GuildHall:
        for (BuildingType building : playerState.getAllBuildings()) {
          if (getBuildingFromType(building).isLargeProductionBuilding()) {
             victoryPoints += 2; 
          }
          if (getBuildingFromType(building).isSmallProductionBuilding()) {
            victoryPoints++;
          }
        }
        break;

      case Fortress:
        int colonistsOnBuildings = 0;
        for (BuildingType building : playerState.getOccupiedBuildings()){
          colonistsOnBuildings = colonistsOnBuildings + 
             playerState.getOccupiedCountOfBuilding(building);
        }
        int victoryPointsFromFortress = playerState.getColonistsOnSanJuan() + colonistsOnBuildings;
        victoryPoints += (victoryPointsFromFortress/3);
        break;
        
      case Residence:
        switch(playerState.getAllPlantationsCount()){
        case 12:
          victoryPoints +=7;
          break;
        case 11:
          victoryPoints +=6;
          break;
        case 10:
          victoryPoints +=5;
          break;
        default:
          victoryPoints +=4;
          break;
        }
        break;
      
      case CityHall:
        for (BuildingType building : playerState.getAllBuildings()) {
          if (getBuildingFromType(building).isViolet()) {
            victoryPoints++; 
          }
        }
        break;
      
      case CustomsHouse:
        int count = playerState.getVictoryPointChips() / 4;
        victoryPoints += count;
        break;
      
      default:
        // No extra points for other buildings
        break;
      }
    }
    
    return victoryPoints;
  }

  public int getSupplyVictoryPoints(int playerCount) {
    switch(playerCount) {
    case 3:
      return 75;
    case 4:
      return 100;
    case 5:
      return 122;
    default:
      throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
  }

  public int getSupplyColonists(int playerCount) {
    switch(playerCount) {
    case 3:
      return 55;
    case 4:
      return 75;
    case 5:
      return 95;
    default:
      throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
  }

  public int getShipColonists(int playerCount) {
    switch(playerCount) {
      case 3:
      case 4:
      case 5:
        return playerCount;
      default:
        throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
  }

  public Map<Good, Integer> getGoodToAvailableCount() {
    Map<Good, Integer> goodToAvailableCount = new HashMap<>();
    goodToAvailableCount.put(Good.Corn, 10);
    goodToAvailableCount.put(Good.Indigo, 11);
    goodToAvailableCount.put(Good.Sugar, 11);
    goodToAvailableCount.put(Good.Tobacco, 9);
    goodToAvailableCount.put(Good.Coffee, 9);
    return goodToAvailableCount;
  }

  public List<Good> getAllPlantations() {
    List<Good> allPlantations = new ArrayList<>();
    allPlantations.addAll(Collections.nCopies(10, Good.Corn));
    allPlantations.addAll(Collections.nCopies(12, Good.Indigo));
    allPlantations.addAll(Collections.nCopies(11, Good.Sugar));
    allPlantations.addAll(Collections.nCopies(9, Good.Tobacco));
    allPlantations.addAll(Collections.nCopies(8, Good.Coffee));
    return allPlantations;
  }

  public Map<BuildingType, Integer> getBuildingsToCount() {
    Map<BuildingType, Integer> buildingsToCount = new HashMap<>();
    for (BuildingType buildingType : BuildingType.values()) {
      Building building = Building.getBuildingFromType(buildingType);
      buildingsToCount.put(buildingType, building.getCountInGame());
    }
    return buildingsToCount;
  }

  public int getInitialPlayerMoney(int playerCount) {
    switch(playerCount) {
    case 3:
    case 4:
    case 5:
      return playerCount - 1;
    default:
      throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
  }

  public Good getInitialPlayerPlantations(int playerCount, int playerPosition) {
    switch(playerPosition) {
    case 0:
      return Good.Indigo;
    case 1:
      return Good.Indigo;
    case 2:
      if (playerCount == 3 || playerCount == 4) {
        return Good.Corn;
      } else if (playerCount == 5) {
        return Good.Indigo;
      } else {
        throw new IllegalArgumentException("Invalid number of players: " + playerCount);
      }
    case 3: 
      return Good.Corn;
    case 4:
      return Good.Corn;
    default:
      throw new IllegalArgumentException("Invalid player position: " + playerPosition);
    }
  }

  public Map<Good, Set<Integer>> getAllowedGoodsToAllowedShipSizes(GameState gameState, Player player) {
    Map <Good, Set<Integer>> allowedGoodsToAllowedShipSizes = new HashMap<>();

    // For each player good, determine if we can ship it, and on what ships
    Set<Good> playerGoods = gameState.getTypesOfGoodsPlayerHas(player);
    for (Good playerGood : playerGoods) {
      int playerGoodCount = gameState.getGoodCount(player, playerGood);
      Set<Integer> allowedShipSizes = new HashSet<>();
      ShipState shipWithGood = gameState.getShipStateForGood(playerGood);
      if (shipWithGood != null) {
        // Is there already a ship with good that is not full?
        if (!shipWithGood.isFull()) {
          allowedShipSizes.add(shipWithGood.getCapacity());
        }
        
      } else {
        // Are there empty ships big enough to ship all player goods? 
        Set<ShipState> emptyShips = gameState.getEmptyShipStatesWithCapacityAtLeast(playerGoodCount);
        if (!emptyShips.isEmpty()) {
          for (ShipState emptyShip : emptyShips) {
            allowedShipSizes.add(emptyShip.getCapacity());
          }
        } else {
          // If no empty ships large enough, choose largest non-empty ship (if it exists)
          ShipState largestEmptyShip = gameState.getLargestEmptyShipState();
          if (largestEmptyShip != null) {
            allowedShipSizes.add(largestEmptyShip.getCapacity());
          }
        }
      }
      
      if (!allowedShipSizes.isEmpty()) {
        allowedGoodsToAllowedShipSizes.put(playerGood, allowedShipSizes);
      }
    }
    
    return allowedGoodsToAllowedShipSizes;
  }

  public Set<Good> getGoodsAllowedToShipOnWharf(GameState gameState, Player player) {
    Set<Good> goods = new HashSet<>();
    if (isAllowedToUseWharf(gameState, player)) {
      goods.addAll(gameState.getPlayerGoods(player));
    }
    return goods;
  }
  
  private boolean isAllowedToUseWharf(GameState gameState, Player player) {
    return gameState.doesPlayerHaveOccupiedBuiling(BuildingType.Wharf, player)
        && !gameState.getPlayerState(player).hasUsedWharf()
        && gameState.getPlayerState(player).hasGoods();
  }
  
  public Set<Good> getGoodsAllowedToTrade(GameState gameState, Player player) {
    Set<Good> goodsAllowedToTrade = new HashSet<>();
    Set<Good> goodsPlayerOwns = new HashSet<>(gameState.getPlayerGoods(player));
    if (gameState.getAllGoodsInTradingHouse().size() == 4) {
       return goodsAllowedToTrade;
    }
    goodsAllowedToTrade.addAll(goodsPlayerOwns);
    if (!gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.Office)) {
      goodsAllowedToTrade.removeAll(gameState.getUniqueGoodsInTradingHouse());
    }
    return goodsAllowedToTrade;
  }
  
  public Map<Good, Integer> getGoodsAllowedToKeepAfterCaptain(GameState gameState, Player player) {
    Map<Good, Integer> goodsAllowedToKeep = new HashMap<>();
    for (Good good : gameState.getPlayerGoods(player)) {
      goodsAllowedToKeep.put(good, 1);
    }
    if (gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.LargeWharehouse) ||
        gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.SmallWarehouse)) {
    for (Good good : gameState.getPlayerState(player).getGoods()) {
      int count = gameState.getGoodCount(player, good);
      goodsAllowedToKeep.put(good, count);
    }
    }
    return goodsAllowedToKeep;
  }
  
  public int getGoodTypesAllowedToKeepAboveOneAfterCaptain(GameState gameState, Player player) {
    int goodTypesAllowedToKeepAboveOne = 0;
    if (gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.SmallWarehouse)) {
      goodTypesAllowedToKeepAboveOne = 1;
    }
    if (gameState.getPlayerState(player).doesPlayerHaveOccupiedBuilding(BuildingType.LargeWharehouse)) {
      goodTypesAllowedToKeepAboveOne = 2;
    }
    return goodTypesAllowedToKeepAboveOne;
  }

  public Set<BuildingType> getBuildingsAllowedToBuy(GameState gameState, Player player) {
    Set<BuildingType> buildingsAllowedToBuy = new HashSet<>();
    
    // Start with buildings still left
    buildingsAllowedToBuy.addAll(gameState.getAvailableBuildings());

    // Remove buildings player already owns    
    buildingsAllowedToBuy.removeAll(gameState.getPlayerState(player).getAllBuildings());
    
    // For remaining builidings, make sure they can afford or can fit
    for (BuildingType buildingType : new HashSet<>(buildingsAllowedToBuy)){
      Building building = getBuildingFromType(buildingType);
      int citySpacesFilled = calculateCitySpacesTotal(gameState.getPlayerState(player).getAllBuildings());
      
      // Check if spacde available in city
      if (16 - citySpacesFilled < building.getIslandSpacesFilled()) {
        buildingsAllowedToBuy.remove(buildingType);
      } else {
        // Check if have enough money
        int roleDiscount = 0;
        if (player.equals(gameState.getCurrentPlayerToChooseRole())) {
          roleDiscount = 1;
        }
        int quarryDiscount = Math.min(gameState.getPlayerState(player).getOccupiedQuarryCount(), building.getQuarryDiscount());
        int actualCost = building.getCost() - quarryDiscount - roleDiscount;
        if (actualCost > gameState.getPlayerState(player).getMoney()) {
          buildingsAllowedToBuy.remove(buildingType);
        }
      }
    }
    return buildingsAllowedToBuy;
  }

  private int calculateCitySpacesTotal(List<BuildingType> buildings) {
    int citySpaces = 0;
    for (BuildingType buildingType : buildings) {
      Building building = getBuildingFromType(buildingType);
      citySpaces += building.getIslandSpacesFilled();
    }
    return citySpaces;
  }

  public Set<Good> getGoodsAllowedToChooseAsPrivilege(GameState gameState,
      Player player) {
    Set<Good> goods = new HashSet<>();
    for (Good good : getGoodsAbleToProduce(gameState, player)) {
      Integer count = gameState.getAvailableCount(good);
      if (count != null && count > 0) {
        goods.add(good);
      }
    }
    return goods;
  }

  public void refillColonistShip(GameState gameState) {
    if (getEmptyBuildingSpacesFromAllPlayers(gameState) < gameState.getPlayers().size()) {
      gameState.transferColonistsFromSupplyToShip(gameState.getPlayers().size());
    } else {
      gameState.transferColonistsFromSupplyToShip(getEmptyBuildingSpacesFromAllPlayers(gameState));
    }
  }
  
  public int getEmptyBuildingSpacesFromAllPlayers(GameState gameState) {
    int emptyBuildingSpaces = 0;
    for (PlayerState playerState : gameState.getPlayerStates()) {
      for (BuildingType buildingType : playerState.getAllBuildings()) {
      Building building = getBuildingFromType(buildingType);
      int count = building.getAllowedColonists() - playerState.getOccupiedCountOfBuilding(buildingType);
      emptyBuildingSpaces += count;
      }
    }
    return emptyBuildingSpaces;
  }
  
  public Map<Good, Integer> createOccupiedPlantationsToCount() {
    Map <Good, Integer> occupiedPlantationsToCount = new HashMap<>();
    occupiedPlantationsToCount.put(Good.Corn, 0);
    occupiedPlantationsToCount.put(Good.Indigo, 0);
    occupiedPlantationsToCount.put(Good.Sugar, 0);
    occupiedPlantationsToCount.put(Good.Tobacco, 0);
    occupiedPlantationsToCount.put(Good.Coffee, 0);
    return occupiedPlantationsToCount;
  }

  public Map<Good, Integer> createUnoccupiedPlantationsToCount() {
    Map <Good, Integer> unoccupiedPlantationsToCount = new HashMap<>();
    unoccupiedPlantationsToCount.put(Good.Corn, 0);
    unoccupiedPlantationsToCount.put(Good.Indigo, 0);
    unoccupiedPlantationsToCount.put(Good.Sugar, 0);
    unoccupiedPlantationsToCount.put(Good.Tobacco, 0);
    unoccupiedPlantationsToCount.put(Good.Coffee, 0);
    return unoccupiedPlantationsToCount;
  }
}

