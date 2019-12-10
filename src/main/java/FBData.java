import java.io.Serializable;
import java.util.ArrayList;

public class FBData implements Serializable {

    private ArrayList<ArrayList<Integer>> antData;
    private String videoID;
    private int frameID = 2;
    private byte [] imageByte;
    private boolean fb;


    public ArrayList<ArrayList<Integer>> getAntData() {
        return antData;
    }

    public String getVideoID(){return this.videoID;}

    public int getFrameID(){return this.frameID;}

    public byte [] getImageByte(){return this.imageByte;}

    public boolean getFB(){
        return fb;
    }

    public void setVideoID(String videoID){
        this.videoID = videoID;
    }

    public void setFrameID(int frameID){
        this.frameID = frameID;
    }

    public void setImageByte(byte [] imageByteInput){imageByte = imageByteInput;}


}