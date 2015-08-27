package javafx.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PlaygroundLineChartSample extends Application {

  @Override
  public void start(Stage stage) {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.TOP_LEFT);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    grid.add(buildChart(4), 0, 0);
    grid.add(buildChart(2), 0, 1);
    Scene scene = new Scene(grid, 800, 600);
    stage.setScene(scene);
    stage.setTitle("Puerto Rico");
    stage.show();

  }

  private LineChart<Number, Number> buildChart(int scoreSize) {

    // defining the axes
    final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Game");
    // creating the chart
    final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
        xAxis, yAxis);

    lineChart.setTitle("Victory Points");
    // defining a series
    List<XYChart.Series> seriesList = new ArrayList<>();
    XYChart.Series series = new XYChart.Series();
    series.setName("Player 1");
    seriesList.add(series);
    series = new XYChart.Series();
    series.setName("Player 2");
    seriesList.add(series);
    series = new XYChart.Series();
    series.setName("Player 3");
    seriesList.add(series);

    Random random = new Random();
    for (int player = 0; player < seriesList.size(); player++) {
      series = seriesList.get(player);
      for (int i = 0; i < scoreSize; i++) {
        int score = random.nextInt(50);
        series.getData().add(new XYChart.Data(i, score));
      }
      lineChart.getData().add(series);
    }
    return lineChart;
  }

  public static void main(String[] args) {
    launch(args);
  }
}