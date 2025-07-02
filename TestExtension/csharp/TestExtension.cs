using System;
using System.Collections.Generic;
using System.IO;
using Thrift.Collections;
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

            extension = new Yaskawa.Ext.Extension("com.yaskawa.yeu.testextension.ext",
                version, "YEU", languages, "", -1);
            Console.WriteLine("API version: "+extension.apiVersion());

            pendant = extension.pendant();
            controller = extension.controller();
            Console.WriteLine("Controller software version:"+controller.softwareVersion());
        }

        private void setup()
        {
            extension.subscribeLoggingEvents();
            Console.WriteLine(" monitoring? "+controller.monitoring());   // only monitoring or able to change functions?     
            Console.WriteLine("Current language:"+pendant.currentLanguage()); // pendant language ISO 693-1 code
            Console.WriteLine("Current locale:"+pendant.currentLocale());
            Console.WriteLine("Screen Name:"+pendant.currentScreenName());
            pendant.registerYMLFile("Frontend.yml");
            pendant.registerUtilityWindow("ymlutil","Frontend","YML Extension", "YML Extension");

            controller.subscribeEventTypes(new THashSet<ControllerEventType> { 
                ControllerEventType.OperationMode, 
                ControllerEventType.ServoState,
                ControllerEventType.ActiveTool,
                ControllerEventType.PlaybackState,
                ControllerEventType.RemoteMode
            });

            pendant.subscribeEventTypes(new THashSet<PendantEventType> { 
                //PendantEventType.Startup,
                //PendantEventType.Shutdown,
                PendantEventType.SwitchedScreen,
                PendantEventType.UtilityOpened,
                PendantEventType.UtilityClosed,
                PendantEventType.UtilityMoved,
                PendantEventType.Clicked
            } );

            pendant.addItemEventConsumer("MYBUTTON", PendantEventType.Clicked, onButtonClicked);
            extension.ping();
        }

        void onButtonClicked(PendantEvent e)
        {
            try {
                var props = e.Props;
                if (props.ContainsKey("item")) {

                    var itemName = props["item"].SValue;

                    if (itemName.Equals("MYBUTTON")) {
                        Any a = new Any();
                        a.SValue = "Button clicked " + (++this._clickCount).ToString() + " times.";
                        pendant.setProperty("mytext", "text", a);
                        Console.WriteLine("p.prop: " + pendant.property("mytext", "text"));
                    }
                }

            } catch (Exception ex) {
                // display error
                Console.WriteLine("Unable to process Clicked event :"+ ex);
            }
        }

        static void Main()  
        {  
            var testExtension = new TestExtension();
            testExtension.setup();
            testExtension.extension.run(() => false);
        }

        protected Yaskawa.Ext.Extension extension;
        protected Yaskawa.Ext.Pendant pendant;
        protected Yaskawa.Ext.Controller controller;

        private int _clickCount = 0;
    }
}  