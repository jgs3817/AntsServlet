import java.util.ArrayList;

public class DataSend {
    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID;

    public DataSend(){
        antData = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> init = new ArrayList<Integer>();
        ArrayList<Integer> init2 = new ArrayList<Integer>();
        init.add(1);
        init.add(2);
        antData.add(init);
        init2.add(3);
        init2.add(4);
        antData.add(init2);

        videoID = "videoID12345";

        frameID = 5;
    }

    public String getVideoID(){
        return videoID;
    }

    public int getFrameID(){
        return frameID;
    }
}
