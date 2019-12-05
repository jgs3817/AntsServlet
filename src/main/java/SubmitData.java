import java.io.Serializable;
import java.util.ArrayList;

public class SubmitData implements Serializable {
    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID;

    public ArrayList<ArrayList<Integer>> getAntData() {
        return antData;
    }

    public String getVideoID(){
        return videoID;
    }

    public int getFrameID(){
        return frameID;
    }
}
