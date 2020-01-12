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

    public ArrayList<ArrayList<Integer>> getOverlayAntData() {
        return overlayAntData;
    }

    public void setOverlayAntData(ArrayList<ArrayList<Integer>> antDataInput){
        this.overlayAntData = new ArrayList<>(antDataInput.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));
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


}
