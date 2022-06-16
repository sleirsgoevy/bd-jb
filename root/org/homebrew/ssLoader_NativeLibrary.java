package org.homebrew;

//this class exists only to fool javac. its never loaded at runtime
class ssLoader_NativeLibrary
{
    public ssLoader_NativeLibrary(Class cls, String s, boolean q){}
    void load(String name){}
    long find(String name)
    {
        return 0;
    }
}
