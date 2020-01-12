import java.io.Serializable;
import java.util.ArrayList;

public class InitData implements Serializable {
    private String videoID;
    private ArrayList<Integer> progress = new ArrayList<Integer>();
    private byte [] imageByte;

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public void setProgress(ArrayList<Integer> progressInput){
        progress = progressInput;
    }

    public void setImageByte(byte [] imageByteInput) {
        this.imageByte = imageByteInput;
    }

    public String getVideoID() {
        return videoID;
    }

    public byte[] getImageByte() {
        return imageByte;
    }

    public ArrayList<Integer> getProgress() {
        return progress;
    }

}
