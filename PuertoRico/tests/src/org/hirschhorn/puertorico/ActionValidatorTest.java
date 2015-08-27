package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hirschhorn.puertorico.actions.MayorAction;
import org.hirschhorn.puertorico.constants.Building;
import org.hirschhorn.puertorico.constants.BuildingType;
import org.hirschhorn.puertorico.constants.Good;
import org.hirschhorn.puertorico.gamestate.GameState;
import org.hirschhorn.puertorico.gamestate.PlayerState;
import org.hirschhorn.puertorico.playerstrategies.PlayerStrategy;
import org.hirschhorn.puertorico.playerstrategies.dad.DadStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionValidatorTest {
  private ActionValidator actionValidator = new ActionValidator();
  private GameState gameState; 
  
  @Before
  public void setUp() {
    Rules rules = new Rules();
    // playerStrategies is needed only to create initial GameState. Not used after.
    List<PlayerStrategy> playerStrategies = Collections.nCopies(4, (PlayerStrategy) DadStrategy.getDefaultDadStrategy());    
    gameState = new GameState(rules, playerStrategies);
  }
  
  @Test
  public void validMayorActionShouldNotBeInvalidated() {
    // Set up 
    Map<BuildingType, Integer> buildingsToCount = new HashMap<>();
    buildingsToCount.put(BuildingType.SmallIndigoPlant, 1);
    buildingsToCount.put(BuildingType.LargeIndigoPlant, 2);
    buildingsToCount.put(BuildingType.LargeSugarMill, 1);
    buildingsToCount.put(BuildingType.CoffeStorage, 2);
    buildingsToCount.put(BuildingType.Office, 1);
    
    List<Good> plantationsToOccupy = new ArrayList<Good>();
    plantationsToOccupy.add(Good.Corn);
    plantationsToOccupy.add(Good.Corn);
    plantationsToOccupy.add(Good.Indigo);
    plantationsToOccupy.add(Good.Indigo);
    plantationsToOccupy.add(Good.Indigo);
    plantationsToOccupy.add(Good.Sugar);
    plantationsToOccupy.add(Good.Coffee);
    plantationsToOccupy.add(Good.Coffee);

    int quarriesToOccupy = 2;
    
    Player player = gameState.getPlayers().get(0);
    PlayerState playerState = gameState.getPlayerState(player);
    
    for (BuildingType buildingType : buildingsToCount.keySet()) {
      playerState.addBuilding(buildingType);
    }
    for (Good plantation : plantationsToOccupy) {
      playerState.addPlantationToOccupiedList(plantation);
    }
    for (int i = 0; i < quarriesToOccupy; i++) {
      playerState.addQuarry();
    }
    
    // Verify state before
    // TODO
    
    // Do action
    playerState = gameState.getPlayerState(player);
    int colonistsAllowedToOccupy = 17;
    Map<BuildingType, Integer> buildingToOccupiedCountAllowed = new HashMap<>();
    for (BuildingType buildingType : playerState.getAllBuildings()) {
      Building building = Building.getBuildingFromType(buildingType);
      buildingToOccupiedCountAllowed.put(buildingType, building.getAllowedColonists());
    }
    List<Good> plantationsAllowedToOccupy = playerState.getAllPlantations();
    int quarriesAllowedToOccupy = playerState.getOccupiedQuarryCount() + playerState.getUnoccupiedQuarryCount();
    
    MayorAction action = new MayorAction(
        plantationsToOccupy,
        buildingsToCount,
        quarriesToOccupy);
    MayorAction validatedAction = actionValidator.validateMayorAction(
        action, 
        gameState,
        colonistsAllowedToOccupy,
        buildingToOccupiedCountAllowed,
        quarriesAllowedToOccupy,
        plantationsAllowedToOccupy);
    
    // Verify state after
    Assert.assertSame(action, validatedAction);
  }
}
