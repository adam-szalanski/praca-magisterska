package com.example.bigdataloadingexample.plot;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class BarChartExample {

    public static final String CHART_DIRECTORY = "charts/";

    public static void main(String[] args) {
        BarChartExample example = new BarChartExample();
        example.saveChartAsPNG();
    }

    private void saveChartAsPNG() {
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600)
                .theme(Styler.ChartTheme.Matlab)
                .title("Porównanie Serii Danych")
                .xAxisTitle("Kategoria")
                .yAxisTitle("Wartość")
                .build();

        // Dostosowanie stylu wykresu
        CategoryStyler styler = chart.getStyler();


        styler.setChartTitleVisible(true);
        styler.setLegendPosition(Styler.LegendPosition.InsideNW);
        styler.setMarkerSize(1);
        styler.setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Bar);

        // Dodawanie danych do wykresu
        CategorySeries series1 = chart.addSeries("Seria 1", Arrays.asList("A", "B", "C", "C"), Arrays.asList(4, 5, 9, 1));
        CategorySeries series2 = chart.addSeries("Seria 2", Arrays.asList("A", "B", "C", "C"), Arrays.asList(7, 6, 5, 1));
        series1.setFillColor(Color.CYAN);
        series2.setFillColor(Color.RED);
        series1.setLabel("Seria X");

        styler.setHasAnnotations(true);
        styler.setAnnotationsFont(new Font("Arial", Font.PLAIN, 20));
        styler.setAnnotationsFontColor(Color.BLACK);
        styler.setAnnotationsPosition(0.9f);

        chart.updateCategorySeries("Seria 1", Arrays.asList("A", "B", "C", "C"), Arrays.asList(4, 5, 9, 2), null);


        // Zapis do pliku PNG
        try {
            BitmapEncoder.saveBitmap(chart, CHART_DIRECTORY + "./Porownanie_Serii_Danych", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            log.error("Błąd zapisu wykresu do pliku PNG: {}", e.getMessage());
        }
    }
}
