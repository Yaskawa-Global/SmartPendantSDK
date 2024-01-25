import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import java.net.Socket;
import java.net.SocketTimeoutException;

import yaskawa.ext.api.IllegalArgument;
import yaskawa.ext.api.ControllerEvent;
import yaskawa.ext.api.ControllerEventType;
import yaskawa.ext.api.CoordFrameRepresentation;
import yaskawa.ext.api.CoordinateFrame;
import yaskawa.ext.api.IntegrationPoint;
import yaskawa.ext.api.JogSpeed;
import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
import yaskawa.ext.api.PredefinedCoordFrameType;
import yaskawa.ext.api.OrientationUnit;
import yaskawa.ext.api.Disposition;
import yaskawa.ext.api.LoggingLevel;
import yaskawa.ext.api.MotionTypes;
import yaskawa.ext.api.Any;
import yaskawa.ext.api.storageInfo;

import yaskawa.ext.*;
import yaskawa.ext.Pendant.PropValue;
import yaskawa.ext.api.Data;
import yaskawa.ext.api.Series;
import yaskawa.ext.api.Category;
import yaskawa.ext.api.DataPoint;

import java.text.MessageFormat;
import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class DemoExtension {


    public DemoExtension() throws TTransportException, IllegalArgument, Exception
    {
        var version = new Version(3,0,0);
        var languages = Set.of("en", "ja");

        // Make first call to SDK API for extension service object/handle
        extension = new Extension("com.yaskawa.yii.demoextension.ext",
                                   version, "Yaskawa", languages,
                                   "localhost", 10080);

        // NB: The above assumes that either:
        //  a) The extension is running on the pendant, or
        //  b) The extension is running on a desktop PC and connecting to a mock Smart Pendant App
        //     on the *same* PC.
        // If, instead, you wish an extention running on the desktop to connect to the pendant
        //  hardware or to the mock Smart Pendant app on another PC over the network, use
        //  the Extension() constructor that takes a hostname & port.  Use a domain name or
        //  IP address for the hostname and either 10080 or 20080 for the port as required.
        // e.g.:
        // extension = new Extension("com.yaskawa.yii.demoextension.ext",
        //                           version, "Yaskawa", languages,
        //                           "192.168.1.31", 20080);


        // The version of the API supported by the Smart Pendant on which we're running
        apiVersion = extension.apiVersion();
        System.out.println("SP API version: "+apiVersion);
        //  (if we wish to support backward-compatability, it is possible we're linked
        //   against a Java client jar that corresponds to a newer API than the SP we're running on,
        //   so we'll need to check the version to ensure we don't use an API function that
        //   isn't supported, unless installed by a YIP package where we
        //   specified the minimum API version we need)

        pendant = extension.pendant();
        controller = extension.controller();

        // Useful for debugging, comment out in production:
        extension.subscribeLoggingEvents(); // receive logs from pendant
        extension.copyLoggingToStdOutput = true; // print log() to output
        extension.outputEvents = true; // print out events received (only use during development)
    }



    // into which languages has this extension been translated?
    public static Set<String> translatedLanguageLocales = Set.of("en", "ja");


    public void setup() throws TException, IOException, Exception
    {
        extension.subscribeLoggingEvents();

        // query current language of pendant
        lang = pendant.currentLanguage();
        localeName = pendant.currentLocale();

        // create Java Locale for the language
        try {
            locale = Locale.forLanguageTag(localeName);
            // and load strings for the language
            pendant.registerTranslationFile(lang,"language/LanguageBundle_"+localeName+".properties");
            strings = ResourceBundle.getBundle("language/LanguageBundle", locale);

            System.out.println(tr("lang_bundle_loaded",localeName));
        } catch (Exception ex) {
            // in case we don't have a translation for that language, fall-back to English
            System.out.println("Language bundle for "+localeName+" not found - defaulting to English.");

            locale = Locale.forLanguageTag("en");
            pendant.registerTranslationFile(lang,"language/LanguageBundle_"+localeName+".properties");
            strings = ResourceBundle.getBundle("language/LanguageBundle", locale);
        }
        formatter = new MessageFormat("");
        formatter.setLocale(locale);


        controller.subscribeEventTypes(Set.of(
            ControllerEventType.PermissionGranted,
            ControllerEventType.PermissionRevoked,
            ControllerEventType.OperationMode,
            ControllerEventType.ServoState,
            ControllerEventType.ActiveTool,
            ControllerEventType.PlaybackState,
            ControllerEventType.RemoteMode,
            ControllerEventType.IOValueChanged,
            ControllerEventType.JoggingModeChanged,
            ControllerEventType.JoggingSpeedChanged
          ));
        controller.requestPermissions(Set.of("networking"));

        pendant.subscribeEventTypes(Set.of(
            PendantEventType.SwitchedScreen,
            PendantEventType.UtilityOpened,
            PendantEventType.UtilityClosed,
            PendantEventType.PanelOpened,
            PendantEventType.PanelClosed,
            PendantEventType.PopupOpened,
            PendantEventType.PopupClosed,
            PendantEventType.Startup,
            PendantEventType.JoggingPanelVisibilityChanged
          ));


        // register language .properties file for YML string translation in UI
        //  (if translations for current language aren't available, register en which will
        //   be used as a fall-back)
        // if (translatedLanguageLocales.contains(localeName))
        //     pendant.registerTranslationFile(lang,"LanguageBundle_"+localeName+".properties");
        // else
        //     pendant.registerTranslationFile("en","LanguageBundle_en.properties");

        pendant.registerImageFile("images/MotoMINI_InHand.png");
        pendant.registerImageFile("images/fast-forward-icon.png");
        pendant.registerImageFile("images/d-icon-256.png");
        pendant.registerImageFile("images/d-icon-lt-256.png");
        pendant.registerImageFile("images/trash_can@4x.png");
        pendant.registerImageFile("images/Yaskawa-Y-logo.png");

        // if support for multiple languages is anticipated, it is good
        //  practice to seperate help HTML files into subdirectories
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
            "Controls1Tab.yml",
            "Controls2Tab.yml",
            "ListDel.yml",
            "Controls3Tab.yml",
            "ChartsTab.yml",
            "ExStorageTab.yml",
            "RobotJogTab.yml",  
            "ControlsTab.yml",
            "LayoutTab.yml",
            "AccessTab.yml",
            "NavTab.yml",
            "NetworkTab.yml",
            "EventsTab.yml",
            "LocalizationTab.yml",
            "UtilWindow.yml",
            "NavPanel.yml",
            "HomeScreen.yml"
          );
        for(var ymlFile : ymlFiles)
            pendant.registerYMLFile("./yml/" + ymlFile);


        // A Utility window
        pendant.registerUtilityWindow("demoWindow",    // id
                                      "UtilWindow",    // Item type
                                      "Demo Extension",// Menu name
                                      "Demo Utility"); // Window title

        // A Navigatio panel (main programming screen)
        pendant.registerIntegration("navpanel", // id
                                    IntegrationPoint.NavigationPanel, // where
                                    "NavPanel", // YML Item type
                                    "Demo",     // Button label
                                    "images/d-icon-256.png"); // Button icon

        // A customized pendant Home Screen replacement
        pendant.registerIntegration("homescreen", // id
                                    IntegrationPoint.HomeScreen, // where
                                    "HomeScreen", // YML Item type
                                    "",  // Button label (N/A)
                                    ""); // Button icon (N/A)

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

        // call onControlsItemClicked() for buttons on ControlsTab
        pendant.addItemEventConsumer("successbutton", PendantEventType.Clicked, this::onControlsItemClicked);
        pendant.addItemEventConsumer("noticebutton", PendantEventType.Clicked, this::onControlsItemClicked);
        pendant.addItemEventConsumer("deleterowbutton", PendantEventType.Clicked, this::onControlsItemClicked);
        pendant.addItemEventConsumer("trashcanbutton", PendantEventType.Clicked, this::onControlsItemClicked);

        // call onControls3ItemClicked() for buttons on ControlsTab3 (list)
        pendant.addItemEventConsumer("appendListRowButton", PendantEventType.Clicked, this::onControls3ItemClicked);
        pendant.addItemEventConsumer("insertListRowButton", PendantEventType.Clicked, this::onControls3ItemClicked);
        pendant.addItemEventConsumer("deleteListRowButton", PendantEventType.Clicked, this::onControls3ItemClicked);
        pendant.addItemEventConsumer("clearListButton", PendantEventType.Clicked, this::onControls3ItemClicked);
        pendant.addItemEventConsumer("listdelMouseArea", PendantEventType.Clicked, this::onControls3ItemClicked);
        pendant.addItemEventConsumer("scrollUpButton", PendantEventType.Clicked, this::onControls3ItemClicked);


        // call onJogPanelButtonClicked() (below) if any jogging panel button clicked
        for(var id : List.of("jogTopLeft", "jogTopRight", "jogBottomLeft", "jogBottomCenter", "jogBottomRight", "JogTopCenter"))
            pendant.addItemEventConsumer(id, PendantEventType.Clicked, this::onJogPanelButtonClicked);


        // call onEventsItemClicked() for various events from Items on the Events tab
        pendant.addItemEventConsumer("eventbutton1", PendantEventType.Clicked, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.TextEdited, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventtextfield1", PendantEventType.EditingFinished, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventcombo1", PendantEventType.Activated, this::onEventsItemClicked);
        pendant.addItemEventConsumer("popupquestion", PendantEventType.Clicked, this::onEventsItemClicked);
        pendant.addItemEventConsumer("eventsNameButton", PendantEventType.Clicked, this::onEventsNameClicked);
        pendant.addItemEventConsumer("eventsVisibleButton", PendantEventType.Clicked, this::onEventsVisibleClicked); 

        pendant.addEventConsumer(PendantEventType.JoggingPanelVisibilityChanged, this::onEventsJogPanelVisibilityChanged);
        // Note: The addEventConsumer will automatically call the subscribeEventTypes for that event Type
        //       So, if the event is not to be monitor constantly, you need to unsubscribe it
        controller.addEventConsumer(ControllerEventType.VariableNamesChanged, this::onEventsController);
        controller.addEventConsumer(ControllerEventType.IONamesChanged, this::onEventsController);
        controller.unsubscribeEventTypes(Set.of(ControllerEventType.VariableNamesChanged, ControllerEventType.IONamesChanged));
        pendant.addItemEventConsumer("eventYlogo", PendantEventType.VisibleChanged, this::onEventsPendant);
        pendant.unsubscribeItemEventTypes(Set.of("eventYlogo"), Set.of(PendantEventType.VisibleChanged));

        // for Popup Dialog Closed events (all popups)
        pendant.addEventConsumer(PendantEventType.PopupClosed, this::onEventsItemClicked);

        // Handle events from Layout tab
        pendant.addItemEventConsumer("row1spacingup", PendantEventType.Clicked, this::onLayoutItemClicked);
        pendant.addItemEventConsumer("row1spacingdown", PendantEventType.Clicked, this::onLayoutItemClicked);

        // Network tab
        pendant.addItemEventConsumer("networkSend", PendantEventType.Clicked, this::onNetworkSendClicked);

        // Navigation Panel
        pendant.addItemEventConsumer("instructionSelect", PendantEventType.Activated, this::onInsertInstructionControls);
        pendant.addItemEventConsumer("instructionText", PendantEventType.EditingFinished, this::onInsertInstructionControls);
        pendant.addItemEventConsumer("insertInstruction", PendantEventType.Clicked, this::onInsertInstructionControls);
   
        // one time init
        pendant.addEventConsumer(PendantEventType.UtilityOpened, this::onOpened);
        pendant.addItemEventConsumer("addKeyButton", PendantEventType.Clicked, this::onAddKey);
        pendant.addItemEventConsumer("rmKeyButton", PendantEventType.Clicked, this::onRmKey);
        pendant.addItemEventConsumer("incChartUpd", PendantEventType.Clicked, this::onIncUpd);
        pendant.addItemEventConsumer("decChartUpd", PendantEventType.Clicked, this::onDecUpd);
        pendant.addItemEventConsumer("incScale", PendantEventType.Clicked, this::onIncScale);
        pendant.addItemEventConsumer("decScale", PendantEventType.Clicked, this::onDecScale);

        //external storage options
        pendant.addEventConsumer(PendantEventType.Startup, this::onStartup);
        pendant.addItemEventConsumer("writeToFileButton", PendantEventType.Clicked, this::onWriteToExternalFile);
        pendant.addItemEventConsumer("readFromFileButton", PendantEventType.Clicked, this::onReadFromExternalFile);
        pendant.addItemEventConsumer("refreshExStorageButton", PendantEventType.Clicked, this::onListExStorageOptions);
        pendant.addItemEventConsumer("exStorageTabComboBox", PendantEventType.Activated, this::onStorageComboBoxClicked);
    
        //set the work home position
        pendant.addItemEventConsumer("setWorkHomeButton", PendantEventType.Clicked, this::onSetWorkHomeButtonClicked);
        
        //goto position
        pendant.addEventConsumer(PendantEventType.UtilityOpened, this::onRobotTabStartup);
        pendant.addItemEventConsumer("jogCoordComboBox", PendantEventType.Activated, this::onJogCoordComboBoxClicked);
        pendant.addItemEventConsumer("jogUserFrame", PendantEventType.EditingFinished, this::onJogUserFrameEdited);
        pendant.addItemEventConsumer("jogTool", PendantEventType.EditingFinished, this::onJogToolEdited);
        pendant.addItemEventConsumer("jogMotionComboBox", PendantEventType.Activated, this::onJogMotionComboBoxClicked);   
        pendant.addItemEventConsumer("jogSpeedComboBox", PendantEventType.Activated, this::onJogSpeedComboBoxClicked);          
        pendant.addItemEventConsumer("jogTarget0Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        pendant.addItemEventConsumer("jogTarget1Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        pendant.addItemEventConsumer("jogTarget2Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        pendant.addItemEventConsumer("jogTarget3Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        pendant.addItemEventConsumer("jogTarget4Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        pendant.addItemEventConsumer("jogTarget5Entry", PendantEventType.EditingFinished, this::onJogTargetEntryEdited);
        //controller event for jog notifications
        controller.addEventConsumer(ControllerEventType.ActiveTool, this::onActiveToolChanged);
        controller.addEventConsumer(ControllerEventType.JoggingSpeedChanged, this::onJogSpeedChanged);

        // home screen
        pendant.addItemEventConsumer("unregisterHomeScreen", PendantEventType.Clicked, this::onHomeScreenUnregisterClicked);

        //testing
        pendant.addItemEventConsumer("rowSelectorComboBox", PendantEventType.Activated, this::onRowSelectorComboBoxClicked);
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


    // Convenience methods for looking up translated strings from id label
    //  and params to subst (if any)
    String tr(String id)
    {
        return strings.getString(id);
    }
    String tr(String id, String arg)
    {
        return formatter.format(strings.getString(id), new Object[] {arg} );
    }
    String tr(String id, Object[] args)
    {
        return formatter.format(strings.getString(id), args);
    }

    void onRowSelectorComboBoxClicked(PendantEvent e)
    {
        try {
            var props = e.getProps();
            var index = props.get("index").getIValue();
            pendant.setProperty("controlstable", "selectedRow", Any.iValue((index)));

            var idx = pendant.property("controlstable", "selectedCell").getAValue();
        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }

    void onControlsItemClicked(PendantEvent e)
    {
        try {

            var props = e.getProps();
            if (props.containsKey("item")) {

                var itemName = props.get("item").getSValue();

                // show a notice in reponse to button clicked
                if (itemName.equals("successbutton")) {

                    // the dispNotice() function is only present in API >= 2.1, so
                    //  fall-back to notice() function if running on older SP SDK API
                    if (apiVersion.compareTo(new Version(2,1,0)) >= 0)
                        pendant.dispNotice(Disposition.Positive, "Success", "It worked!");
                    else
                        pendant.notice("Success", "It worked!");
                }
                else if (itemName.equals("noticebutton")) {
                    pendant.notice("A Notice","For your information.");
                }
                else if (itemName.equals("deleterowbutton")) {

                    // which row of the table is selected? (-1 if none)
                    var selectedCell = pendant.property("controlstable","selectedCell").getAValue();
                    var selectedRow = selectedCell.get(0).getIValue();

                    if (selectedRow >= 0) {
                        // delete row by reading all the row data back here,
                        //  removing the selected row, then sending all the row data back

                        var rows = pendant.property("controlstable","rows").getAValue();
                        rows.remove((int)selectedRow);
                        pendant.setProperty("controlstable","rows", Any.aValue(rows));
                    }

                }
                else if (itemName.equals("trashcanbutton")) 
                {
                    // similar to example above, but delegate returns row as part of the event
                    //delegates use the same id for all rows in the table
                    if(props.containsKey("row"))
                    {
                        var selectedRow = props.get("row").getIValue();
                            if (selectedRow >= 0) 
                            {
                            // delete row by reading all the row data back here,
                            //  removing the selected row, then sending all the row data back                      
                            var rows = pendant.property("ctableanddelegate","rows").getAValue();
                            rows.remove((int)selectedRow);
                            pendant.setProperty("ctableanddelegate","rows", Any.aValue(rows));
                        }
                                           
                    }

                }

            }

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }

    void onControls3ItemClicked(PendantEvent e)
    {
        try {
 
            var props = e.getProps();
            if (props.containsKey("item")) {

                var itemName = props.get("item").getSValue();

                Map<String, Any> newElement = new HashMap<String, Any>();
                    
                //get the text to insert
                var newElementStr = pendant.property("listElementStringText","text").getSValue();
                newElement.put("text", Any.sValue(newElementStr));
                //extension.log(LoggingLevel.Debug,"Element text: " + newElementStr);

                //get the color 
                var newElementColor = pendant.property("listElementColorSelectorComboBox", "currentText").getSValue();
                newElement.put("color", Any.sValue(newElementColor));
                //extension.log(LoggingLevel.Debug,"Element color: " + newElementColor);

                //get the selected row
                //var selectedRow = pendant.property("list","selectedRow").getIValue();
                var selectedRow = pendant.property("listRowSelectorComboBox", "currentIndex").getIValue();

                if (itemName.equals("appendListRowButton")) {                   
                    pendant.appendRow("list", newElement);
                }
                else if (itemName.equals("insertListRowButton")) 
                {
                    pendant.insertRow("list", (int)selectedRow, newElement);
                }
                else if (itemName.equals("deleteListRowButton")) 
                {
                    pendant.deleteRow("list", (int)selectedRow);
                }
                else if (itemName.equals("clearListButton")) 
                {
                    extension.log(LoggingLevel.Debug,"Clearing list ... ");
                    pendant.clearRows("list");
                }
                else if(itemName.equals("listdelMouseArea")) 
                {
                    extension.log(LoggingLevel.Debug,"List delegate clicked ... index = " + props.get("index").getIValue());
                }
                else if (itemName.equals("scrollUpButton")) 
                {
                    extension.log(LoggingLevel.Debug,"Scroll up button clicked ... ");
                    pendant.setProperty("list", "verticalScrollPosition", 0.011);
                }
                else
                {
                    extension.log(LoggingLevel.Debug,"Unhandled click ... ");
                }
            }

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }
    
    void onJogPanelButtonClicked(PendantEvent e)
    {
        // jog panel buttin clicked, issue a user notice
        try {
            var id = e.getProps().get("identifier").getSValue();

            pendant.notice(tr("jog_panel_button_clicked"),tr("the_id_button_was_clicked",id));

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
                    pendant.popupDialog("myeventpopup1", tr("a_popup_dialog"),
                                        tr("popup_question"),
                                        tr("popup_q_positive"),tr("popup_q_negative"));
                }
            }

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event :"+exceptionMessage(ex));
        }
    }

    
    void onEventsNameClicked(PendantEvent e)
    {
        try {
            if(pendant.property("eventsNameButton", "text").getSValue().contains("Unsubscribe")) {
                controller.unsubscribeEventTypes(Set.of(ControllerEventType.VariableNamesChanged, ControllerEventType.IONamesChanged));
                pendant.setProperty("eventsNameButton", "text", "Subscribe Var/IO Name Event");
            }
            else {
                controller.subscribeEventTypes(Set.of(ControllerEventType.VariableNamesChanged, ControllerEventType.IONamesChanged));
                pendant.setProperty("eventsNameButton", "text", "Unsubscribe Var/IO Name Event");
            }
        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event:"+exceptionMessage(ex));
        }
    }


    void onEventsVisibleClicked(PendantEvent e)
    {
        try {
            if(pendant.property("eventsVisibleButton", "text").getSValue().contains("Unsubscribe")) {
                pendant.unsubscribeItemEventTypes(Set.of("eventYlogo"), Set.of(PendantEventType.VisibleChanged));
                pendant.setProperty("eventsVisibleButton", "text", "Subscribe to Visible Change Event");
            }
            else {
                pendant.subscribeItemEventTypes(Set.of("eventYlogo"), Set.of(PendantEventType.VisibleChanged));
                pendant.setProperty("eventsVisibleButton", "text", "Unsubscribe to Visible Change Event");
            }
        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Clicked event:"+exceptionMessage(ex));
        }
    }


    void onEventsController(ControllerEvent e)
    {
        try {
            pendant.setProperty("eventtext1","text",e.toString());
        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Controller event :"+exceptionMessage(ex));
        }
    }


    void onEventsPendant(PendantEvent e)
    {
        try {
            pendant.setProperty("eventtext1","text",e.toString());
        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process Pendant event :"+exceptionMessage(ex));
        }
    }


    void onEventsJogPanelVisibilityChanged(PendantEvent e)
    {
        try {

            pendant.setProperty("eventtext1","text",e.toString());

        } catch (Exception ex) {
            // display error
            System.out.println("Unable to process jog panel visibility event :"+exceptionMessage(ex));
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

    private boolean init = false;

    public void onAddKey(PendantEvent e)
    {
        /* prepare new series */
        Series s1 = new Series(
                            Arrays.<Double>asList(0.0,1.0,2.0,3.0,4.0,5.0,6.0),
                            Arrays.<Double>asList(6.0,5.0,4.0,3.0,2.0,1.0,0.0)
                        );
        s1.setColor("#ffd166");

        try {
            /* add key to chart */
            pendant.addChartKey("exampleLine", "Added Series", Data.sData(s1));
        } catch (Exception ex) {
            System.out.println("onAddKey: " + ex);
        }
    }

    private boolean hideKey = true;
    public void onRmKey(PendantEvent e)
    {
        try {
            /* remove key from chart */
            pendant.hideChartKey("exampleLine", "Added Series", hideKey);
            hideKey = !hideKey;
        } catch (Exception ex) {
            System.out.println("onRmKey: " + ex);
        }
    }

    public synchronized void onIncUpd(PendantEvent e)
    {
        updRate += 10;
        try {
            pendant.setProperty("chartUpd", "text", updRate);
        } catch (Exception ex) {
            System.out.println("onIncUpd: " + ex);
        }
    }

    public synchronized void onDecUpd(PendantEvent e)
    {
        updRate -= 10;
        try {
            pendant.setProperty("chartUpd", "text", updRate);
        } catch (Exception ex) {
            System.out.println("onDecUpd: " + ex);
        }
    }

    public synchronized void onIncScale(PendantEvent e)
    {
        try {
            chartScale += 0.1;
            pendant.setChartConfig("exampleLine", Map.of(
                "title","Demo Line Chart",
                "grid",true,
                "key",true,
                "scale",chartScale
            ));
            pendant.setProperty("chartScale", "text", chartScale);
        } catch (Exception ex) {
            System.out.println("onIncScale: " + ex);
        }
    }

    public synchronized void onDecScale(PendantEvent e)
    {
        try {
            chartScale -= 0.1;
            pendant.setChartConfig("exampleLine", Map.of(
                "title","Demo Line Chart",
                "grid",true,
                "key",true,
                "scale",chartScale
            ));
            pendant.setProperty("chartScale", "text", chartScale);
        } catch (Exception ex) {
            System.out.println("onDecScale: " + ex);
        }
    }

    public void onOpened(PendantEvent e)
    {
        if (!init) {
            try {
                // Chart Panel
                
                /* Line Chart */
                pendant.setChartConfig("exampleLine", Map.of(
                    "title", "Demo Line Chart",
                    "grid", true,
                    "key", true,
                    "tick", true,
                    "x", Map.of(
                        "label", "X Label"
                    ),
                    "y", Map.of(
                        "label", "Y Label",
                        "min", -10,
                        "max", 10
                    ),
                    "ry", Map.of(
                        "label", "RY Label"
                    )
                ));

                /* data set for left hand scale */
                Series s1 = new Series(
                    Arrays.<Double>asList(1.0,2.0,3.0,4.0,5.0,6.0),
                    Arrays.<Double>asList(1.0,2.0,3.0,4.0,5.0,6.0)
                );
                s1.setColor("#06d6a0");
                Map<String, Data> ds = new HashMap<String, Data>();
                ds.put("Series 1", Data.sData(s1));
                pendant.setChartData("exampleLine", ds);
                
                /* data set for right hand scale */
                Series s2 = new Series(
                    Arrays.<Double>asList(1.0,2.0,3.0,4.0,5.0,6.0),
                    Arrays.<Double>asList(0.0,0.2,0.4,0.8,0.4,0.2)
                );
                s2.setColor("#ef476f");
                s2.setVertex("cross");
                Series s3 = new Series(
                    Arrays.<Double>asList(0.0),
                    Arrays.<Double>asList(0.0)
                );
                s3.setColor("#26547c");
                s3.setMaxPts(60);
                Map<String, Data> dsr = new HashMap<String, Data>();
                dsr.put("Series 2", Data.sData(s2));
                dsr.put("Series 3", Data.sData(s3));
                pendant.setChartData("exampleLine", dsr, true);
                
                /* variables used to update line chart */
                init = true;
                updRate = 50;
                chartScale = 1.0;

                // Scatter Chart
                pendant.setChartConfig("exampleScatter", Map.of(
                    "title", "Demo Scatter Chart",
                    "tick", true,
                    "x", Map.of(
                        "label", "X Label"
                    ),
                    "y", Map.of(
                        "label", "Y Label"
                    )
                ));

                Random rand = new Random();

                ArrayList<Double> x = new ArrayList<Double>();
                ArrayList<Double> y = new ArrayList<Double>();
                ArrayList<Double> z = new ArrayList<Double>();

                for (int i = 0; i < 20; ++i) {
                    x.add(rand.nextDouble() * 200 - 100);
                    y.add(rand.nextDouble() * 200 - 100);
                    z.add(rand.nextDouble() * 8 + 2);
                }

                Series s4 = new Series(x,y);
                s4.setZ(z);
                s4.setColor("#607196");
                Map<String, Data> dsScatter = new HashMap<String, Data>();
                dsScatter.put("Navy", Data.sData(s4));
                pendant.setChartData("exampleScatter", dsScatter);

                ArrayList<Double> rx = new ArrayList<Double>();
                ArrayList<Double> ry = new ArrayList<Double>();
                ArrayList<Double> rz = new ArrayList<Double>();

                for (int i = 0; i < 20; ++i) {
                    rx.add(Double.valueOf(i));
                    ry.add(rand.nextDouble() * 200 - 100);
                    rz.add(rand.nextDouble() * 8 + 2);
                }
                Series s5 = new Series(rx,ry);
                s5.setZ(z);
                s5.setColor("#9fa2b2");
                Map<String, Data> rdsScatter = new HashMap<String, Data>();
                rdsScatter.put("Navy", Data.sData(s5));
                pendant.setChartData("exampleScatter", rdsScatter, true);

                /* Bar Chart */
                pendant.setChartConfig("exampleBar", Map.of(
                    "title", "Demo Bar Chart",
                    "display", "percent",
                    "tick", true,
                    "x", Map.of(
                        "label", "X Label"
                    ),
                    "y", Map.of(
                        "label", "Y Label"
                    )
                ));
                
                Category c1 = new Category(420);
                c1.setColor("#16262e");
                Category c2 = new Category(69);
                c2.setColor("#2e4756");
                Category c3 = new Category(21);
                c3.setColor("#3c7a89");
                Map<String, Data> dsBar = new HashMap<String, Data>();
                dsBar.put("Darkest", Data.cData(c1));
                dsBar.put("Darker", Data.cData(c2));
                dsBar.put("Dark", Data.cData(c3));
                pendant.setChartData("exampleBar", dsBar);

                /* Pie Chart */
                pendant.setChartConfig("examplePie", Map.of(
                    "title", "Demo Pie Chart",
                    "tick", true,
                    "display", "value",
                    "hole", 0.3
                ));

                Category c4 = new Category(3.14);
                c4.setColor("#3c7a89");
                Category c5 = new Category(4.14);
                c5.setColor("#9fa2b2");
                Category c6 = new Category(5.14);
                c6.setColor("#dbc2cf");
                Map<String, Data> dsPie = new HashMap<String, Data>();
                dsPie.put("Pi", Data.cData(c4));
                dsPie.put("Pi + 1", Data.cData(c5));
                dsPie.put("Pi + 2", Data.cData(c6));
                pendant.setChartData("examplePie", dsPie);  

            } catch (Exception ex) {
                System.out.println("Exception in run init: " + exceptionMessage(ex));
            }

            /* start a data producing thread */
            updThread = new Thread(() -> {
                while (run.get()) {
                    try {
                        Thread.sleep(updRate);
                        update.set(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            updThread.start();
        }
    
        // GoToPosititon
        try {
            int tool = (int)pendant.property("gotoPosButton", "toolNumber").getIValue();
            pendant.setProperty("jogTool", "text", tool);
            int speed = (int)pendant.property("gotoPosButton", "jogSpeed").getIValue();
            pendant.setProperty("jogSpeedComboBox", "currentIndex", speed - 1);
        } catch (Exception ex) {
            System.out.println("Exception in run init: " + exceptionMessage(ex));
        }
    }

    public boolean updateChart()
    {
        if (update.compareAndSet(true, false)) {
            try {
                DataPoint pt = new DataPoint(time, Math.sin(time));
                pendant.appendChartPoint("exampleLine", "Series 3", pt, true);
                pendant.incrementChartKey("exampleBar", "Darker", 1.0);

                time += 0.1;

                if (time > 12) {
                    time = 0;
                }
            } catch (Exception ex) { 
                System.out.println("appendChartPoint: " + ex);
            }
        }

        return false;
    }


    public void close()
    {
        run.set(false);
        if (updThread != null) {
            try {
                updThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateExStorageOptions()
    {
        try {

            List<storageInfo> storage = extension.listAvailableStorage();
            List<Object> storageNames = new ArrayList<Object>();
            for(int i = 0;i<storage.size();i++)
            {
                storageNames.add(storage.get(i).path);
            }
            pendant.setProperty("exStorageTabComboBox", "options", storageNames);
            var index = pendant.property("exStorageTabComboBox", "currentIndex").getIValue();

            if((storage.size() > 0) && (index >= 0))
            {
                List<String> files = extension.listFiles(storage.get((int)index).path);
                List<Object> fileNames = new ArrayList<Object>(files);
                pendant.setProperty("listFilesComboBox", "options", fileNames);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void onStartup(PendantEvent e)
    {
        //global startup ...
        //updateExStorageOptions();
    }
    
    public synchronized void onListExStorageOptions(PendantEvent e)
    {
        updateExStorageOptions();
    }

    public synchronized void onWriteToExternalFile(PendantEvent e)
    {
        try {
            List<Any> storageLocs = pendant.property("exStorageTabComboBox", "options").getAValue();
            var index = pendant.property("exStorageTabComboBox", "currentIndex").getIValue();

            if(storageLocs.size() > 0)
            {
                //open the file based on the text field
            	String fname = pendant.property("writeFileName", "text").getSValue();
                String storagePath = storageLocs.get((int)index).getSValue();
                var fid = extension.openFile(storagePath + "/" + fname, "w");
                //var fid = extension.openFile(storagePath + "/" + fname, "a");

                //get the text from the text field
                String filetext = pendant.property("writeText", "text").getSValue();
                extension.write(fid, filetext);

                //close and flush after writing
                extension.closeFile(fid);

                //reload files available for reading
                List<String> files = extension.listFiles(storagePath);
                List<Object> fileNames = new ArrayList<Object>(files);
                pendant.setProperty("listFilesComboBox", "options", fileNames);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    public synchronized void onReadFromExternalFile(PendantEvent e)
    {
        try {
            List<Any> storageLocs = pendant.property("exStorageTabComboBox", "options").getAValue();
            var index = pendant.property("exStorageTabComboBox", "currentIndex").getIValue();

            if(storageLocs.size() > 0)
            {
                //get the files available for reading
            	//String fname = pendant.property("filetext", "text").getSValue();;
                List<Any> fileNames = pendant.property("listFilesComboBox", "options").getAValue();
                var fileindex = pendant.property("listFilesComboBox", "currentIndex").getIValue();
                String fname = fileNames.get((int)fileindex).getSValue();

                //open and read the selected file
                String storagePath = storageLocs.get((int)index).getSValue();
                var fid = extension.openFile(storagePath + "/" + fname, "r");
                String data = extension.read(fid);

                extension.log(LoggingLevel.Info,"Read file from:" + storagePath + " filename = " + fname);
                extension.log(LoggingLevel.Info,"File contents :" + data);
                String[] lines = data.split("\n");
                extension.log(LoggingLevel.Info,"Num lines =" + lines.length);

                //Tables hold a List (rows) of Map<String, Any> (columns)
                List<Any> stringlines = new ArrayList<Any>();
                for(String line: lines)
                {
                    //extension.log(LoggingLevel.Info,"File lines :" + line);
                    Map<String,Any> el = Map.of("filelinekey", Any.sValue(line));
                    stringlines.add(Any.mValue(el));
                }
                //extension.log(LoggingLevel.Info,"Array length for read:" + stringlines.size());
                

                //copy the file contents to the table
                var rows = Any.aValue(stringlines);
                pendant.setProperty("readfilecontentstable","rows", rows);
                extension.closeFile(fid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    public synchronized void onStorageComboBoxClicked(PendantEvent e)
    {
          try {
            //List<storageInfo> storage = extension.listAvailableStorage();
            List<Any> storageLocs = pendant.property("exStorageTabComboBox", "options").getAValue();
            var props = e.getProps();
            var index = props.get("index").getIValue();

            if((storageLocs.size() > 0) && (index >= 0))
            {
                //List<String> files = extension.listFiles(storage.get((int)index).path);
                String storagePath = storageLocs.get((int)index).getSValue();
                List<String> files = extension.listFiles(storagePath);
                List<Object> fileNames = new ArrayList<Object>(files);
                pendant.setProperty("listFilesComboBox", "options", fileNames);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    public synchronized void onSetWorkHomeButtonClicked(PendantEvent e)
    {
        
        try {

            Robot myRobot = controller.currentRobot();
            yaskawa.ext.api.Position pos = myRobot.workHomePosition();

            List<Double> newHome =  new ArrayList<Double>();
            for(int i = 0;i<6;i++)
            {
                Any val = pendant.property("workHome" + String.valueOf(i) + "Entry", "text");
                newHome.add(Double.valueOf(val.getSValue()));
            }
            pos.setJoints(newHome);
            myRobot.setWorkHomePosition(pos);
            //extension.log(LoggingLevel.Info,"set work home finished");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public synchronized void onRobotTabStartup(PendantEvent e)
    {
        try{

            //set joint mode
            pendant.setProperty("gotoPosButton", "jogMode", yaskawa.ext.api.JogMode.Joint.ordinal());

            //update the combobox
            pendant.setProperty("jogCoordComboBox", "currentIndex", Any.iValue(0));

            //update the setpoint based on the current robot position
            setJointTargetFromRobot();

            //update the current work home position from the controller
            setWorkHomeFromController();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void setJointTargetFromRobot()
    {
        try{

            //get the current joint positions
            Robot myRobot = controller.currentRobot();
            yaskawa.ext.api.Position pos = myRobot.jointPosition(OrientationUnit.Degree);
            List<Double> joints = pos.getJoints();

            //set to the current position
            List<Any> target =  new ArrayList<Any>();
            for(int i = 0;i<joints.size();i++)
                target.add(Any.rValue(joints.get(i)));
    
            pendant.setProperty("gotoPosButton", "target", Any.aValue(target));

            // Update UI
            updateTextFromTarget(target);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void updateTextFromTarget(List<Any> target)
    {
        try{
            List<PropValue> propValues = new ArrayList<PropValue>();
            for(int i=0; i<6 && i<target.size(); i++)
            {
                String posStr = String.valueOf(target.get(i).getRValue());
                propValues.add(new PropValue("jogTarget" + String.valueOf(i) + "Entry", "text", Any.sValue(posStr)));
            }
            pendant.setProperties(propValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void setTCPTargetFromRobot(PredefinedCoordFrameType coordType)
    {
        try{
            extension.log(LoggingLevel.Info,"setTCPTarget ... mode changed to " + String.valueOf(coordType.name()));
            Robot myRobot = controller.currentRobot();

            //build the current frame for jogging ...
            CoordinateFrame frame = new CoordinateFrame(CoordFrameRepresentation.Implicit, coordType);
            frame.setRobot(controller.currentRobotIndex());
            int tool = (int)pendant.property("gotoPosButton", "toolNumber").getIValue();
            if(tool == -1)
                tool = controller.currentRobot().activeTool();
            frame.setTool(tool);                
            if(coordType == PredefinedCoordFrameType.User)
            {
                // Note userFrame is using 0 based indexing but Smart Pendant displays UF starting at 1 
                int uf = (int)pendant.property("gotoPosButton", "userFrameNumber").getIValue();
                frame.setUserFrame(uf);    
            }
            yaskawa.ext.api.Position tcp = myRobot.toolTipPosition(frame, tool);

            List<Double> pos = tcp.getPos(); 
            List<Double> orient = tcp.getOrient().getV();
            List<Any> target =  new ArrayList<Any>();
            target.add(Any.rValue(pos.get(0)));
            target.add(Any.rValue(pos.get(1)));
            target.add(Any.rValue(pos.get(2)));
            target.add(Any.rValue(orient.get(0)));
            target.add(Any.rValue(orient.get(1)));
            target.add(Any.rValue(orient.get(2)));
        
            //set the position in the new coordinate system
            pendant.setProperty("gotoPosButton", "target", Any.aValue(target));

            // Update UI
            updateTextFromTarget(target);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public synchronized void clearTarget()
    {
        try{

            //set to the current position
            List<Any> target =  new ArrayList<Any>();
            for(int i = 0;i<6;i++)
            {
                pendant.setProperty("jogTarget" + String.valueOf(i) + "Entry", "text", "");
            }
    
            pendant.setProperty("gotoPosButton", "target", Any.aValue(target));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void setWorkHomeFromController()
    {
        try{

            //get the work home position
            Robot myRobot = controller.currentRobot();
            yaskawa.ext.api.Position pos = myRobot.workHomePosition();
            List<Double> joints = pos.getJoints();

            //set to the current position
            List<Any> target =  new ArrayList<Any>();
            for(int i = 0;i<joints.size();i++)
            {
                pendant.setProperty("workHome" + String.valueOf(i) + "Entry", "text", Any.sValue(String.valueOf(joints.get(i))));
                target.add(Any.rValue(joints.get(i)));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PredefinedCoordFrameType indexToCoordType(int index) {
        switch (index) {
            case 0: return PredefinedCoordFrameType.Joint;
            case 1: return PredefinedCoordFrameType.World;
            case 2: return PredefinedCoordFrameType.Robot;
            case 3: return PredefinedCoordFrameType.User; 
            default: return PredefinedCoordFrameType.None;
        }
    }

    public int coordTypeToIndex(PredefinedCoordFrameType coordType) {
        
        switch (coordType) {
            case Joint: return 0;
            case World: return 1;
            case Robot: return 2;
            case User: return 3;
            default: return -1;
        }
    }

    public synchronized void onJogCoordComboBoxClicked(PendantEvent e)
    {
        try {
            var props = e.getProps();
            int index = (int)props.get("index").getIValue();
            PredefinedCoordFrameType coordType = indexToCoordType(index);

            pendant.setProperty("gotoPosButton", "targetCoordType", coordType.getValue());

            if(coordType == PredefinedCoordFrameType.Joint)
                setJointTargetFromRobot();
            else
                setTCPTargetFromRobot(coordType);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void onJogUserFrameEdited(PendantEvent e)
    {
        try {
            var props = e.getProps();
            // Note userFrame is using 0 based indexing but Smart Pendant displays UF starting at 1 
            int uf = Integer.valueOf(props.get("text").getSValue()) - 1;
            pendant.setProperty("gotoPosButton", "userFrameNumber", uf);

            PredefinedCoordFrameType coordType = 
                PredefinedCoordFrameType.findByValue((int)pendant.property("gotoPosButton", "targetCoordType").getIValue());
            if(coordType == PredefinedCoordFrameType.Joint)
                setJointTargetFromRobot();
            else
                setTCPTargetFromRobot(coordType);     
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void onJogToolEdited(PendantEvent e)
    {
        try {
            var props = e.getProps();
            int tool = Integer.valueOf(props.get("text").getSValue());
            pendant.setProperty("gotoPosButton", "toolNumber", tool);

            PredefinedCoordFrameType coordType = 
                PredefinedCoordFrameType.findByValue((int)pendant.property("gotoPosButton", "targetCoordType").getIValue());
            if(coordType == PredefinedCoordFrameType.Joint)
                setJointTargetFromRobot();
            else
                setTCPTargetFromRobot(coordType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void onJogMotionComboBoxClicked(PendantEvent e)
    {
        try {
            var props = e.getProps();
            int index = (int)props.get("index").getIValue();

            MotionTypes motionType = MotionTypes.DefaultInterpolation;
            switch (index) {
                case 1: motionType = MotionTypes.JointInterpolation; break;
                case 2: motionType = MotionTypes.LinearInterpolation; break;
            }

            pendant.setProperty("gotoPosButton", "motionType", motionType.getValue());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
    public synchronized void onJogSpeedComboBoxClicked(PendantEvent e)
    {
        try {
            var props = e.getProps();
            int index = (int)props.get("index").getIValue();

            JogSpeed jogSpeed = JogSpeed.Low;
            switch (index) {
                case 1: jogSpeed = JogSpeed.Medium; break;
                case 2: jogSpeed = JogSpeed.High; break;
                case 3: jogSpeed = JogSpeed.Top; break;
            } 

            pendant.setProperty("gotoPosButton", "jogSpeed", jogSpeed.getValue());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized void onJogTargetEntryEdited(PendantEvent e)
    {
        try {
            setJogTargetFromEntry();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void setJogTargetFromEntry()
    {
        try {
            List<Any> target =  new ArrayList<Any>();
            for(int i = 0;i<6;i++)
            {
                Any val = pendant.property("jogTarget" + String.valueOf(i) + "Entry", "text");
                target.add(val);
            }
                
            pendant.setProperty("gotoPosButton", "target", Any.aValue(target));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Synchronize tool display with jogging tool
    public synchronized void onActiveToolChanged(ControllerEvent e)
    {
        try {
            int tool = (int)e.props.get("activeTool").getIValue();
            pendant.setProperty("activeTool", "text", tool);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Synchronize speed display with jogging speed
    public synchronized void onJogSpeedChanged(ControllerEvent e)
    {
        try {
            int speed = (int)e.props.get("jogSpeedLevel").getIValue();
            int currentSpeed = (int)pendant.property("jogSpeedComboBox", "currentIndex").getIValue() + 1;
            if(speed != currentSpeed)
                pendant.setProperty("jogSpeedComboBox", "currentIndex", speed - 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onHomeScreenUnregisterClicked(PendantEvent e)
    {
        try {

            pendant.unregisterIntegration("homescreen");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

            // run 'forever' (or until API service shutsdown)
            try {
                thisExtension.extension.run(thisExtension::updateChart);
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

    protected Thread updThread;
    protected int updRate;
    protected double chartScale;
    protected double time;
    protected AtomicBoolean run = new AtomicBoolean(true);
    protected AtomicBoolean update = new AtomicBoolean(false);

    protected Extension extension;
    protected final Pendant pendant;
    protected Controller controller;

    protected Version apiVersion;

    protected String lang; // e.g. "en", "ja"
    protected String localeName; // e.g. "en", "ja", "ja_JP", "es_es", "es_mx"

    protected Locale locale;
    protected ResourceBundle strings;
    protected MessageFormat formatter;

}
