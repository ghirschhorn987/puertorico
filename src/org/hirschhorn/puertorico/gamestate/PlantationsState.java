package org.hirschhorn.puertorico.gamestate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hirschhorn.puertorico.Player;
import org.hirschhorn.puertorico.constants.Good;

public class PlantationsState {
  private List<Good> coveredPlantations;
  private List<Good> uncoveredPlantations;
  private List<Good> discardedPlantations;
  private int availableQuarryCount;
  
  public PlantationsState(PlantationsState plantationsState) {
    coveredPlantations = new ArrayList<>(plantationsState.coveredPlantations);
    uncoveredPlantations = new ArrayList<>(plantationsState.uncoveredPlantations);
    discardedPlantations = new ArrayList<>(plantationsState.discardedPlantations);
    availableQuarryCount = plantationsState.availableQuarryCount;
  }
  
  public PlantationsState(
      int playerCount,
      List<Good> allPlantations) {

    coveredPlantations = new ArrayList<>(allPlantations);
    uncoveredPlantations = new ArrayList<>();
    discardedPlantations = new ArrayList<>();
    this.availableQuarryCount = 8;
    
    if (playerCount < 3 || playerCount > 5) {
      throw new IllegalArgumentException("Invalid number of players: " + playerCount);
    }
    int numberOfCoveredPiles = playerCount + 1;

    shuffleCoveredPlantations();
    
    // Take first x from covered and uncover them
    uncoveredPlantations.addAll(coveredPlantations.subList(0, numberOfCoveredPiles));
    for (Good plantation : uncoveredPlantations){
      if (uncoveredPlantations.contains(plantation)){
        coveredPlantations.remove(plantation);
      }
    }

  }
  
  public List<Good> getDiscardedPlantations(){
    return discardedPlantations;
  }
  public List<Good> getUncoveredPlantations(){
    return uncoveredPlantations;
  }
  
  public List<Good> getCoveredPlantations(Good good){
    return coveredPlantations;
  }
  public int getAvailableQuarryCount(){
    return availableQuarryCount;
  }
  public void removeQuarry() {
    availableQuarryCount--;
  }
  
  public Good removeCoveredPlantations() {
    return coveredPlantations.remove(0);
  }
  
  public boolean removeUncoveredPlantation(Good plantation) {
    return uncoveredPlantations.remove(plantation);
  }

  public void transferPlantationsFromUncoveredToDiscarded() {
   discardedPlantations.addAll(uncoveredPlantations);
   uncoveredPlantations.clear();
  }

  public Good removeHiddenPlantation() {
    return removeCoveredPlantations();
  }

  public void rebuildUncoveredPlantations(List<Player> players) {
    for (int x = 0; x < players.size() + 1; x++) {
      if (coveredPlantations.isEmpty()) {
        if (discardedPlantations.isEmpty()) {
          // Nothing left to uncover
          break;
        }
        coveredPlantations.addAll(discardedPlantations);
        discardedPlantations.clear();

        shuffleCoveredPlantations();
      }
      if (coveredPlantations.size() != 0){
        uncoveredPlantations.add(coveredPlantations.get(0));
        coveredPlantations.remove(0);
      }
    }
  }

  private void shuffleCoveredPlantations() {
    Collections.shuffle(coveredPlantations);
  }

  public List<Good> getAllCoveredPlantations() {
    return coveredPlantations;
  }
}
