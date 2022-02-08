import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
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
import yaskawa.ext.api.Any;

import yaskawa.ext.*;
import static yaskawa.ext.Pendant.propValue;

import java.util.*;

public class DemoExtension {


    public DemoExtension() throws TTransportException, IllegalArgument, Exception
    {
        var version = new Version(2,0,6);
        var languages = Set.of("en");

        // Make first call to SDK API for extension service object/handle
        extension = new Extension("com.yaskawa.yii.demoextension.ext",
                                   version, "Yaskawa", languages//,
                                   //"192.168.1.66",20080
                                   );

        // NB: The above assumes that either:
        //  a) The extension is running on the pendant, or
        //  b) The extension is running on a desktop PC and connecting to a mock Smart Pendant App
        //     on the *same* PC.
        // If, instead, you wish an extension running on the desktop to connect to the pendant
        //  hardware or to the mock Smart Pendant app on another PC over the network, use
        //  the Extension() constructor that takes a hostname & port.  Use a domain name or
        //  IP address for the hostname and either 10080 or 20080 for the port as required.
        // e.g.:
        // extension = new Extension("com.yaskawa.yii.demoextension",
        //                           version, "Yaskawa", languages,
        //                           "192.168.1.200", 20080);

        pendant = extension.pendant();
        controller = extension.controller();

        // Useful for debugging, comment out in production:
        extension.subscribeLoggingEvents(); // receive logs from pendant
        extension.copyLoggingToStdOutput = true; // print log() to output
        extension.outputEvents = true; // print out events received
    }



    public void setup() throws TException, IOException, Exception
    {
        extension.subscribeLoggingEvents();
        lang = pendant.currentLanguage();

        controller.subscribeEventTypes(Set.of(
            ControllerEventType.PermissionGranted,
            ControllerEventType.PermissionRevoked,
            ControllerEventType.OperationMode,
            ControllerEventType.ServoState,
            ControllerEventType.ActiveTool,
            ControllerEventType.PlaybackState,
            ControllerEventType.RemoteMode,
            ControllerEventType.IOValueChanged
          ));
        controller.requestPermissions(Set.of("networking"));

        pendant.subscribeEventTypes(Set.of(
            PendantEventType.SwitchedScreen,
            PendantEventType.UtilityOpened,
            PendantEventType.UtilityClosed,
            PendantEventType.PanelOpened,
            PendantEventType.PanelClosed,
            PendantEventType.PopupOpened,
            PendantEventType.PopupClosed
          ));

        pendant.registerImageFile("images/MotoMINI_InHand.png");
        pendant.registerImageFile("images/fast-forward-icon.png");
        pendant.registerImageFile("images/d-icon-256.png");
        pendant.registerImageFile("images/d-icon-lt-256.png");


        // if support for multiple languages is anticipated, it is good
        //  practice to separate help HTML files into subdirectories
        //  named by ISO language codes, like "en", "de" etc. which can
        //  be selected based on the pendant's currently set language
        String helpFile = "help/"+lang+"/something-help.html";
        // check lang file exists, and if not, fall-back to English version
        File f = new File(helpFile);
        if (!(f.exists() && !f.isDirectory())) // non-existent
            helpFile = "help/en/something-help.html";

        pendant.registerHTMLFile(helpFile);


        // Register all our YML files
        //  (while everything may be in a single file, good practice
        //   to break things up into smaller reusable parts)
        var ymlFiles = List.of(
            "ControlsTab.yml",
            "LayoutTab.yml",
            "AccessTab.yml",
            "NavTab.yml",
            "NetworkTab.yml",
            "EventsTab.yml",
            "UtilWindow.yml",
            "NavPanel.yml"
          );
        for(var ymlFile : ymlFiles)
            pendant.registerYMLFile(ymlFile);


        // A Utility window
        pendant.registerUtilityWindow("demoWindow",    // id
                                      "UtilWindow",    // Item type
                                      "Demo Extension",// Menu name
                                      "Demo Utility"); // Window title

        // A Navigation panel (main programming screen)
        pendant.registerIntegration("navpanel", // id
                                    IntegrationPoint.NavigationPanel, // where
                                    "NavPanel", // YML Item type
                                    "Demo",     // Button label
                                    "images/d-icon-256.png"); // Button icon

        // place a button with icon on each jogging panel integration point
        //  (may have icon and/or short label, but width is limited)
        String jogPanelIconLight = "images/d-icon-lt-256.png";
        String jogPanelIconDark = "images/d-icon-256.png";
        //                          id                 where displayed                                      label    icon
        pendant.registerIntegration("jogTopLeft",      IntegrationPoint.SmartFrameJogPanelTopLeft,      "", "TPL",   jogPanelIconLight);
        pendant.registerIntegration("jogTopRight",     IntegrationPoint.SmartFrameJogPanelTopRight,     "", "TPR",   jogPanelIconLight);
        pendant.registerIntegration("jogBottomLeft",   IntegrationPoint.SmartFrameJogPanelBottomLeft,   "", "BTL",   jogPanelIconDark);
        pendant.registerIntegration("jogBottomCenter", IntegrationPoint.SmartFrameJogPanelBottomCenter, "", "BTCTR", jogPanelIconDark);
        pendant.registerIntegration("jogBottomRight",  IntegrationPoint.SmartFrameJogPanelBottomRight,  "", "BTR",   jogPanelIconDark);
        // unlike the integration points above, which only show when the jogging mode is 'smart frame', this one
        //  remains for all jogging modes:
        pendant.registerIntegration("JogTopCenter",    IntegrationPoint.JogPanelTopCenter,              "", "TOP",   jogPanelIconLight);

        pendant.registerIntegration("test2",    IntegrationPoint.SmartFrameJogPanelBottomAny,              "", "ANY",   jogPanelIconLight);

        // call onJogPanelButtonClicked() (below) if any jogging panel button clicked
        for(var id : List.of("jogTopLeft", "jogTopRight", "jogBottomLeft", "jogBottomCenter", "jogBottomRight", "JogTopCenter"))
            pendant.addItemEventConsumer(id, PendantEventType.Clicked, this::onJogPanelButtonClicked);


        // call onEventsItemClicked() for various events from Items on the Events tab
        pendant.addItemEventConsumer("eventbutton1", PendantEventType.Clicked, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.TextEdited, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.EditingFinished, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventcombo1", PendantEventType.Activated, this::onEventsItemClicked);
        pendant.addItemEventConsumer("popupquestion", PendantEventType.Clicked, this::onEventsItemClicked);

        // for Popup Dialog Closed events (all popups)
        pendant.addEventConsumer(PendantEventType.PopupClosed, this::onEventsItemClicked);

        // Handle events from Layout tab
        pendant.addItemEventConsumer("row1spacingup", PendantEventType.Clicked, this::onLayoutItemClicked);
        pendant.addItemEventConsumer("row1spacingdown", PendantEventType.Clicked, this::onLayoutItemClicked);

        // Network tab
        pendant.addItemEventConsumer("networkSend", PendantEventType.Clicked, this::onNetworkSendClicked);

        // Network recv tab
        pendant.addItemEventConsumer("networkRecv", PendantEventType.Clicked, this::onNetworkRecvClicked);


        // Navigation Panel
        pendant.addItemEventConsumer("instructionSelect", PendantEventType.Activated, this::onInsertInstructionControls);
        pendant.addItemEventConsumer("instructionText", PendantEventType.EditingFinished, this::onInsertInstructionControls);
        pendant.addItemEventConsumer("insertInstruction", PendantEventType.Clicked, this::onInsertInstructionControls);

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


    void onJogPanelButtonClicked(PendantEvent e)
    {
        // jog panel button clicked, issue a user notice
        try {
            var id = e.getProps().get("identifier").getSValue();

            pendant.notice("Jog Panel Button Clicked","The "+id+" Button was clicked.");

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Jog Panel button click :"+exceptionMessage(ex));
        }

    }


    void onLayoutItemClicked(PendantEvent e)
    {
        try {
            var itemName = e.getProps().get("item").getSValue();

            if (itemName.equals("row1spacingup")) {
                var spacing = pendant.property("layoutcontent","itemspacing").getIValue();
                pendant.setProperty("layoutcontent", "itemspacing", spacing+4);
            }
        else if (itemName.equals("row1spacingdown")) {
            var spacing = pendant.property("layoutcontent","itemspacing").getIValue();
            pendant.setProperty("layoutcontent", "itemspacing", spacing-4);
        }

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Layout tab event :"+exceptionMessage(ex));
        }
    }



    void onEventsItemClicked(PendantEvent e)
    {
        try {
            pendant.setProperty("eventtext1","text",e.toString());

            var props = e.getProps();
            if (props.containsKey("item")) {

                var itemName = props.get("item").getSValue();

                // if the popupquestion was Clicked, open a popupDialog question
                if (itemName.equals("popupquestion")) {
                    pendant.popupDialog("myeventpopup1", "A Popup Dialog",
                                        "Here we can ask a question with two custom response options"
                                        +" or just have a single positive response.",
                                        "OK Then","No Way");
                }
            }

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }

    void onNetworkSendClicked(PendantEvent e)
    {
        try {
            // get data & address to send TCP message
            var data = pendant.property("networkData","text").getSValue()+"\n";
            var ipAddress = pendant.property("networkIPAddress","text").getSValue();
            var port = Integer.parseInt(pendant.property("networkPort","text").getSValue());

            // create a port access to the outside
            //  (this would usually be done once during setup/init,
            //   but in this case the port is dynamic)
            var accessHandle = controller.requestNetworkAccess("LAN",port, "tcp");

            // open TCP socket
            Socket socket = new Socket(ipAddress, port);
            socket.setSoTimeout(1500);
            OutputStream output = socket.getOutputStream();

            // write data (UTF-8 encoded)
            var utf8Data = data.getBytes(StandardCharsets.UTF_8);
            output.write(utf8Data);

            InputStream input = socket.getInputStream();
            var buffer = new byte[100];
            String error = new String();
            try {
                int n = input.read(buffer);
                if (n<100)
                    buffer[n] = 0;
            } catch (SocketTimeoutException tex) {
                error = "Write successful, timeout on response read";
            } catch (Exception ex) {
                error = ex.getClass().getSimpleName()+(exceptionMessage(ex).equals("") ? "" : " - "+exceptionMessage(ex));
                try { extension.log(LoggingLevel.Debug,"Unable to read network message response :"+error); } catch (Exception all) {}
            }

            socket.close();

            // set response
            try {
                String inputStr = new String(buffer, StandardCharsets.UTF_8);
                pendant.setProperty("networkResponse","text",inputStr);
            } catch (Exception all) {}


            // set error
            try { pendant.setProperty("networkError","text",error); } catch (Exception all) {}

            // show notice that send was successful
            if (error.equals(""))
                pendant.notice("Data Sent","The data was sent to "+ipAddress+":"+port,"");

            controller.removeNetworkAccess(accessHandle);
        } catch (Exception ex) {
            // display error
            var error = ex.getClass().getSimpleName()+(exceptionMessage(ex).equals("") ? "" : " - "+exceptionMessage(ex));
            try { pendant.setProperty("networkError","text",error); } catch (Exception all) {}
            System.out.println("Unable to send network message :"+error);
            try { extension.log(LoggingLevel.Debug,"Unable to send network message :"+error); } catch (Exception all) {}
        }

    }

    void onNetworkRecvClicked(PendantEvent e)
    {
        try {
            // get data & address to send TCP message
            var data = pendant.property("networkData","text").getSValue()+"\n";
            var ipAddress = pendant.property("networkIPAddress","text").getSValue();
            var port = Integer.parseInt(pendant.property("networkPort","text").getSValue());

            // create a port access to the outside
            //  (this would usually be done once during setup/init,
            //   but in this case the port is dynamic)
            //var accessHandle = controller.requestNetworkAccess("LAN",port, "tcp");
            var accessHandle = controller.requestNetworkService("LAN",port, "tcp");


            // Incoming Socket
            Socket socketIn = new Socket(ipAddress, port); 

            // Server Socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server Started");
            System.out.println("Waiting for a client...");
            socketIn = serverSocket.accept();
            System.out.println("Client accepted"); 

           // take input from the client socket
           DataInputStream in = new DataInputStream(
                   new BufferedInputStream(socketIn.getInputStream()));

           String line = "";

            // reads message from client until "Over" is sent
            while (!line.equals("Over"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println(line);
 
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
 
            // close connection
            socket.close();
            in.close();


/*           
            // open TCP socket
            Socket socket = new Socket(ipAddress, port);
            socket.setSoTimeout(1500);
            OutputStream output = socket.getOutputStream();


            // write data (UTF-8 encoded)
            var utf8Data = data.getBytes(StandardCharsets.UTF_8);
            output.write(utf8Data);

            InputStream input = socket.getInputStream();
            var buffer = new byte[100];
            String error = new String();
            try {
                int n = input.read(buffer);
                if (n<100)
                    buffer[n] = 0;
            } catch (SocketTimeoutException tex) {
                error = "Write successful, timeout on response read";
            } catch (Exception ex) {
                error = ex.getClass().getSimpleName()+(exceptionMessage(ex).equals("") ? "" : " - "+exceptionMessage(ex));
                try { extension.log(LoggingLevel.Debug,"Unable to read network message response :"+error); } catch (Exception all) {}
            }

            socket.close();
*/

            // set response
            try {
                String inputStr = new String(buffer, StandardCharsets.UTF_8);
                pendant.setProperty("networkResponse","text",inputStr);
            } catch (Exception all) {}


            // set error
            try { pendant.setProperty("networkError","text",error); } catch (Exception all) {}

            // show notice that send was successful
            if (error.equals(""))
                pendant.notice("Data Sent","The data was sent to "+ipAddress+":"+port,"");

            controller.removeNetworkAccess(accessHandle);
        } catch (Exception ex) {
            // display error
            var error = ex.getClass().getSimpleName()+(exceptionMessage(ex).equals("") ? "" : " - "+exceptionMessage(ex));
            try { pendant.setProperty("networkError","text",error); } catch (Exception all) {}
            System.out.println("Unable to send network message :"+error);
            try { extension.log(LoggingLevel.Debug,"Unable to send network message :"+error); } catch (Exception all) {}
        }

    }


 
    void onInsertInstructionControls(PendantEvent e)
    {
        try {
            String cmd = "";
            var props = e.getProps();
            var itemName = props.get("item").getSValue();

            if (itemName.equals("instructionSelect")) {
                var index = props.get("index").getIValue();
                if (index == 0)
                    pendant.setProperty("instructionText", "text", "CALL JOB:OR_RG_MOVE (1, 0, 40, \"WIDTH\")");
                else if (index == 1)
                    pendant.setProperty("instructionText", "text", "GETS B000 $B000");
            }
            else if (itemName.equals("insertInstruction")) {

                // Insert cmd INFORM text into the current job at the current selected line
                cmd = pendant.property("instructionText", "text").getSValue();

                String output = pendant.insertInstructionAtSelectedLine(cmd);

                System.out.println("Command Insertion result: " + output);

                pendant.setProperty("instructionInsertResult", "text", "Result:" + output);
            }


        } catch (Exception ex) {
            // display error
            var error = ex.getClass().getSimpleName()+(exceptionMessage(ex).equals("") ? "" : " - "+exceptionMessage(ex));
            try { pendant.setProperty("instructionInsertResult","text",error); } catch (Exception all) {}
            System.out.println("Unable to handle instruction insertion:"+error);
        }

    }




    public void close()
    {
    }



    public static void main(String[] args) {
        DemoExtension thisExtension = null;
        try {

            // launch
            try {
                thisExtension = new DemoExtension();
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

            // run 'forever' (or until API service shuts down)
            try {
                thisExtension.extension.run(() -> false);
            } catch (Exception e) {
                System.out.println("Exception occurred:"+exceptionMessage(e));
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
