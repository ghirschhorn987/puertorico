package org.hirschhorn.puertorico.playerstrategies.sam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.hirschhorn.puertorico.Rules;
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
import org.hirschhorn.puertorico.playerstrategies.DefaultPlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;

public class SamStrategy extends DefaultPlayerStrategy {
  
  @Override
  public PlayerStrategy getCopy() {
    return new SamStrategy();
  }
  
  @Override
  public SettlerAction doSettler(GameState gameState,
      Set<Good> plantationsAllowedToChoose, boolean allowedToChooseQuarry,
      boolean allowedToUseHaciendaBuildingToGetExtraPlantation) {
      Good plantationToChoose = null;
      boolean usingHacienda = false;
      List<Good> plantations = new ArrayList<>(Arrays.asList(
          Good.Corn,
          Good.Indigo,
          Good.Coffee,
          Good.Tobacco,
          Good.Sugar));
      if (gameState.getPlayerState(gameState.getCurrentPlayerToDoAction()).getGoodCount(Good.Indigo) >= 4) {
        plantations.remove(Good.Indigo);
        plantations.add(Good.Indigo);
      }
      if (gameState.getPlayerState(gameState.getCurrentPlayerToDoAction()).getGoodCount(Good.Coffee) >= 2) {
        plantations.remove(Good.Coffee);
        plantations.add(Good.Coffee);
      }
      if (gameState.getPlayerState(gameState.getCurrentPlayerToDoAction()).getGoodCount(Good.Tobacco) >= 3) {
        plantations.remove(Good.Tobacco);
        plantations.add(Good.Tobacco);
      }
      for (Good plantation : plantations) {
        if (plantationsAllowedToChoose.contains(plantation)) {
          plantationToChoose = plantation;
        }
      }
      if (allowedToUseHaciendaBuildingToGetExtraPlantation){
        usingHacienda = true;
      }
    return new SettlerAction(plantationToChoose, false, usingHacienda);
  }

  @Override
  public MayorAction doMayor(GameState gameState, int colonistsAllowedToOccupy,
      Map<BuildingType, Integer> buildingToOccupiedCountAllowed,
      int quarriesAllowedToOccupy, List<Good> plantationsAllowedToOccupy) {
    // TODO Auto-generated method stub
    return super.doMayor(gameState, colonistsAllowedToOccupy,
        buildingToOccupiedCountAllowed, quarriesAllowedToOccupy,
        plantationsAllowedToOccupy);
  }

  @Override
  public boolean declineExtraColonistPrivilegeForMayor(GameState gameState) {
    return false;
  }


  public BuilderAction doBuilder(GameState gameState,
      Set<BuildingType> buildingsAllowedToBuy) {
    PlayerState playerState = gameState.getPlayerState(gameState.getCurrentPlayerToDoAction());
    int spendingPower = playerState.getMoney() + playerState.getOccupiedQuarryCount();
    List<BuildingType> buildingsToBuy = new ArrayList<>();
    for (BuildingType buildingType : buildingsAllowedToBuy) {
      if (buildingType.equals(BuildingType.Wharf)
          || buildingType.equals(BuildingType.Residence)
          || buildingType.equals(BuildingType.CustomsHouse)
          || buildingType.equals(BuildingType.Fortress)
          || buildingType.equals(BuildingType.GuildHall)
          || buildingType.equals(BuildingType.CityHall)) {
        return new BuilderAction(buildingType);
      }
    }
    if (spendingPower >= 7){
      return new BuilderAction(null);
    }
    for (BuildingType buildingType : buildingsAllowedToBuy) {
      if (playerState.getAllPlantations().contains(Building.getBuildingFromType(buildingType).getProductionGood())){
        buildingsToBuy.add(buildingType);
      }
    }
    for (BuildingType buildingType : new ArrayList<>(buildingsToBuy)) {
      Building building = Building.getBuildingFromType(buildingType);
      if (alreadyCanProduceGoods(gameState, playerState, building) || isFirstAndSmallProduction(gameState, playerState, buildingType, building)) {
        buildingsToBuy.remove(buildingType);
      }
    }
    if (buildingsToBuy.isEmpty()) {
      return new BuilderAction(null);
    }
    return new BuilderAction(buildingsToBuy.get(0));
  }

  private boolean isFirstAndSmallProduction(
      GameState gameState,
      PlayerState playerState,
      BuildingType buildingType,
      Building building) {
    boolean isFirst = false;
    for (BuildingType candidate : playerState.getAllBuildings()) {
      Building candidateBuilding = Building.getBuildingFromType(candidate);
      if (candidateBuilding.isProduction()) {
        isFirst = !(candidateBuilding.getProductionGood().equals(Building.getBuildingFromType(buildingType).getProductionGood()));
      }
    }
    if (building.isSmallProductionBuilding() && isFirst) {
      return true;
    }
    return false;
  }

  private boolean alreadyCanProduceGoods(GameState gameState,
      PlayerState playerState, Building building) {
    int plantationCount = Collections.frequency(playerState.getAllPlantations(), building.getProductionGood());
    int buildingCapacity = building.getAllowedColonists();
    if (buildingCapacity - plantationCount >= 0){
      return true;
    }
    return false;
  }

  @Override
  public CraftsmanAction doCraftsman(GameState gameState,
      Set<Good> goodsAllowedToChooseAsPrivilege) {
    Good goodForPrivledge = null;
    List<Good> goods = Arrays.asList(
        Good.Coffee,
        Good.Corn,
        Good.Indigo,
        Good.Tobacco,
        Good.Sugar);
    for (Good good : goods) {
      if (goodsAllowedToChooseAsPrivilege.contains(good)) {
        goodForPrivledge = good;
      }
    }
    return new CraftsmanAction(goodForPrivledge);
  }

  @Override
  public TraderAction doTrader(GameState gameState,
      Set<Good> goodsAllowedToTrade) {
    // TODO Auto-generated method stub
    return super.doTrader(gameState, goodsAllowedToTrade);
  }

  @Override
  public CaptainAction doCaptain(GameState gameState,
      Map<Good, Set<Integer>> goodsToShipSizesAllowedToShipOn,
      Set<Good> goodsAllowedToShipOnWharf) {
    // TODO Auto-generated method stub
    return super.doCaptain(gameState, goodsToShipSizesAllowedToShipOn,
        goodsAllowedToShipOnWharf);
  }

  @Override
  public List<Good> chooseGoodsToKeepAfterCaptain(GameState gameState,
      Map<Good, Integer> goodsAllowedToKeep,
      int goodTypesAllowedToKeepMoreThanOneOf) {
    // TODO Auto-generated method stub
    return super.chooseGoodsToKeepAfterCaptain(gameState, goodsAllowedToKeep,
        goodTypesAllowedToKeepMoreThanOneOf);
  }

  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    List<Role> roles = new ArrayList<>(getPrioritizedRoles(gameState));
    for (Role role : roles) {
      if (availableRoles.contains(role)) {
        return role;
      }
    }
    throw new IllegalStateException("Shouldn't be here");
  }
  
  private List<Role> getPrioritizedRoles(GameState gameState) {
    //give each role one point based on: goods, money, plantations, buildings, colonists
    //return highest score order
    PlayerState playerState = gameState.getPlayerState(gameState.getCurrentPlayerToChooseRole());
    List<Role> roles = new ArrayList<>();
    Map<Role, AtomicInteger> roleToScore = getInitialScores(gameState);
    
    //check goods for trader
    if (playerState.getGoodCount(Good.Coffee) > 0 && canTradeGood(Good.Coffee, gameState, playerState)) {
      roleToScore.get(Role.Trader).addAndGet(100);
    } else if (playerState.getGoodCount(Good.Tobacco) > 0 && canTradeGood(Good.Tobacco, gameState, playerState)) {
      roleToScore.get(Role.Trader).addAndGet(6);
    } else if (playerState.getGoodCount(Good.Sugar) > 0 && canTradeGood(Good.Sugar, gameState, playerState)) {
      roleToScore.get(Role.Trader).addAndGet(3);
    }
    
    //check goods for captain
    roleToScore.get(Role.Captain).addAndGet(playerState.getGoods().size());
    
    //construct list based on scores
    List<Map.Entry<Role, AtomicInteger>> entryList = new ArrayList<>(roleToScore.entrySet());
    Collections.sort(entryList, getComparator());
    for (Map.Entry<Role, AtomicInteger> entry : entryList) {
      roles.add(entry.getKey());
    }
    return roles;
  }

  private boolean canTradeGood(Good coffee, GameState gameState, PlayerState playerstate) {
    // TODO Auto-generated method stub
    return false;
  }

  private Map<Role, AtomicInteger> getInitialScores(GameState gameState) {
    Map <Role, AtomicInteger> roles = new HashMap<>();
    for (Role role : gameState.getAllRoles()) {
      roles.put(role, new AtomicInteger(0));
    }
    return roles;
  }
  
  private static Comparator<Map.Entry<Role, AtomicInteger>> getComparator() {
    return new Comparator<Map.Entry<Role,AtomicInteger>>() {

      @Override
      public int compare(
          Entry<Role, AtomicInteger> o1,
          Entry<Role, AtomicInteger> o2) {
          if (o1.getValue().get() < o2.getValue().get()) {
            return -1;
          }
          if (o1.getValue().get() == o2.getValue().get()) {
            return 0;
          }
            return 1;
      }
    };
    
  }
}
