package ccpGuard.login.crypt;


public final class ProtectionCrypt
{
    int    x;
    int    y;
    byte[] state = new byte[256];

    final int arcfour_byte()
    {
        int x;
        int y;
        int sx, sy;

        x = (this.x + 1) & 0xff;
        sx = (int)state[x];
        y = (sx + this.y) & 0xff;
        sy = (int)state[y];
        this.x = x;
        this.y = y;
        state[y] = (byte)(sx & 0xff);
        state[x] = (byte)(sy & 0xff);
        return (int)state[((sx + sy) & 0xff)];
    }

    public void setModKey(int key)
    {
        byte[] bKey = new byte[16];
        intToBytes(InitBox.getValue((byte) (key & 0xff)), bKey, 0);
        intToBytes(InitBox.getValue((byte) (key >> 0x08 & 0xff)),bKey,4);
        intToBytes(InitBox.getValue((byte) (key >> 0x10 & 0xff)),bKey,8);
        intToBytes(InitBox.getValue((byte) (key >> 0x18 & 0xff)),bKey,12);
        setKey(bKey);
    }

    public synchronized void doCrypt(byte[] src, int srcOff, byte[] dest, int destOff, int len)
    {
        int end = srcOff + len;
        for(int si = srcOff, di = destOff; si < end; si++, di++)
            dest[di] = (byte)(((int)src[si] ^ arcfour_byte()) & 0xff);
    }

    public void setKey(byte[] key)
    {
        int t, u;
        int counter;
        this.x = 0;
        this.y = 0;

        for(counter = 0; counter < 256; counter++)
            state[counter] = (byte)counter;

        int keyindex   = 0;
        int stateindex = 0;
        for(counter = 0; counter < 256; counter++)
        {
            t = (int)state[counter];
            stateindex = (stateindex + key[keyindex] + t) & 0xff;
            u = (int)state[stateindex];
            state[stateindex] = (byte)(t & 0xff);
            state[counter] = (byte)(u & 0xff);
            if(++keyindex >= key.length)
                keyindex = 0;
        }
    }
	
	public static int bytesToInt(byte[] array, int offset)
    {
        return (((int) array[offset++] & 0xff) | (((int) array[offset++] & 0xff) << 8) | (((int) array[offset++] & 0xff) << 16) | (((int) array[offset++] & 0xff) << 24));
    }

    public static void intToBytes(int value, byte[] array, int offset)
    {
        array[offset++] = (byte) (value & 0xff);
        array[offset++] = (byte) (value >> 0x08 & 0xff);
        array[offset++] = (byte) (value >> 0x10 & 0xff);
        array[offset++] = (byte) (value >> 0x18 & 0xff);
    }
}

