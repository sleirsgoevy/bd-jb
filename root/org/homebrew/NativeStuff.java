package org.homebrew;

public class NativeStuff
{
    public static native long multiNewArrayGadget(long payload, int[] array);
    private static boolean gadget_initialized = false;
    private static long mov_rax_rdi_0x10_ret = 0;
    private static long getcontext = 0;
    private static long setcontext = 0;
    private static long open = 0;
    private static long getdents = 0;
    private static long close = 0;
    private static void init() throws Exception
    {
        if(!gadget_initialized)
        {
            NativeUtils.addSymbol("Java_org_homebrew_NativeStuff_multiNewArrayGadget",
                NativeUtils.dlsym(74, "Java_java_lang_reflect_Array_multiNewArray"));
            gadget_initialized = true;
        }
        if(mov_rax_rdi_0x10_ret == 0)
            mov_rax_rdi_0x10_ret = NativeUtils.dlsym(0x2001, "sceKernelGetEventData");
        if(getcontext == 0)
            getcontext = NativeUtils.dlsym(0x2001, "getcontext");
        if(setcontext == 0)
            setcontext = NativeUtils.dlsym(0x2001, "setcontext");
        if(open == 0)
            open = NativeUtils.dlsym(0x2001, "open");
        if(getdents == 0)
            getdents = NativeUtils.dlsym(0x2001, "getdents");
        if(close == 0)
            close = NativeUtils.dlsym(0x2001, "close");
    }
    private static long callGadget(long func1, long func2, long[] args) throws Exception
    {
        //TODO: allocate less memory
        long obj1 = NativeUtils.allocateMemory(8);
        long obj2 = NativeUtils.allocateMemory(0xa0);
        long obj3 = NativeUtils.allocateMemory(256);
        long obj4 = NativeUtils.allocateMemory(0xe0);
        long obj5 = NativeUtils.allocateMemory(0x4000);
        long obj6 = NativeUtils.allocateMemory(0x160);
        NativeUtils.putLong(obj1, obj2);
        NativeUtils.putLong(obj2+0x98, obj3);
        NativeUtils.putInt(obj3+0xc4, 0);
        NativeUtils.putLong(obj3, obj4);
        NativeUtils.putLong(obj3+0x10, obj5);
        NativeUtils.putLong(obj4+0xd8, mov_rax_rdi_0x10_ret); //obj3 -> obj5
        NativeUtils.putLong(obj5, obj6);
        NativeUtils.putLong(obj6+0x158, func1);
        int[] q = new int[]{1};
        multiNewArrayGadget(obj1, q);
        for(int i = 1; i < 7; i++)
            NativeUtils.putLong(obj5+0x40+8*i, args[i]);
        NativeUtils.putLong(obj5+0xe0, args[0]);
        NativeUtils.putLong(obj5, obj6); //overwrites sigmask
        NativeUtils.putLong(obj6+0x158, func2);
        long ans = multiNewArrayGadget(obj1, q);
        if(ans != 0)
            ans = NativeUtils.fastGetLong(ans);
        NativeUtils.freeMemory(obj1);
        NativeUtils.freeMemory(obj2);
        NativeUtils.freeMemory(obj3);
        NativeUtils.freeMemory(obj4);
        NativeUtils.freeMemory(obj5);
        NativeUtils.freeMemory(obj6);
        return ans;
    }
    public static long callFunction(long func, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) throws Exception
    {
        init();
        long[] arr = new long[]{func, arg1, arg2, arg3, arg4, arg5, arg6};
        return callGadget(getcontext, setcontext, arr);
    }
    public static String[] listdir(String path) throws Exception
    {
        init();
        long pmem = NativeUtils.allocateMemory(path.length() + 1);
        for(int i = 0; i < path.length(); i++)
        {
            NativeUtils.putByte(pmem + i, (byte)path.charAt(i));
        }
        NativeUtils.putByte(pmem + path.length(), (byte)0);
        int fd = (int)callFunction(open, pmem, 0, 0, 0, 0, 0);
        long buf = NativeUtils.allocateMemory(16384);
        long ll = -1;
        java.util.ArrayList ans = new java.util.ArrayList();
        while((ll = callFunction(getdents, fd, buf, 16384, 0, 0, 0)) > 0)
        {
            long i = 0;
            while(i < ll)
            {
                long l = (NativeUtils.getByte(buf + i + 5) + 256) % 256 * 256 + (NativeUtils.getByte(buf + i + 4) + 256) % 256;
                long l2 = (NativeUtils.getByte(buf + i + 7) + 256) % 256;
                String name = "";
                for(long k = 0; k < l2; k++)
                    name += (char)((NativeUtils.getByte(buf + i + 8 + k) + 256) % 256);
                i += l;
                ans.add(name);
            }
        }
        callFunction(close, fd, 0, 0, 0, 0, 0);
        NativeUtils.freeMemory(pmem);
        NativeUtils.freeMemory(buf);
        String[] arr = new String[ans.size()];
        for(int i = 0; i < ans.size(); i++)
            arr[i] = (String)ans.get(i);
        return arr;
    }
}
