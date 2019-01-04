using System;
using System.IO;
using System.Collections.Generic;

using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;

//using ControllerEvent = Yaskawa.Ext.API.ControllerEvent;
//using ControllerEventType = Yaskawa.Ext.API.ControllerEventType;
//using PendantEvent = Yaskawa.Ext.API.PendantEvent;
//using PendantEventType = Yaskawa.Ext.API.PendantEventType;
//using UtilityWindowWidth = Yaskawa.Ext.API.UtilityWindowWidth;
//using UtilityWindowHeight = Yaskawa.Ext.API.UtilityWindowHeight;
//using UtilityWindowExpansion = Yaskawa.Ext.API.UtilityWindowExpansion;


using Yaskawa.Ext.API;
using Yaskawa.Ext;

class TestExtension
{  

    public TestExtension()
    {
        var version = new Yaskawa.Ext.Version(1,0,0);
        var languages = new HashSet<string> { "en", "ja" } ;

        extension = new Yaskawa.Ext.Extension("mylaunchkey",
                                              "yii.test-extension", 
                                              version, "YII", languages);
        Console.WriteLine("API version: "+extension.apiVersion());

        pendant = extension.pendant();
        controller = extension.controller();
        Console.WriteLine("Controller software version:"+controller.softwareVersion());
    }

    protected Yaskawa.Ext.Extension extension;
    protected Yaskawa.Ext.Pendant pendant;
    protected Yaskawa.Ext.Controller controller;

    //bool quit;
    Timer eventPollTimer;

    Utility utility;


    public void Run()
    {
        //Console.WriteLine(" monitoring? "+controller.monitoring());        
        //Console.WriteLine("Current language:"+pendant.currentLanguage());
        Console.WriteLine("Current locale:"+pendant.currentLocale());

        Console.WriteLine("Screen Name:"+pendant.currentScreenName());

        string yml = File.ReadAllText("../test.yml");
        var errors = pendant.registerYML(yml);
        if (errors.Count > 0) {
            Console.WriteLine("YML Errors encountered:");
            foreach(var e in errors)
                Console.WriteLine("  "+e);
        }

        // pendant.registerUtilityWindow("testutil",false,"",
        //                               "Test Extension", "Test Extension",
        //                               UtilityWindowWidth.FullWidth, UtilityWindowHeight.HalfHeight,
        //                               UtilityWindowExpansion.expandableNone);


        pendant.registerUtilityWindow("ymlutil",true,"MyUtility",
                                      "YML Extension", "YML Extension",
                                      UtilityWindowWidth.FullWidth, UtilityWindowHeight.HalfHeight,
                                      UtilityWindowExpansion.expandableNone);

        controller.subscribeEventTypes(new HashSet<ControllerEventType> { 
            ControllerEventType.OperationMode, 
            ControllerEventType.ServoState,
            ControllerEventType.ActiveTool,
            ControllerEventType.PlaybackState,
            ControllerEventType.RemoteMode
            });

        pendant.subscribeEventTypes(new HashSet<PendantEventType> { 
            //PendantEventType.Startup,
            //PendantEventType.Shutdown,
            PendantEventType.SwitchedScreen,
            PendantEventType.UtilityOpened,
            PendantEventType.UtilityClosed,
            PendantEventType.UtilityMoved,
            PendantEventType.Clicked
            } );


        extension.ping();

        Application.EnableVisualStyles();
        utility = new Utility();

        eventPollTimer = new Timer();
        eventPollTimer.Interval = 300;
        eventPollTimer.Tick += new EventHandler(PollForEvents);
        eventPollTimer.Start();

        //quit = false;
        //do {
            //Application.DoEvents();
            //System.Threading.Thread.Sleep(50);
        //} while (!quit);
        Application.Run();

        extension.Dispose();
    }

    private int ClickCount = 0;

    private void PollForEvents(Object o, EventArgs args)
    {    
        foreach (ControllerEvent e in controller.events()) {
            Console.Write("ControllerEvent:"+e.EventType);
            foreach(var p in e.Props) 
                Console.Write("   "+p.Key+":"+p.Value);
            Console.WriteLine();
        }
        foreach (PendantEvent e in pendant.events()) {
            Console.WriteLine("PendantEvent:"+e.EventType);
            foreach(var p in e.Props) 
                Console.WriteLine("  "+p.Key+": "+p.Value);
            switch (e.EventType) {
                case PendantEventType.Clicked: {
                    if (e.Props["item"] == "mybutton")
                        pendant.setProperty("mytext", "text", "Button clicked "+ (++this.ClickCount).ToString()+" times.");
                } break;
                case PendantEventType.UtilityOpened: {
                    int x = int.Parse(e.Props["x"]);
                    int y = int.Parse(e.Props["y"]);
                    int width = int.Parse(e.Props["width"]);
                    int height = int.Parse(e.Props["height"]);

                    // only testutil is native; ymlutil is integrated
                    if (e.Props["identifier"] == "testutil") {
                        utility.Size = new Size(width,height);
                        utility.SetDesktopLocation(x,y);
                        //utility.WindowState = FormWindowState.Normal;
                        utility.Show();
                    }
                } break;
                case PendantEventType.UtilityClosed: {
                    //utility.WindowState = FormWindowState.Minimized;
                    if (e.Props["identifier"] == "testutil") {
                        utility.Hide();
                    }
                } break;
                case PendantEventType.Shutdown: {
                    //quit = true;
                    Application.Exit();
                } break;
            }
        }
    }


    static void Main()  
    {  
        var testExtension = new TestExtension();
        testExtension.Run();
    }  

}  
