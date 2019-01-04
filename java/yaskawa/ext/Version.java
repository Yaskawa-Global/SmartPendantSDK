package yaskawa.ext;

import yaskawa.ext.api.*;


public class Version extends yaskawa.ext.api.Version 
{
    public Version(int major, int minor, int patch, String release, String build)
    {
        setNmajor((short)major);
        setNminor((short)minor);
        setNpatch((short)patch);
        if (!release.isEmpty())
            setRelease(release);
        if (!build.isEmpty())
            setBuild(build);
    }

    public Version(int major, int minor, int patch)
    {
        this(major, minor, patch, "", "");
    }


    public Version(yaskawa.ext.api.Version v) 
    {
        super(v);
    }

    public String toString()
    {
        return Integer.toString(nmajor)+"."+Integer.toString(nminor)+"."+Integer.toString(npatch)
                + (isSetRelease() ? "-"+release : "")
                + (isSetBuild() ? "+"+build : "");
    } 

}

