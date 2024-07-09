import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AlternationController {
    @FXML
    private LineChart<Number, Number> graphicAlternation;
    @FXML
    private NumberAxis xAxisAlternation;
    @FXML
    private NumberAxis yAxisAlternation;
    @FXML
    private ScrollBar alternationScrollBar;
    @FXML
    private ScrollBar noiseScrollBar;
    @FXML
    private Spinner cyclesSpinner;

    @Setter
    private Stage alterStage;
    private GraphicController graphicController;
    private ECGCollection ecgCollection;

    @FXML
    private void initialize(){
        graphicController = Main.getGraphicController();
        ecgCollection = new ECGCollection(graphicController);

        ecgCollection.appendArray();
        //Инициализация движков значениями минимума, максимума и текущего:
        alternationScrollBar.setMin(0.0);
        alternationScrollBar.setMax(0.3);
        alternationScrollBar.setValue(graphicController.getModel().getLevelAlternation());

        noiseScrollBar.setMin(0.0);
        noiseScrollBar.setMax(graphicController.getModel().getMaxNoise());
        noiseScrollBar.setValue(graphicController.getModel().getLevelNoise());

        cyclesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,100, graphicController.getModel().getCountCycle()));

        //Следить за изменениями альтернации:
        alternationScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementAlternation((Double) newStatus)
        );

        //Следить за изменениями шума:
        noiseScrollBar.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementNoise((Double) newStatus)
        );

        //Следить за изменениями количества циклов:
        cyclesSpinner.valueProperty().addListener((observable, oldStatus, newStatus) ->
                replacementCycles((Integer) newStatus)
        );


        buildGraphicAlternation(ecgCollection.getValueFunctionAndTime());
    }

    /*
    * Функция закрывает окно для демонстрации альтернации зубца Т - кнопка "Изменить параметры"
    * */
    @FXML
    private void closeClick(){
        alterStage.close();
    }

    /*
     * Функция открывает окно для демонстрации сглаживания
     * */
    @FXML
    private void smoothGraphic() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("Noise.fxml"));
        AnchorPane alt = (AnchorPane) loader.load();

        Stage noiseStage = new Stage();

        noiseStage.setTitle("КП5");
        noiseStage.initModality(Modality.WINDOW_MODAL);
        noiseStage.initOwner(Main.getPrimaryStage());

        Scene scene = new Scene(alt);

        NoiseController noiseController = loader.getController();
        noiseController.setNoiseStage(noiseStage);

        noiseStage.setScene(scene);
        noiseStage.showAndWait();
    }

    /*
    * Функция построения графика альтернации зубца Т
    * */
    private void buildGraphicAlternation(List<ECGCollection.Array> valueFunctionAndTime){
        yAxisAlternation.setAutoRanging(false);
        yAxisAlternation.setLowerBound(-0.5);
        yAxisAlternation.setUpperBound(1.5);
        yAxisAlternation.setTickUnit(0.5);

        xAxisAlternation.setAutoRanging(false);
        xAxisAlternation.setUpperBound(graphicController.getModel().getCycleTime() * graphicController.getModel().getCountCycle());
        xAxisAlternation.setTickUnit(graphicController.getModel().getCycleTime());

        graphicAlternation.setCreateSymbols(false);

        XYChart.Series<Number, Number> graphic = new XYChart.Series<>();
        graphic.setName("Альтернация зубца Т");

        for(int i = 0; i < ecgCollection.getValueFunctionAndTime().size(); i++) {
            graphic.getData().add(new XYChart.Data<>(valueFunctionAndTime.get(i).getArray()[0], valueFunctionAndTime.get(i).getArray()[1]));
        }

        graphicAlternation.getData().add(graphic);
    }

    /*
    * Функция изменения значения альтернации (нового)
    * */
    private void replacementAlternation(Double newStatus){
        graphicController.getModel().setLevelAlternation(newStatus);
        graphicAlternation.getData().clear();
        ecgCollection.appendArray();
        buildGraphicAlternation(ecgCollection.getValueFunctionAndTime());
    }

    /*
     * Функция изменения значения шума (нового)
     * */
    private void replacementNoise(Double newStatus){
        graphicController.getModel().setLevelNoise(newStatus);
        graphicAlternation.getData().clear();
        ecgCollection.appendArray();
        buildGraphicAlternation(ecgCollection.getValueFunctionAndTime());
    }

    /*
     * Функция изменения значения количества циклов (нового)
     * */
    private void replacementCycles(Integer newStatus){
        graphicController.getModel().setCountCycle(newStatus);
        graphicAlternation.getData().clear();
        ecgCollection.appendArray();
        buildGraphicAlternation(ecgCollection.getValueFunctionAndTime());
    }

    @FXML
    private Boolean handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
        if (file != null) {
            if (!file.getPath().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }
            return ecgCollection.exportToTXT(file.getAbsolutePath());
        }
        return true;
    }
}
