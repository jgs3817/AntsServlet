import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static junit.framework.TestCase.failNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.when;

public class TestServlet extends ServletAnts {
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Before
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testDoGet() throws IOException, ServletException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        ServletAnts testServlet = new ServletAnts();
        testServlet.doGet(request,response);
        String output = stringWriter.getBuffer().toString();
        Assert.assertThat(output,is(equalTo("This is servletants")));
    }

//    @Test
//    public void testReceiveSubmitData() throws IOException, ServletException {
//        when(request.getServletPath()).thenReturn("/submitpage");
//
//        //when(request.getReader().lines().collect(Collectors.joining())).thenReturn("{\"antData\":[[1,213,108],[2,223,151]],\"videoID\":\"vid_1\",\"frameID\":2}");
//
//        // Expected output from the receiveSubmitData method
//        SubmitData expectedSubmitData = new SubmitData();
//        expectedSubmitData.setVideoID("vid_1");
//        expectedSubmitData.setFrameID(2);
//        ArrayList<ArrayList<Integer>> antData = new ArrayList<ArrayList<Integer>>();
//
//        ArrayList<Integer> antOneData = new ArrayList<Integer>();
//        antOneData.add(1);
//        antOneData.add(213);
//        antOneData.add(108);
//
//        ArrayList<Integer> antTwoData = new ArrayList<Integer>();
//        antTwoData.add(2);
//        antTwoData.add(223);
//        antTwoData.add(151);
//
//        antData.add(antOneData);
//        antData.add(antTwoData);
//        expectedSubmitData.setAntData(antData);
//
//        //Actual output from receiveSubmitData method
//        ServletAnts testServlet = new ServletAnts();
//        testServlet.doPost(request,response);
//
//        when(request.getReader().lines().collect(Collectors.joining(System.lineSeparator()))).thenReturn("{\"antData\":[[1,213,108],[2,223,151]],\"videoID\":\"vid_1\",\"frameID\":2}");
//
//        SubmitData testSubmitData;
//        testSubmitData = testServlet.receiveSubmitData(request,response);
//
//        Assert.assertEquals(expectedSubmitData.getAntData(), testSubmitData.getAntData());
//
//        //Assert.assertThat(expectedSubmitData.getAntData(),is(equalTo(testSubmitData.getAntData())));
//
//
//
//    }


}
