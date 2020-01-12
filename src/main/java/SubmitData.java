import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SubmitData implements Serializable {
    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID;

    public ArrayList<ArrayList<Integer>> getAntData() {
        return antData;
    }

    public String getVideoID(){return this.videoID;}

    public int getFrameID(){return this.frameID;}

    public void setVideoID(String videoIDInput){videoID = videoIDInput;}

    public void setFrameID(int frameIDInput){frameID = frameIDInput;}

    public void setAntData(ArrayList<ArrayList<Integer>> antDataInput) {
        antData = new ArrayList<>(antDataInput.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));
    }

}



