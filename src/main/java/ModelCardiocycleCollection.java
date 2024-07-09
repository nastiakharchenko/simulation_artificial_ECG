import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


public class ModelCardiocycleCollection {
    @Getter
    private static List<ModelCardiocycle> modelCardiocycleCollection;
    @Getter
    private Double cycleTime;
    @Setter
    @Getter
    private Integer countCycle = 30;
    @Setter
    @Getter
    private Double levelAlternation = 0.1;
    @Setter
    @Getter
    private Double levelNoise = 0.0;
    @Getter
    private Double maxNoise = 0.05;
    private Double[] limitTime = new Double[]{ 0.3, 0.43, 0.463, 0.517, 0.56, 0.6, 0.8 };
    private Double[] minLimitAmplitude = new Double[]{ -0.2, -0.3, -0.3, -0.3, -0.1, -0.3 };
    private Double[] maxLimitAmplitude = new Double[]{ 0.5, 0.3, 1.1, 0.3, 0.1, 0.8 };

    public ModelCardiocycleCollection(Double heartRatePass){
        setCycleTime(heartRatePass);

        modelCardiocycleCollection = new ArrayList<>();
        for(int i = 0; i < maxLimitAmplitude.length; i++){
            modelCardiocycleCollection.add(new ModelCardiocycle(minLimitAmplitude[i], maxLimitAmplitude[i], limitTime[i], limitTime[i+1]));
        }

        //Задание первоначальных значений:
        getModelCardiocycleCollectionIndex("P").setAmplitudeCurrent(0.1);
        getModelCardiocycleCollectionIndex("Q").setAmplitudeCurrent(-0.1);
        getModelCardiocycleCollectionIndex("R").setAmplitudeCurrent(1.0);
        getModelCardiocycleCollectionIndex("S").setAmplitudeCurrent(-0.18);
        getModelCardiocycleCollectionIndex("ST").setAmplitudeCurrent(0.03);
        getModelCardiocycleCollectionIndex("T").setAmplitudeCurrent(0.2);

        getModelCardiocycleCollectionIndex("P").setWidthB1Current(0.03);
        getModelCardiocycleCollectionIndex("P").setWidthB2Current(0.03);
        getModelCardiocycleCollectionIndex("Q").setWidthB1Current(0.01);
        getModelCardiocycleCollectionIndex("Q").setWidthB2Current(0.01);
        getModelCardiocycleCollectionIndex("R").setWidthB1Current(0.01);
        getModelCardiocycleCollectionIndex("R").setWidthB2Current(0.01);
        getModelCardiocycleCollectionIndex("S").setWidthB1Current(0.01);
        getModelCardiocycleCollectionIndex("S").setWidthB2Current(0.01);
        getModelCardiocycleCollectionIndex("ST").setWidthB1Current(0.01);
        getModelCardiocycleCollectionIndex("ST").setWidthB2Current(0.05);
        getModelCardiocycleCollectionIndex("T").setWidthB1Current(0.05);
        getModelCardiocycleCollectionIndex("T").setWidthB2Current(0.04);
    }

    public void setCycleTime(Double cycleTime){
        this.cycleTime = 60.0 / cycleTime;
    }

    /*
    * Функция получения объекта коллекции по индексу
    * */
    public ModelCardiocycle getModelCardiocycleCollectionIndex(String prong){
        return switch(prong){
            case "P" -> modelCardiocycleCollection.get(0);
            case "Q" -> modelCardiocycleCollection.get(1);
            case "R" -> modelCardiocycleCollection.get(2);
            case "S" -> modelCardiocycleCollection.get(3);
            case "ST" -> modelCardiocycleCollection.get(4);
            case "T" -> modelCardiocycleCollection.get(5);
            default -> throw new IllegalStateException("Unexpected value: " + prong);
        };
    }

    /*
    * Функция подсчета значения суммы Гауссовских функций
    * */
    public Double functionGauss(Double time){
        Double summValueFunction = 0.0;
        for(int i = 0; i < 6; i++){
            if(time <= modelCardiocycleCollection.get(i).timeCurrent * cycleTime){
                summValueFunction += modelCardiocycleCollection.get(i).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(i).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(i).getWidthB1Current() * cycleTime, 2))   );
            }
            else{
                summValueFunction += modelCardiocycleCollection.get(i).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(i).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(i).getWidthB2Current() * cycleTime, 2))   );
            }
        }
        return summValueFunction;
    }

    /*
     * Функция подсчета значения суммы Гауссовских функций для отображения уровня альтернации зубца Т
     * */
    public Double functionGauss(Double time, Boolean status){
        double summValueFunction = 0.0;
        for(int i = 0; i < 5; i++){
            if(time <= modelCardiocycleCollection.get(i).timeCurrent * cycleTime){
                summValueFunction += modelCardiocycleCollection.get(i).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(i).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(i).getWidthB1Current() * cycleTime, 2))   );
            }
            else{
                summValueFunction += modelCardiocycleCollection.get(i).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(i).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(i).getWidthB2Current() * cycleTime, 2))   );
            }
        }
        if(!status){
            if(time <= modelCardiocycleCollection.get(5).timeCurrent * cycleTime){
                summValueFunction += modelCardiocycleCollection.get(5).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(5).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(5).getWidthB1Current() * cycleTime, 2))   );
            }
            else{
                summValueFunction += modelCardiocycleCollection.get(5).getAmplitudeCurrent() *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(5).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(5).getWidthB2Current() * cycleTime, 2))   );
            }
        } else{
            if(time <= modelCardiocycleCollection.get(5).timeCurrent * cycleTime){
                summValueFunction += modelCardiocycleCollection.get(5).getAmplitudeCurrent() * (1 + levelAlternation / modelCardiocycleCollection.get(5).getAmplitudeCurrent()) *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(5).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(5).getWidthB1Current() * cycleTime, 2))   );
            }
            else{
                summValueFunction += modelCardiocycleCollection.get(5).getAmplitudeCurrent() * (1 + levelAlternation / modelCardiocycleCollection.get(5).getAmplitudeCurrent()) *
                        Math.exp(   - Math.pow((time - modelCardiocycleCollection.get(5).getTimeCurrent() * cycleTime), 2) /
                                (2 * Math.pow(modelCardiocycleCollection.get(5).getWidthB2Current() * cycleTime, 2))   );
            }
        }
        return summValueFunction;
    }

    /*
     * Функция подсчета значения суммы Гауссовских функций для наложения шума
     * */
    public Double functionGauss(Double time, Boolean status, Boolean noise){
        double summValueFunction = functionGauss(time, status);

        if(noise){
            summValueFunction += (- levelNoise + Math.random() * 2 * levelNoise);
        }
        return summValueFunction;
    }
}
