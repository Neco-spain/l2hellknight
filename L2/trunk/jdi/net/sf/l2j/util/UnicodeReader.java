package net.sf.l2j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class UnicodeReader extends Reader
{
  PushbackInputStream internalIn;
  InputStreamReader internalIn2 = null;
  String defaultEnc;
  private static final int BOM_SIZE = 4;

  UnicodeReader(InputStream in, String defaultEnc)
  {
    internalIn = new PushbackInputStream(in, 4);
    this.defaultEnc = defaultEnc;
  }

  public String getDefaultEncoding()
  {
    return defaultEnc;
  }

  public String getEncoding()
  {
    if (internalIn2 == null)
      return null;
    return internalIn2.getEncoding();
  }

  protected void init()
    throws IOException
  {
    if (internalIn2 != null) {
      return;
    }

    byte[] bom = new byte[4];

    int n = internalIn.read(bom, 0, bom.length);
    int unread;
    String encoding;
    int unread;
    if ((bom[0] == -17) && (bom[1] == -69) && (bom[2] == -65))
    {
      String encoding = "UTF-8";
      unread = n - 3;
    }
    else
    {
      int unread;
      if ((bom[0] == -2) && (bom[1] == -1))
      {
        String encoding = "UTF-16BE";
        unread = n - 2;
      }
      else
      {
        int unread;
        if ((bom[0] == -1) && (bom[1] == -2))
        {
          String encoding = "UTF-16LE";
          unread = n - 2;
        }
        else
        {
          int unread;
          if ((bom[0] == 0) && (bom[1] == 0) && (bom[2] == -2) && (bom[3] == -1))
          {
            String encoding = "UTF-32BE";
            unread = n - 4;
          }
          else
          {
            int unread;
            if ((bom[0] == -1) && (bom[1] == -2) && (bom[2] == 0) && (bom[3] == 0))
            {
              String encoding = "UTF-32LE";
              unread = n - 4;
            }
            else
            {
              encoding = defaultEnc;
              unread = n;
            }
          }
        }
      }
    }
    if (unread > 0) {
      internalIn.unread(bom, n - unread, unread);
    }

    if (encoding == null)
      internalIn2 = new InputStreamReader(internalIn);
    else
      internalIn2 = new InputStreamReader(internalIn, encoding);
  }

  public void close()
    throws IOException
  {
    init();
    internalIn2.close();
  }

  public int read(char[] cbuf, int off, int len)
    throws IOException
  {
    init();
    return internalIn2.read(cbuf, off, len);
  }
}