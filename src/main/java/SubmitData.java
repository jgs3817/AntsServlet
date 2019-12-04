import java.io.Serializable;
import java.util.ArrayList;

public class SubmitData implements Serializable {
    private ArrayList<ArrayList<Integer>> antData;

    public ArrayList<ArrayList<Integer>> antDataReturn() {
        return antData;
    }
}
