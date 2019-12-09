import com.google.gson.Gson;
import com.google.gson.JsonArray;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@WebServlet(urlPatterns={"/submit_page", "/landingpage", "/FBpage", "/init"},loadOnStartup = 1)
public class ServletAnts extends HttpServlet {


    private ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();
    private String videoID;
    private int frameID;

    public ServletAnts(){}

//    static void postLandingData(){
//        SubmitData submitData = new SubmitData();
//        Gson gson = new Gson();
//        String jsonString = gson.toJson(submitData);
//        byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);
//
//        HttpURLConnection conn = null;
//
//        try{
//            URL myURL = new URL("http://localhost:8080/AntsServlet/landing_page");
//            conn = null;
//            conn = (HttpURLConnection) myURL.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Accept", "text/html");
//            conn.setRequestProperty("charset", "utf-8");
//            conn.setDoOutput(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try (OutputStream outputStream = conn.getOutputStream()) {
//            outputStream.write(body,0,body.length);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
//            String inputLine;
//            while ((inputLine = bufferedReader.readLine()) != null) {
//                System.out.println(inputLine);
//            }
//            bufferedReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        String path = req.getServletPath();


        switch(path){
            case "/submit_page":{

                // Receive SubmitData
                String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                //System.out.println(reqBody);
                resp.setContentType("text/html");
                resp.getWriter().write("Data submitted!");
                Gson gson = new Gson();
                SubmitData submitData = gson.fromJson(reqBody, SubmitData.class);
                antData = submitData.getAntData();
                videoID = submitData.getVideoID();
                frameID = submitData.getFrameID();

                System.out.println("Ant data: " + antData);
                System.out.println(videoID);
                System.out.println(frameID);

                //Insert into DB
                String dbUrl = "jdbc:postgresql://localhost:5432/postgres";


                try {
                    Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
                    Statement s=conn.createStatement();

                    for(int i=0; i < antData.size(); i++) {

                        String submitQuery = "--video\n" +
                                "DO $$\n" +
                                "BEGIN\n" +
                                "IF NOT EXISTS (\n" +
                                "\tSELECT * FROM video WHERE video_id ='" + videoID + "')\n" +
                                "\tTHEN\n" +
                                "\t\tINSERT INTO video (video_id) VALUES ('" + videoID + "');\n" +
                                "\tEND IF;\n" +
                                "END $$;\n" +
                                "\n" +
                                "--ant\n" +
                                "DO $$\n" +
                                "BEGIN\n" +
                                "IF NOT EXISTS (\n" +
                                "\tSELECT * FROM ant WHERE ant_id =" + antData.get(i).get(0) + "AND video_id='" + videoID + "')\n" +
                                "\tTHEN\n" +
                                "\t\tINSERT INTO ant (ant_id, video_id) VALUES (" + antData.get(i).get(0) + ",'" + videoID + "');\n" +
                                "\tEND IF;\n" +
                                "END $$;\n" +
                                "\n" +
                                "\n" +
                                "--frame\n" +
                                "DO $$\n" +
                                "BEGIN\n" +
                                "IF NOT EXISTS(\n" +
                                "\tSELECT * FROM frame WHERE video_id = '" + videoID + "'AND frame_id = " + frameID +")\n" +
                                "\tTHEN\n" +
                                "\t\tINSERT INTO frame (video_id, frame_id) values ('"+ videoID +"', "+ frameID +");\n" +
                                "\tEND IF;\n" +
                                "END $$;" +
                                "--coordinates\n" +
                                "INSERT INTO coordinates (ant_id, frame_id, x_coord, y_coord, video_id) values (" + antData.get(i).get(0) + "," + frameID + ", " + antData.get(i).get(1) + ", " + antData.get(i).get(2) + ", '" + videoID + "');";

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
            break;
            case "/FBpage":{

                // Receive FBData Request
                String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqBody);

                Gson gson = new Gson();
                FBData fbData = gson.fromJson(reqBody, FBData.class);
                System.out.println("frameID");
                System.out.println(fbData.getFrameID());

                // Fetch data from resources
                String file_name= String.format("%05d",fbData.getFrameID());
                String filePath ="./vid_1/" + file_name + ".png";
                BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

                // Convert Image into byte and store it in class FBData
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", bos);
                byte [] image_byte = bos.toByteArray();
                fbData.setImageByte(image_byte);

                // Send FBData object over
                Gson respGson = new Gson();
                String jsonString = respGson.toJson(fbData);
                //byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);

                resp.setContentType("application/json");
                resp.getWriter().write(jsonString);

            }
            break;
            case "/landingpage":{

                //Receive vid_id
                String reqVidID = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqVidID);

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

                    ResultSet rset=s.executeQuery(mostRecentLabelledData);

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

                // Fetch data from resources
                String file_name= String.format("%05d",landingData.getFrameID());
                String filePath ="./"+ landingData.getVideoID() +"/" + file_name + ".png";
                BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

                // Convert Image into byte and store it in class LandingData
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", bos);
                byte [] image_byte = bos.toByteArray();
                landingData.setImageByte(image_byte);

                // Send LandingData object over
                Gson respGson = new Gson();
                String jsonString = respGson.toJson(landingData);
                byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);

                resp.setContentType("application/json");
                resp.getWriter().write(jsonString);
            }
            break;
            case "/init":{

                ArrayList<Integer> vidSize = new ArrayList<Integer>();
                vidSize.add(0);
                vidSize.add(9);
                vidSize.add(5);
                vidSize.add(3);
                vidSize.add(6);

                //Receive Init request
                String reqInit = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(reqInit);

                String dbUrl = "jdbc:postgresql://localhost:5432/postgres";

                ArrayList<InitData> initDataArrayList = new ArrayList<InitData>();

                for(int i=1; i<5; i++){
                    InitData initData = new InitData();
                    initData.setVideoID("vid_" + i);

                    // Query DB for progress
                    try {

                        Connection conn= DriverManager.getConnection(dbUrl, "postgres", "winn");
                        Statement s=conn.createStatement();

                        String progressQuery = "SELECT MAX(frame_id)\n" +
                                "FROM coordinates \n" +
                                "WHERE video_id = '"+ initData.getVideoID() +"' ";

                        ResultSet rset = s.executeQuery(progressQuery);

                        while(rset.next()){
                            //System.out.println(rset.getInt("ant_id")+" "+ rset.getInt("x_coord") + " " + rset.getInt("y_coord"));
                            int progressNum = rset.getInt("max");
                            initData.setProgress(progressNum);
                        }

                        initData.setProgress(vidSize.get(i));


                        //String filePath = "./" + initData.getVideoID();
                        //File file = new File(String.valueOf(getClass().getClassLoader().getResource("filePath")));
                        //int progressDeno = file.listFiles().length;
                        //initData.setProgress(progressDeno);

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


                    // Fetch data from resources
                    String file_name= String.format("%05d",1);
                    String filePath ="./" + initData.getVideoID() + "/" + file_name + ".png";
                    System.out.println(filePath);
                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(filePath));

                    // Convert Image into byte and store it in class FBData
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bos);
                    byte [] image_byte = bos.toByteArray();
                    initData.setImageByte(image_byte);


                    initDataArrayList.add(initData);

                    System.out.println("test1");

                }


                for(InitData i : initDataArrayList){
                    i.printInitData();
                }


                // Send ArrayList of InitData object over
                Gson respGson = new Gson();
                String jsonString = respGson.toJson(initDataArrayList);
                //byte[] body = jsonString.getBytes(StandardCharsets.UTF_8);
                System.out.println(jsonString);

                resp.setContentType("application/json");
                resp.getWriter().write(jsonString);





            }


        }



    }
}
