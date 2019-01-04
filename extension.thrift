namespace * yaskawa.ext.api
namespace csharp Yaskawa.Ext.API


typedef i64 ExtensionID
typedef i64 ControllerID
typedef i64 PendantID


exception InvalidID {
//    1: string message;
}

exception IllegalArgument {
    1:required string msg;
}


struct Version {
    1: i16 nmajor;
    2: i16 nminor;
    3: i16 npatch;
    4: optional string release;
    5: optional string build;
}

service Extension
{
    Version apiVersion();

    ExtensionID registerExtension(1:string canonicalName,
                                  2:string launchKey,
                                  3:Version version,
                                  4:string vendor,
                                  5:set<string> supportedLanguages)
                        throws (1:IllegalArgument e);
    void unregisterExtension(1:ExtensionID id) throws (1:InvalidID e);

    void ping(1:ExtensionID id) throws (1:InvalidID e);

    ControllerID controller(1:ExtensionID id) throws (1:InvalidID e);
    PendantID pendant(1:ExtensionID id) throws (1:InvalidID e);

}






//
// Pendant Service

enum PendantEventType {
    Startup = 0,
    Shutdown,
    SwitchedScreen,
    PopupOpened,
    PopupClosed,
    UtilityOpened,
    UtilityClosed,
    UtilityMoved,
    Clicked,
    Pressed,
    Released,
    TextEdited,

    Other = 16384
}

struct PendantEvent {
    1: required PendantEventType eventType;
    2: map<string,string> props;
}

enum UtilityWindowWidth {
    HalfWidth = 0,
    FullWidth
}

enum UtilityWindowHeight {
    QuarterHeight = 0,
    HalfHeight,
    FullHeight
}

enum UtilityWindowExpansion {
    expandableNone = 0,
    expandableWidth = 1,
    expandableHeight = 2,
    expandableBoth = 3
}

service Pendant
{
    void subscribeEventTypes(1:PendantID p, 2:set<PendantEventType> types);
    void unsubscribeEventTypes(1:PendantID p, 2:set<PendantEventType> types);

    list<PendantEvent> events(1:PendantID p);

    string currentLanguage(1:PendantID p);
    string currentLocale(1:PendantID p);

    string currentScreenName(1:PendantID p);

    /** Register an Item type described using a YML source code string
        Returns a list of parsing errors (0 on success)
    */
    list<string> registerYML(1:PendantID p, 2:string ymlSource);

    void registerUtilityWindow(1:PendantID p, 2:string identifier, 
                               3:bool intergated, 4:string itemType,
                               5:string menuItemName, 6:string windowTitle, 
                               7:UtilityWindowWidth widthFormat, 8:UtilityWindowHeight heightFormat,
                               9:UtilityWindowExpansion sizeExpandability)
                          throws (1:IllegalArgument e);

    /** get property of an item by id, with various types */
    bool   boolProperty(1:PendantID p, 2:string itemID, 3:string name)
                          throws (1:IllegalArgument e);
    i64    intProperty(1:PendantID p, 2:string itemID, 3:string name)
                          throws (1:IllegalArgument e);
    double realProperty(1:PendantID p, 2:string itemID, 3:string name)
                          throws (1:IllegalArgument e);
    string stringProperty(1:PendantID p, 2:string itemID, 3:string name)
                          throws (1:IllegalArgument e);

    /** Set property of an item by id, with various types (no overloading in Thrift) */
    void setBoolProperty(1:PendantID p, 2:string itemID, 3:string name, 4:bool value)
                        throws (1:IllegalArgument e);
    void setIntProperty(1:PendantID p, 2:string itemID, 3:string name, 4:i64 value)
                        throws (1:IllegalArgument e);
    void setRealProperty(1:PendantID p, 2:string itemID, 3:string name, 4:double value)
                        throws (1:IllegalArgument e);
    void setStringProperty(1:PendantID p, 2:string itemID, 3:string name, 4:string value)
                        throws (1:IllegalArgument e);
}




//
// Controller Service

typedef i32 RobotIndex
typedef i32 ToolIndex


enum ControllerEventType {
    Connected = 0,
    RobotModel, // TODO: move to Robot?
    ExclusiveControl,
    CycleTime,
    PowerOnTime,
    ServoOnTime,
    EnabledOptionsChanged,
    OperationMode,
    ServoState,
    PlaybackState,
    SpeedOverride,
    Held,
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
    RobotTCPPosition, // TODO: move to robot
    BrakeRelease, // TODO: robot?
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
    SafetyRobotRangeLimitDataChanged, // Robot?
    SafetyAxisSpeedMonitorDataChanged, // Robot?
    SafetySpeedLimitDataChanged, // Robot?
    SafetyExternalForceMonitorFileChanged, // Robot? Name?
    SafetyIOListChanged,

    VariablesChanged, // allow sub by variable list, like watch?
    VariableNamesChanged,
    IONamesChanged,
}

struct ControllerEvent {
    1: required ControllerEventType eventType;
    2: optional map<string,string> props;
}


service Controller
{
    void connect(1:ControllerID c, 2:string hostName);
    void disconnect(1:ControllerID c);

    void subscribeEventTypes(1:ControllerID c, 2:set<ControllerEventType> types);
    void unsubscribeEventTypes(1:ControllerID c, 2:set<ControllerEventType> types);

    list<ControllerEvent> events(1:ControllerID c);

    bool connected(1:ControllerID c);
    string connectedHostName(1:ControllerID c);

    string softwareVersion(1:ControllerID c);

    bool monitoring(1:ControllerID c);
    //void setMonitoring(1:ControllerID c, 2:bool monitor);

    //bool acquireExclusiveControl(1:ControllerID c);
    //void releaseExclusiveControl(1:ControllerID c);
    bool haveExclusiveControl(1:ControllerID c);
    //string noExclusiveControlReason(1:ControllerID c);

    i8 robotCount(1:ControllerID c);
    RobotIndex currentRobot(1:ControllerID c);
}

service Robot
{
    string model(1:RobotIndex r);
    i32 dof(1:RobotIndex r);

    bool forceLimitingAvailable(1:RobotIndex r);
    bool forceLimitingActive(1:RobotIndex r);
    bool forceLimitingStopped(1:RobotIndex r);
    bool switchBoxAvailable(1:RobotIndex r);

    ToolIndex activeTool(1:RobotIndex r);
    void setActiveTool(1:RobotIndex r, 2:ToolIndex tool);

}


service Tool
{
    double weight(1:ToolIndex t)
}


//service Job
//{
//}

//service InformJob extends Job
//{
//    i32 lineCount(),
//
//}
