package org.hirschhorn.puertorico;

import java.util.Collections;
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
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

public class ActionValidator {

  private static final Logger logger = Logger.getLogger(ActionValidator.class.getName());
  
  static {
   // logger.setLevel(Level.SEVERE);
  }
  
  private PlayerStrategy strategy = new DefaultPlayerStrategy();
  
  public Role validateChooseRole(Role playerRole, GameState gameState, List<Role> availableRoles) {
    boolean valid = true;
    if (!availableRoles.contains(playerRole)) {
      valid = false;
    } 
    if (valid) {
      return playerRole;
    }
    Role validRole  = strategy.chooseRole(gameState, availableRoles);
    logger.warning("Ignoring invalid role choice: " + playerRole + ". Role chosen instead: " + validRole);
    return validRole;
  }

  public SettlerAction validateSettlerAction(
      SettlerAction playerAction,
      GameState gameState,
      Set<Good> plantationsAllowedToChoose,
      boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
    boolean valid = true;
    if (playerAction.isChooseQuarryInstead()) {
      if (!allowedToChooseQuarry || playerAction.getChosenPlantation() != null) {
        valid = false;
      }
    } else {
      if (playerAction.getChosenPlantation() == null || !plantationsAllowedToChoose.contains(playerAction.getChosenPlantation())) {
        valid = false;      
      }
    }
    if (playerAction.isUseHaciendaBuildingToGetExtraPlantation()) {
      if (!allowedToUseHaciendaBuildingToGetExtraPlantation) {
        valid = false;
      }
    }
    if (valid) {
      return playerAction;
    }
    SettlerAction validAction = strategy.doSettler(gameState, plantationsAllowedToChoose, allowedToChooseQuarry, allowedToUseHaciendaBuildingToGetExtraPlantation);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }
  
  public MayorAction validateMayorAction(
      MayorAction playerAction,
      GameState gameState,
      int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy,
      List<Good> plantationsAllowedToOccupy) {
    boolean valid = true;
    int totalColonistsPlaced = 0;
 
    for (Map.Entry<BuildingType, Integer> entry : playerAction.getBuildingToOccupiedCount().entrySet()){
      BuildingType buildingType = entry.getKey();
      Integer colonistsToPlace = entry.getValue();
      Integer countAllowed = buildingToOccupiedCountAllowed.get(buildingType);
      if (countAllowed == null || colonistsToPlace > countAllowed){
        valid = false;
      }
      totalColonistsPlaced += colonistsToPlace;
    }
    
    Set<Good> uniquePlantationsToPlace = new HashSet<>(playerAction.getOccupiedPlantations());
    for (Good plantationType : uniquePlantationsToPlace) {
      Integer colonistsToPlace = Collections.frequency(playerAction.getOccupiedPlantations(), plantationType);
      Integer countAllowed = Collections.frequency(plantationsAllowedToOccupy, plantationType);
      if (countAllowed == null || colonistsToPlace > countAllowed) {
        valid = false;
      }
      totalColonistsPlaced += colonistsToPlace;
    }
    
    if (playerAction.getOccupiedQuarryCount() > quarriesAllowedToOccupy){
      valid = false;
    }
    totalColonistsPlaced += playerAction.getOccupiedQuarryCount();
    
    if (totalColonistsPlaced > colonistsAllowedToOccupy) {
      valid = false;
    }
    
    // Declining is only applicable if allowed. Otherwise value is ignored.
    // So no need to validate.
    if (valid) {
      return playerAction;
    }
    MayorAction validAction = strategy.doMayor(gameState, colonistsAllowedToOccupy, buildingToOccupiedCountAllowed, quarriesAllowedToOccupy, plantationsAllowedToOccupy);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }

  public BuilderAction validateBuilderAction(
      BuilderAction playerAction,
      GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
    boolean valid = true;
    BuildingType buildingType = playerAction.getBuildingToBuy();
    if (buildingsAllowedToBuy.isEmpty()) {
      if (buildingType != null) {
        valid = false;
      }
    } else {
      if (buildingType != null && !buildingsAllowedToBuy.contains(buildingType)) {
        valid = false;
      }
    }
    if (valid) {
      return playerAction;
    }
    BuilderAction validAction = strategy.doBuilder(gameState, buildingsAllowedToBuy);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }

  
  public CraftsmanAction validateCraftsmanAction(
      CraftsmanAction playerAction,
      GameState gameState,
      Set<Good> goodsAllowedToChooseAsPrivilege) {
    boolean valid = true;
    Good chosenGood = playerAction.getChosenGoodForPrivilge();
    if (goodsAllowedToChooseAsPrivilege.isEmpty()) {
      if (chosenGood != null) {
        valid = false;
      }
    } else {
      if (!goodsAllowedToChooseAsPrivilege.contains(chosenGood)) {
        valid = false;
      }
    }
    if (valid) {
      return playerAction;
    }
    CraftsmanAction validAction = strategy.doCraftsman(gameState, goodsAllowedToChooseAsPrivilege);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }
  
  public TraderAction validateTraderAction(
      TraderAction playerAction,
      GameState gameState,
      Set<Good> goodsAllowedToTrade) {
    boolean valid = true;
    Good chosenGood = playerAction.getChosenGood();
    if (chosenGood != null) {
      if (goodsAllowedToTrade.isEmpty() || !goodsAllowedToTrade.contains(chosenGood)) {
        valid = false;
      }
    }
    if (valid) {
      return playerAction;
    }
    TraderAction validAction = strategy.doTrader(gameState, goodsAllowedToTrade);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }
  
  public CaptainAction validateCaptainAction(
      CaptainAction playerAction,
      GameState gameState,
      Map<Good, Set<Integer>> allowedGoodsToAllowedShipSizes,
      Set<Good> goodsAllowedToShipOnWharf) {
    boolean valid = true;   
    Good goodToShip = playerAction.getGoodToShip();
    if (playerAction.isUsingWharf()) {
      if (goodToShip == null || !goodsAllowedToShipOnWharf.contains(goodToShip)) {
        valid = false;
      }
    } else {
      Set<Good> allowedGoods = allowedGoodsToAllowedShipSizes.keySet();
      if (goodToShip == null) {
        if (!allowedGoods.isEmpty()) {
          valid = false; 
        }
      } else {
        if (!allowedGoods.contains(goodToShip)) {
          valid = false;
        }
        if (!allowedGoodsToAllowedShipSizes.get(goodToShip).contains(playerAction.getChosenShipSize())) {
          valid = false;
        }
      }
    }
    if (valid) {
      return playerAction;
    }
    CaptainAction validAction = strategy.doCaptain(gameState, allowedGoodsToAllowedShipSizes, goodsAllowedToShipOnWharf);
    logger.warning("Ignoring invalid action: " + playerAction + ". Action chosen instead: " + validAction);
    return validAction;
  }

  public List<Good> validateChosenGoodsAfterCaptain(
      List<Good> playerChosenGoods,
      GameState gameState,
      Map<Good, Integer> goodsAllowedToKeep,
      int goodTypesAllowedToKeepMoreThanOneOf) {
    boolean valid = true;
    Set<Good> uniqueChosenGoods = new HashSet<>(playerChosenGoods);
    int playerChosenGoodTypesWithMoreThanOne = 0;
    for (Good chosenGood : uniqueChosenGoods) {
      Integer chosenGoodCount = Collections.frequency(playerChosenGoods, chosenGood);
      Integer countAllowed = goodsAllowedToKeep.get(chosenGood);
      if (countAllowed == null || chosenGoodCount < 0 || chosenGoodCount > countAllowed) {
        valid = false;
      }
      if (chosenGoodCount > 1) {
        playerChosenGoodTypesWithMoreThanOne++;
      }
    }    

    // Player can always choose 1 good type, plus additional types if they are allowed
    if (uniqueChosenGoods.size() > (1 + goodTypesAllowedToKeepMoreThanOneOf)) {
      valid = false;
    }
    
    // Player can only choose more than one of a certain amount of types
    if (playerChosenGoodTypesWithMoreThanOne > goodTypesAllowedToKeepMoreThanOneOf) {
      valid = false;
    }
    
    if (valid) {
      return playerChosenGoods;
    }
    List<Good> validChosenGoods = strategy.chooseGoodsToKeepAfterCaptain(gameState, goodsAllowedToKeep, goodTypesAllowedToKeepMoreThanOneOf);
    logger.warning("Ignoring invalid chosen goods after captain: " + playerChosenGoods + ". Goods chosen instead: " + validChosenGoods);
    return validChosenGoods;
  }

}
