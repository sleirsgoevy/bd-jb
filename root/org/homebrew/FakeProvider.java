package org.homebrew;

import java.security.Provider;
import com.oracle.ProviderAdapter;
import com.oracle.ProviderAccessor;
import com.oracle.security.Service;
import java.lang.reflect.*;

public class FakeProvider extends Provider
{
    private Service svc;
    private static Provider p_sun;
    private static Provider p_sun_rsa;
    private static Service s_sha1;
    private static Service s_x_509;
    private static Service s_x509;
    private static Service s_sha_rsa;
    private static Service s_sha;
    private static Service s_sha_1;
    public void installFakeAccessor()
    {
        try
        {
            p_sun = java.security.Security.getProvider("SUN");
            p_sun_rsa = java.security.Security.getProvider("SunRsaSign");
            s_sha1 = ProviderAdapter.getService(p_sun, "MessageDigest", "SHA1");
            s_x_509 = ProviderAdapter.getService(p_sun, "CertificateFactory", "X.509");
            s_x509 = ProviderAdapter.getService(p_sun, "CertificateFactory", "X509");
            s_sha_rsa = ProviderAdapter.getService(p_sun_rsa, "Signature", "SHA1withRSA");
            s_sha = ProviderAdapter.getService(p_sun, "MessageDigest", "SHA");
            s_sha_1 = ProviderAdapter.getService(p_sun, "MessageDigest", "SHA-1");
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.toString());
        }
        ProviderAdapter.setProviderAccessor(new ProviderAccessor()
        {
            public void putService(Provider pr, Object o)
            {
                throw new RuntimeException("putService");
            }
            public java.util.Set getServices(Provider pr)
            {
                throw new RuntimeException("getServices");
            }
            public Service getService(Provider pr, String s1, String s2)
            {
                if(s1.equals("fakeType") && s2.equals("fakeAlgorithm"))
                    return svc;
                if(/*pr == p_sun && */s1.equals("MessageDigest") && s2.equals("SHA1"))
                    return s_sha1;
                if(pr == p_sun && s1.equals("CertificateFactory") && s2.equals("X.509"))
                    return s_x_509;
                if(pr == p_sun && s1.equals("CertificateFactory") && s2.equals("X509"))
                    return s_x509;
                if(pr == p_sun && s1.equals("Signature") && s2.equals("SHA1withRSA"))
                    return null;
                if(pr == p_sun_rsa && s1.equals("Signature") && s2.equals("SHA1withRSA"))
                    return s_sha_rsa;
                if(pr == p_sun && s1.equals("MessageDigest") && s2.equals("SHA"))
                    return s_sha;
                if(pr == p_sun && s1.equals("MessageDigest") && s2.equals("SHA-1"))
                    return s_sha_1;
                throw new RuntimeException("should not get here");
            }
        });
    }
    public void installRealAccessor() throws Exception
    {
        Class cls = Class.forName("java.security.Provider$1");
        Constructor c = cls.getDeclaredConstructors()[0];
        c.setAccessible(true);
        ProviderAdapter.setProviderAccessor((ProviderAccessor)c.newInstance(new Object[0]));
    }
    public FakeProvider(String a, double b, String c)
    {
        super(a, b, c);
    }
    public void setService(Service s)
    {
        svc = s;
    }
}
