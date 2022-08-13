package sun.net.www.protocol.file;

public class FileURLConnection extends java.net.URLConnection
{
    public FileURLConnection(java.net.URL u, java.io.File f)
    {
        super(u);
    }
    public void connect(){}
}
