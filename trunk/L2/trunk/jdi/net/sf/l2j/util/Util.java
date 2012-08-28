package net.sf.l2j.util;

import java.io.PrintStream;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;

public class Util
{
  public static boolean isInternalIP(String ipAddress)
  {
    return (ipAddress.startsWith("192.168.")) || (ipAddress.startsWith("10.")) || (ipAddress.startsWith("127.0.0.1"));
  }

  public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000L);
  }

  public static int[] parseCommaSeparatedIntegerArray(String s)
  {
    if (s.isEmpty()) {
      return new int[0];
    }
    String[] tmp = s.replaceAll(",", ";").split(";");
    int[] ret = new int[tmp.length];
    for (int i = 0; i < tmp.length; i++) {
      ret[i] = Integer.parseInt(tmp[i]);
    }
    return ret;
  }

  public static String printData(byte[] data, int len)
  {
    TextBuilder result = new TextBuilder();

    int counter = 0;

    for (int i = 0; i < len; i++)
    {
      if (counter % 16 == 0)
      {
        result.append(fillHex(i, 4) + ": ");
      }

      result.append(fillHex(data[i] & 0xFF, 2) + " ");
      counter++;
      if (counter != 16)
        continue;
      result.append("   ");

      int charpoint = i - 15;
      for (int a = 0; a < 16; a++)
      {
        int t1 = data[(charpoint++)];
        if ((t1 > 31) && (t1 < 128))
        {
          result.append((char)t1);
        }
        else
        {
          result.append('.');
        }
      }

      result.append("\n");
      counter = 0;
    }

    int rest = data.length % 16;
    if (rest > 0)
    {
      for (int i = 0; i < 17 - rest; i++)
      {
        result.append("   ");
      }

      int charpoint = data.length - rest;
      for (int a = 0; a < rest; a++)
      {
        int t1 = data[(charpoint++)];
        if ((t1 > 31) && (t1 < 128))
        {
          result.append((char)t1);
        }
        else
        {
          result.append('.');
        }
      }

      result.append("\n");
    }

    return result.toString();
  }

  public static String fillHex(int data, int digits)
  {
    String number = Integer.toHexString(data);

    for (int i = number.length(); i < digits; i++)
    {
      number = "0" + number;
    }

    return number;
  }

  public static void printCpuInfo() {
    System.out.println("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
    System.out.println("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
  }

  public static void printOSInfo()
  {
    System.out.println("Operating System: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version") + " Architecture: " + System.getProperty("os.arch"));
  }

  public static void printSection(String s) {
    int maxlength = 79;
    s = "-[ " + s + " ]";
    int slen = s.length();
    if (slen > maxlength)
    {
      System.out.println(s);
      return;
    }

    for (int i = 0; i < maxlength - slen; i++)
      s = "=" + s;
    System.out.println(s);
  }

  public static String printData(byte[] raw)
  {
    return printData(raw, raw.length);
  }
}