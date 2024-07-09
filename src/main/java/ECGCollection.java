import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ECGCollection {

    //Класс для хранения значений вида <x, y> для построения графика
    public static class Array{
        @Getter
        public Double[] array;

        public Array(Double x, Double y){
            array = new Double[2];
            array[0] = x;
            array[1] = y;
        }
    }

    @Getter
    @Setter
    private Double alpha = 0.4;
    @Getter
    @Setter
    private Integer WoMov = 2;
    @Getter
    @Setter
    private Integer WoAdapt = 100;
    @Getter
    @Setter
    private Double ho = 0.05;
    @Getter
    @Setter
    private List<Array> valueFunctionAndTime;

    private final GraphicController graphicController;

    public ECGCollection(GraphicController graphicController1){
        this.graphicController = graphicController1;
        appendArray();
    }

    /*
    * Функция заполнения массива значений для простроения графиков
    * */
    public void appendArray(){
        valueFunctionAndTime = new ArrayList<>();

        boolean markStatus = true;
        double iterator;
        double time;

        int glitchIndex = (int) (Math.random() * graphicController.getModel().getCountCycle());

        for(int i = 0, k = 0; i < graphicController.getModel().getCountCycle(); i++, k++){
            iterator = 0.01;
            time = 0.0;

            if(i != glitchIndex){
                markStatus = k % 2 == 0;
            } else{
                k++;
            }

            for(int j = 0; j < (int) (1 / iterator); j++){
                valueFunctionAndTime.add(new Array(time + i * graphicController.getModel().getCycleTime()
                        , graphicController.getModel().functionGauss(time, markStatus, true)));
                time += iterator * graphicController.getModel().getCycleTime();
            }
        }
    }

    /*
     * Функция реализации метода экспоненциального сглаживания
     * */
    public List<Array> exponentialSmooth(){
        List<Array> value = new ArrayList<>();

        value.add(valueFunctionAndTime.get(0));

        for(int i = 1; i < valueFunctionAndTime.size(); i++){
            value.add(new Array(valueFunctionAndTime.get(i).getArray()[0],(value.get(i-1).getArray()[1] + alpha * (valueFunctionAndTime.get(i).getArray()[1] - value.get(i-1).getArray()[1]))));
        }

        return value;
    }

    /*
     * Функция реализации метода скользящего окна
     * */
    public List<Array> movingAverageSmooth(){
        List<Array> value = new ArrayList<>();
        value.add(valueFunctionAndTime.get(0));

        int W = 0;
        double mu = 1.0;
        for(int i = 1; i < valueFunctionAndTime.size(); i++){
            value.add(new Array(valueFunctionAndTime.get(i).getArray()[0],(value.get(i-1).getArray()[1] + mu * (valueFunctionAndTime.get(i + W).getArray()[1] - valueFunctionAndTime.get(i-1-W).getArray()[1]))));
            if(W < WoMov && i < WoMov){
                W++;
                mu = 1.0 / (1 + 2 * W);
            }
            if((valueFunctionAndTime.size() - i - 1) < WoMov){
                W--;
                mu = 1.0 / (1 + 2 * W);
            }
        }

        return value;
    }

    /*
     * Функция реализации метода адаптивного сглаживания
     * */
    public List<Array> adaptiveSmooth(){
        List<Array> value = new ArrayList<>();
        value.add(valueFunctionAndTime.get(0));

        Integer[] arrayW = new Integer[valueFunctionAndTime.size()];

        int W = 0;
        arrayW[0] = W;

        double mu;
        for(int i = 1; i < valueFunctionAndTime.size(); i++){
            arrayW[i] = W;
            int count = 0;
            while(true){
                mu = 1.0 / (1 + 2 * arrayW[i]);
                if(count == 0){
                    value.add(new Array(valueFunctionAndTime.get(i).getArray()[0],(value.get(i-1).getArray()[1] + mu * (valueFunctionAndTime.get(i + arrayW[i]).getArray()[1] - valueFunctionAndTime.get(i-1-arrayW[i]).getArray()[1]))));
                    count++;
                } else{
                    value.set(i, new Array(valueFunctionAndTime.get(i).getArray()[0],(value.get(i-1).getArray()[1] + mu * (valueFunctionAndTime.get(i + arrayW[i]).getArray()[1] - valueFunctionAndTime.get(i-1-arrayW[i]).getArray()[1]))));
                }

                if(Math.abs(value.get(i).getArray()[1] - valueFunctionAndTime.get(i).getArray()[1]) > ho){
                    arrayW[i] -= 1;
                } else{
                    break;
                }
            }
            if(W < WoAdapt && i < WoAdapt){
                W++;
            }
            if((valueFunctionAndTime.size() - i) <= WoAdapt){
                W--;
            }
        }

//        for(int j = 0; j < arrayW.length - 1; j++){
//            if(arrayW[j+1] - arrayW[j] > 1){
//                arrayW[j+1] = arrayW[j] + 1;
//            }
//        }
//
//        for(int j = arrayW.length - 1; j > 0; j--){
//            if(arrayW[j-1] - arrayW[j] > 1){
//                arrayW[j-1] = arrayW[j] + 1;
//            }
//        }
//
//        for(int i = 1; i < valueFunctionAndTime.size(); i++){
//            mu = 1.0 / (1 + 2 * arrayW[i]);
//            value.set(i, new Array(valueFunctionAndTime.get(i).getArray()[0],(value.get(i-1).getArray()[1] + mu * (valueFunctionAndTime.get(i + arrayW[i]).getArray()[1] - valueFunctionAndTime.get(i-1-arrayW[i]).getArray()[1]))));
//        }

        return value;
    }

    public Boolean exportToTXT(final String filename) {
        if (valueFunctionAndTime.isEmpty()) {
            return false;
        }
        final StringBuilder strBuild = new StringBuilder();
        for(int i = 0; i < valueFunctionAndTime.size(); i+=7){
            for(int j = i; j < i + 7; j++){
                strBuild.append(valueFunctionAndTime.get(j).getArray()[1].toString()).append("    ");
            }
            strBuild.append("\n");
        }

        try (final BufferedWriter buffRead = Files.newBufferedWriter(Paths.get(filename))) {
            buffRead.write(strBuild.toString());
        } catch (final IOException e) {
            return false;
        }
        return true;
    }
}
