package org.homebrew;

import java.util.Map;

//public class java.lang.NativeLibraryAccessor extends java.lang.ClassLoader.NativeLibrary
public class iveLibraryAccessor extends ssLoader_NativeLibrary
{
    private Map symbols;
    public iveLibraryAccessor(Map arg)
    {
        super(java.lang.Object.class, "", false);
        symbols = arg;
    }
    long find(String name)
    {
        Object q = symbols.get(name);
        if(q != null)
            return ((Long)q).longValue();
        return super.find(name);
    }
}
