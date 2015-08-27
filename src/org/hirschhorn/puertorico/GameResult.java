package org.hirschhorn.puertorico;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameResult {

  private Map<String, PlayerResult> strategySimpleNameToResult;
  
  public GameResult() {
    this.strategySimpleNameToResult = new HashMap<>();
  }

  public PlayerResult getPlayerResult(String strategySimpleName) {
    return strategySimpleNameToResult.get(strategySimpleName);
  }
  
  public void putPlayerResult(String strategySimpleName, PlayerResult playerResult) {
    strategySimpleNameToResult.put(strategySimpleName, playerResult);
  }
  
  public Set<String> getStrategies() {
    return new HashSet<>(strategySimpleNameToResult.keySet());
  }
  
}
