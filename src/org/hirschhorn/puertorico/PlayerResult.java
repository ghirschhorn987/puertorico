package org.hirschhorn.puertorico;

import java.util.HashMap;
import java.util.Map;

public class PlayerResult {

  private Map<ResultsUI.Statistic, Number> variableToValue;
  
  public PlayerResult() {
    variableToValue = new HashMap<>();
  }

  public Number getValue(ResultsUI.Statistic variable) {
    return variableToValue.get(variable);
  }
  
  public void setValue(ResultsUI.Statistic variable, Number value) {
    variableToValue.put(variable, value);
  }

}

