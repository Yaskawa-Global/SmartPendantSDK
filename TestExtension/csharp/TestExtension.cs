using System;
using System.Collections.Generic;
using System.IO;
using Yaskawa.Ext.API;
//using System.Windows.Forms;

namespace TestExtension
{
    class TestExtension
    {
        private TestExtension()
        {
            var version = new Yaskawa.Ext.Version(1,0,0);
            var languages = new HashSet<string> { "en", "ja" } ;

            extension = new Yaskawa.Ext.Extension("mylaunchkey",
                "yeu.test-extension", 
                version, "YEU", languages);
            Console.WriteLine("API version: "+extension.apiVersion());

            pendant = extension.pendant();
            controller = extension.controller();
            Console.WriteLine("Controller software version:"+controller.softwareVersion());
        }

        protected Yaskawa.Ext.Extension extension;
        protected Yaskawa.Ext.Pendant pendant;
        protected Yaskawa.Ext.Controller controller;
        private bool _quit;
        protected System.Timers.Timer eventPollTimer;

        public void Run()
        {
            Console.WriteLine(" monitoring? "+controller.monitoring());   // only monitoring or able to change functions?     
            Console.WriteLine("Current language:"+pendant.currentLanguage()); // pendant language ISO 693-1 code
            Console.WriteLine("Current locale:"+pendant.currentLocale());
            Console.WriteLine("Screen Name:"+pendant.currentScreenName());
            string yml = File.ReadAllText("Frontend.yml");
            var errors = pendant.registerYML(yml);
            if (errors.Count > 0) {
                Console.WriteLine("YML Errors encountered:");
                foreach(var e in errors)
                    Console.WriteLine("  "+e);
            }
            pendant.registerutilitywindow("ymlutil","Frontend","YML Extension", "YML Extension");

            controller.subscribeEventTypes(new HashSet<ControllerEventType> { 
                ControllerEventType.OperationMode, 
                ControllerEventType.ServoState,
                ControllerEventType.ActiveTool,
                ControllerEventType.PlaybackState,
                ControllerEventType.RemoteMode
            });

            pendant.subscribeEventTypes(new HashSet<PendantEventType> { 
                PendantEventType.Startup,
                PendantEventType.Shutdown,
                PendantEventType.SwitchedScreen,
                PendantEventType.UtilityOpened,
                PendantEventType.UtilityClosed,
                PendantEventType.UtilityMoved,
                PendantEventType.Clicked
            } );


            extension.ping();

            //Application.EnableVisualStyles();
            //utility = new Utility();

        
            //eventPollTimer.AutoReset = true;
            //eventPollTimer.Tick += new EventHandler(PollForEvents);

            _quit = false;
            eventPollTimer = new System.Timers.Timer(500);

            do {
                //Application.DoEvents();
            
                eventPollTimer.Elapsed += new System.Timers.ElapsedEventHandler(PollForEvents);
                eventPollTimer.Enabled = true;
                System.Threading.Thread.Sleep(50);
                //Console.WriteLine(quit);
            } while (!_quit);
            eventPollTimer.Enabled = false;
            extension.Dispose();
        }

        private int _clickCount = 0;

        private void PollForEvents(Object o, EventArgs args)
        {
            Any a = new Any();
            foreach (ControllerEvent e in controller.events()) {
                Console.Write("ControllerEvent: "+e.EventType);
                foreach(var p in e.Props) 
                    Console.Write("   "+p.Key+":"+p.Value);
                Console.WriteLine();
            }
            foreach (PendantEvent e in pendant.events()) 
            {
                Console.WriteLine("PendantEvent: "+e.EventType);
                //Console.WriteLine(e.Props);
                foreach (var p in e.Props)
                {
                    Console.WriteLine("  " + p.Key + ": " + p.Value);
                }
                switch (e.EventType)
                {
                    case PendantEventType.Clicked:
                    {
                        if (String.Equals(e.Props["item"].SValue, "MYBUTTON"))
                        {

                            a.SValue = "Button clicked " + (++this._clickCount).ToString() + " times.";
                            pendant.setProperty("mytext", "text", a);
                            Console.WriteLine("p.prop: " + pendant.property("mytext", "text"));
                        }
                    } break;
                    case PendantEventType.Shutdown: {
                        _quit = true;
                        Console.WriteLine(_quit);
                    } break;
                    case PendantEventType.Startup:
                    {
                        Console.Write("Pendant started");
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
}  