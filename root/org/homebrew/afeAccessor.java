package org.homebrew;

public class afeAccessor extends jdk.internal.reflect.MagicAccessorImpl implements javax.media.protocol.Seekable
{
    public long seek(long addr)
    {
        return jdk.internal.misc.Unsafe.theUnsafe.getLong(addr);
    }
    public long tell()
    {
        return 0;
    }
    public boolean isRandomAccess()
    {
        return false;
    }
}
