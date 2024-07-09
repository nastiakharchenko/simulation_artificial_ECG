import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class GraphicController {
    @FXML
    private LineChart<Number, Number> graphic;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private ScrollBar amplitudeScrollBar;
    @FXML
    private ScrollBar timeScrollBar;
    @FXML
    private ScrollBar widthB1ScrollBar;
    @FXML
    private ScrollBar widthB2ScrollBar;

    @FXML
    private Slider heartRateSLider;

    @FXML
    private RadioButton variableP;
    @FXML
    private RadioButton variableQ;
    @FXML
    private RadioButton variableR;
    @FXML
    private RadioButton variableS;
    @FXML
    private RadioButton variableST;
    @FXML
    private RadioButton variableT;

    @Getter
    @Setter
    private ModelCardiocycleCollection model;
    private String radioButtonProng = "P";

    public GraphicController() {}

    @FXML
    private void initialize(){
        model = new ModelCardiocycleCollection(60.0);

        //Групировать RadioButtons
        ToggleGroup group = new ToggleGroup();
        variableP.setToggleGroup(group);
        variableQ.setToggleGroup(group);
        variableR.setToggleGroup(group);
        variableS.setToggleGroup(group);
        variableST.setToggleGroup(group);
        variableT.setToggleGroup(group);

        //Настройка слайдера ЧСС
        heartRateSLider.setMin(0.0);
        heartRateSLider.setMax(220.0);
        heartRateSLider.setValue(60.0 / model.getCycleTime());
        heartRateSLider.setShowTickMarks(true);
        heartRateSLider.setShowTickLabels(true);


        //Слушать зубцы
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldValue, Toggle newValue){
                RadioButton selectedBtn = (RadioButton) newValue;
                radioButtonProng = selectedBtn.getText();

                //Присваивание значений минимума и максимума для параметров:
                amplitudeScrollBar.setMin(model.getModelCardiocycleCollectionIndex(radioButtonProng).getAmplitudeMin());
                amplitudeScrollBar.setMax(model.getModelCardiocycleCollectionIndex(radioButtonProng).getAmplitudeMax());
                amplitudeScrollBar.setValue(model.getModelCardiocycleCollectionIndex(radioButtonProng).getAmplitudeCurrent());

                timeScrollBar.setMin(model.getModelCardiocycleCollectionIndex(radioButtonProng).getTimeMin() * model.getCycleTime());
                timeScrollBar.setMax(model.getModelCardiocycleCollectionIndex(radioButtonProng).getTimeMax() * model.getCycleTime());
                timeScrollBar.setValue(model.getModelCardiocycleCollectionIndex(radioButtonProng).getTimeCurrent() * model.getCycleTime());

                widthB1ScrollBar.setMin(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB1Min() * model.getCycleTime());
                widthB1ScrollBar.setMax(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB1Max() * model.getCycleTime());
                widthB1ScrollBar.setValue(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB1Current() * model.getCycleTime());

                widthB2ScrollBar.setMin(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB2Min() * model.getCycleTime());
                widthB2ScrollBar.setMax(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB2Max() * model.getCycleTime());
                widthB2ScrollBar.setValue(model.getModelCardiocycleCollectionIndex(radioButtonProng).getWidthB2Current() * model.getCycleTime());
            }
        });

        //Слушать амплитуды
        amplitudeScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementAmplitude((Double) newStatus)
        );

        //Слушать время
        timeScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementTime((Double) newStatus)
        );

        //Слушать ширину B1
        widthB1ScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementB1((Double) newStatus)
        );

        //Слушать ширину B2
        widthB2ScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementB2((Double) newStatus)
        );

        //Слушать ЧСС
        heartRateSLider.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementHeartRateSLider((Double) newStatus)
        );

        printGraph();
    }

    /*
    * Функция построения графика
    * */
    private void printGraph(){
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-0.4);
        yAxis.setUpperBound(1.1);
        yAxis.setTickUnit(0.1);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(model.getCycleTime());
        xAxis.setTickUnit(0.1 * model.getCycleTime());

        graphic.setCreateSymbols(false);

        XYChart.Series<Number, Number> graphicSeriesNullFirst = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesP = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesQ = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesR = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesS = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesST = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesT = new XYChart.Series<>();
        XYChart.Series<Number, Number> graphicSeriesNullSecond = new XYChart.Series<>();

        graphicSeriesNullFirst.setName(" ");
        graphicSeriesP.setName("P");
        graphicSeriesQ.setName("Q");
        graphicSeriesR.setName("R");
        graphicSeriesS.setName("S");
        graphicSeriesST.setName("ST");
        graphicSeriesT.setName("T");
        graphicSeriesNullSecond.setName(" ");

        double iterator = 0.001;
        double time = 0.0;
        for(int i = 0; i < (int) (1 / iterator); i++){
            if(time < model.getModelCardiocycleCollectionIndex("P").getTimeMin() * model.getCycleTime()){
                graphicSeriesNullFirst.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time < model.getModelCardiocycleCollectionIndex("Q").getTimeMin() * model.getCycleTime()){
                graphicSeriesP.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time < model.getModelCardiocycleCollectionIndex("R").getTimeMin() * model.getCycleTime()){
                graphicSeriesQ.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time <= model.getModelCardiocycleCollectionIndex("S").getTimeMin() * model.getCycleTime()){
                graphicSeriesR.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time <= model.getModelCardiocycleCollectionIndex("ST").getTimeMin() * model.getCycleTime()){
                graphicSeriesS.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time < model.getModelCardiocycleCollectionIndex("T").getTimeMin() * model.getCycleTime()){
                graphicSeriesST.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else if(time < model.getModelCardiocycleCollectionIndex("T").getTimeMax() * model.getCycleTime()){
                graphicSeriesT.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            } else{
                graphicSeriesNullSecond.getData().add(new XYChart.Data<>(time, model.functionGauss(time)));
            }
            time += iterator * model.getCycleTime();
        }

        graphic.getData().add(graphicSeriesNullFirst);
        graphic.getData().add(graphicSeriesP);
        graphic.getData().add(graphicSeriesQ);
        graphic.getData().add(graphicSeriesR);
        graphic.getData().add(graphicSeriesS);
        graphic.getData().add(graphicSeriesST);
        graphic.getData().add(graphicSeriesT);
        graphic.getData().add(graphicSeriesNullSecond);
    }

    /*
    * Функция изменения значения амплитуды (нового)
    * */
    private void replacementAmplitude(Double newStatus){
        model.getModelCardiocycleCollectionIndex(radioButtonProng).setAmplitudeCurrent(newStatus);
        graphic.getData().clear();
        printGraph();
    }

    /*
     * Функция изменения значения времени (нового)
     * */
    private void replacementTime(Double newStatus){
        model.getModelCardiocycleCollectionIndex(radioButtonProng).setTimeCurrent(newStatus / model.getCycleTime());
        graphic.getData().clear();
        printGraph();
    }

    /*
     * Функция изменения значения ширины (нового) - B1
     * */
    private void replacementB1(Double newStatus){
        model.getModelCardiocycleCollectionIndex(radioButtonProng).setWidthB1Current(newStatus / model.getCycleTime());
        graphic.getData().clear();
        printGraph();
    }

    /*
     * Функция изменения значения ширины (нового) - B2
     * */
    private void replacementB2(Double newStatus){
        model.getModelCardiocycleCollectionIndex(radioButtonProng).setWidthB2Current(newStatus / model.getCycleTime());
        graphic.getData().clear();
        printGraph();
    }

    /*
     * Функция изменения значения ЧСС (нового)
     * */
    private void replacementHeartRateSLider(Double newStatus){
        model.setCycleTime(newStatus);
        graphic.getData().clear();
        printGraph();
    }

    /*
    * Функция вызова окна для генерации ЭКГ (уровня альтернации зубца Т) - кнопка "Генерация"
    * */
    @FXML
    private void generationClick() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("Alternation.fxml"));
        AnchorPane alt = (AnchorPane) loader.load();

        Stage alterStage = new Stage();

        alterStage.setTitle("КП4");
        alterStage.initModality(Modality.WINDOW_MODAL);
        alterStage.initOwner(Main.getPrimaryStage());

        Scene scene = new Scene(alt);

        AlternationController altController = loader.getController();
        altController.setAlterStage(alterStage);

        alterStage.setScene(scene);
        alterStage.showAndWait();
    }
}
