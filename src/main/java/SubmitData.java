import java.io.Serializable;
import java.util.ArrayList;

public class SubmitData implements Serializable {
    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID;

    public ArrayList<ArrayList<Integer>> antDataReturn() {
        return antData;
    }

    public String videoIDReturn(){
        return videoID;
    }

    public int frameIDReturn(){
        return frameID;
    }
}
