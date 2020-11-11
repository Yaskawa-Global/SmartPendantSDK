import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

import java.net.Socket;

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

public class DemoExtension {


    public DemoExtension() throws TTransportException, IllegalArgument, Exception
    {
        var version = new Version(1,0,1);
        var languages = Set.of("en");

        // Make first call to SDK API for extension service object/handle
        extension = new Extension("com.yaskawa.yii.demo-extension.ext",
                                   version, "Yaskawa", languages//,
                                   //"192.168.1.66",20080
                                   );

        // NB: The above assumes that either:
        //  a) The extension is running on the pendant, or
        //  b) The extension is running on a desktop PC and connecting to a mock Smart Pendant App
        //     on the *same* PC.
        // If, instead, you wish an extention running on the desktop to connect to the pendant
        //  hardware or to the mock Smart Pendant app on another PC over the network, use
        //  the Extension() constructor that takes a hostname & port.  Use a domain name or
        //  IP address for the hostname and either 10080 or 20080 for the port as required.
        // e.g.:
        // extension = new Extension("com.yaskawa.yii.demo-extension",
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

        pendant.registerImageFile("images/MotoMINI_InHand.png");
        pendant.registerImageFile("images/fast-forward-icon.png");

        // if support for multiple languages is anticipated, it is good
        //  practice to seperate help HTML files into subdirectories
        //  named by ISO language codes, like "en", "de" etc. which can
        //  be selected based on the pendant's currently set language
        pendant.registerHTMLFile("help/"+lang+"/something-help.html");

        pendant.registerYMLFile("ControlsTab.yml");
        pendant.registerYMLFile("LayoutTab.yml");
        pendant.registerYMLFile("AccessTab.yml");
        pendant.registerYMLFile("NetworkTab.yml");
        pendant.registerYMLFile("UtilWindow.yml");

        pendant.registerUtilityWindow("demoWindow",   // id
                                      "UtilWindow", // Item type
                                      "Demo Extension", // Menu name
                                      "Demo Utility"); // Window title


        // Handle events from Layout tab
        pendant.addItemEventConsumer("row1spacingup", PendantEventType.Clicked, this::onLayoutItemClicked);
        pendant.addItemEventConsumer("row1spacingdown", PendantEventType.Clicked, this::onLayoutItemClicked);

        // call onEventsItemClicked() for various events from Items on the Events tab
        pendant.addItemEventConsumer("eventbutton1", PendantEventType.Clicked, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.TextEdited, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.EditingFinished, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventcombo1", PendantEventType.Activated, this::onEventsItemClicked);

        pendant.addItemEventConsumer("networkSend", PendantEventType.Clicked, this::onNetworkSendClicked);

    }


    // handy method to get the message from an Exception
    String exceptionMessage(Exception e)
    {
        if (e instanceof IllegalArgument)
            return ((IllegalArgument)e).getMsg();
        if (e.getMessage() != null)
            return e.getMessage();
        return "";
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
            System.out.println("Unable to Layout tab process event :"+exceptionMessage(ex));
        }
    }



    void onEventsItemClicked(PendantEvent e)
    {
        try {
            pendant.setProperty("eventtext1","text",e.toString());
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
            //   but in this case the port is dymamic)
            var accessHandle = controller.requestNetworkAccess("LAN",port, "tcp");

            // open TCP socket
            Socket socket = new Socket(ipAddress, port);
            OutputStream output = socket.getOutputStream();

            // write data (UTF-8 encoded)
            var utf8Data = data.getBytes(StandardCharsets.UTF_8);
            output.write(utf8Data);

            socket.close();

            // clear error
            try { pendant.setProperty("networkError","text",""); } catch (Exception all) {}

            // show notice that send was successful
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
                System.out.println("Extension failed to start, aborting: "+e.toString());
                return;
            }

            try {
                thisExtension.setup();
            } catch (Exception e) {
                System.out.println("Extension failed in setup, aborting: "+e.toString());
                return;
            }

            // run 'forever' (or until API service shutsdown)
            try {
                thisExtension.extension.run(() -> false);
            } catch (Exception e) {
                System.out.println("Exception occured:"+e.toString());
            }

        } catch (Exception e) {

            System.out.println("Exception: "+e.toString());

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
