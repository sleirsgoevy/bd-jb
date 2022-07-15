package org.homebrew;

import java.net.URL;
import java.nio.ByteBuffer;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.util.Set;
import jdk.internal.security.CodeSigner;
import jdk.internal.security.Entry;
import jdk.internal.security.ProtectionParameter;
import jdk.internal.security.Service;
import jdk.internal.security.keystore.Builder;

public class JavaSecurityProxy implements jdk.internal.access.JavaSecurityAccess
{
    jdk.internal.access.JavaSecurityAccess real;
    public JavaSecurityProxy(jdk.internal.access.JavaSecurityAccess r)
    {
        real = r;
    }
    public Object doIntersectionPrivilege(PrivilegedAction a, AccessControlContext b, AccessControlContext c)
    {
        return real.doIntersectionPrivilege(a, b, c);
    }
    public Object doIntersectionPrivilege(PrivilegedAction a, AccessControlContext b)
    {
        return real.doIntersectionPrivilege(a, b);
    }
    public ProtectionDomainCache getProtectionDomainCache()
    {
        return real.getProtectionDomainCache();
    }
    public Object doPrivilegedWithCombiner(PrivilegedExceptionAction a, AccessControlContext b, Permission[] c) throws PrivilegedActionException
    {
        return real.doPrivilegedWithCombiner(a, b, c);
    }
    public Object doPrivileged(PrivilegedAction a, AccessControlContext b, Permission[] c)
    {
        return real.doPrivileged(a, b, c);
    }
    public Entry getEntry(KeyStore a, String b, ProtectionParameter c) throws NoSuchAlgorithmException, GeneralSecurityException
    {
        return real.getEntry(a, b, c);
    }
    public Service getService(Provider a, String b, String c)
    {
        return real.getService(a, b, c);
    }
    public void putService(Provider a, Service b)
    {
        real.putService(a, b);
    }
    public Set getServices(Provider a)
    {
        return real.getServices(a);
    }
    public Provider configure(Provider a, String b)
    {
        return real.configure(a, b);
    }
    public Object newInstance(Class a, String b, Object c) throws Exception
    {
        return real.newInstance(a, b, c);
    }
    public boolean checkEngine(String a)
    {
        return real.checkEngine(a);
    }
    public String getEngineName(String a)
    {
        return real.getEngineName(a);
    }
    public CodeSource newCodeSource(URL a, CodeSigner[] b)
    {
        try
        {
            a = new URL("file:///app0/cdc/lib/ext/../../../../VP/stuff.jar");
        }
        catch(Exception e){}
        return real.newCodeSource(a, b);
    }
    public void update(MessageDigest a, ByteBuffer b)
    {
        real.update(a, b);
    }
    public Builder newKeyStoreBuilder(KeyStore a, ProtectionParameter b)
    {
        return real.newKeyStoreBuilder(a, b);
    }
}
