namespace * yaskawa.ext.api
namespace csharp Yaskawa.Ext.API


typedef i64 ExtensionID
typedef i64 ControllerID
typedef i64 PendantID
typedef i64 FileID

exception InvalidID {
}

exception IllegalArgument {
    1:required string msg;
}


typedef list<double> Vector;
typedef list<i64> IVector;

typedef Vector Point;

struct Matrix {
    1: i64 rows;
    2: i64 cols;
    3: list<Vector> m;
}

/** Orientations in 3D can be represented either as:
    * A 4-element unit-length Quaternion (x,y,z,w)
    * A 3x3 transformation matrix
    * A 3-vector of Euler angles 
        (Tait/Cardan-Bryant) Roll, Pitch, Yaw angles (alpha,beta,gamma).
        This is equivelent to EulerXYZ and assumes that the
         x-axis points forward, y-axis points right and the z-axis up.
         (Aeronautical convention)
*/
enum OrientationRepresentation { 
    Quaternion, 
    Matrix, 
    EulerRPY 
}

/** If rep is Quaternion or EulerRPY then v contains the elements, 
    otherwise if rep is Matrix m contains the 3x3 transform */
struct Orient {
    1: OrientationRepresentation rep = OrientationRepresentation.EulerRPY;
    2: optional Vector v;
    3: optional Matrix m;
}

struct VectorOrient {
    1: Vector v;
    2: Orient o;
}

/** Position and Orientaiton in 3D defined by
    an origin point and two points that define a plane
*/
struct PointPlane {
    1: Point origin;
    2: Point xx;
    3: Point xy;
}

struct Version {
    1: i16 nmajor;
    2: i16 nminor;
    3: i16 npatch;
    4: optional string release;
    5: optional string build;
}


enum LoggingLevel {
    Debug = 0, 
    Info = 1, 
    Warn = 2, 
    Critical = 3 
}

/** A Coordinate Frame is a reference frame in space
    against which concrete coordinates are relative.

    In 3D space a frame can be represented by an origin
    and the direction of the x,y & z axes - explicitly or
    otherwise.

    * Implicit - the coordinate frame is implicit in its type (e.g. predefined)
               (e.g. the base frame is relative to the fixed mount of the robot)
    * Transform - the frame is represented by a standard 4x4 transformation matrix
    * OffsetOrient - the frame is represented by an origin and orientation in 3D
    * OriginPlane - the frame is defined by an origin and two additional points making up a plane               
*/
enum CoordFrameRepresentation { 
    Implicit = 0, 
    Transform, 
    OffsetOrient, 
    OriginPlane 
}

/** Type of predefined coordinate frames (representaton is implicit)
    * Joint - the joint space of the robot (dimension equals the number of axes / dof)
    * World - Cartesian frame of environment (typically coincident with the robot base)
    * Base  - Cartesian frame of the base mount of the robot
              (for robots not mounted on a moveable base, fixed relative to the robot frame)
    * Robot - Cartesian frame of the robot (e.g. from first axis)
    * ToolPlate - Cartesian frame of the tool mounting plate
    * ToolTip - Cartesian frame of the tip of the tool (i.e. End-Effector) 
                (this depends on the specific tool)
    * User - Cartesian frame configured by user stored in the controller
             (multiple user frames can be defined and referenced by index)        
*/
enum PredefinedCoordFrameType
{
    Joint = 1,  
    World = 2,  
    Base  = 7,   
    Robot = 3, 
    ToolPlate = 4, 
    ToolTip   = 5, 
    User      = 6,
    None=0
}

/** Represents a coordinate frame
    Used as a reference frame for position coordinates

    If rep is Implicit then it represents one of the predefined
    frames defined by the physical configuration of the cell, robot and/or tool.  
    If predefined is:
    * Joint - the frame is in the axis space of the robot joints (hence dimension is dof of the robot)
    * World - the fixed Cartesian frame of the cell (often coincident with Base)
    * Base - the base mount of the robot - requires robot set
    * Robot - the robot itself (e.g. origin at first axis) - requires robot set
              (unless mounted on a moveable base, fixed offet, possibly 0, from base)
    * ToolPlate - toolplate of the end-effector (as when no tool mounted)
    * ToolTip - 'business end' of the tool.  Depends on which tool is mounted/active
                and requires tool be set 
    * User - User defined frames configured in the controller - requires userFrame set.
             User frames also have an associated tool in the YRC Controller, hence requires
             tool to be set.  pointplane may be set if user frame is defined
             via origin point and points in plane

    If rep is Transform then transform Matrix must be valid
    If rep is OffsetOrient, vecorient must be valid
*/
struct CoordinateFrame {
    1: CoordFrameRepresentation rep = CoordFrameRepresentation.Implicit;
    2: PredefinedCoordFrameType predefined;
    3: optional string name;
    4: optional RobotIndex robot;
    5: optional ToolIndex tool; 
    6: optional Matrix transform;
    7: optional VectorOrient vecorient;
    8: optional UserFrameIndex userFrame;
    9: optional PointPlane pointplane; // points in robot frame
}


enum DistanceUnit    { None, Millimeter, Inch, Meter }
enum OrientationUnit { None, Pulse, Radian, Degree }

struct Position {
    1: CoordinateFrame frame;

    2: optional DistanceUnit    distUnit;
    3: OrientationUnit orientUnit;

    // Cartesian
    4: optional Vector pos;
    5: optional Orient orient;

    // Joint space
    6: optional Vector joints;

    7: optional IVector  closure;
}


/** Useful union for holding one of several data types */
union Any {
    1: bool   bValue;
    2: i64    iValue;
    3: double rValue;
    4: string sValue;
    5: Vector vValue;
    6: Position pValue;
    7: list<Any> aValue; // aka array
    8: map<string, Any> mValue;
}


struct LoggingEvent {
    1: i64 timestamp;   // millisecs since 1970-01-01
    2: string datetime; // string representation of timestamp
    3: LoggingLevel level;
    4: string entry;
}

/** Data structures for passing values to charts for plotting
    
    Series data is used for line and scatter charts, while
    category data is used for pie and bar charts
*/
struct Series {
    1: Vector x;
    2: Vector y;
    3: optional Vector z;
    4: optional string color;
    5: optional string vertex;
    6: optional string style;
    7: optional bool hidden;
    8: optional i32 maxPts;
}

struct Category {
    1: double v;
    2: optional string color;
    3: optional bool hidden;
}

struct DataPoint {
    1: double x;
    2: double y;
    3: optional double z;
}

union Data {
    1: Series sData;
    2: Category cData;
}

typedef map<string, Data> DataSet;

/** External File Storage: name, location and volume details
    (API version 2.3 and later)
*/
struct storageInfo {
    1: string path;
    2: string volname;
    3: string volsize;
}

/**
  The Extension API.

  Use this interface to initially register the extension with the main pendant
  API Service and obtain handle IDs to the Controller and Pendant services.

  Note in this function-level documentation, functions of the Controller Service
  take an initial ControllerID parameter, Pendant Service functions take an initial PendantID etc.  
  However, if you are using a Yaskawa supplied client library these may be wrapped as 
  object methods and hence the initial id should be omitted.
*/
service Extension
{
    /** Version of API the service implements.
        Smart Pendant | API version
        2.0           | 2.0.4
        2.1           | 2.1.0
        2.2           | 2.2.0
        2.3           | 2.3.0
        3.0           | 3.0.0
    */
    Version apiVersion();

    /** Register extension with Smart Pendant API service.  
        Extension must exist in the extension database (i.e. through installation)
    */
    ExtensionID registerExtension(1:string canonicalName,
                                  2:string launchKey,
                                  3:Version version,
                                  4:string vendor,
                                  5:set<string> supportedLanguages)
                        throws (1:IllegalArgument e);

    void unregisterExtension(1:ExtensionID id) throws (1:InvalidID e);

    /** Indicate liveliness 
        API service will automatically unregister extensions that are unresponsive for some period.
        If extension is not regularly calling events(), call ping() to indicate the extension is operational.
    */
    void ping(1:ExtensionID id) throws (1:InvalidID e);

    /** Obtain ID handle for Robot Conroller API */
    ControllerID controller(1:ExtensionID id) throws (1:InvalidID e);

    /** Obtain ID handle for Pendant UI API */
    PendantID pendant(1:ExtensionID id) throws (1:InvalidID e);


    /** Log message to standard pendant logging facility 
        Visible to end-users upon plain-text log file export.
        Note that Debug level logging is ignored unless in Developer access level.
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void log(1:ExtensionID id, 2:LoggingLevel level, 3:string message);

    /** Subscribe to receive log message events via events() */
    void subscribeLoggingEvents(1:ExtensionID id);

    void unsubscribeLoggingEvents(1:ExtensionID id);

    /** Obtain list of logging events that have occured since last call 
        NB: For development troubleshooting only, logging events 
        only available when development access enabled 
    */
    list<LoggingEvent> logEvents(1:ExtensionID id);

    /** Obtain a list of ExternalStorageDevice structs corresponding to USB storage 
        available to the pendant/and or controller.  If no storage is available this 
        list will have no elements. 
        (API version 2.3 and later)
    */
    list<storageInfo> listAvailableStorage(1:ExtensionID eid) throws (1:InvalidID e);

    /** list files/directories in external storage for the specified storage path 
        (API version 2.3 and later)
    */
    list<string> listFiles(1:ExtensionID eid, 2:string path) throws (1:InvalidID e);

    /** Open files for reading and/or writing
        the argument path is the full path to the file of interest
        the argument flags can be 'r' (read) or 'w' (write w/ append) 
        (the FileID will return -1 if it failed to open the file) 
        (API version 2.3 and later)
    */ 
    FileID openFile(1:ExtensionID eid,2:string path, 3:string flags) throws (1:InvalidID e);

    /** Close files from reading and/or writing
        (API version 2.3 and later)
    */ 
    void closeFile(1:ExtensionID eid, 2:FileID id) throws (1:InvalidID e);

    /** Check if the file is available for read/write. 
        (API version 2.3 and later)
    */
    bool isOpen(1:ExtensionID eid, 2:FileID id) throws (1:InvalidID e);
    
    /** Read all data from the file. 
        (API version 2.3 and later)
    */
    string read(1:ExtensionID eid, 2:FileID id) throws (1:InvalidID e);

    /** Read a chunk of data from the file. 
        the argument offset indicates the number of bytes into the file
        the argument len indicates the number of bytes to read 
        (API version 2.3 and later)
    */ 
    string readChunk(1:ExtensionID eid, 2:FileID id, 3:i64 offset, 4:i64 len) throws (1:InvalidID e);

    /** Write a string to a file.  This will create a new file (and or directory) 
        if missing, but will simply append if the file already exists.
        (API version 2.3 and later)
    */
    void write(1:ExtensionID eid, 2:FileID id, 3:string data) throws (1:InvalidID e)

    /** Write the file to disk.  For files not local to the pendant this 
        will FTP them to the controller.
        (API version 2.3 and later)
    */
    void flush(1:ExtensionID eid, 2:FileID id) throws (1:InvalidID e);

    /* Undocumented */

    string publicKey(1:ExtensionID id) throws (1:InvalidID e);
    string oneTimeAuthToken(1:ExtensionID id, 2:string oneTimeSalt, 3:binary publicKey) throws (1:InvalidID e);
    list<string> installPackage(1:ExtensionID id, 2:string authToken, 3:binary packageData, 4:string overridePasscodeEnc);
}






//
// Pendant Service (UI)

enum PendantEventType {
    Startup = 0,
    Shutdown,
    SwitchedScreen,
    PopupOpened,
    PopupClosed,
    UtilityOpened,
    UtilityClosed,
    UtilityMoved, // future
    Clicked,
    Pressed,
    Released,
    TextEdited,
    Accepted,
    EditingFinished,
    CheckedChanged,
    Activated,
    PanelOpened,
    PanelClosed,
    Canceled,
    JoggingPanelVisibilityChanged,
    VisibleChanged,
    IntegrationPointSwitchStateChanged,
    Other = 16384
}

struct PendantEvent {
    1: required PendantEventType eventType;
    2: optional map<string,Any> props;
}


struct PropValues {
    1: required string itemID;
    2: required map<string,Any> props;
}

enum IntegrationPoint {
    UtilityWindow = 0,
    NavigationPanel = 10,
    ProgrammingCommandBar = 20, // future
    ProgrammingHeaderBar = 30,  // future
    SmartFrameJogPanelTopLeft = 40,
    SmartFrameJogPanelTopRight = 41,
    SmartFrameJogPanelTopAny = 44,
    SmartFrameJogPanelBottomLeft = 45,
    SmartFrameJogPanelBottomCenter = 46,
    SmartFrameJogPanelBottomRight = 47,
    SmartFrameJogPanelBottomAny = 49,
    JogPanelTopCenter = 50,
    HomeScreen = 60,
    JobTestPanelCenter = 70,
    JobTestPanelBottomLeft = 71,
    JobTestPanelBottomRight = 72,
    JobTestPanelTopLeft = 73,
    JobTestPanelTopRight = 74,
}


enum Disposition {
    Negative = 1,
    Neutral = 0,
    Positive = 2
}


/** The Pendant API provides functions for interacting with and 
    integrating the main Smart Pendant user-interface.

    (Extensions are not required to have a user-interface)
*/
service Pendant
{
    /** Version of the Smart Pendant itself
        (avoid using this for conditional feature use - use the Extension apiVersion() instead)
    */
    Version pendantVersion(1:PendantID p);

    /** Subscribe to specified set of Pendant service events.  May be called multiple times to add to subscription. */
    void subscribeEventTypes(1:PendantID p, 2:set<PendantEventType> types);

    /** Unsubscribe from specified set of Pendant service events. */
    void unsubscribeEventTypes(1:PendantID p, 2:set<PendantEventType> types);

    /** Subscribe to specified set of Pendant service YML Item-specific events.
        Specified events will be sent for all specified items.  May be called multiple times to add to subscription. */
    void subscribeItemEventTypes(1:PendantID p, 2:set<string> itemIDs, 3:set<PendantEventType> types);

    /** Unsubscribe from specified set of Pendant service YML Item-specific events. */
    void unsubscribeItemEventTypes(1:PendantID p, 2:set<string> itemIDs, 3:set<PendantEventType> types);

    /** Obtain list of Pendant service events that have occured since last call */
    list<PendantEvent> events(1:PendantID p);

    /** Query the current UI language of the pendant interface.  
        Returns IETF language codes (RFCs 5646, 5645, 4647) of languages
        (typically ISO 693-1 code when region insignificant)
    */
    string currentLanguage(1:PendantID p);

    /* Query the current UI locale (which indicates the language & region) */
    string currentLocale(1:PendantID p);

    /** The UI screen currently shown to the pendant user */
    string currentScreenName(1:PendantID p);

    /** Register an Item type described using a YML source code string
        Returns a list of parsing errors (0 on success)
    */
    list<string> registerYML(1:PendantID p, 2:string ymlSource);

    /** Register an image file for later reference by filename (must be uniquely named, with .jpg or .png).
        If file cannot be accessed by service, it will be locally read and registerImageData called instead.
    */
    void registerImageFile(1:PendantID p, 2:string imageFileName)
                          throws (1:IllegalArgument e);

    /** Register an image for later reference by name (must be uniquely named, with .jpg or .png extension) */
    void registerImageData(1:PendantID p, 2:binary imageData, 3:string imageName)
                          throws (1:IllegalArgument e);

    /** Register a HTML file for later reference by filename (must be uniquely named, with .html).
        If file cannot be accessed by service, it will be locally read and registerHTMLData called instead.
    */
    void registerHTMLFile(1:PendantID p, 2:string htmlFileName)
                          throws (1:IllegalArgument e);

    /** Register HTML for later reference by name (must be uniquely named, with .html extension) */
    void registerHTMLData(1:PendantID p, 2:binary htmlData, 3:string htmlName)
                          throws (1:IllegalArgument e);

    /** Register a translation file (e.g. a Java properties file for a language); extension is used to determine format */
    void registerTranslationFile(1:PendantID p, 2:string locale, 3:string translationFileName)
                          throws (1:IllegalArgument e);

    /** Register translation file data (translationName typically filename-like; extension is used to determine format) */
    void registerTranslationData(1:PendantID p, 2:string locale, 3:binary translationData, 4:string translationName)
                          throws (1:IllegalArgument e);


    /** Register a Utility window with the UI.  
        The itemType references a previously registered YML item instantiated for the window
        UI content.
        A main menu entry will automatically be added to the pendant UI, for opening the utility window.
    */
    void registerUtilityWindow(1:PendantID p, 2:string identifier, 
                               3:string itemType,
                               4:string menuItemName, 5:string windowTitle)
                          throws (1:IllegalArgument e);

    void unregisterUtilityWindow(1:PendantID p, 2:string identifier)
                          throws (1:IllegalArgument e);
    
    /** Open (make visible) previously registered Utility Window */
    void openUtilityWindow(1:PendantID p, 2:string identifier);

    /** Close a visible Utility Window (make invisible - state is maintained) */
    void closeUtilityWindow(1:PendantID p, 2:string identifier);

    /** Collapse previously registered Utility Window, if in expanded state (and expandCollapseResize true) */
    void collapseUtilityWindow(1:PendantID p, 2:string identifier);

    /** Expand previously registered Utility Window, if in collapsed state (and expandCollapseResize true) */
    void expandUtilityWindow(1:PendantID p, 2:string identifier);


    /** Register UI content at the specified integration point in the pendant UI.
        The itemType should reference a YML item previously registered via registerYML(). 
    */
    void registerIntegration(1:PendantID p, 2:string identifier, 3:IntegrationPoint integrationPoint,
                             4:string itemType, 5:string buttonLabel, 6:string buttonImage)
                          throws (1:IllegalArgument e);

    void unregisterIntegration(1:PendantID p, 2:string identifier)
                          throws (1:IllegalArgument e);

    /** Register a Switch component at the specified integraiton point in the pendant UI.
        When the switch is toggled, it creates a PendantEvent with a type of IntegrationPointSwitchStateChanged that can be gotten via events()
        Switches registered with registerSwitch() can be unregistered with unregisterIntegration()
    */
    void registerSwitch(1:PendantID p, 2:string identifier, 3:IntegrationPoint integrationPoint, 
        4:string switchLabel, 5:string offPositionLabel, 6:string onPositionLabel, 7:bool defaultState)
                          throws (1:IllegalArgument e);

    /** get property of an item by id */
    Any property(1:PendantID p, 2:string itemID, 3:string name)
                throws (1:IllegalArgument e);

    /** Set property of an item by id */
    void setProperty(1:PendantID p, 2:string itemID, 3:string name, 4:Any value)
                     throws (1:IllegalArgument e);

    /** Set several properties (potentially for different items) at once.  This is more
        efficient that many repeated calls to setProperty().
        Note it is asynchronous so no errors/exceptions are thrown */
    oneway void setProperties(1:PendantID p, 2:list<PropValues> propValuesList);

    /** Set the configuration of a chart by ID. */
    void setChartConfig(1:PendantID p, 2:string chartID, 3:Any config)
        throws (1:IllegalArgument e);

    /** Get the configuration of a chart by ID */
    Any getChartConfig(1:PendantID p, 2:string chartID)
        throws (1:IllegalArgument e);

    /** Set the dataset of a chart by ID. In line and scatter charts,
        you can set 'right' to true to pass the dataset for a secondary
        scale on the right hand side.
    */
    void setChartData(1:PendantID p, 2:string chartID, 3:DataSet dataset, 4:bool right)
        throws (1:IllegalArgument e);

    /** Get the dataset of a chart by ID. In line and scatter charts,
        you can set 'right' to true to access the dataset for a secondary
        scale on the right hand side.
    */
    DataSet getChartData(1:PendantID p, 2:string chartID, 3:bool right)
        throws (1:IllegalArgument e);

    /** Add a new key to the dataset of a chart by ID. In line and scatter charts,
        you can set 'right' to true to pass the dataset for a secondary
        scale on the right hand side.
    */
    void addChartKey(1:PendantID p, 2:string chartID, 3:string key, 4:Data data, 5:bool right)
        throws (1:IllegalArgument e);

    /** Removes an existing key from the dataset of a chart by ID. In line and 
        scatter charts, you can set 'right' to true to remove from the 
        secondary dataset.
    */
    void removeChartKey(1:PendantID p, 2:string chartID, 3:string key, 4:bool right)
        throws (1:IllegalArgument e);
    
    /** Hides an existing key from the dataset of a chart by ID. In line and 
        scatter charts, you can set 'right' to true to hide a key from the 
        secondary dataset.
    */
    void hideChartKey(1:PendantID p, 2:string chartID, 3:string key, 4:bool hidden, 5:bool right)
        throws (1:IllegalArgument e);

    /** Append new data points to a specified key in the data of a chart by ID.
        This function will only have an effect on line/scatter charts. Set 
        'right' to true to pass the dataset for a secondary scale on the right 
        hand side.
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void appendChartPoints(1:PendantID p, 2:string chartID, 3:string key, 
                    4:list<DataPoint> points, 5:bool right);

    /** Increments a category value by `val`.
    */
    void incrementChartKey(1:PendantID p, 2:string chartID, 3:string key, 4:double val)
        throws (1:IllegalArgument e);

    /** Export the current chart contents to the specified filename (must be uniquely named, with .jpg or .png).
        Calls exportChartImageData if the extension is unable to access the file.
    */
    string exportChartImage(1:PendantID p, 2:string chartID, 3:string imageFileName)
        throws (1:IllegalArgument e);

    /** Export the current chart contents to a binary blob (must be uniquely named, with .jpg or .png extension) */
    binary exportChartImageData(1:PendantID p, 2:string chartID, 3:string imageFileName)
        throws (1:IllegalArgument e);

    /** Show notice to user.
        Notices are automaticlly hidden after a short display period.
        Notice messages are logged, if log parameter if provided, that will be logged instead of title & message.
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void notice(1:PendantID p, 2:string title, 3:string message, 4:string log);

    /** Show notice to user with specified disposition.
        As for notice() but displayed in a way that connotes the specified disposition.
        For example, a Positive disposition may be shown in green.
        Note it is asynchronous so no errors/exceptions are thrown.
        (API version 2.1 and later)
    */
    oneway void dispNotice(1:PendantID p, 2:Disposition disposition, 3:string title, 4:string message, 5:string log);


    /** Show error to user.
        Errors should only indicate important situations that the user must be aware of and for which deliberate
        acknowledgement is required before proceeding.  Typically, some action will be required to correct the situation.
        Errors are displayed until dismissed by the user.
        Error messages are logged, if log parameter if provided, that will be logged instead of title & message.
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void error(1:PendantID p, 2:string title, 3:string message, 4: string log);


    /** Display modal pop-up dialog.  Typically, Yes/No, although negativeOption can be omitted 
        The identifier can be used to associate the corresponding PopupOpened & PopupClosed events triggered by
        user positive/negative selection or automatic dismissal/cancellation - for example is screen switched, alarm etc.
     */
    void popupDialog(1:PendantID p, 2:string identifier, 3:string title, 4:string message, 5:string positiveOption, 6:string negativeOption)
                     throws (1:IllegalArgument e);
    /** Cancel an open popup dialog.  If the dialog has a negative option, behaves as if user selected it, otherwise
        no event is generated */
    void cancelPopupDialog(1:PendantID p, 2:string identifier);

    /** Inserts an instruction, returns a string:
       Success,
       UnsupportedCommand,
       InvalidFormat,
       ProgrammingViewClosed,
       JobDoesNotExist,
       CallingJobFromSameJob,
       ExceededMaxArguments,
       JobNotEditable,
       MultiSelectActive,
       TimedOut,
       Unknown */
    string insertInstructionAtSelectedLine(1:PendantID p, 2:string instruction);

    /** Displays a standard pendant UI screen by passing a string with the screen identifier. (Only available from SDK API 2.2 onward)
        Refer to the YML - URI Links documentation for the list of supported screens and settings.
        Do not include the URI "&lt;a href&gt; screen:" portion in the identifier.  For example to display the jogging panel use:
        pendant.displayScreen("programmingView?panel=jogging"); */
    void displayScreen(1:PendantID p, 2:string identifier);

    /** Displays an html file in a standard Smart Pendant help dialog. (Only available from SDK API 3.0 onward) */
    void displayHelp(1:PendantID p, 2:string title, 3:string htmlContentFile);
}




//
// Controller Service

typedef i32 RobotIndex
typedef i32 ToolIndex
typedef i32 UserFrameIndex
typedef i32 ZoneIndex

enum ControllerEventType {
    Connected = 0,
    RobotModel,
    ExclusiveControl,
    CycleTime,
    PowerOnTime,
    ServoOnTime,
    EnabledOptionsChanged,
    OperationMode,
    ServoState,
    PlaybackState,
    SpeedOverride,
    ActiveTool,
    AlarmActive,
    ActiveAlarmsChanged,
    RestartRequired,
    EStopEngaged,
    EnableSwitchActive,
    RemoteMode,
    JoggingActive,
    JoggingSpeedChanged,
    JoggingModeChanged,
    RobotTCPPosition, // unused/future
    BrakeRelease, // unused/future
    SoftLimitRelease,
    SelfInterferenceRelease
    AllLimitsRelease,
    ParametersChanged,
    PredefinedPositionsChanged,
    FeatureAvailabilityChanged,
    JointLimitsChanged,
    JointMotorPulseDegreeRatioChanged,
    FunctionalSafetyHardwareAvailable,
    NetworkInterfacesChanged,

    CurrentRobot,

    JobTagsChanged,
    JobListChanged,
    JobStackChanged,
    CurrentJob,
    DefaultJob,
    ToolsChanged,
    ToolIOsChanged,
    UserFramesChanged,
    ZonesChanged,
    SafetyRobotRangeLimitDataChanged, // unused/future
    SafetyAxisSpeedMonitorDataChanged, // unused/future
    SafetySpeedLimitDataChanged, // unused/future
    SafetyExternalForceMonitorFileChanged, // unused/future
    SafetyIOListChanged,

    VariablesChanged, // unused/future
    VariableNamesChanged,
    IONamesChanged,
    IOValueChanged,

    PermissionGranted = 1000,
    PermissionRevoked = 1001
}

struct ControllerEvent {
    1: required ControllerEventType eventType;
    2: optional map<string,Any> props;
}

enum OperationMode { Automatic=0, Manual=1 }
enum ServoState { Off=0, Ready=1, On=2 }
enum PlaybackState { Run=0, Hold=1, Idle=2 }
enum PlaybackCycle { Step=0, Once=1, Continuous=2}


enum ControlGroupType {
    Robot = 0,
    Base,
    Station,
    Combined = 254,
    None = 255
}

/** A simple control group (of axes)
    A Robot, Robot Base (e.g. rail) or Station
*/
struct SimpleControlGroup {
    1: ControlGroupType type;
    2: optional i8 index;
}

/** Set of simple control groups combined into a new
    control group, optionally designating a master
*/
struct CombinedControlGroup {
    1: list<SimpleControlGroup> groups;
    2: optional SimpleControlGroup master;
}

/** General control group
    May be 
    * simple, such as a single Robot OR
    * a combined control group consisting of multiple
      simple control groups.
*/
struct ControlGroup {
    1: ControlGroupType type;
    2: i8 number;
    3: optional SimpleControlGroup sgroup;
    4: optional CombinedControlGroup cgroup;
}


struct RobotJobInfo {
    1: string name;
    2: string programmingLanguage; // e.g. "INFORM"
    3: string jobType;
    4: bool editable;
    5: i64 timestamp;   // last edited - millisecs since 1970-01-01
    6: string datetime; // string representation of timestamp
    7: string comment;
    8: CoordinateFrame frame;
    9: ControlGroup controlling;
}

/** Default units kg, m and radian
    (offsetUnit and orientUnit only available for API version 3.0 and later)
*/
struct Tool {
    1: ToolIndex index;
    2: optional string name;
    3: optional double weight; // kg
    4: optional Vector offset;
    5: optional Orient orient;
    6: optional Vector centerOfMass; // m
    7: optional Vector momentOfInertia; // kg-m^2
    8: optional string blockIOName;
    9: optional DistanceUnit     offsetUnit; // default m (API >=v3.0)
    10: optional OrientationUnit orientUnit; // default radians (only relevent if Orient contains angles; API >= 3.0)
}


enum Scope { Local, Global }

/** Variable address space */
enum AddressSpace {
    Unified,
    Byte,
    Int,
    DoubleInt,
    Real,
    String,
    Position
}

/** Variable address (scope, address-space & address/index) */
struct VariableAddress {
    1: Scope scope = Scope.Global;
    2: AddressSpace aspace = AddressSpace.Unified;
    3: i64 address;
}

enum ZoneAction { Status, Alarm }

/** Zone - a region in space
    * In joint space, defined by minimum and maximum joint angles
    * In Cartesian space, a lower/min and upper/max corner defining a rectangular prism (a box)
      (only Base, Robot and User-Frames are supported for Cartesian zones)
    
    Action determines if an I/O status signal changes in response to zone entry/exit,
    or if an Alarm is issued upon entry.

    Note: index is 0-based, interface Zone Numbers are 1-based
*/
struct Zone {
    1: i16 number;
    2: optional string name;
    3: optional bool enabled;
    4: optional ZoneAction action;
    5: optional Position minPos;
    6: optional Position maxPos;
}

/** Available modes of jogging 
    * Joint - the joint space of the robot (dimension equals the number of axes / dof)
    * World - Cartesian frame of environment (typically coincident with the robot base)
    * Tool  - Cartesian frame of the tip of the tool (i.e. End-Effector) 
                (this depends on the specific tool)
    * User - Cartesian frame configured by user stored in the controller
             (multiple user frames can be defined and referenced by index)  
    * Hand - Hand guiding mode for jogging
    * Smart - Smart Frame, based on the pendant orientation 
    (API version 3.0 and later)     
*/
enum JogMode {
    Joint = 0,  
    World = 1,  
    Tool  = 2,   
    User  = 3, 
    Hand  = 4, 
    Smart = 5,
}

/** Available speeds for jogging 
    * Low - slowest
    * Medium  - 
    * High - 
    * Top - fastest
    (API version 3.0 and later)
*/
enum JogSpeed {
    Low    = 1,
    Medium = 2,  
    High   = 3,  
    Top    = 4
}





/** Interface to Robot Controllers 

    In general, a pendant may operate in connected or disconnected states.  When connected to a Robot Controller
    it may be monitoring or not have exclusive control (i.e. not be the single-point-of-control).

    However, typically, once an extension is running, the pendant is connected to the controller and 
    is the single-point-of-control. 
*/
service Controller
{
    //
    // Permissions

    /** Request specified permissions. */
    bool requestPermissions(1:ControllerID c, 2:set<string> permissions) throws (1:IllegalArgument e);

    /** Check permisions obtained. */
    bool havePermission(1:ControllerID c, 2:string permission) throws (1:IllegalArgument e);

    /** Relinquish permissions (no effect if not held). */
    void relinquishPermissions(1:ControllerID c, 2:set<string> permissions);



    //
    // Controller selection (currently unused)

    /** Connect to the specified Robot Controller (by IP adress or hostname if DNS available)
        Typically, the pendant will already be connected to a controller when extensions are started,
        so calling connect() is not required.
    */
    void connect(1:ControllerID c, 2:string hostName);

    /** Disconnect from the connected controller.  This leaves the pendant in the 'disconnected' state. 
        When disconnected, many functions are unavailable or will return default values.
    */
    void disconnect(1:ControllerID c);


    //
    // Events

    /** Subscribe to the specified events, if not already.
        Note: If using a Yaskawa supplied client library with event consumer callback support,
              registering an event consumer callback will automatically subscribe to the appropriate event.
    */
    void subscribeEventTypes(1:ControllerID c, 2:set<ControllerEventType> types);

    /** Unsubscribe from the specified events.  
        If called directly, this may causes event consumers for the events not to be called.
    */
    void unsubscribeEventTypes(1:ControllerID c, 2:set<ControllerEventType> types);

    /** Poll the API Service for pending events.
        Note: If using a Yaskawa supplied client library, this does not need to be called explicitly.        
    */
    list<ControllerEvent> events(1:ControllerID c);


    //
    // State

    /** Returns true if the pendant is connected to a robot controller */
    bool connected(1:ControllerID c);
    /** Returns the hostname or IP address of the robot controller to which the pendant is connected, if any */
    string connectedHostName(1:ControllerID c);

    /** The software version string of the robot controller system software. */
    string softwareVersion(1:ControllerID c);

    /** Returns true if the pendant is only monitoring the robot controller to which it is connected.  This
        implies that no functions that modify the controller and/or robot state will succeed.
    */
    bool monitoring(1:ControllerID c);
    //void setMonitoring(1:ControllerID c, 2:bool monitor);

    //bool acquireExclusiveControl(1:ControllerID c);
    //void releaseExclusiveControl(1:ControllerID c);
    /** Returns true if this pendant is the single-point-of-control for the connected Robot Controller.
        If not, most functions that modify the controller and/or robot state will fail.
    */
    bool haveExclusiveControl(1:ControllerID c);
    //string noExclusiveControlReason(1:ControllerID c);


    /** Current operation mode of the controller
          Automatic (aka Play) - running jobs
          Manual (aka Teach) - for editing jobs, teaching points, jogging, setup etc.
    */
    OperationMode operationMode(1:ControllerID c);

    /** Are the servo drives engaged? 
        On - yes, robot(s) are being actively controlled
        Off - no.  Typically brakes are engaged (unless brake-release engaged)
        Ready - ready to engage servos.  Typically requires operator to use servo enable switch.
    */
    ServoState servoState(1:ControllerID c);


    /** Indicates if a job us running or stopped. 
        Run - jobs are running (robot may be moving)
        Held - jobs were running but have been held/paused.
        Idle - no jobs are running
    */
    PlaybackState playbackState(1:ControllerID c);

    /** The playback cycle affects how the controller runs a job.
        Step - a job is run line-for-line.
        Once - a job is run from the beginning to the end.
        Continuous - a job is run indefinitely from the beginning to the end.
        (API 3.0 and Later)
     */
    PlaybackCycle playbackCycle(1:ControllerID c);

    /**Sets the playback cycle mode. (API 3.0 and Later)*/
    void setPlaybackCycle(1:ControllerID c, 2:PlaybackCycle cycle);

    /** Run the current robot job from the current line.  Requires Servos engaged & Automatic/Play operation and 'jobcontrol' permission. */
    void run(1:ControllerID c);

    /** Pause running job (servoes will remain engaged. 'jobcontrol' permission required. */
    void pause(1:ControllerID c);

    /** Resume running job from paused state. 'jobcontrol' permission required. */
    void resume(1:ControllerID c);

    /** Stop runnng job (will stop motion and disengage servos). 'jobcontrol' permission required. */
    void stop(1:ControllerID c);



    //
    // Jobs


    /** Name of the current job (e.g. job being run or edited) 
        Empty if none.
    */
    string currentJob(1:ControllerID c);

    /** Set the current job. 'jobcontrol' permission required. Pass line=1 for start of job, line=0 for default/no-change. */
    void setCurrentJob(1:ControllerID c, 2:string name, 3:i32 line) throws (1:IllegalArgument e);

    /** Name of the default (aka master) job.  Empty if no default job designated */
    string defaultJob(1:ControllerID c);

    /** query if job with specified name exists */
    bool jobExists(1:ControllerID c, 2:string name);

    /** Details for the named job (throws if non-existent job) */
    RobotJobInfo jobDetails(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** List of robot job names (empty if not connected) */
    list<string> jobs(1:ControllerID c);

    /** List of robot job names matching the name regular expression AND with the given tag
        (empty if not connected or no matches) */
    list<string> jobsMatching(1:ControllerID c, 2:string nameRegex, 3:string tag) throws (1:IllegalArgument e);

    /** Duplicate an existing job with a new name for the copy */
    void duplicateJob(1:ControllerID c, 2:string existingName, 3:string newName) throws (1:IllegalArgument e);

    /** delete the specified job.  The default job cannot be deleted. */
    void deleteJob(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** Read source code for named job (in the programmingLanguage listed in jobDetails() ) */
    string jobSource(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** Replace named job with the source code provided, in given programmingLanguage (e.g. "INFORM").
        Will thow if syntax errors in source.
    */
    void storeJobSource(1:ControllerID c, 2:string name, 3:string programmingLanguage, 4:string sourceCode) throws (1:IllegalArgument e);



    //
    // Tools

    /** List of tools mapping index -> name.
        Unset/defaulted tools are omitted (e.g. those with no name, 0 weight etc.)
        Indices (map keys) may not be sequential.  Returned map may be empty.
    */
    map<ToolIndex, string> tools(1:ControllerID c) throws (1:IllegalArgument e);

    /** Query information on a specific tool, by index */
    Tool tool(1:ControllerID c, 2:ToolIndex index) throws (1:IllegalArgument e);



    //
    // I/O

    /** Return input number of given input name */
    i32 inputNumber(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);
    /** Return input group number for group beginning with given input name */
    i32 inputGroupNumber(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);
    /** Return output nunber of given output name */
    i32 outputNumber(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);
    /** Return output group number for group beginning with given input name */
    i32 outputGroupNumber(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** Return name of specified input number */
    string inputName(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Return name of specified output number */
    string outputName(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Set name of specified input
    Note it is asynchronous so no errors/exceptions are thrown.*/
    oneway void setInputName(1:ControllerID c, 2:i32 num, 3:string name);
    /** Set name of specified output
    Note it is asynchronous so no errors/exceptions are thrown.*/
    oneway void setOutputName(1:ControllerID c, 2:i32 num, 3:string name);

    /** Start monitoring specified input 
        Note that I/O monitoring is limited to a maximum 32 I/O signals 
    */
    void monitorInput(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Start monitoring all inputs in given input group  
        Note that I/O monitoring is limited to a maximum 32 I/O signals (1 group = 8 signals) 
    */
    void monitorInputGroups(1:ControllerID c, 2:i32 groupNum, 3:i32 count) throws (1:IllegalArgument e);
    /** Start monitoring specified output 
        Note that I/O monitoring is limited to a maximum 32 I/O signals 
    */
    void monitorOutput(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Start monitoring all outputs in given output group 
        Note that I/O monitoring is limited to a maximum 32 I/O signals (1 group = 8 signals) 
    */
    void monitorOutputGroups(1:ControllerID c, 2:i32 groupNum, 3:i32 count) throws (1:IllegalArgument e);

    /** Stop monitoring specified input */
    void unmonitorInput(1:ControllerID c, 2:i32 num);
    /** Stop monitoring all inputs in specified group */
    void unmonitorInputGroups(1:ControllerID c, 2:i32 groupNum, 3:i32 count);
    /** Stop monitoring specified output */
    void unmonitorOutput(1:ControllerID c, 2:i32 num);
    /** Stop monitoring all outputs in specified group */
    void unmonitorOutputGroups(1:ControllerID c, 2:i32 groupNum, 3:i32 count);

    /** Return value of given input */
    bool inputValue(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Return values of input groups from specified group number (upto 4 contiguous groups/bytes, from least significant byte) */
    i32 inputGroupsValue(1:ControllerID c, 2:i32 groupNum, 3:i32 count) throws (1:IllegalArgument e);

    /** Return the value of given output */
    bool outputValue(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Return values of output groups from specified group number (upto 4 contiguous groups/bytes) */
    i32 outputGroupsValue(1:ControllerID c, 2:i32 groupNum, 3:i32 count) throws (1:IllegalArgument e);

    /** Set the value of the specified output number
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void setOutput(1:ControllerID c, 2:i32 num, 3:bool value);
    /** Set the values of the outputs in the specified contigous output groups (upto 4 contiguous groups/bytes)
        Note it is asynchronous so no errors/exceptions are thrown.
    */
    oneway void setOutputGroups(1:ControllerID c, 2:i32 groupNum, 3:i32 count, 4:i32 value);

    /** Return the logical IO address of the named input */
    i32 inputAddress(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);
    /** Return the logical IO address of the given input number */
    i32 inputAddressByNumber(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);
    /** Return the logical IO address of the named output */
    i32 outputAddress(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);
    /** Return the logical IO address of the given output number */
    i32 outputAddressByNumber(1:ControllerID c, 2:i32 num) throws (1:IllegalArgument e);

    /** Start monitoring a logical IO address.  Will generate IOValueChanged events
        Note that I/O monitoring is limited to a maximum 32 I/O signals 
    */
    void monitorIOAddress(1:ControllerID c, 2:i32 address) throws (1:IllegalArgument e);
    /** Stop monitoring a logical IO address. (events for address may still be generated if it corresponds to a monitored input or output) */
    void unmonitorIOAddress(1:ControllerID c, 2:i32 address);

    /** Return the value of the given general input by logicial IO address */
    bool inputAddressValue(1:ControllerID c, 2:i32 address) throws (1:IllegalArgument e);
    /** Return the value of the given general output by logicial IO address */
    bool outputAddressValue(1:ControllerID c, 2:i32 address) throws (1:IllegalArgument e);
    /** Return the value of the given logicial IO address 
        (API version 3.0 and later)
    */
    bool ioAddressValue(1:ControllerID c, 2:i32 address) throws (1:IllegalArgument e);
    /** Set the value of the given output by logical IO address
    Note it is asynchronous so no errors/exceptions are thrown.*/
    oneway void setOutputAddress(1:ControllerID c, 2:i32 address, 3:bool value);

    // FieldBus Protocols

    /** Obtain input group number (byte) of field bus status input.  e.g. busType 'ethip' yields EtherNet/IP status byte group */
    i32 fieldBusStatusInputGroup(1:ControllerID c, 2:string busType) throws (1:IllegalArgument e);


    //
    // Control Groups & Robots

    /** Return the list of control groups configured on the controller.
        If only one robot is connected to the controller, this will return a single element,
        containing the simple control group representing the robot.
    */
    list<ControlGroup> controlGroups(1:ControllerID c);

    /** Returns the index of the currently active control group. */
    i8 currentControlGroup(1:ControllerID c);

    /** Returns the number of robots connected to the controller */
    i8 robotCount(1:ControllerID c);

    /** Returns the index of the currently active robot.
        Note: index is 0-based, but in the UI the first robot is Robot 1.
    */
    RobotIndex currentRobot(1:ControllerID c);


    //
    // Variables

    /** Variable value by name */
    Any variable(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** Variable value by address */
    Any variableByAddr(1:ControllerID c, 2:VariableAddress addr) throws (1:IllegalArgument e);

    /** Set variable value by name */
    void setVariable(1:ControllerID c, 2:string name, 3:Any value) throws (1:IllegalArgument e);

    /** Set variable by address */
    void setVariableByAddr(1:ControllerID c, 2:VariableAddress addr, 3:Any value) throws (1:IllegalArgument e);

    /** Lookup variable address by name and space */
    VariableAddress variableAddrByNameAndSpace(1:ControllerID c, 2:string name, 3:AddressSpace space) throws (1:IllegalArgument e);

    /** Lookup variable address by name (less efficient) */
    VariableAddress variableAddrByName(1:ControllerID c, 2:string name) throws (1:IllegalArgument e);

    /** Set name of variable by address */
    void setVariableName(1:ControllerID c, 2:VariableAddress addr, 3:string name) throws (1:IllegalArgument e);

    /** Start monitoring a variable.  Will generate VariableChanged events.
        Note that a maximum of 1 position variable, 4 string variables and 15 variables of each of the remain types can be monitored at the same time.
        (API version 3.0 and later)
    */
    void monitorVariable(1:ControllerID c, 2:VariableAddress addr) throws (1:IllegalArgument e);

    /** Stop monitoring a variable.
        (API version 3.0 and later)
    */
    void unmonitorVariable(1:ControllerID c, 2:VariableAddress addr) throws (1:IllegalArgument e);


    //
    // Zones

    /** Query information on specified zone, by index (not number) */
    Zone zone(1:ControllerID c, 2:ZoneIndex index) throws (1:IllegalArgument e);

    /** Creates a new Zone and returns its index.  It will have default values
        which can be change via modifyZone() */
    ZoneIndex newZone(1:ControllerID c) throws (1:IllegalArgument e);

    /** Modify Zone information.  Only fields set in Zone will be updated. */
    void modifyZone(1:ControllerID c, 2:ZoneIndex index, 3:Zone z) throws (1:IllegalArgument e);

    /** Delete a Zone */
    void deleteZone(1:ControllerID c, 2:ZoneIndex index) throws (1:IllegalArgument e);


    //
    // User Frames

    /** List of user frames mapping index -> name.
        NB: Indices (map keys) may not be sequential. Returned map may be empty. */
    map<UserFrameIndex, string> userFrames(1:ControllerID c)  throws (1:IllegalArgument e);

    /** Query information on specified User Frame, by index (not number) */
    CoordinateFrame userFrame(1:ControllerID c, 2:UserFrameIndex index) throws (1:IllegalArgument e);

    /** Creates a new User Frame with default values and returns its index. */
    UserFrameIndex newUserFrame(1:ControllerID c) throws (1:IllegalArgument e);

    /** Set the specified User Frame to the provided values 
        Future: Not implemented yet  */
    void setUserFrame(1:ControllerID c, 2:UserFrameIndex index, 3:CoordinateFrame f) throws (1:IllegalArgument e);

    /** Delete a User Frame */
    void deleteUserFrame(1:ControllerID c, 2:UserFrameIndex index) throws (1:IllegalArgument e);


    //
    // Networking

    /** Query current controller network interface IP address.
        controllerInterface must be one of ['LAN1','LAN'/'LAN2' or 'LAN3']
        (NB: On YRC1000micro, 'LAN' is the external Ethernet port, corresponding to 'LAN2' on the YRC1000)
    */
    string networkInterfaceAddress(1:ControllerID c, 2:string controllerInterface) throws (1:IllegalArgument e);

    /** Request external network access via specified protocol and port originating
        from the given controller interface. The controllerInferface may be left blank, in which case
        connections will be routed from the controller according to the destination address and
        current subnetwork of the external LAN ports).
        Access only persists while power is maintained to the controller.
        The protocol must be either 'tcp' or 'udp'. controllerInterface must be one of ['LAN1','LAN'/'LAN2' or 'LAN3'].
        Returns a handle that can subsequently used to remove the access, or -1 if the access request
        failed (may happen in case of network conflicts with other extensions).
        Requires 'networking' permision.
    */
    i32 requestNetworkAccess(1:ControllerID c,
                             2:string controllerInterface,
                             3:i32 port,
                             4:string protocol) throws (1:IllegalArgument e);
    void removeNetworkAccess(1:ControllerID c, 2:i32 accessHandle) throws (1:IllegalArgument e);
    i32 requestNetworkService(1:ControllerID c,
                             2:string controllerInterface,
                             3:i32 port,
                             4:string protocol) throws (1:IllegalArgument e);
    void removeNetworkService(1:ControllerID c, 2:i32 accessHandle) throws (1:IllegalArgument e);


}


/** Represents a single robot 

    Often there will only be one robot connected to a given controller
    but, for example, the YRC Controller is capable of supporting up-to 8 robots (or 72 axes).
*/
service Robot
{
    /** The model string of this robot */
    string model(1:RobotIndex r);

    /** Number of degrees-of-freedom / axes */
    i32 dof(1:RobotIndex r);

    /** Current position of the robot in joint coordinate frame (i.e. axis angles) */
    Position jointPosition(1:RobotIndex r, 2:OrientationUnit unit);

    /** Coordinates of the ToolTip (TCP) of of the specified tool
        in the given coordinate frame (using active tool if none specified) */
    Position toolTipPosition(1:RobotIndex r, 2:CoordinateFrame frame, 3:ToolIndex tool);


    /** Does this robot support force limiting? (collaborative robot?) */
    bool forceLimitingAvailable(1:RobotIndex r);

    /** Is force limiting currently active? (i.e. PFL - Power & Force Limiting) */
    bool forceLimitingActive(1:RobotIndex r);

    /** Is the robot stopped due to an over-limit event? */
    bool forceLimitingStopped(1:RobotIndex r);

    /** Is an end-of-arm switch box installed? */
    bool switchBoxAvailable(1:RobotIndex r);

    /** Index of the currently active tool */
    ToolIndex activeTool(1:RobotIndex r);

    /** Set the currently active tool */
    void setActiveTool(1:RobotIndex r, 2:ToolIndex tool);

    /** Set the work home position
        (API version 3.0 and later)
    */
    Position workHomePosition(1:RobotIndex r);

    /** Set the robots current work home position
        (API version 3.0 and later)
    */
    void setWorkHomePosition(1:RobotIndex r, 2:Position p);

    /** Set the second home position
        (API version 3.0 and later)
    */
    Position secondHomePosition(1:RobotIndex r);

    /** Set the robots current second home position
        (API version 3.0 and later)
    */
    void setSecondHomePosition(1:RobotIndex r, 2:Position p);

    /**Get a robot's maximum linear speed
        (API version 3.0 and later)
    */
    double maximumLinearSpeed(1:RobotIndex r);
}


