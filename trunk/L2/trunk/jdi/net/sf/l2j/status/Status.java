package net.sf.l2j.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.util.Rnd;

public class Status extends Thread
{
  private ServerSocket statusServerSocket;
  private int _uptime;
  private int _statusPort;
  private String _statusPw;
  private int _mode;
  private List<LoginStatusThread> _loginStatus;

  public void run()
  {
    while (true)
      try
      {
        Socket connection = statusServerSocket.accept();

        if (_mode != 1)
          continue;
        new GameStatusThread(connection, _uptime, _statusPw); continue;

        if (_mode != 2)
          continue;
        LoginStatusThread lst = new LoginStatusThread(connection, _uptime);
        if (!lst.isAlive())
          continue;
        _loginStatus.add(lst);

        if (!isInterrupted())
          continue;
        try
        {
          statusServerSocket.close();
        } catch (IOException io) {
          io.printStackTrace();
        }break;

        continue;
      }
      catch (IOException e)
      {
        if (isInterrupted())
        {
          try
          {
            statusServerSocket.close();
          } catch (IOException io) {
            io.printStackTrace();
          }
        }
      }
  }

  public Status(int mode)
    throws IOException
  {
    super("Status");
    _mode = mode;
    Properties telnetSettings = new Properties();
    InputStream is = new FileInputStream(new File("./config/telnet.ini"));
    telnetSettings.load(is);
    is.close();

    _statusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
    _statusPw = telnetSettings.getProperty("StatusPW");
    if (_mode == 1)
    {
      if (_statusPw == null)
      {
        System.out.println("Server's Telnet Function Has No Password Defined!");
        System.out.println("A Password Has Been Automaticly Created!");
        _statusPw = rndPW(10);
        System.out.println("Password Has Been Set To: " + _statusPw);
      }
      System.out.println("StatusServer Started! - Listening on Port: " + _statusPort);
      System.out.println("Password Has Been Set To: " + _statusPw);
    }
    else
    {
      System.out.println("StatusServer Started! - Listening on Port: " + _statusPort);
      System.out.println("Password Has Been Set To: " + _statusPw);
    }
    statusServerSocket = new ServerSocket(_statusPort);
    _uptime = (int)System.currentTimeMillis();
    _loginStatus = new FastList();
  }

  private String rndPW(int length)
  {
    TextBuilder password = new TextBuilder();
    String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
    String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
    String digits = "1234567890";
    for (int i = 0; i < length; i++)
    {
      int charSet = Rnd.nextInt(3);
      switch (charSet)
      {
      case 0:
        password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length() - 1)));
        break;
      case 1:
        password.append(upperChar.charAt(Rnd.nextInt(upperChar.length() - 1)));
        break;
      case 2:
        password.append(digits.charAt(Rnd.nextInt(digits.length() - 1)));
      }
    }

    return password.toString();
  }

  public void sendMessageToTelnets(String msg)
  {
    List lsToRemove = new FastList();
    for (LoginStatusThread ls : _loginStatus)
    {
      if (ls.isInterrupted())
      {
        lsToRemove.add(ls);
      }
      else
      {
        ls.printToTelnet(msg);
      }
    }
  }
}