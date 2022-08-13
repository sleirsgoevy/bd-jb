package org.homebrew;

import java.lang.reflect.*;

public class NativeUtils
{
    private static Class NativeLibrary;
    private static Constructor newNativeLibrary;
    private static Method load;
    private static Method find;
    private static Field handle;
    private static Object theUnsafe;
    private static Method unsafeDefineClass;
    private static Method unsafeAllocateMemory;
    private static Method unsafeFreeMemory;
    private static Method unsafeGetByte;
    private static Method unsafeGetInt;
    private static Method unsafeGetLong;
    private static Method unsafePutByte;
    private static Method unsafePutInt;
    private static Method unsafePutLong;
    private static Constructor newNativeLibraryAccessor;
    private static Field nativeLibraries;
    private static java.util.HashMap symbols;
    private static javax.media.protocol.Seekable fastLongAccessor;
    private static void replaceBytesString(byte[] buf, String haystack, String needle)
    {
        for(int i = 0; i + haystack.length() <= buf.length; i++)
        {
            boolean ok = true;
            for(int j = 0; ok && j < haystack.length(); j++)
                if(buf[i+j] != (byte)haystack.charAt(j))
                    ok = false;
            if(ok)
                for(int j = 0; j < needle.length(); j++)
                    buf[i+j] = (byte)needle.charAt(j);
        }
    }
    public static byte[] readResource(String name) throws java.io.IOException
    {
        return readResource(NativeUtils.class.getResourceAsStream(name));
    }
    public static byte[] readResource(java.io.InputStream is) throws java.io.IOException
    {
        byte[] arr = new byte[0];
        int len = 0;
        int pos = 0;
        while(true)
        {
            if(pos == len)
            {
                len *= 2;
                if(len == 0)
                    len = 1;
                byte[] arr2 = new byte[len];
                for(int i = 0; i < pos; i++)
                    arr2[i] = arr[i];
                arr = arr2;
            }
            int chk = is.read(arr, pos, len-pos);
            if(chk < 0)
                break;
            pos += chk;
        }
        byte[] data = new byte[pos];
        for(int i = 0; i < pos; i++)
            data[i] = arr[i];
        return data;
    }
    private static void init() throws Exception
    {
        if(NativeLibrary == null)
            NativeLibrary = Class.forName("java.lang.ClassLoader$NativeLibrary");
        if(newNativeLibrary == null)
        {
            newNativeLibrary = NativeLibrary.getDeclaredConstructors()[0];
            newNativeLibrary.setAccessible(true);
        }
        if(load == null)
        {
            load = NativeLibrary.getDeclaredMethod("load0", new Class[]{String.class, Boolean.TYPE});
            load.setAccessible(true);
        }
        if(find == null)
        {
            find = NativeLibrary.getDeclaredMethod("findEntry", new Class[]{String.class});
            find.setAccessible(true);
        }
        if(handle == null)
        {
            handle = NativeLibrary.getDeclaredField("handle");
            handle.setAccessible(true);
        }
        if(theUnsafe == null)
        {
            Field f = Class.forName("jdk.internal.misc.Unsafe").getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = f.get(null);
        }
        if(unsafeDefineClass == null)
            unsafeDefineClass = theUnsafe.getClass().getDeclaredMethod("defineClass", new Class[]{String.class, (new byte[0]).getClass(), Integer.TYPE, Integer.TYPE, ClassLoader.class, java.security.ProtectionDomain.class});
        if(unsafeAllocateMemory == null)
            unsafeAllocateMemory = theUnsafe.getClass().getDeclaredMethod("allocateMemory", new Class[]{Long.TYPE});
        if(unsafeFreeMemory == null)
            unsafeFreeMemory = theUnsafe.getClass().getDeclaredMethod("freeMemory", new Class[]{Long.TYPE});
        if(unsafeGetByte == null)
            unsafeGetByte = theUnsafe.getClass().getDeclaredMethod("getByte", new Class[]{Long.TYPE});
        if(unsafeGetInt == null)
            unsafeGetInt = theUnsafe.getClass().getDeclaredMethod("getInt", new Class[]{Long.TYPE});
        if(unsafeGetLong == null)
            unsafeGetLong = theUnsafe.getClass().getDeclaredMethod("getLong", new Class[]{Long.TYPE});
        if(unsafePutByte == null)
            unsafePutByte = theUnsafe.getClass().getDeclaredMethod("putByte", new Class[]{Long.TYPE, Byte.TYPE});
        if(unsafePutInt == null)
            unsafePutInt = theUnsafe.getClass().getDeclaredMethod("putInt", new Class[]{Long.TYPE, Integer.TYPE});
        if(unsafePutLong == null)
            unsafePutLong = theUnsafe.getClass().getDeclaredMethod("putLong", new Class[]{Long.TYPE, Long.TYPE});
        if(nativeLibraries == null)
        {
            nativeLibraries = java.lang.ClassLoader.class.getDeclaredField("nativeLibraries");
            nativeLibraries.setAccessible(true);
        }
        if(newNativeLibraryAccessor == null)
        {
            byte[] data = readResource("/org/homebrew/iveLibraryAccessor.class");
            replaceBytesString(data,
                "org/homebrew/ssLoader_NativeLibrary",
                "java/lang/ClassLoader$NativeLibrary"
            );
            replaceBytesString(data,
                "org/homebrew/iveLibraryAccessor",
                "java/lang/NativeLibraryAccessor"
            );
            Class cls = (Class)unsafeDefineClass.invoke(theUnsafe, new Object[]{null, data, new Integer(0), new Integer(data.length), null, null});
            newNativeLibraryAccessor = cls.getDeclaredConstructors()[0];
            newNativeLibraryAccessor.setAccessible(true);
        }
        if(fastLongAccessor == null)
        {
            byte[] data = readResource("/org/homebrew/afeAccessor.class");
            replaceBytesString(data,
                "org/homebrew/afeAccessor",
                "java/lang/UnsafeAccessor"
            );
            Class cls = (Class)unsafeDefineClass.invoke(theUnsafe, new Object[]{null, data, new Integer(0), new Integer(data.length), null, null});
            fastLongAccessor = (javax.media.protocol.Seekable)cls.newInstance();
        }
    }
    public static long dlopen(String name) throws Exception
    {
        init();
        Object lib = newNativeLibrary.newInstance(new Object[]{null, null, new Boolean(false)});
        load.invoke(lib, new Object[]{name, new Boolean(false)});
        return ((Long)handle.get(lib)).longValue();
    }
    public static long dlsym(long ptr, String name) throws Exception
    {
        init();
        Object lib = newNativeLibrary.newInstance(new Object[]{null, null, new Boolean(false)});
        handle.set(lib, new Long(ptr));
        return ((Long)find.invoke(lib, new Object[]{name})).longValue();
    }
    public static void addSymbol(String name, long value) throws Exception
    {
        init();
        synchronized(newNativeLibraryAccessor)
        {
            if(symbols == null)
            {
                symbols = new java.util.HashMap();
                Object obj = newNativeLibraryAccessor.newInstance(new Object[]{symbols});
                java.util.Map vec = (java.util.Map)nativeLibraries.get(NativeUtils.class.getClassLoader());
                if(vec == null)
                {
                    vec = new java.util.HashMap();
                    nativeLibraries.set(NativeUtils.class.getClassLoader(), vec);
                }
                vec.put("FakeLibrary", obj);
            }
            symbols.put(name, new Long(value));
        }
    }
    public static long allocateMemory(long sz) throws Exception
    {
        init();
        return ((Long)unsafeAllocateMemory.invoke(theUnsafe, new Object[]{new Long(sz)})).longValue();
    }
    public static void freeMemory(long addr) throws Exception
    {
        init();
        unsafeFreeMemory.invoke(theUnsafe, new Object[]{new Long(addr)});
    }
    public static byte getByte(long addr) throws Exception
    {
        init();
        return ((Byte)unsafeGetByte.invoke(theUnsafe, new Object[]{new Long(addr)})).byteValue();
    }
    public static int getInt(long addr) throws Exception
    {
        init();
        return ((Integer)unsafeGetInt.invoke(theUnsafe, new Object[]{new Long(addr)})).intValue();
    }
    public static long getLong(long addr) throws Exception
    {
        init();
        return ((Long)unsafeGetLong.invoke(theUnsafe, new Object[]{new Long(addr)})).longValue();
    }
    public static void putByte(long addr, byte val) throws Exception
    {
        init();
        unsafePutByte.invoke(theUnsafe, new Object[]{new Long(addr), new Byte(val)});
    }
    public static void putInt(long addr, int val) throws Exception
    {
        init();
        unsafePutInt.invoke(theUnsafe, new Object[]{new Long(addr), new Integer(val)});
    }
    public static void putLong(long addr, long val) throws Exception
    {
        init();
        unsafePutLong.invoke(theUnsafe, new Object[]{new Long(addr), new Long(val)});
    }
    public static Object getUnsafe() throws Exception
    {
        init();
        return theUnsafe;
    }
    public static long fastGetLong(long addr)
    {
        return fastLongAccessor.seek(addr);
    }
}
