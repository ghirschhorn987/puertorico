package org.hirschhorn.puertorico;

import java.util.HashMap;
import java.util.Map;

import org.hirschhorn.puertorico.ResultsUI.Statistic;

public class PlayerStats {

  private Map<ResultsUI.Statistic, Number> variableToValue;

  public PlayerStats(PlayerStats playerStats) {
    variableToValue = new HashMap<>(playerStats.variableToValue);
  }
  
  public PlayerStats() {
    variableToValue = new HashMap<>();
  }

  public String toString() {
    return variableToValue.toString();
  }
  
  public Number getValue(ResultsUI.Statistic variable) {
    Number value = variableToValue.get(variable);
    if (value == null) {
      value = new Double(0);
    }
    return value;
  }
  
  public void setValue(ResultsUI.Statistic variable, Number value) {
    variableToValue.put(variable, value);
  }

  public void incrementValue(Statistic variable, Number value) {
    Number currentValue = getValue(variable);
    if (currentValue == null) {
      currentValue = new Double(0);
    }
    variableToValue.put(variable, currentValue.doubleValue() + value.doubleValue());
  }

}

