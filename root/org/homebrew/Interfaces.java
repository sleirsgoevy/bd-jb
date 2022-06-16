package org.homebrew;

public class Interfaces
{
    public static interface IFile
    {
        String[] list() throws java.rmi.RemoteException;
    }
    public static class CFile extends java.io.File implements IFile
    {
        public CFile(String path)
        {
            super(path);
        }
    }
    public static interface IProtocol
    {
        javax.microedition.io.Connection prim_openProtocol(String path, String unused, int mode) throws java.io.IOException, java.rmi.RemoteException;
        void prim_realOpen() throws java.io.IOException, java.rmi.RemoteException;
    }
    public static class CProtocol extends com.sun.cdc.io.j2me.file.Protocol implements IProtocol{}
    public static interface IService
    {
        Object newInstance(Object arg) throws java.security.NoSuchAlgorithmException, java.rmi.RemoteException;
    }
    public static class CService extends com.oracle.security.Service implements IService
    {
        public CService(java.security.Provider pr)
        {
            super(pr);
        }
    }
}
