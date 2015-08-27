package org.hirschhorn.puertorico.playerstrategies;

import java.util.List;

import org.hirschhorn.puertorico.constants.Role;
import org.hirschhorn.puertorico.gamestate.GameState;

public class OrderedRolePlayerStrategy extends DefaultPlayerStrategy {

  @Override
  public PlayerStrategy getCopy() {
    return new OrderedRolePlayerStrategy();
  }
  
  @Override
  public Role chooseRole(GameState gameState, List<Role> availableRoles) {
    Role[] desired = {
        Role.Craftsman,
        Role.Settler,
        Role.Mayor,
        Role.Captain,
        Role.Builder,
        Role.Trader,
        Role.Prospector
    };
    for (Role role : desired) {
      if (availableRoles.contains(role)) {
        return role;
      }
    }
    throw new IllegalStateException("Should not be here");
  }

}
