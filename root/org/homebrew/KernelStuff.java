package org.homebrew;

/* offsets are for 4.03 */

public class KernelStuff
{
    private static KernelRW krw = null;
    private static int pipe_r = -1;
    private static int pipe_w = -1;
    private static long kbase = 0;
    private static long curproc = 0;
    private static long rpipe = 0;
    private static long read = 0;
    private static long write = 0;

    public static void setRW(KernelRW k)
    {
        if(krw == null)
            krw = k;
    }

    private static void init() throws Exception
    {
        if(krw == null)
            throw new Exception("kernel exploit has not run");
        if(pipe_r < 0)
        {
            long mem8 = NativeUtils.allocateMemory(8);
            NativeStuff.callFunction(NativeUtils.dlsym(0x2001, "pipe"), mem8, 0, 0, 0, 0, 0);
            pipe_r = NativeUtils.getInt(mem8);
            pipe_w = NativeUtils.getInt(mem8+4);
            NativeUtils.freeMemory(mem8);
        }
        if(kbase == 0)
            kbase = krw.kread8(krw.kqueue_addr) - 0x318ad3;
        if(curproc == 0)
            curproc = pfind0((int)NativeStuff.callFunction(NativeUtils.dlsym(0x2001, "getpid"), 0, 0, 0, 0, 0, 0));
        if(rpipe == 0)
        {
            long fd = krw.kread8(curproc+0x48);
            long ofiles = krw.kread8(fd);
            long pipe_file = krw.kread8(ofiles+8+48*pipe_r);
            rpipe = krw.kread8(pipe_file);
        }
        if(read == 0)
            read = NativeUtils.dlsym(0x2001, "read");
        if(write == 0)
            write = NativeUtils.dlsym(0x2001, "write");
    }

    private static long pfind0(int pid0) throws Exception
    {
        for(long proc = krw.kread8(kbase+0x27edcb8); proc != 0; proc = krw.kread8(proc))
        {
            int pid = (int)(krw.kread8(proc+0xbc) & 0xffffffffl);
            if(pid == pid0)
                return proc;
        }
        return 0;
    }

    public static long pfind(int pid0) throws Exception
    {
        init();
        return pfind0(pid0);
    }

    public static int copyout(long dst, long src, int count) throws Exception
    {
        init();
        krw.kwrite20(rpipe, 0x4000000040000000l, 0x4000000000000000l, 0);
        krw.kwrite20(rpipe+16, src, 0, 0);
        return (int)NativeStuff.callFunction(read, pipe_r, dst, count, 0, 0, 0);
    }

    public static int copyin(long dst, long src, int count) throws Exception
    {
        init();
        krw.kwrite20(rpipe, 0, 0x4000000000000000l, 0);
        krw.kwrite20(rpipe+16, dst, 0, 0);
        return (int)NativeStuff.callFunction(write, pipe_w, src, count, 0, 0, 0);
    }

    public static void jailbreak(long authid) throws Exception
    {
        init();
        long cred = krw.kread8(curproc+0x40);
        long mem = NativeUtils.allocateMemory(0x70);
        copyout(mem, cred, 0x70);
        NativeUtils.putInt(mem+4, 0); //cr_uid
        NativeUtils.putInt(mem+8, 0); //cr_ruid
        NativeUtils.putInt(mem+12, 0); //cr_svuid
        NativeUtils.putInt(mem+20, 0); //cr_rgid
        NativeUtils.putInt(mem+24, 0); //cr_svgid
        NativeUtils.putLong(mem+88, authid);
        NativeUtils.putLong(mem+96, -1);
        NativeUtils.putLong(mem+104, -1);
        copyin(cred, mem, 0x70);
        long fd = krw.kread8(curproc+0x48);
        long pid1 = pfind0(1);
        long fd1 = krw.kread8(pid1+0x48);
        copyout(mem, fd1+8, 24);
        copyin(fd+8, mem, 24);
        NativeUtils.freeMemory(mem);
    }

    public static void jailbreak() throws Exception
    {
        jailbreak(0x4801000000000013l);
    }
}
