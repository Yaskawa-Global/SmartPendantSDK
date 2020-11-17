import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.math.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

import java.net.Socket;
import java.net.SocketTimeoutException;

import yaskawa.ext.api.IllegalArgument;
import yaskawa.ext.api.ControllerEvent;
import yaskawa.ext.api.ControllerEventType;
import yaskawa.ext.api.CoordFrameRepresentation;
import yaskawa.ext.api.CoordinateFrame;
import yaskawa.ext.api.IntegrationPoint;
import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
import yaskawa.ext.api.PredefinedCoordFrameType;
import yaskawa.ext.api.OrientationUnit;
import yaskawa.ext.api.VariableAddress;
import yaskawa.ext.api.Scope;
import yaskawa.ext.api.AddressSpace;
import yaskawa.ext.api.LoggingLevel;

import yaskawa.ext.*;

import java.util.*;

public class Calculator {


    public Calculator() throws TTransportException, IllegalArgument, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en");

        // Make first call to SDK API for extension service object/handle
        extension = new Extension("com.yaskawa.yii.calculator.ext",
                                   version, "Yaskawa", languages);


        pendant = extension.pendant();
        controller = extension.controller();

        // Useful for debugging, comment out in production:
        //extension.subscribeLoggingEvents(); // receive logs from pendant
        //extension.copyLoggingToStdOutput = true; // print log() to output
        //extension.outputEvents = true; // print out events received
    }



    public void setup() throws TException, IOException, Exception
    {

        extension.subscribeLoggingEvents();
        lang = pendant.currentLanguage();

        controller.subscribeEventTypes(Set.of(
          ));

        pendant.subscribeEventTypes(Set.of(
            PendantEventType.UtilityOpened,
            PendantEventType.UtilityClosed
          ));

        //pendant.registerImageFile("images/MotoMINI_InHand.png");

        //pendant.registerHTMLFile("help/"+lang+"/something-help.html");

        // Register all our YML files
        //  (while everything may be in a single file, good practice
        //   to break things up into smaller reusable parts)
        var ymlFiles = List.of(
            "Calculator.yml"
          );
        for(var ymlFile : ymlFiles)
            pendant.registerYMLFile(ymlFile);


        // A Utility window
        pendant.registerUtilityWindow("calcWindow",    // id
                                      "Calculator",    // Item type
                                      "Calculator",// Menu name
                                      "Calculator"); // Window title


        var ids = List.of("ac", "negate", "percent", "plus", "minus", "divide", "multiply",
                          "digit0", "digit1", "digit2", "digit3", "digit4",
                          "digit5", "digit6", "digit7", "digit8", "digit9",
                          "decimal", "equals");

        for (var id : ids)
            pendant.addItemEventConsumer(id, PendantEventType.Clicked, this::onButtonClicked);

        clear();
        updateDisplay();

    }


    // handy method to get the message from an Exception
    static String exceptionMessage(Exception e)
    {
        var exceptionClassName = e.getClass().getSimpleName();
        if (e instanceof IllegalArgument)
            return exceptionClassName+":"+((IllegalArgument)e).getMsg();
        if (e.getMessage() != null)
            return exceptionClassName+":"+e.getMessage();
        return exceptionClassName;
    }


    boolean equalClicked;
    boolean opClicked;
    boolean periodClicked;
    String operator;
    double lhs;
    int nextUnit;
    boolean pendingNegative;
    int dp; // decimal places
    double display;


    void clear()
    {
        display = 0.0;
        equalClicked = true;
        opClicked = false;
        periodClicked = false;
        operator = "";
        nextUnit = 1; // +ve int for ints, -ve int for decimals
        dp = 0;
        lhs = 0;
        pendingNegative = false;
    }



    // set 'display' double to text field, if it fits
    void updateDisplay()
    {
        try {
            var s = String.format("%1."+dp+"f", display);
            if (s.length() < 16)
                pendant.setProperty("display", "text", s);
            else {
                // try with fewer dp
                while ((dp > 0) && (s.length() >= 15)) {
                    dp--;
                    s = String.format("%1."+dp+"f", display);
                }
                if (s.length() < 16)
                    pendant.setProperty("display", "text", s);
                else
                    pendant.setProperty("display", "text", "ERROR");
            }
        } catch (Exception ex) {}
    }


    void setSufficientDecPlaces()
    {
        if ((long)display == display) { // whole
            dp = 0;
        }
        else {
            var s = String.format("display=%1s",display);
            var dot = s.indexOf('.');
            if (dot < 0)
                dp = 0;
            else {
                dp = s.length() - dot -1;
                if (dp > 9) dp=9;
            }
        }
    }


    private static double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            BigDecimal bd = new BigDecimal(Double.toString(value));
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
    }


    void onButtonClicked(PendantEvent e)
    {
        try {

            var button = e.getProps().get("item").getSValue();
            if (button.startsWith("digit")) {
                int digit = Integer.parseInt(button.substring(5,6));

                if (equalClicked || opClicked) {
                    display = pendingNegative ? -digit : digit;
                    pendingNegative = false;
                    equalClicked = false;
                    opClicked = false;
                    nextUnit = 1;
                    dp=0;
                }
                else {

                    if (nextUnit == 1) {
                        display = display*10.0+ (display < 0 ? -digit : digit);
                    }
                    else if (nextUnit < 0) {
                        display = display+Math.pow(10,nextUnit)*(double)digit;
                        nextUnit -= 1;
                        dp += 1;
                    }

                }

            }
            else if (button.equals("decimal")) {
                if (equalClicked || opClicked) {
                    display = 0;
                    dp = 0;
                }
                opClicked = false;
                equalClicked = false;
                nextUnit = -1;
            }
            else if (   button.equals("plus") || button.equals("minus")
                     || button.equals("multiply") || button.equals("divide")) {
                opClicked = true;
                operator = button;
                lhs = display;
                equalClicked = false;
                nextUnit = 1;
                pendingNegative = false;
            }
            else if (button.equals("equals")) {
                if (operator.equals("plus"))
                    display = lhs+display;
                else if (operator.equals("minus"))
                    display = lhs-display;
                else if (operator.equals("multiply"))
                    display = lhs*display;
                else if (operator.equals("divide")) {
                    if (display != 0.0) {
                        display = lhs/display;
                    }
                    else
                        display = 0.0;
                }
                // round to nearest 10dp
                display = round(display,10);
                setSufficientDecPlaces();
                opClicked = false;
                equalClicked = true;
                operator = "";
                pendingNegative = false;
            }
            else if (button.equals("negate")) {
                if (display == 0.0)
                    pendingNegative = true;
                else
                    display = -display;
            }
            else if (button.equals("percent")) {
                display *= 0.01;
                dp += 2;
                opClicked = false;
                equalClicked = false;
                pendingNegative = true;
            }
            else if (button.equals("ac")) {
                clear();
            }
            else
                System.err.println("Unhandled calc button:"+button);

            updateDisplay();

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }




    public void close()
    {
    }



    public static void main(String[] args) {
        Calculator thisExtension = null;
        try {

            // launch
            try {
                thisExtension = new Calculator();
            } catch (Exception e) {
                System.out.println("Extension failed to start, aborting: "+exceptionMessage(e));
                return;
            }

            try {
                thisExtension.setup();
            } catch (Exception e) {
                System.out.println("Extension failed in setup, aborting: "+exceptionMessage(e));
                return;
            }

            // run 'forever' (or until API service shutsdown)
            try {
                thisExtension.extension.run(() -> false);
            } catch (Exception e) {
                System.out.println("Exception occured:"+exceptionMessage(e));
            }

        } catch (Exception e) {

            System.out.println("Exception: "+exceptionMessage(e));

        } finally {
            if (thisExtension != null)
                thisExtension.close();
        }
    }


    protected Extension extension;
    protected Pendant pendant;
    protected Controller controller;

    protected String lang;

}
