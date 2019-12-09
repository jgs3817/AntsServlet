import java.io.Serializable;
import java.util.ArrayList;

public class InitDataArrayList implements Serializable {

    private ArrayList<String> arrayJsonString = new ArrayList<String>();

    public void addInitData(String input){
        arrayJsonString.add(input);
    }

    public ArrayList<String> getArrayJsonString(){
        return arrayJsonString;
    }
}
