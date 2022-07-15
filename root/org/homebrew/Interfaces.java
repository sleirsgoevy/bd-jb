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
    public static interface IFileURLConnection
    {
        java.io.InputStream getInputStream() throws java.io.IOException, java.rmi.RemoteException;
    }
    public static class CFileURLConnection extends sun.net.www.protocol.file.FileURLConnection implements IFileURLConnection
    {
        public CFileURLConnection(java.net.URL u, java.io.File f)
        {
            super(u, f);
        }
    }
}
