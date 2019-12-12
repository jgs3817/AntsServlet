import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LandingData {
    private ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> overlayAntData = new ArrayList<ArrayList<Integer>>();
    private String videoID;
    private int frameID;
    private byte [] imageByte;
    private byte [] overlayImageByte;



    public ArrayList<ArrayList<Integer>> getAntData() {
        return antData;
    }

    public ArrayList<ArrayList<Integer>> getOverlayAntData(){return overlayAntData;}

    public String getVideoID(){
        return videoID;
    }

    public int getFrameID(){
        return frameID;
    }

    public byte [] getImageByte(){return this.imageByte;}

    public byte [] getOverlayImageByte(){return overlayImageByte;}

    public void setVideoID(String videoID){
        this.videoID = videoID;
    }

    public void setFrameID(int frameID){
        this.frameID = frameID;
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

    public void setOverlayAntData(ArrayList<ArrayList<Integer>> overlayAntDataInput){
        this.overlayAntData = new ArrayList<>(overlayAntDataInput.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));
    }


    public void setImageByte(byte [] imageByteInput){this.imageByte = imageByteInput;}

    public void setOverlayImageByte(byte [] overlayImageByteInput){this.overlayImageByte = overlayImageByteInput;}

}
