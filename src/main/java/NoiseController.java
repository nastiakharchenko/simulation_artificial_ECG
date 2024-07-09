import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.List;

public class NoiseController {
    @FXML
    private LineChart<Number, Number> graphicNoise;
    @FXML
    private NumberAxis xAxisNoise;
    @FXML
    private NumberAxis yAxisNoise;
    @FXML
    private RadioButton noSmooth;
    @FXML
    private RadioButton exponential;
    @FXML
    private RadioButton movingAverage;
    @FXML
    private RadioButton adaptive;
    @FXML
    private Slider alpha;
    @FXML
    private Slider WoMov;
    @FXML
    private Slider WoAdapt;
    @FXML
    private Slider ho;
    @FXML
    private Label alphaCurrent;
    @FXML
    private Label WoMovCurrent;
    @FXML
    private Label WoAdaptCurrent;
    @FXML
    private Label hoCurrent;

    @Setter
    private Stage noiseStage;
    private String method = "Без сглаживания";
    private GraphicController graphicController;
    private ECGCollection ecgCollection;

    @FXML
    private void initialize(){
        graphicController = Main.getGraphicController();
        ecgCollection = new ECGCollection(graphicController);

        alphaCurrent.setText(ecgCollection.getAlpha().toString());
        WoMovCurrent.setText(ecgCollection.getWoMov().toString());
        WoAdaptCurrent.setText(ecgCollection.getWoAdapt().toString());
        hoCurrent.setText(ecgCollection.getHo().toString());

        alpha.setMin(0.0);
        alpha.setMax(1.6);
        alpha.setValue(ecgCollection.getAlpha());

        WoMov.setMin(0.0);
        WoMov.setMax(100.0);
        WoMov.setValue(ecgCollection.getWoMov());

        WoAdapt.setMin(0.0);
        WoAdapt.setMax(100.0);
        WoAdapt.setValue(ecgCollection.getWoAdapt());

        ho.setMin(0.01);
        ho.setMax(0.2);
        ho.setValue(ecgCollection.getHo());

        ToggleGroup group = new ToggleGroup();
        noSmooth.setToggleGroup(group);
        exponential.setToggleGroup(group);
        movingAverage.setToggleGroup(group);
        adaptive.setToggleGroup(group);

        alpha.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementAlpha((Double) newStatus)
        );

        WoMov.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementWoMov((Double) newStatus)
        );

        WoAdapt.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementWoAdapt((Double) newStatus)
        );

        ho.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementHo((Double) newStatus)
        );

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldValue, Toggle newValue){
                RadioButton selectedBtn = (RadioButton) newValue;
                method = selectedBtn.getText();
                graphicNoise.getData().clear();
                getMethod();
            }
        });

        getMethod();
    }

    /*
     * Функция изменения значения alpha (нового)
     * */
    public void replacementAlpha(Double newStatus){
        double ceilNumber = Math.ceil(newStatus * 100) / 100;
        alphaCurrent.setText(Double.toString(ceilNumber));
        ecgCollection.setAlpha(newStatus);
        graphicNoise.getData().clear();
        ecgCollection.appendArray();
        getMethod();
    }

    /*
     * Функция изменения значения Wo (нового) для метода скользящего окна
     * */
    public void replacementWoMov(Double newStatus){
        long ceilNumber = Math.round(newStatus);
        WoMovCurrent.setText(Long.toString(ceilNumber));
        ecgCollection.setWoMov(Math.toIntExact(Math.round(newStatus)));
        graphicNoise.getData().clear();
        ecgCollection.appendArray();
        getMethod();
    }

    /*
     * Функция изменения значения Wo (нового) для адаптивного сглаживания
     * */
    public void replacementWoAdapt(Double newStatus){
        long ceilNumber = Math.round(newStatus);
        WoAdaptCurrent.setText(Long.toString(ceilNumber));
        ecgCollection.setWoAdapt(Math.toIntExact(Math.round(newStatus)));
        graphicNoise.getData().clear();
        ecgCollection.appendArray();
        getMethod();
    }

    /*
     * Функция изменения значения ho (нового)
     * */
    public void replacementHo(Double newStatus){
        double ceilNumber = Math.ceil(newStatus * 100) / 100;
        hoCurrent.setText(Double.toString(ceilNumber));
        ecgCollection.setHo(newStatus);
        graphicNoise.getData().clear();
        ecgCollection.appendArray();
        getMethod();
    }

    /*
     * Функция построения графика шума
     * */
    public void buildGraphicNoise(List<ECGCollection.Array> valueFunctionAndTime){
        yAxisNoise.setAutoRanging(false);
        yAxisNoise.setLowerBound(-0.5);
        yAxisNoise.setUpperBound(1.5);
        yAxisNoise.setTickUnit(0.5);

        xAxisNoise.setAutoRanging(false);
        xAxisNoise.setUpperBound(graphicController.getModel().getCycleTime() * graphicController.getModel().getCountCycle());
        xAxisNoise.setTickUnit(graphicController.getModel().getCycleTime());

        graphicNoise.setCreateSymbols(false);

        XYChart.Series<Number, Number> graphic = new XYChart.Series<>();
        graphic.setName("Сглаживание шума");

        for(int i = 0; i < ecgCollection.getValueFunctionAndTime().size(); i++) {
            graphic.getData().add(new XYChart.Data<>(valueFunctionAndTime.get(i).getArray()[0], valueFunctionAndTime.get(i).getArray()[1]));
        }

        graphicNoise.getData().add(graphic);
    }

    private void getMethod(){
        switch(method){
            case "Без сглаживания" ->
                    {
                        buildGraphicNoise(ecgCollection.getValueFunctionAndTime());
                        alpha.setDisable(true);
                        WoMov.setDisable(true);
                        WoAdapt.setDisable(true);
                        ho.setDisable(true);
                    }
            case "Экспоненциальное" ->
                    {
                        buildGraphicNoise(ecgCollection.exponentialSmooth());
                        alpha.setDisable(false);
                        WoMov.setDisable(true);
                        WoAdapt.setDisable(true);
                        ho.setDisable(true);
                    }
            case "Скользящего среднего" ->
                    {
                        buildGraphicNoise(ecgCollection.movingAverageSmooth());
                        alpha.setDisable(true);
                        WoMov.setDisable(false);
                        WoAdapt.setDisable(true);
                        ho.setDisable(true);
                    }
            case "Адаптивного" ->
                    {
                        buildGraphicNoise(ecgCollection.adaptiveSmooth());
                        alpha.setDisable(true);
                        WoMov.setDisable(true);
                        WoAdapt.setDisable(false);
                        ho.setDisable(false);
                    }
            default -> throw new IllegalStateException("Unexpected value: " + method);
        };
    }
}
