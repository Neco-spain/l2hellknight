package l2m.commons.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64
{
  public static final int NO_OPTIONS = 0;
  public static final int ENCODE = 1;
  public static final int DECODE = 0;
  public static final int GZIP = 2;
  public static final int DONT_BREAK_LINES = 8;
  private static final int MAX_LINE_LENGTH = 76;
  private static final byte EQUALS_SIGN = 61;
  private static final byte NEW_LINE = 10;
  private static final String PREFERRED_ENCODING = "UTF-8";
  private static final byte[] ALPHABET;
  private static final byte[] _NATIVE_ALPHABET = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
  static final byte[] DECODABET;
  private static final byte WHITE_SPACE_ENC = -5;
  private static final byte EQUALS_SIGN_ENC = -1;

  public static void main(String[] args)
    throws IOException
  {
    BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("Enter String to encode: ");
    System.out.println(encodeBytes(bf.readLine().getBytes()));
  }

  static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes)
  {
    encode3to4(threeBytes, 0, numSigBytes, b4, 0);
    return b4;
  }

  static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset)
  {
    int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0) | (numSigBytes > 1 ? source[(srcOffset + 1)] << 24 >>> 16 : 0) | (numSigBytes > 2 ? source[(srcOffset + 2)] << 24 >>> 24 : 0);

    switch (numSigBytes)
    {
    case 3:
      destination[destOffset] = ALPHABET[(inBuff >>> 18)];
      destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
      destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
      destination[(destOffset + 3)] = ALPHABET[(inBuff & 0x3F)];
      return destination;
    case 2:
      destination[destOffset] = ALPHABET[(inBuff >>> 18)];
      destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
      destination[(destOffset + 2)] = ALPHABET[(inBuff >>> 6 & 0x3F)];
      destination[(destOffset + 3)] = 61;
      return destination;
    case 1:
      destination[destOffset] = ALPHABET[(inBuff >>> 18)];
      destination[(destOffset + 1)] = ALPHABET[(inBuff >>> 12 & 0x3F)];
      destination[(destOffset + 2)] = 61;
      destination[(destOffset + 3)] = 61;
      return destination;
    }

    return destination;
  }

  public static String encodeObject(Serializable serializableObject)
  {
    return encodeObject(serializableObject, 0);
  }

  public static String encodeObject(Serializable serializableObject, int options)
  {
    ByteArrayOutputStream baos = null;
    OutputStream b64os = null;
    ObjectOutputStream oos = null;
    GZIPOutputStream gzos = null;

    int gzip = options & 0x2;
    int dontBreakLines = options & 0x8;
    try
    {
      baos = new ByteArrayOutputStream();
      b64os = new OutputStream(baos, 0x1 | dontBreakLines);

      if (gzip == 2)
      {
        gzos = new GZIPOutputStream(b64os);
        oos = new ObjectOutputStream(gzos);
      }
      else {
        oos = new ObjectOutputStream(b64os);
      }
      oos.writeObject(serializableObject);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      Object localObject1 = null;
      return localObject1;
    }
    finally
    {
      try
      {
        oos.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        gzos.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        b64os.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        baos.close();
      }
      catch (Exception e)
      {
      }

    }

    try
    {
      return new String(baos.toByteArray(), "UTF-8");
    }
    catch (UnsupportedEncodingException uue) {
    }
    return new String(baos.toByteArray());
  }

  public static String encodeBytes(byte[] source)
  {
    return encodeBytes(source, 0, source.length, 0);
  }

  public static String encodeBytes(byte[] source, int options)
  {
    return encodeBytes(source, 0, source.length, options);
  }

  public static String encodeBytes(byte[] source, int off, int len)
  {
    return encodeBytes(source, off, len, 0);
  }

  public static String encodeBytes(byte[] source, int off, int len, int options)
  {
    int dontBreakLines = options & 0x8;
    int gzip = options & 0x2;

    if (gzip == 2)
    {
      ByteArrayOutputStream baos = null;
      GZIPOutputStream gzos = null;
      OutputStream b64os = null;
      try
      {
        baos = new ByteArrayOutputStream();
        b64os = new OutputStream(baos, 0x1 | dontBreakLines);
        gzos = new GZIPOutputStream(b64os);

        gzos.write(source, off, len);
        gzos.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
        Object localObject1 = null;
        return localObject1;
      }
      finally
      {
        try
        {
          gzos.close();
        }
        catch (Exception e)
        {
        }
        try
        {
          b64os.close();
        }
        catch (Exception e)
        {
        }
        try
        {
          baos.close();
        }
        catch (Exception e)
        {
        }

      }

      try
      {
        return new String(baos.toByteArray(), "UTF-8");
      }
      catch (UnsupportedEncodingException uue)
      {
        return new String(baos.toByteArray());
      }

    }

    boolean breakLines = dontBreakLines == 0;

    int len43 = len * 4 / 3;
    byte[] outBuff = new byte[len43 + (len % 3 > 0 ? 4 : 0) + (breakLines ? len43 / 76 : 0)];

    int d = 0;
    int e = 0;
    int len2 = len - 2;
    int lineLength = 0;
    for (; d < len2; e += 4)
    {
      encode3to4(source, d + off, 3, outBuff, e);

      lineLength += 4;
      if ((breakLines) && (lineLength == 76))
      {
        outBuff[(e + 4)] = 10;
        e++;
        lineLength = 0;
      }
      d += 3;
    }

    if (d < len)
    {
      encode3to4(source, d + off, len - d, outBuff, e);
      e += 4;
    }

    try
    {
      return new String(outBuff, 0, e, "UTF-8");
    }
    catch (UnsupportedEncodingException uue) {
    }
    return new String(outBuff, 0, e);
  }

  static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset)
  {
    if (source[(srcOffset + 2)] == 61)
    {
      int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12;

      destination[destOffset] = (byte)(outBuff >>> 16);
      return 1;
    }

    if (source[(srcOffset + 3)] == 61)
    {
      int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[source[(srcOffset + 2)]] & 0xFF) << 6;

      destination[destOffset] = (byte)(outBuff >>> 16);
      destination[(destOffset + 1)] = (byte)(outBuff >>> 8);
      return 2;
    }

    try
    {
      int outBuff = (DECODABET[source[srcOffset]] & 0xFF) << 18 | (DECODABET[source[(srcOffset + 1)]] & 0xFF) << 12 | (DECODABET[source[(srcOffset + 2)]] & 0xFF) << 6 | DECODABET[source[(srcOffset + 3)]] & 0xFF;

      destination[destOffset] = (byte)(outBuff >> 16);
      destination[(destOffset + 1)] = (byte)(outBuff >> 8);
      destination[(destOffset + 2)] = (byte)outBuff;

      return 3;
    }
    catch (Exception e)
    {
      System.out.println(StringUtil.concat(new String[] { String.valueOf(source[srcOffset]), ": ", String.valueOf(DECODABET[source[srcOffset]]) }));
      System.out.println(StringUtil.concat(new String[] { String.valueOf(source[(srcOffset + 1)]), ": ", String.valueOf(DECODABET[source[(srcOffset + 1)]]) }));
      System.out.println(StringUtil.concat(new String[] { String.valueOf(source[(srcOffset + 2)]), ": ", String.valueOf(DECODABET[source[(srcOffset + 2)]]) }));
      System.out.println(StringUtil.concat(new String[] { String.valueOf(source[(srcOffset + 3)]), ": ", String.valueOf(DECODABET[source[(srcOffset + 3)]]) }));
    }return -1;
  }

  public static byte[] decode(byte[] source, int off, int len)
  {
    int len34 = len * 3 / 4;
    byte[] outBuff = new byte[len34];
    int outBuffPosn = 0;

    byte[] b4 = new byte[4];
    int b4Posn = 0;
    int i = 0;
    byte sbiCrop = 0;
    byte sbiDecode = 0;
    for (i = off; i < off + len; i++)
    {
      sbiCrop = (byte)(source[i] & 0x7F);
      sbiDecode = DECODABET[sbiCrop];

      if (sbiDecode >= -5)
      {
        if (sbiDecode < -1)
          continue;
        b4[(b4Posn++)] = sbiCrop;
        if (b4Posn <= 3)
          continue;
        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
        b4Posn = 0;

        if (sbiCrop == 61) {
          break;
        }

      }
      else
      {
        System.err.println(StringUtil.concat(new String[] { "Bad Base64 input character at ", String.valueOf(i), ": ", String.valueOf(source[i]), "(decimal)" }));
        return null;
      }
    }

    byte[] out = new byte[outBuffPosn];
    System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
    return out;
  }

  public static byte[] decode(String s)
  {
    try
    {
      bytes = s.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException uee)
    {
      bytes = s.getBytes();
    }

    byte[] bytes = decode(bytes, 0, bytes.length);

    if ((bytes != null) && (bytes.length >= 2))
    {
      int head = bytes[0] & 0xFF | bytes[1] << 8 & 0xFF00;
      if ((bytes.length >= 4) && (35615 == head))
      {
        ByteArrayInputStream bais = null;
        GZIPInputStream gzis = null;
        ByteArrayOutputStream baos = null;
        byte[] buffer = new byte[2048];
        int length = 0;
        try
        {
          baos = new ByteArrayOutputStream();
          bais = new ByteArrayInputStream(bytes);
          gzis = new GZIPInputStream(bais);

          while ((length = gzis.read(buffer)) >= 0)
          {
            baos.write(buffer, 0, length);
          }

          bytes = baos.toByteArray();
        }
        catch (IOException e)
        {
        }
        finally
        {
          try
          {
            baos.close();
          }
          catch (Exception e)
          {
          }
          try
          {
            gzis.close();
          }
          catch (Exception e)
          {
          }
          try
          {
            bais.close();
          }
          catch (Exception e)
          {
          }
        }
      }

    }

    return bytes;
  }

  public static Object decodeToObject(String encodedObject)
  {
    byte[] objBytes = decode(encodedObject);

    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    Object obj = null;
    try
    {
      bais = new ByteArrayInputStream(objBytes);
      ois = new ObjectInputStream(bais);

      obj = ois.readObject();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        bais.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        ois.close();
      }
      catch (Exception e)
      {
      }
    }

    return obj;
  }

  static
  {
    byte[] __bytes;
    try
    {
      __bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException use)
    {
      __bytes = _NATIVE_ALPHABET;
    }
    ALPHABET = __bytes;

    DECODABET = new byte[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9 };
  }

  public static class OutputStream extends FilterOutputStream
  {
    private boolean encode;
    private int position;
    private byte[] buffer;
    private int bufferLength;
    private int lineLength;
    private boolean breakLines;
    private byte[] b4;
    private boolean suspendEncoding;

    public OutputStream(OutputStream pOut)
    {
      this(pOut, 1);
    }

    public OutputStream(OutputStream pOut, int options)
    {
      super();

      breakLines = ((options & 0x8) != 8);
      encode = ((options & 0x1) == 1);
      bufferLength = (encode ? 3 : 4);
      buffer = new byte[bufferLength];
      position = 0;
      lineLength = 0;
      suspendEncoding = false;
      b4 = new byte[4];
    }

    public void write(int theByte)
      throws IOException
    {
      if (suspendEncoding)
      {
        out.write(theByte);
        return;
      }

      if (encode)
      {
        buffer[(position++)] = (byte)theByte;
        if (position >= bufferLength)
        {
          out.write(Base64.encode3to4(b4, buffer, bufferLength));

          lineLength += 4;
          if ((breakLines) && (lineLength >= 76))
          {
            out.write(10);
            lineLength = 0;
          }

          position = 0;
        }

      }
      else if (Base64.DECODABET[(theByte & 0x7F)] > -5)
      {
        buffer[(position++)] = (byte)theByte;
        if (position >= bufferLength)
        {
          int len = Base64.decode4to3(buffer, 0, b4, 0);
          out.write(b4, 0, len);

          position = 0;
        }
      }
      else if (Base64.DECODABET[(theByte & 0x7F)] != -5)
      {
        throw new IOException("Invalid character in Base64 data.");
      }
    }

    public void write(byte[] theBytes, int off, int len)
      throws IOException
    {
      if (suspendEncoding)
      {
        out.write(theBytes, off, len);
        return;
      }

      for (int i = 0; i < len; i++)
      {
        write(theBytes[(off + i)]);
      }
    }

    public void flushBase64()
      throws IOException
    {
      if (position > 0)
      {
        if (encode)
        {
          out.write(Base64.encode3to4(b4, buffer, position));
          position = 0;
        }
        else
        {
          throw new IOException("Base64 input not properly padded.");
        }
      }
    }

    public void close()
      throws IOException
    {
      flushBase64();

      super.close();

      buffer = null;
      out = null;
    }

    public void suspendEncoding()
      throws IOException
    {
      flushBase64();
      suspendEncoding = true;
    }

    public void resumeEncoding()
    {
      suspendEncoding = false;
    }
  }

  public static class InputStream extends FilterInputStream
  {
    private boolean encode;
    private int position;
    private byte[] buffer;
    private int bufferLength;
    private int numSigBytes;
    private int lineLength;
    private boolean breakLines;

    public InputStream(InputStream pIn)
    {
      this(pIn, 0);
    }

    public InputStream(InputStream pIn, int options)
    {
      super();

      breakLines = ((options & 0x8) != 8);
      encode = ((options & 0x1) == 1);
      bufferLength = (encode ? 4 : 3);
      buffer = new byte[bufferLength];
      position = -1;
      lineLength = 0;
    }

    public int read()
      throws IOException
    {
      if (position < 0)
      {
        if (encode)
        {
          byte[] b3 = new byte[3];
          int numBinaryBytes = 0;
          for (int i = 0; i < 3; i++)
          {
            try
            {
              int b = in.read();

              if (b >= 0)
              {
                b3[i] = (byte)b;
                numBinaryBytes++;
              }

            }
            catch (IOException e)
            {
              if (i == 0) {
                throw e;
              }
            }
          }

          if (numBinaryBytes > 0)
          {
            Base64.encode3to4(b3, 0, numBinaryBytes, buffer, 0);
            position = 0;
            numSigBytes = 4;
          }
          else
          {
            return -1;
          }

        }
        else
        {
          byte[] b4 = new byte[4];
          int i = 0;
          for (i = 0; i < 4; i++)
          {
            int b = 0;
            do
            {
              b = in.read();
            }
            while ((b >= 0) && (Base64.DECODABET[(b & 0x7F)] <= -5));

            if (b < 0) {
              break;
            }
            b4[i] = (byte)b;
          }

          if (i == 4)
          {
            numSigBytes = Base64.decode4to3(b4, 0, buffer, 0);
            position = 0;
          } else {
            if (i == 0)
            {
              return -1;
            }

            throw new IOException("Improperly padded Base64 input.");
          }

        }

      }

      if (position >= 0)
      {
        if (position >= numSigBytes) {
          return -1;
        }
        if ((encode) && (breakLines) && (lineLength >= 76))
        {
          lineLength = 0;
          return 10;
        }
        lineLength += 1;

        int b = buffer[(position++)];

        if (position >= bufferLength) {
          position = -1;
        }
        return b & 0xFF;
      }

      throw new IOException("Error in Base64 code reading stream.");
    }

    public int read(byte[] dest, int off, int len)
      throws IOException
    {
      for (int i = 0; i < len; i++)
      {
        int b = read();

        if (b >= 0) {
          dest[(off + i)] = (byte)b; } else {
          if (i != 0) break;
          return -1;
        }
      }

      return i;
    }
  }
}