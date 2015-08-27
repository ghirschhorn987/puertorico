package org.hirschhorn.puertorico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hirschhorn.puertorico.ResultsUI.Statistic;

public class GameStats {

  private Map<String, PlayerStats> strategySimpleNameToStats;
  
  public GameStats(GameStats gameStats) {
    strategySimpleNameToStats = new HashMap<>();
    for (Map.Entry<String, PlayerStats> entry : gameStats.strategySimpleNameToStats.entrySet()) {
      strategySimpleNameToStats.put(entry.getKey(), new PlayerStats(entry.getValue()));
    }   
  }

  public GameStats() {
    this.strategySimpleNameToStats = new HashMap<>();
  }

  public String toString() {
    return strategySimpleNameToStats.toString();
  }
  
  public PlayerStats getPlayerStats(String strategySimpleName) {
    return strategySimpleNameToStats.get(strategySimpleName);
  }
  
  public void putPlayerStats(String strategySimpleName, PlayerStats playerStats) {
    strategySimpleNameToStats.put(strategySimpleName, playerStats);
  }
  
  public Set<String> getStrategies() {
    return new HashSet<>(strategySimpleNameToStats.keySet());
  }

  public Number getPlayerStatistic(
      String strategyNameSimple,
      Statistic statistic) {
    PlayerStats playerStats = strategySimpleNameToStats.get(strategyNameSimple);
    if (playerStats == null) {
      return new Double(0);
    }
    return playerStats.getValue(statistic);
  }
  
  public void incrementPlayerStatistic(
      String strategyNameSimple,
      Statistic statistic,
      Number value) {
    PlayerStats playerStats = strategySimpleNameToStats.get(strategyNameSimple);
    if (playerStats == null) {
      playerStats = new PlayerStats();
      strategySimpleNameToStats.put(strategyNameSimple, playerStats);
    }
    playerStats.incrementValue(statistic, value);
  }
}
