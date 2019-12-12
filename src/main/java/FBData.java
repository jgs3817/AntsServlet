import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FBData implements Serializable {

    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID = 2;
    private byte [] imageByte;
    private boolean fb;

    public ArrayList<ArrayList<Integer>> getAntData() {
        return antData;
    }

    public void setAntData(ArrayList<ArrayList<Integer>> antDataInput){
        this.antData = new ArrayList<>(antDataInput.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));

//        for (int i = 0; i < antDataInput.size(); i++) {
//            ArrayList<Integer> individualAnt = new ArrayList<Integer>();
//            for (int j = 0; j < antDataInput.get(i).size(); j++) {
//                individualAnt.add(antDataInput.get(i).get(j));
//            }
//            this.antData.add(individualAnt);
//        }
    }



    public boolean getFB(){return fb;}

    public void setFB(boolean input){this.fb = input;}

    public String getVideoID(){return this.videoID;}

    public int getFrameID(){return this.frameID;}

    public byte [] getImageByte(){return this.imageByte;}

    public void setVideoID(String videoID){
        this.videoID = videoID;
    }

    public void setFrameID(int frameID){
        this.frameID = frameID;
    }

    public void setImageByte(byte [] imageByteInput){imageByte = imageByteInput;}


}
