package org.hirschhorn.puertorico.gamestate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hirschhorn.puertorico.constants.Good;

public class TradingHouse {
  List<Good> goods;
  
  public TradingHouse(TradingHouse tradingHouse) {
    goods = new ArrayList<>(tradingHouse.goods);
  }
  
  public TradingHouse() {
    goods = new ArrayList<>();
  }
  
  public int getGoodCount(Good chosenGood){
    int count = 0;
    for (Good good : goods) {
      if (good.equals(chosenGood)) {
        count++;
      }
    }
    return count;
  }
  
  public Set<Good> getUniqueGoods(){
    return new HashSet<Good>(goods);
  }
  
  public List<Good> getAllGoods(){
    return new ArrayList<>(goods);    
  }
  
  public void addGood(Good good) {
    goods.add(good);
  }

  public void clearGoods() {
    goods.clear();
  }
}
