import com.google.gson.Gson;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@WebServlet(urlPatterns={"/submitpage", "/landingpage", "/FBpage", "/init"},loadOnStartup = 1)
public class ServletAnts extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("text/html");
        resp.getWriter().write("This is a servlet");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        String path = req.getServletPath();

        switch(path){
            case "/submitpage":{

                // Receive SubmitData
                SubmitData submitData = receiveSubmitData(req, resp);

                System.out.println("Submit Data:");
                System.out.println("Ant data: " + submitData.getAntData());
                System.out.println(submitData.getVideoID());
                System.out.println(submitData.getFrameID());

                //Insert into DB
                insertIntoDB(submitData);


            }
            break;
            case "/FBpage":{

                // Receive FBData Request
                String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqBody);

                Gson gson = new Gson();
                FBData fbData = gson.fromJson(reqBody, FBData.class);
                System.out.println("frameID");
                System.out.println(fbData.getFrameID());

                // Selecting if its next or previous frame
                int chosenFrame;
                int overlayFrame;
                if(fbData.getFB()){
                    chosenFrame = fbData.getFrameID() + 1;
                } else {chosenFrame = fbData.getFrameID() - 1;
                }

                overlayFrame = chosenFrame - 1;
                //System.out.println("Chosen: " + chosenFrame);

                //Query Overlay Ant Data from DB
                String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
                try {

                    Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
                    Statement s=conn.createStatement();

                    String mostRecentLabelledData = "SELECT ant_id, x_coord, y_coord\n" +
                            "FROM coordinates\n" +
                            "WHERE video_id = '"+ fbData.getVideoID() +"' AND frame_id = " + (overlayFrame);

                    ResultSet rset=s.executeQuery(mostRecentLabelledData);

                    ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();

                    while(rset.next()){
                        //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                        ArrayList<Integer> oneAntData = new ArrayList<Integer>();
                        oneAntData.add(rset.getInt("ant_id"));
                        oneAntData.add(rset.getInt("x_coord"));
                        oneAntData.add(rset.getInt("y_coord"));
                        antData.add(oneAntData);
                    }
                    fbData.setOverlayAntData(antData);

                    rset.close();
                    s.close();
                    conn.close();
                }
                catch (SQLException except) {
                    int count = 1;
                    while (except != null) {
                        System.out.println("SQLException " + count);
                        System.out.println("Code: " + except.getErrorCode());
                        System.out.println("SqlState: " + except.getSQLState());
                        System.out.println("Error Message: " + except.getMessage());
                        except = except.getNextException();
                        count++;
                    }
                }

                // Fetch chosen frame from resources
                if(chosenFrame > 0) {
                    String file_name = String.format("%05d", chosenFrame);
                    String filePath = "./"+ fbData.getVideoID() +"/" + file_name + ".png";
                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

                    // Convert Image into byte and store it in class FBData
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bos);
                    byte[] imageByte = bos.toByteArray();
                    fbData.setImageByte(imageByte);
                } else {fbData.setError(true);}

                // Fetch overlay frame from resources
                if(overlayFrame > 0) {
                    String file_name = String.format("%05d", overlayFrame);
                    String filePath = "./vid_1/" + file_name + ".png";
                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

                    // Convert Image into byte and store it in class FBData
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bos);
                    byte[] overlayImageByte = bos.toByteArray();
                    fbData.setOverlayImageByte(overlayImageByte);
                }

                // Send FBData object over
                Gson respGson = new Gson();
                String jsonString = respGson.toJson(fbData);
                //byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);

                resp.setContentType("application/json");
                resp.getWriter().write(jsonString);

            }
            break;
            case "/landingpage":{

                System.out.println("landing page doPost");

                //Receive vid_id
                String reqVidID = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqVidID);

                LandingData landingData = queryLastLabelledFrame(reqVidID);

                System.out.println("test1");

                landingData.setImageByte(fetchFrameImage(landingData.getVideoID(), landingData.getFrameID()));

                // Fetch overlay image frame from resources
                if(landingData.getFrameID() > 1) {
                    landingData.setOverlayImageByte(fetchFrameImage(landingData.getVideoID(), landingData.getFrameID()-1));
                }

                System.out.println("OverlayFrameID");
                System.out.println((landingData.getFrameID()-1));

                //Query Overlay Ant Data from DB
                landingData.setOverlayAntData(queryAntData(landingData.getVideoID(), landingData.getFrameID()-1));

                // Send LandingData object over
                sendLandingData(resp, landingData);
            }
            break;
            case "/init":{
                //Receive Init request
                String reqInit = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqInit);

                String dbUrl = "jdbc:postgresql://localhost:5432/postgres";

                InitDataArrayList initDataArrayList = new InitDataArrayList();

                ArrayList<Integer> vidSize = new ArrayList<Integer>();
                vidSize.add(0);
                vidSize.add(9);
                vidSize.add(5);
                vidSize.add(3);
                vidSize.add(6);

                for(int i=1; i<5; i++){
                    InitData initData = new InitData();
                    initData.setVideoID("vid_" + i);

                    // Query DB for progress
                    initData.setProgress(queryProgress(initData.getVideoID(), vidSize.get(i)));

                    // Fetch thumbnail
                    initData.setImageByte(fetchFrameImage(initData.getVideoID(), 1));

                    // Convert the initData  object into json string
                    Gson respGson = new Gson();
                    String jsonString = respGson.toJson(initData);
                    initDataArrayList.addInitData(jsonString);

                    System.out.println("test1");
                }
                // Send ArrayList of jsonString over
                Gson respGson = new Gson();
                String jsonArrayListString = respGson.toJson(initDataArrayList);
                System.out.println(jsonArrayListString);
                resp.setContentType("text/html");
                resp.getWriter().write(jsonArrayListString);
            }
            break;
        }
    }

    /*
    This function
    takes in submitData object
    insert the data into the database
     */
    private void insertIntoDB(SubmitData submitData) {
        try {
            String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
            Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
            Statement s=conn.createStatement();

            for(int i=0; i < submitData.getAntData().size(); i++) {

                String submitQuery = "--video\n" +
                        "DO $$\n" +
                        "BEGIN\n" +
                        "IF NOT EXISTS (\n" +
                        "\tSELECT * FROM video WHERE video_id ='" + submitData.getVideoID() + "')\n" +
                        "\tTHEN\n" +
                        "\t\tINSERT INTO video (video_id) VALUES ('" + submitData.getVideoID() + "');\n" +
                        "\tEND IF;\n" +
                        "END $$;\n" +
                        "\n" +
                        "--ant\n" +
                        "DO $$\n" +
                        "BEGIN\n" +
                        "IF NOT EXISTS (\n" +
                        "\tSELECT * FROM ant WHERE ant_id =" + submitData.getAntData().get(i).get(0) + "AND video_id='" + submitData.getVideoID() + "')\n" +
                        "\tTHEN\n" +
                        "\t\tINSERT INTO ant (ant_id, video_id) VALUES (" + submitData.getAntData().get(i).get(0) + ",'" + submitData.getVideoID() + "');\n" +
                        "\tEND IF;\n" +
                        "END $$;\n" +
                        "\n" +
                        "\n" +
                        "--frame\n" +
                        "DO $$\n" +
                        "BEGIN\n" +
                        "IF NOT EXISTS(\n" +
                        "\tSELECT * FROM frame WHERE video_id = '" + submitData.getVideoID() + "'AND frame_id = " + submitData.getFrameID() +")\n" +
                        "\tTHEN\n" +
                        "\t\tINSERT INTO frame (video_id, frame_id) values ('"+ submitData.getVideoID() +"', "+ submitData.getFrameID() +");\n" +
                        "\tEND IF;\n" +
                        "END $$;" +
                        "--coordinates\n" +
                        "INSERT INTO coordinates (ant_id, frame_id, x_coord, y_coord, video_id) values (" + submitData.getAntData().get(i).get(0) + "," + submitData.getFrameID() + ", " + submitData.getAntData().get(i).get(1) + ", " + submitData.getAntData().get(i).get(2) + ", '" + submitData.getVideoID() + "');";

                s.execute(submitQuery);


            }
            conn.close();
        }
        catch (SQLException except) {
            int count = 1;
            while (except != null) {
                System.out.println("SQLException " + count);
                System.out.println("Code: " + except.getErrorCode());
                System.out.println("SqlState: " + except.getSQLState());
                System.out.println("Error Message: " + except.getMessage());
                except = except.getNextException();
                count++;
            }
        }
    }

    /*
    This function
    converts landingData object into json string
    sends this string back to the client
     */
    private void sendLandingData(HttpServletResponse resp, LandingData landingData) throws IOException {
        Gson respGson = new Gson();
        String jsonString = respGson.toJson(landingData);
        byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("application/json");
        resp.getWriter().write(jsonString);
    }

    /*
    This function
    receives the client request and data
    converts them into submitData object
    returns submitData object
    */
    private SubmitData receiveSubmitData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        //System.out.println(reqBody);
        resp.setContentType("text/html");
        resp.getWriter().write("Data submitted!");
        Gson gson = new Gson();
        return gson.fromJson(reqBody, SubmitData.class);
    }

    /*
    This function
    takes in videoID
    queries the last labelled frame and antData associated to that frame from the database
    stores them in landingData object
    returns landingData
     */
    private LandingData queryLastLabelledFrame(String reqVidID){
        LandingData landingData = new LandingData();
        landingData.setVideoID(reqVidID);

        //Query from DB
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        try {
            Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
            Statement s=conn.createStatement();

            String mostRecentLabelledData = "SELECT ant_id, x_coord, y_coord, frame_id\n" +
                    "FROM coordinates\n" +
                    "WHERE video_id = '" + reqVidID + "' and frame_id = (SELECT frame_id \n" +
                    "\t\t\t\t\tFROM frame \n" +
                    "\t\t\t\t\tWHERE video_id='"+ reqVidID +"' \n" +
                    "\t\t\t\t\tORDER BY frame_id DESC\n" +
                    "\t\t\t\t\tLIMIT 1)\n" +
                    "ORDER BY ant_id";

            ResultSet rset = s.executeQuery(mostRecentLabelledData);

            ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();
            while(rset.next()){
                //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                ArrayList<Integer> oneAntData = new ArrayList<Integer>();
                oneAntData.add(rset.getInt("ant_id"));
                oneAntData.add(rset.getInt("x_coord"));
                oneAntData.add(rset.getInt("y_coord"));
                landingData.setFrameID(rset.getInt("frame_id"));
                antData.add(oneAntData);
            }
            landingData.setAntData(antData);

            rset.close();
            s.close();
            conn.close();
        }
        catch (SQLException except) {
            int count = 1;
            while (except != null) {
                System.out.println("SQLException " + count);
                System.out.println("Code: " + except.getErrorCode());
                System.out.println("SqlState: " + except.getSQLState());
                System.out.println("Error Message: " + except.getMessage());
                except = except.getNextException();
                count++;
            }
        }
        return landingData;
    }

    /*
    This function
    takes in videoID and frameID
    fetches the frame image from resources corresponding to the inputs
    converts the frame image to byte format
    returns the image byte
    */
    private byte [] fetchFrameImage(String videoID, int frameID) throws IOException {
        // Fetch frame image from resources
        String file_name= String.format("%05d",frameID);
        String filePath ="./"+ videoID +"/" + file_name + ".png";
        BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

        // Convert Image into byte
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte [] imageByte = bos.toByteArray();

        return imageByte;
    }

    /*
    This function
    takes videoID and frameID
    queries ant_id, x_coord and y_coord corresponding to the inputs
    stores them in antData object
    returns antData object
     */
    private ArrayList<ArrayList<Integer>> queryAntData(String videoID, int frameID){
        ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();
        try {
            String dbUrl = "jdbc:postgresql://localhost:5432/postgres";

            Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
            Statement s=conn.createStatement();

            String antDataQuery = "SELECT ant_id, x_coord, y_coord\n" +
                    "FROM coordinates\n" +
                    "WHERE video_id = '"+ videoID +"' AND frame_id = " + (frameID);

            ResultSet rset=s.executeQuery(antDataQuery);


            while(rset.next()){
                //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                ArrayList<Integer> oneAntData = new ArrayList<Integer>();
                oneAntData.add(rset.getInt("ant_id"));
                oneAntData.add(rset.getInt("x_coord"));
                oneAntData.add(rset.getInt("y_coord"));
                antData.add(oneAntData);
            }

            rset.close();
            s.close();
            conn.close();
        }
        catch (SQLException except) {
            int count = 1;
            while (except != null) {
                System.out.println("SQLException " + count);
                System.out.println("Code: " + except.getErrorCode());
                System.out.println("SqlState: " + except.getSQLState());
                System.out.println("Error Message: " + except.getMessage());
                except = except.getNextException();
                count++;
            }
        }

        return antData;
    }

    /*
    This function
    takes in videoID and videoSize
    queries the number of labelled frames of that videoID
    stores it in an arraylist at the zeroth index
    stores the the total number of frames of that videoID as the first index
    returns the arraylist
     */
    private ArrayList<Integer> queryProgress(String videoID, int videoSize){

        ArrayList<Integer> progress = new ArrayList<>();

        try {

            String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
            Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
            Statement s=conn.createStatement();

            String progressQuery = "SELECT MAX(frame_id)\n" +
                    "FROM coordinates \n" +
                    "WHERE video_id = '"+ videoID +"' ";

            ResultSet rset = s.executeQuery(progressQuery);

            while(rset.next()){
                //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                progress.add(rset.getInt("max"));
            }

//                        String filePath = "./" + initData.getVideoID();
//                        File file = new File("filePath");
//                        int progressDeno = file.listFiles().length;
//                        initData.setProgress(progressDeno);

            rset.close();
            s.close();
            conn.close();
        }
        catch (SQLException except) {
            int count = 1;
            while (except != null) {
                System.out.println("SQLException " + count);
                System.out.println("Code: " + except.getErrorCode());
                System.out.println("SqlState: " + except.getSQLState());
                System.out.println("Error Message: " + except.getMessage());
                except = except.getNextException();
                count++;
            }
        }
        progress.add(videoSize);

        return progress;
    }
}
