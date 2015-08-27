package org.hirschhorn.puertorico.playerstrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

public class DefaultPlayerStrategy implements PlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new DefaultPlayerStrategy();
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    return availableRoles.get(0);
  }

  @Override
  public SettlerAction doSettler(
      GameState gameState,
      Set<Good> plantationsAllowedToChoose,
      boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
    Good chosenPlantation = null;
    if (!plantationsAllowedToChoose.isEmpty()) {
      chosenPlantation = plantationsAllowedToChoose.iterator().next();
    }
    boolean chooseQuarryInstead = false;
    boolean useHaciendaBuildingToGetExtraPlantation = false;
    return new SettlerAction(chosenPlantation, chooseQuarryInstead, useHaciendaBuildingToGetExtraPlantation);
  }

  @Override
  public MayorAction doMayor(
      GameState gameState,
      int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy,
      List<Good> plantationsAllowedToOccupy) {
    
    int remainingColonists = colonistsAllowedToOccupy;
    List<Good> occupiedPlantations = new ArrayList<>();
    Map<BuildingType, Integer> buildingToOccupiedCount = new HashMap<>();
    
    Iterator<Good> plantationIterator = plantationsAllowedToOccupy.iterator();
    while (remainingColonists > 0 && plantationIterator.hasNext()) {
      Good plantation = plantationIterator.next();
      occupiedPlantations.add(plantation);
      remainingColonists--;
    }
    Iterator<Map.Entry<BuildingType, Integer>> buildingIterator = buildingToOccupiedCountAllowed.entrySet().iterator();
    while (remainingColonists > 0 && buildingIterator.hasNext()) {
      Map.Entry<BuildingType, Integer> entry = buildingIterator.next();
      BuildingType buildingType = entry.getKey();
      int countAllowed = entry.getValue();
      int occupiedBuildingCount = Math.min(remainingColonists, countAllowed);
      buildingToOccupiedCount.put(buildingType, occupiedBuildingCount);
      remainingColonists -= occupiedBuildingCount;
    }

    int occupiedQuarryCount = Math.min(remainingColonists, quarriesAllowedToOccupy);
    remainingColonists -= occupiedQuarryCount; 
    
    return new MayorAction(occupiedPlantations, buildingToOccupiedCount, occupiedQuarryCount);
  }

  @Override
  public boolean declineExtraColonistPrivilegeForMayor(GameState gameState) {
    return false;
  }
  
  @Override
  public BuilderAction doBuilder(GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
    BuildingType buildingToBuy = null;
    if (!buildingsAllowedToBuy.isEmpty()) {
      buildingToBuy = buildingsAllowedToBuy.iterator().next();
    }
    return new BuilderAction(buildingToBuy);
  }
  
  @Override
  public CraftsmanAction doCraftsman(GameState gameState,
      Set<Good> goodsAllowedToChooseAsPrivilege) {
    Good chosenGoodForPrivilege = null;
    if (!goodsAllowedToChooseAsPrivilege.isEmpty()) {
      chosenGoodForPrivilege = goodsAllowedToChooseAsPrivilege.iterator().next();
    }    
    return new CraftsmanAction(chosenGoodForPrivilege);
  }

  @Override
  public TraderAction doTrader(GameState gameState, Set<Good> goodsAllowedToTrade) {
    return new TraderAction(null);
  }

  @Override
  public CaptainAction doCaptain(GameState gameState,
      Map<Good, Set<Integer>> goodsToShipSizesAllowedToShipOn,
      Set<Good> goodsAllowedToShipOnWharf) {
    
    boolean isUsingWharf = false;
    int chosenShipSize = 0;
    Good goodToShip = null;
    if (!goodsToShipSizesAllowedToShipOn.isEmpty()){
      goodToShip = goodsToShipSizesAllowedToShipOn.keySet().iterator().next();
      Set<Integer> shipSizes = goodsToShipSizesAllowedToShipOn.get(goodToShip);
      chosenShipSize = shipSizes.iterator().next();
    }
    return new CaptainAction(chosenShipSize, goodToShip, isUsingWharf);
  }
  
  @Override
  public List<Good> chooseGoodsToKeepAfterCaptain(
      GameState gameState,
      Map<Good, Integer> goodsAllowedToKeep,
      int goodTypesAllowedToKeepMoreThanOneOf) {
    List<Good> goodsToKeep = new ArrayList<>();
    List<Good> goodTypesOwned = new ArrayList<>(goodsAllowedToKeep.keySet());
    
    switch (goodTypesOwned.size()) {
      case 0:
        break;
      case 1:
        Good good = goodTypesOwned.get(0);
        if (goodTypesAllowedToKeepMoreThanOneOf == 0) {
          goodsToKeep.add(good);
        } else {
          int count = goodsAllowedToKeep.get(good);
          goodsToKeep.addAll(Collections.nCopies(count, good));
        }
        break;
      case 2:
        Good good1 = goodTypesOwned.get(0);
        Good good2 = goodTypesOwned.get(1);        
        if (goodTypesAllowedToKeepMoreThanOneOf == 0) {
          goodsToKeep.add(good1);
        } else if (goodTypesAllowedToKeepMoreThanOneOf == 1) {
          int count = goodsAllowedToKeep.get(good1);
          goodsToKeep.addAll(Collections.nCopies(count, good1));
          goodsToKeep.add(good2);
        } else if (goodTypesAllowedToKeepMoreThanOneOf == 2) {
          int count1 = goodsAllowedToKeep.get(good1);
          goodsToKeep.addAll(Collections.nCopies(count1, good1));
          int count2 = goodsAllowedToKeep.get(good2);
          goodsToKeep.addAll(Collections.nCopies(count2, good2));
        }
        break;
      case 3:
      case 4:
      case 5:
        good1 = goodTypesOwned.get(0);
        good2 = goodTypesOwned.get(1);        
        Good good3 = goodTypesOwned.get(2);        
        if (goodTypesAllowedToKeepMoreThanOneOf == 0) {
          goodsToKeep.add(good1);
        } else if (goodTypesAllowedToKeepMoreThanOneOf == 1) {
          int count = goodsAllowedToKeep.get(good1);
          goodsToKeep.addAll(Collections.nCopies(count, good1));
          goodsToKeep.add(good2);
        } else if (goodTypesAllowedToKeepMoreThanOneOf == 2) {
          int count1 = goodsAllowedToKeep.get(good1);
          goodsToKeep.addAll(Collections.nCopies(count1, good1));
          int count2 = goodsAllowedToKeep.get(good2);
          goodsToKeep.addAll(Collections.nCopies(count2, good2));
          goodsToKeep.add(good3);
        }
        break;
      default:
        throw new IllegalStateException("unknown number of good types owned: " + goodTypesOwned.size());
    }
    return goodsToKeep;
  }

}
