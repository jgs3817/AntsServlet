import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FBData implements Serializable {

    private ArrayList<ArrayList<Integer>> overlayAntData;
    private String videoID;
    private int frameID;
    private byte [] imageByte;
    private byte [] overlayImageByte;
    private boolean fb;
    private boolean error = false;

    public ArrayList<ArrayList<Integer>> getOverlayAntDataAntData() {
        return overlayAntData;
    }

    public void setOverlayAntData(ArrayList<ArrayList<Integer>> antDataInput){
        this.overlayAntData = new ArrayList<>(antDataInput.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));

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

    public byte [] getOverlayImageByte(){return overlayImageByte;}

    public void setVideoID(String videoID){
        this.videoID = videoID;
    }

    public void setFrameID(int frameID){
        this.frameID = frameID;
    }

    public void setImageByte(byte [] imageByteInput){imageByte = imageByteInput;}

    public void setOverlayImageByte(byte [] overlayImageByteInput){overlayImageByte = overlayImageByteInput;}

    public void setError(boolean errorInput){error = errorInput;}

    public boolean getError(){return error;}


}
