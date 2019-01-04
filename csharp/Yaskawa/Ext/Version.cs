using System;

using Yaskawa.Ext;


namespace Yaskawa.Ext
{

    public class Version : API.Version 
    {
        public Version(int major, int minor, int patch, string release="", string build="")
        {
            Nmajor = (short)major;
            Nminor = (short)minor;
            Npatch = (short)patch;
            if (!string.IsNullOrEmpty(release))
                Release = release;
            if (!string.IsNullOrEmpty(build))
                Build = build;
        }

        public Version(API.Version v) 
        {
            Nmajor = v.Nmajor;
            Nminor = v.Nminor;
            Npatch = v.Npatch;
            if (!string.IsNullOrEmpty(v.Release))
                Release = v.Release;
            if (!string.IsNullOrEmpty(v.Build))
                Build = v.Build;
        }

        public override string ToString()
        {
            return Nmajor.ToString()+"."+Nminor.ToString()+"."+Npatch.ToString()
                   + (__isset.release ? "-"+Release : "")
                   + (__isset.build ? "+"+Build : "");
        } 

    }

}
