# AntsServlet
Servlet for Ants Project

The protocol used in the client-server model of this project is the Hypertext Transfer Protocol (HTTP). 
All data transferred between the client and the server is sent as text/html.
The data to be transferred is also stored in an object which is converted from a GSon object to a JSon string.

There are 4 different URL patterns in the servlet, which are matched to the corresponding POST methods in the client.

URL Pattern : /init
Initialisation of landing page requires the app to display the thumbnails of 4 videos and their corresponding progress bars
postInit method is called after the user enters their username and presses the “Start” button to request for videoIDs, thumbnails and progresses for 4 videos
Receives the request, queries the information from the database and retrieves a thumbnail from the resources directory. This is stored in an object of class InitData, in ArrayList type, which is then sent back to the client as a JSon string

URL Pattern : /landingpage
Once the user selects one of the videos from the Landing page, the app has to display the last labelled frame, and relevant ant data
postLanding method is called, sending the videoID of the selected video to the servlet and requesting necessary information
Receives the request and queries the last labelled frame and the ant data corresponding to that frame from the database. The servlet also retrieves the last labelled image frame from the resources. In addition, it also queries overlay ant data and overlay frame image. This is stored in an object of class LandingData, which is sent back to the client as a JSon string

URL Pattern : /fbpage
(forwards/backwards page)
When the user clicks ‘Next’ or ‘Previous’ buttons, the app has to display the next or previous frame, their corresponding overlay frame and their ants positions
postFB method is called, sending an object of class FBData containing the current videoID and frameID and a boolean variable indicating whether the ‘Next’ or ‘Previous’ button was pressed
Receives the request, queries the ant data of the correct frame and retrieves the correct frame image. The same is done for the overlay frame as well. This is stored in the same object received from the client and sent back as a JSon string

URL Pattern : /submitpage
Once the user has labelled all the ants and clicks on the ‘Submit’ button to submit the labels.
postSubmit method is called, storing videoID, frameID and ants data (a two-dimensional ArrayList, each row containing antID and its x,y-coordinates) in an object of class SubmitData and sending the object to the servlet
Receives the submit data and insert them into the database
