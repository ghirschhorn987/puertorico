package org.hirschhorn.puertorico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ResultsUI {

  public static final int SCREEN_WIDTH = 1400;
  public static final int SCREEN_HEIGHT = 700;
  public static final int ROW_HEIGHT = 250;

  public enum Statistic {
    Game,
    Position,
    Wins,
    VictoryPoints,
    VictoryMarginPct,
    VictoryPointChips,
    VictoryPointBonus,
    HasWharf,
    HasHarbor,
    LargeBuldings,
    LargeBuildingsOccupied,
    CumulativeMoneyReceived,
    CumulativeGoodsProduced,
    MoneyAfterRound8,
  }

  public ResultsUI(List<GameResult> gameResults) {
    this.gameResults = gameResults;
  }

  private List<GameResult> gameResults;

  public void displayCharts(Stage stage) {    
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(1);
    grid.setVgap(1);
    grid.setPadding(new Insets(1, 1, 1, 1));

    //int columnCount = getStrategies().size();
    int columnCount = 3;
    
    List<ColumnConstraints> columns = new ArrayList<>();
    for (int i = 0; i < columnCount; i++) {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(100 / columnCount);
      columns.add(column);
    }
    grid.getColumnConstraints().addAll(columns);

    List<String> strategies = getStrategies();
    
    // row 0
    int rowOffset = 0;
    grid.add(buildWinsChart(Statistic.Wins, (SCREEN_WIDTH - 100) / columnCount, ROW_HEIGHT * 2), 0, rowOffset + 0);
    grid.add(buildGamesChart(Statistic.VictoryPoints, ((SCREEN_WIDTH - 100) / columnCount) * (columnCount - 1), ROW_HEIGHT * 2), 1, rowOffset + 0, columnCount - 1, 1);
     
    Statistic sortResultsByVariable = Statistic.VictoryMarginPct;

    List<Statistic> fixedScaleVariables = Arrays.asList(
        Statistic.VictoryMarginPct
    );
    List<Statistic> autoScaleVariables = Arrays.asList(
        Statistic.CumulativeMoneyReceived,
        Statistic.LargeBuldings,
        Statistic.VictoryPointChips,
        Statistic.MoneyAfterRound8,
        Statistic.LargeBuildingsOccupied,        
        Statistic.VictoryPointBonus,
        Statistic.CumulativeGoodsProduced,
        Statistic.HasWharf,
        Statistic.HasHarbor
    );
    List<Statistic> allVariables = new ArrayList<>(fixedScaleVariables);
    allVariables.addAll(autoScaleVariables);
    int lastRowNumber = addStatisticsCharts(grid, ++rowOffset, columnCount, strategies, allVariables, sortResultsByVariable);
    rowOffset = rowOffset + lastRowNumber;

//    for (Statistic variable : fixedScaleVariables) {
//      addPlayerChart(grid, ++rowOffset, columnCount, strategies, variable, sortResultsByVariable, 0.0, 3.5, 0.1);
//    }
//    for (Statistic variable : autoScaleVariables) {
//      addPlayerChart(grid, ++rowOffset, columnCount, strategies, variable, sortResultsByVariable);
//    }
        
    stage.setTitle("Puerto Rico Results");
    ScrollPane scroll = new ScrollPane();
    //scroll.setHbarPolicy(ScrollBarPolicy.ALWAYS);
    //scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
    scroll.setContent(grid);
    Scene scene = new Scene(scroll, SCREEN_WIDTH, SCREEN_HEIGHT);

    stage.setScene(scene);
    stage.show();    
  }

  private void addPlayerChart(
      GridPane grid,
      int rowOffset,
      int columnCount,
      List<String> strategies,
      Statistic variable,
      Statistic sortResultsByVariable) {
    addPlayerChart(grid, rowOffset, columnCount, strategies, variable, sortResultsByVariable, null, null, null);
  }
  
  private void addPlayerChart(
      GridPane grid,
      int rowOffset,
      int columnCount,
      List<String> strategies,
      Statistic variable,
      Statistic sortResultsByVariable,
      Double yAxisMin,
      Double yAxisMax,
      Double yAxisTick) {
    
    int columnWidth = (SCREEN_WIDTH - 100) / columnCount;
    int rowHeight = ROW_HEIGHT;
    for (int row = 0; row < 1; row++) {
      for (int col = 0; col < columnCount; col++) {
        String strategy = strategies.get(row * columnCount + col);
        LineChart<Number, Number> chart = buildPlayerChart(strategy, variable, sortResultsByVariable, yAxisMin, yAxisMax, yAxisTick, columnWidth, rowHeight);
        grid.add(chart, col, rowOffset + row);
      }
    }    
  }    
  
  private int addStatisticsCharts(
      GridPane grid,
      int rowOffset,
      int columnCount,
      List<String> strategies,
      List<Statistic> variables,
      Statistic sortResultsByVariable) {
    
    int columnWidth = (SCREEN_WIDTH - 100) / columnCount;
    int rowHeight = ROW_HEIGHT;
    int row = 0;
    int column = 0;
    for (Statistic variable : variables) {
      if (column >= columnCount) {
        column = 0;
        row++;
      }
      LineChart<Number, Number> chart = buildStatisticsChart(strategies, variable, sortResultsByVariable, null, null, null, columnWidth, rowHeight);
      grid.add(chart, column, row + rowOffset);
      column++;
    }
    return row;
  }    

  private LineChart<Number, Number> buildGamesChart(Statistic variable, int columnWidth, int rowHeight) {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Game");
    LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
    chart.setTitle("Victory Points");
    chart.setLegendSide(Side.BOTTOM);
    chart.setCreateSymbols(false);

    List<XYChart.Series> seriesList = new ArrayList<>();
    for (String strategy : getStrategies()) {
      XYChart.Series series = new XYChart.Series();
      series.setName(strategy);
      seriesList.add(series);
    }

    int i = 0;
    for (String strategy : getStrategies()) {
      XYChart.Series series = seriesList.get(i);
      List<PlayerResult> results = getPlayerResultsForStrategy(strategy);
      List<Number> values = getValues(results, variable);
      for (int x = 0; x < values.size(); x++) {
        series.getData().add(new XYChart.Data(x, values.get(x)));
      }
      chart.getData().add(series);
      i++;
    }
    
    chart.setPrefSize(columnWidth, rowHeight);
    return chart;
  }
  
  private BarChart<String, Number> buildWinsChart(Statistic variable, int columnWidth, int rowHeight) {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Player");
    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
    chart.setTitle("Wins");
    chart.setLegendVisible(false);

    BarChart.Series series = new BarChart.Series();
    for (String strategy : getStrategies()) {
      List<PlayerResult> results = getPlayerResultsForStrategy(strategy);
      List<Number> values = getValues(results, variable);
      int totalWins = 0;
      for (int x = 0; x < values.size(); x++) {
        totalWins += values.get(x).intValue();
      }
      series.getData().add(new XYChart.Data(strategy, totalWins));
    }

    chart.getData().add(series);   
    chart.setPrefSize(columnWidth, rowHeight);
    return chart;
  }

  private LineChart<Number, Number> buildPlayerChart(
      String strategy,
      Statistic variable,
      Statistic sortResultsByVariable,
      Double yAxisMin,
      Double yAxisMax,
      Double yAxisTick,
      int columnWidth,
      int rowHeight) {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    if (yAxisMin != null) {
      yAxis = new NumberAxis(yAxisMin, yAxisMax, yAxisTick);
    }
    LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
    chart.setTitle(strategy + " - " + variable);
    chart.setLegendSide(Side.LEFT);
    chart.setCreateSymbols(false);
    
    List<XYChart.Series> seriesList = new ArrayList<>();
    
    List<PlayerResult> playerResults = getPlayerResultsForStrategy(strategy);
    for (Integer position : getPlayerPositions(playerResults)) {
      XYChart.Series series = new XYChart.Series();
      series.setName("" + position);
      seriesList.add(series);
      List<PlayerResult> positionResultsSorted = getPlayerResultsForPosition(playerResults, position);
      Collections.sort(positionResultsSorted, getVariableValueComparator(sortResultsByVariable));
      List<Number> values = getValues(positionResultsSorted, variable);
      for (int x = 0; x < values.size(); x++) {
        series.getData().add(new XYChart.Data(x, values.get(x)));
      }
      chart.getData().add(series);
    }
    
    chart.setPrefSize(columnWidth, rowHeight);
    return chart;
  }

  private LineChart<Number, Number> buildStatisticsChart(
      List<String> strategies,
      Statistic variable,
      Statistic sortResultsByVariable,
      Double yAxisMin,
      Double yAxisMax,
      Double yAxisTick,
      int columnWidth,
      int rowHeight) {
    NumberAxis xAxis = new NumberAxis();
    NumberAxis yAxis = new NumberAxis();
    if (yAxisMin != null) {
      yAxis = new NumberAxis(yAxisMin, yAxisMax, yAxisTick);
    }
    LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
    chart.setTitle(variable.toString());
    chart.setLegendSide(Side.BOTTOM);
    chart.setCreateSymbols(false);
    
    List<XYChart.Series> seriesList = new ArrayList<>();
    for (String strategy : strategies) {
      XYChart.Series series = new XYChart.Series();
      seriesList.add(series);
      series.setName(strategy.substring(0, 4));

      List<PlayerResult> playerResultsSorted = getPlayerResultsForStrategy(strategy);
      Collections.sort(playerResultsSorted, getVariableValueComparator(sortResultsByVariable));
      List<Number> values = getValues(playerResultsSorted, variable);
      for (int x = 0; x < values.size(); x++) {
        series.getData().add(new XYChart.Data(x, values.get(x)));
      }
      chart.getData().add(series);
    }
    
    chart.setPrefSize(columnWidth, rowHeight);
    return chart;
  }

  private List<String> getStrategies() {
    Set<String> strategies = new HashSet<>();
    for (GameResult result : gameResults) {
      strategies.addAll(result.getStrategies());
    }
    List<String> strategiesSorted = new ArrayList<>(strategies);  
    Collections.sort(strategiesSorted);
    return strategiesSorted;
  }
  
  private List<PlayerResult> getPlayerResultsForStrategy(String strategy) {
    List<PlayerResult> playerResults = new ArrayList<>();
    for (GameResult gameResult : gameResults) {
      PlayerResult playerResult = gameResult.getPlayerResult(strategy);
      if (playerResult == null) {
        playerResult = new PlayerResult();
      }
      playerResults.add(playerResult);
    }
    return playerResults;    
  }

  private List<PlayerResult> getPlayerResultsForPosition(List<PlayerResult> playerResults, int position) {
    List<PlayerResult> positionPlayerResults = new ArrayList<>();
    for (PlayerResult playerResult : playerResults) {
      if (playerResult.getValue(Statistic.Position).intValue() == position) {
        positionPlayerResults.add(playerResult);
      }
    }
    return positionPlayerResults;    
  }
  
  private List<Integer> getPlayerPositions(List<PlayerResult> playerResults) {
    Set<Integer> positions = new HashSet<>();
    for (PlayerResult result : playerResults) {
      positions.add(result.getValue(Statistic.Position).intValue());
    }
    List<Integer> positionsSorted = new ArrayList<>(positions);  
    Collections.sort(positionsSorted);
    return positionsSorted;    
  }
  
  private List<Number> getValues(List<PlayerResult> playerResults, Statistic variableName) {
    List<Number> values = new ArrayList<>();
    for (PlayerResult result : playerResults) {
      Number val = result.getValue(variableName);
      if (val == null) {
        val = new Double(0);
      }
      values.add(val);
    }
    return values;
  }
  
  private static Comparator<PlayerResult> getVariableValueComparator(final Statistic variableName) {
    return new Comparator<PlayerResult>() {
      @Override
      public int compare(PlayerResult o1, PlayerResult o2) {
        Double result1 = new Double(0);
        Double result2 = new Double(0);
        Number val1 = o1.getValue(variableName);
        if (val1 != null) {
          result1 = new Double(val1.doubleValue());
        }
        Number val2 = o2.getValue(variableName);
        if (val2 != null) {
          result2 = new Double(val2.doubleValue());
        }
        return result1.compareTo(result2);
      }
    };
  }
  
}
