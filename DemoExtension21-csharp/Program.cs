using System;
using System.Collections.Generic;
using System.Data.SqlTypes;
using System.IO;
using Yaskawa.Ext.API;

namespace DemoExtension21_csharp
{
    class demoExtension21_csharp
    {
        private demoExtension21_csharp() 
        {
            var _version = new Yaskawa.Ext.Version(1,0,0);
            var _languages = new HashSet<string> { "en", "ja" } ;

            extension = new Yaskawa.Ext.Extension("mylaunchkey",
                "yeu.test-extension", 
                _version, "YEU", _languages);
            Console.WriteLine("API version: "+extension.apiVersion());

            pendant = extension.pendant();
            controller = extension.controller();
            Console.WriteLine("Controller software version:"+controller.softwareVersion());


        }
        protected Yaskawa.Ext.Extension extension;
        protected Yaskawa.Ext.Pendant pendant;
        protected Pendant Pendant;
        protected Yaskawa.Ext.Controller controller;
        protected bool run = true;
        protected bool update = false;
        protected double time;
        public void setup()
        {
            
        }
        public void Run()
        {
            
        }

        public void updateChart()
        {
            if (update.Equals(true))
            {
                update = !update;
                try
                {
                    DataPoint pt = new DataPoint();
                    pt.X = time;
                    pt.Y = Math.Sin(time);
                    Pendant.ISync.appendChartPoints("exampleline", "series 3", pt, true);
                    pendant.incrementChartKey("exampleBar", "Darker", 1.0);
                }
                catch (Exception _e)
                {
                    Console.WriteLine("appencChartPoint: " + _e);
                }
            }
        }
        static void Main(string[] args)
        {
            demoExtension21_csharp _demoExtension = null;
            try {
                // launch
                try {
                    _demoExtension = new demoExtension21_csharp();
                } catch (Exception _e) {
                    Console.WriteLine("Extension failed to start, aborting: "+ _e);
                    return;
                }

                try {
                    _demoExtension.setup();
                } catch (Exception _e) {
                    Console.WriteLine("Extension failed in setup, aborting: "+_e);
                    return;
                }

                // run 'forever' (or until API service shutsdown)
                try {
                    _demoExtension.updateChart();
                } catch (Exception _e) {
                    Console.WriteLine("Exception occured:"+ _e);
                }

            } catch (Exception _e) {

                Console.WriteLine("Exception: "+ _e);

            } finally {
                if (_demoExtension != null)
                    _demoExtension.close();
            }
        }
    }
}
