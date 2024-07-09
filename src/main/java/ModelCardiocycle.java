import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelCardiocycle {
    public Double amplitudeCurrent;
    public Double timeCurrent;
    public Double widthB1Current;
    public Double widthB2Current;

    public Double amplitudeMin;
    public Double timeMin;
    public Double widthB1Min;
    public Double widthB2Min;

    public Double amplitudeMax;
    public Double timeMax;
    public Double widthB1Max;
    public Double widthB2Max;

    public ModelCardiocycle(Double amplitudeMinPass, Double amplitudeMaxPass, Double timeMinPass, Double timeMaxPass){
        this.amplitudeMin = amplitudeMinPass;
        this.amplitudeMax = amplitudeMaxPass;
        this.amplitudeCurrent = (amplitudeMinPass + amplitudeMaxPass) / 2;

        this.timeMin = timeMinPass;
        this.timeMax = timeMaxPass;
        this.timeCurrent = (timeMinPass + timeMaxPass) / 2;

        this.widthB1Min = 0.0;
        this.widthB1Max = (timeMinPass + timeMaxPass) / 15;
        this.widthB1Current = (timeMinPass + timeMaxPass) / 20;

        this.widthB2Min = 0.0;
        this.widthB2Max = (timeMinPass + timeMaxPass) / 15;
        this.widthB2Current = (timeMinPass + timeMaxPass) / 20;
    }
}
