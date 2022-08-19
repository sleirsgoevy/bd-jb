package org.homebrew;

public class SignalServer
{
    private NativeStuff.SignalThread thr;
    private java.net.Socket sock;
    private java.io.InputStream is;
    private java.io.OutputStream os;
    private byte[] wrbuf;
    private int wrsize = 0;
    private int wrpos = 0;
    private long rw_page = 0;
    private long ro_page = 0;
    private SignalServer(java.net.Socket sk) throws Exception
    {
        wrbuf = new byte[1];
        sock = sk;
        is = sk.getInputStream();
        os = sk.getOutputStream();
        thr = new NativeStuff.SignalThread();
        thr.awaitSignal();
        /*long namebuf = NativeUtils.allocateMemory(8);
        for(int i = 0; i < 7; i++)
            NativeUtils.putByte(namebuf+i, (byte)"/xotext".charAt(i));
        NativeUtils.putByte(namebuf+7, (byte)0);*/
        int fd = (int)NativeStuff.callFunction(NativeUtils.dlsym(0x2001, "shm_open"), 1 /*namebuf*/, 0x602, 0x1ff, 0, 0, 0);
        //NativeUtils.freeMemory(namebuf);
        NativeStuff.callFunction(NativeUtils.dlsym(0x2001, "ftruncate"), fd, 0x8000, 0, 0, 0, 0);
        long mmap = NativeUtils.dlsym(0x2001, "mmap");
        long mprotect = NativeUtils.dlsym(0x2001, "mprotect");
        rw_page = NativeStuff.callFunction(mmap, 0, 0x8000, 3, 1, fd, 0);
        NativeStuff.callFunction(mprotect, rw_page+0x4000, 0x4000, 0, 0, 0, 0);
        ro_page = NativeStuff.callFunction(mmap, 0, 0x8000, 1, 1, fd, 0);
        NativeStuff.callFunction(mprotect, ro_page+0x4000, 0x4000, 0, 0, 0, 0);
        NativeStuff.callFunction(NativeUtils.dlsym(0x2001, "close"), fd, 0, 0, 0, 0, 0);
        int cmd;
        while((cmd = is.read()) != -1)
        {
            if(cmd == 0) //dlsym
            {
                String s = "";
                int q;
                while((q = is.read()) != 0)
                    s += (char)q;
                long addr = 0;
                for(int i = 0; addr == 0 && i < 16384; i++)
                    addr = NativeUtils.dlsym(i, s);
                writeLong(addr);
            }
            else if(cmd == 1) //write
            {
                int size = is.read();
                long addr = readLong();
                for(int i = 0; i < size; i++)
                    NativeUtils.putByte(addr+i, (byte)is.read());
            }
            else if(cmd == 2) //read
            {
                int size = is.read();
                long addr = readLong();
                for(int i = 0; i < size; i++)
                    writeByte(NativeUtils.getByte(addr+i));
            }
            else if(cmd == 3) //execute
            {
                thr.poke(thr.RAX, readLong());
                thr.poke(thr.RCX, readLong());
                thr.poke(thr.RDX, readLong());
                thr.poke(thr.RBX, readLong());
                thr.poke(thr.RSP, readLong());
                thr.poke(thr.RBP, readLong());
                thr.poke(thr.RSI, readLong());
                thr.poke(thr.RDI, readLong());
                thr.poke(thr.R8, readLong());
                thr.poke(thr.R9, readLong());
                thr.poke(thr.R10, readLong());
                thr.poke(thr.R11, readLong());
                thr.poke(thr.R12, readLong());
                thr.poke(thr.R13, readLong());
                thr.poke(thr.R14, readLong());
                thr.poke(thr.R15, readLong());
                thr.poke(thr.RIP, readLong());
                thr.poke(thr.EFLAGS, readLong());
                readLong();
                readLong();
                thr.cont();
                writeLong(thr.peek(thr.TRAPNO));
                writeLong(thr.peek(thr.RAX));
                writeLong(thr.peek(thr.RCX));
                writeLong(thr.peek(thr.RDX));
                writeLong(thr.peek(thr.RBX));
                writeLong(thr.peek(thr.RSP));
                writeLong(thr.peek(thr.RBP));
                writeLong(thr.peek(thr.RSI));
                writeLong(thr.peek(thr.RDI));
                writeLong(thr.peek(thr.R8));
                writeLong(thr.peek(thr.R9));
                writeLong(thr.peek(thr.R10));
                writeLong(thr.peek(thr.R11));
                writeLong(thr.peek(thr.R12));
                writeLong(thr.peek(thr.R13));
                writeLong(thr.peek(thr.R14));
                writeLong(thr.peek(thr.R15));
                writeLong(thr.peek(thr.RIP));
                writeLong(thr.peek(thr.EFLAGS));
                writeLong(thr.peek(thr.ERRCODE));
                writeLong(thr.peek(thr.CR2));
            }
            else if(cmd == 4) //get pages
            {
                writeLong(rw_page);
                writeLong(ro_page);
            }
            else if(cmd == 5)
            {
                os.write(wrbuf, 0, wrpos);
                wrpos = 0;
            }
        }
    }
    private void writeByte(byte b)
    {
        if(wrpos == wrbuf.length)
        {
            byte[] wrbuf2 = new byte[wrbuf.length*2];
            for(int i = 0; i < wrbuf.length; i++)
                wrbuf2[i] = wrbuf[i];
            wrbuf = wrbuf2;
        }
        wrbuf[wrpos++] = b;
    }
    private void writeLong(long l)
    {
        for(int i = 0; i < 64; i += 8)
            writeByte((byte)(l >> i));
    }
    private long readLong() throws Exception
    {
        long ans = 0;
        for(int i = 0; i < 64; i += 8)
            ans |= (((long)is.read()) & 255) << i;
        return ans;
    }
    public static void run(java.net.Socket sock) throws Exception
    {
        new SignalServer(sock);
    }
}
