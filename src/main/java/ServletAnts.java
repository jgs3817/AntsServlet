import com.google.gson.Gson;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@WebServlet(urlPatterns={"/submitpage", "/landingpage", "/fbpage", "/init"},loadOnStartup = 1)
public class ServletAnts extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("text/html");
        resp.getWriter().write("This is servletants");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        String path = req.getServletPath();

        switch(path){
            case "/submitpage":{
                // Receive SubmitData
                SubmitData submitData = receiveSubmitData(req, resp);

                //Insert into DB
                insertIntoDB(submitData);
            }
            break;
            case "/fbpage":{

                // Receive FBData Request
                String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                Gson gson = new Gson();
                FBData fbData = gson.fromJson(reqBody, FBData.class);

                // Selecting if its next or previous frame
                int chosenFrame;
                int overlayFrame;
                if(fbData.getFB()){
                    chosenFrame = fbData.getFrameID() + 1;
                } else {chosenFrame = fbData.getFrameID() - 1;
                }
                overlayFrame = chosenFrame - 1;

                //Query Overlay Ant Data from DB
                fbData.setOverlayAntData(queryAntData(fbData.getVideoID(),overlayFrame));

                // Fetch chosen frame from resources
                if(chosenFrame > 0) {
                    fbData.setImageByte(fetchFrameImage(fbData.getVideoID(),chosenFrame));
                }

                // Fetch overlay frame from resources
                if(overlayFrame > 0) {
                    fbData.setOverlayImageByte(fetchFrameImage(fbData.getVideoID(),overlayFrame));
                }

                // Send FBData object over
                Gson respGson = new Gson();
                String jsonString = respGson.toJson(fbData);

                resp.setContentType("application/json");
                resp.getWriter().write(jsonString);

            }
            break;
            case "/landingpage":{

                //Receive vid_id
                String reqVidID = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                LandingData landingData = new LandingData();

                landingData = queryLastLabelledFrame(reqVidID);

                System.out.println(landingData.getVideoID());
                System.out.println(landingData.getFrameID());
                System.out.println(landingData.getAntData());

                landingData.setImageByte(fetchFrameImage(landingData.getVideoID(), landingData.getFrameID()));

                // Send LandingData object over
                sendLandingData(resp, landingData);
            }
            break;
            case "/init":{
                //Receive Init request
                String reqInit = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                //String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
                String dbUrl = System.getenv("JDBC_DATABASE_URL");

                InitDataArrayList initDataArrayList = new InitDataArrayList();

                for(int i=1; i<5; i++){
                    InitData initData = new InitData();
                    initData.setVideoID("vid_" + i);

                    // Query DB for progress
                    initData.setProgress(queryProgress(initData.getVideoID()));

                    // Fetch thumbnail
                    initData.setImageByte(fetchFrameImage(initData.getVideoID(), 1));

                    // Convert the initData object into json string
                    Gson initGson = new Gson();
                    String jsonString = initGson.toJson(initData);
                    initDataArrayList.addInitData(jsonString);
                }

                // Send ArrayList of jsonString over
                Gson respGson = new Gson();
                String jsonArrayListString = respGson.toJson(initDataArrayList);
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
            String dbUrl = System.getenv("JDBC_DATABASE_URL");
            Connection conn= DriverManager.getConnection(dbUrl);
            Statement s=conn.createStatement();

            for(int i=0; i < submitData.getAntData().size(); i++) {

                String submitQuery = "INSERT INTO coordinates (ant_id, frame_id, x_coord, y_coord, video_id) values (" + submitData.getAntData().get(i).get(0) + "," + submitData.getFrameID() + ", " + submitData.getAntData().get(i).get(1) + ", " + submitData.getAntData().get(i).get(2) + ", '" + submitData.getVideoID() + "');";
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
        //String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        try {
            Connection conn= DriverManager.getConnection(dbUrl);
            Statement s=conn.createStatement();

            String mostRecentLabelledData = "SELECT ant_id, x_coord, y_coord, frame_id\n" +
                    "FROM coordinates\n" +
                    "WHERE video_id = '" + reqVidID + "' and frame_id = (SELECT frame_id \n" +
                    "\t\t\t\t\tFROM coordinates \n" +
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

        //System.out.println(landingData.getAntData());
        //System.out.println(landingData.getFrameID());


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
            String dbUrl = System.getenv("JDBC_DATABASE_URL");

            Connection conn= DriverManager.getConnection(dbUrl);
            Statement s=conn.createStatement();

            String antDataQuery = "SELECT ant_id, x_coord, y_coord\n" +
                    "FROM coordinates\n" +
                    "WHERE video_id = '"+ videoID +"' AND frame_id = " + (frameID);

            ResultSet rset=s.executeQuery(antDataQuery);


            while(rset.next()){
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
    private ArrayList<Integer> queryProgress(String videoID){

        ArrayList<Integer> progress = new ArrayList<>();

        try {

            String dbUrl = System.getenv("JDBC_DATABASE_URL");
            //String dbUrl = "jdbc:postgresql://localhost:5432/postgres";


            Connection conn= DriverManager.getConnection(dbUrl);
            Statement s=conn.createStatement();

            String progressQuery = "SELECT MAX(frame_id)\n" +
                    "FROM coordinates \n" +
                    "WHERE video_id = '" + videoID + "'";

            ResultSet rset = s.executeQuery(progressQuery);

            while(rset.next()){
                //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                progress.add(rset.getInt("max"));
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

        String filePath = Paths.get("").toAbsolutePath().toString();
        //System.out.println(filePath + "\\src\\main\\resources\\" + videoID);
        File directory = new File(filePath + "/src/main/resources/" + videoID);
        int fileCount = directory.listFiles().length;

        progress.add(fileCount);

        System.out.println("progress method:" + progress);

        return progress;
    }
}
