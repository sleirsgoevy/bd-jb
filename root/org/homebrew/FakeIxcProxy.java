package org.homebrew;

public class FakeIxcProxy extends com.sony.gemstack.org.dvb.io.ixc.IxcProxy
{
    private Object theRemote;
    public void forgetRemote(){}
    public Object getRemote()
    {
        return theRemote;
    }
    public FakeIxcProxy(Object tr)
    {
        super(com.sony.gemstack.core.CoreAppContext.getContext().getIxcClassLoader(), com.sony.gemstack.core.CoreAppContext.getContext().getIxcClassLoader());
        theRemote = tr;
    }
    public Object pInvokeMethod(Object[] paramArrayOfObject, String paramString1, String paramString2) throws Exception {
        return invokeMethod(paramArrayOfObject, paramString1, paramString2);
    }
    public Object replaceObject(Object o, com.sony.gemstack.core.CoreIxcClassLoader ccl)
    {
        return o;
    }
}
