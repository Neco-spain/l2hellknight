package org.mmocore.test.testserver;

import java.io.IOException;
import java.io.PrintStream;
import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

public class TestServer
{
  public static int PORT = 51966;

  public static void main(String[] args) throws IOException
  {
    if (args.length > 0)
    {
      PORT = Integer.parseInt(args[0]);
    }

    SelectorHelper sh = new SelectorHelper();
    SelectorConfig ssc = new SelectorConfig(null, null, sh, sh);
    ssc.setMaxSendPerPass(2);
    SelectorThread selector = new SelectorThread(ssc, sh, sh, null);
    selector.openServerSocket(null, PORT);

    selector.start();

    System.err.println("TestServer active on port " + PORT);
  }
}