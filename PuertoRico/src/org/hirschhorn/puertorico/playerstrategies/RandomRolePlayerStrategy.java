package org.hirschhorn.puertorico.playerstrategies;

import java.util.List;

import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;

public class RandomRolePlayerStrategy extends DefaultPlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new RandomRolePlayerStrategy();
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    int choice = (int) Math.floor(Math.random() * availableRoles.size());
    return availableRoles.get(choice);
  }

}
