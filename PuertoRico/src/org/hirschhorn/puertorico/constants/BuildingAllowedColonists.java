package org.hirschhorn.puertorico.constants;

public enum BuildingAllowedColonists {
  ONE(1),
  TWO(2),
  THREE(3);
  
  private int allowedColonistsValue;
  
  BuildingAllowedColonists(int allowedColonistsValue) {
    this.allowedColonistsValue = allowedColonistsValue;
  }
  
  public int getAllowedColonistsValue() {
    return allowedColonistsValue;
  }
}
