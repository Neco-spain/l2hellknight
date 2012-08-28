package net.sf.l2j.status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.L2LoginServer;
import net.sf.l2j.loginserver.LoginController;

public class LoginStatusThread extends Thread
{
  private static final Logger _log = Logger.getLogger(LoginStatusThread.class.getName());
  private Socket _cSocket;
  private PrintWriter _print;
  private BufferedReader _read;
  private boolean _redirectLogger;
  private String _pass;

  private void telnetOutput(int type, String text)
  {
    if (type == 1) System.out.println("TELNET | " + text);
    else if (type == 2) System.out.print("TELNET | " + text);
    else if (type == 3) System.out.print(text);
    else if (type == 4) System.out.println(text); else
      System.out.println("TELNET | " + text);
  }

  private boolean isValidIP(Socket client) {
    boolean result = false;
    InetAddress ClientIP = client.getInetAddress();

    String clientStringIP = ClientIP.getHostAddress();

    telnetOutput(1, "Connection from: " + clientStringIP);

    if (Config.DEVELOPER) telnetOutput(2, "");
    try
    {
      Properties telnetSettings = new Properties();
      InputStream telnetIS = new FileInputStream(new File("./config/telnet.ini"));
      telnetSettings.load(telnetIS);
      telnetIS.close();

      String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost");

      if (Config.DEVELOPER) telnetOutput(3, "Comparing ip to list...");

      String ipToCompare = null;
      for (String ip : HostList.split(","))
        if (!result) {
          ipToCompare = InetAddress.getByName(ip).getHostAddress();
          if (clientStringIP.equals(ipToCompare)) result = true;
          if (!Config.DEVELOPER) continue; telnetOutput(3, clientStringIP + " = " + ipToCompare + "(" + ip + ") = " + result);
        }
    }
    catch (IOException e)
    {
      if (Config.DEVELOPER) telnetOutput(4, "");
      telnetOutput(1, "Error: " + e);
    }

    if (Config.DEVELOPER) telnetOutput(4, "Allow IP: " + result);
    return result;
  }

  public LoginStatusThread(Socket client, int uptime) throws IOException
  {
    _cSocket = client;

    _print = new PrintWriter(_cSocket.getOutputStream());
    _read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));

    if (isValidIP(client)) {
      telnetOutput(1, client.getInetAddress().getHostAddress() + " accepted.");
      _print.println("Welcome To The L2J Telnet Session.");
      _print.println("Please Insert Your Login!");
      _print.print("Login: ");
      _print.flush();
      String tmpLine = _read.readLine();
      if (tmpLine == null) {
        _print.println("Error.");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
        return;
      }

      if (!validLogin(tmpLine))
      {
        _print.println("Incorrect Login!");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
        return;
      }

      _print.println("Login Correct!");
      _print.flush();

      _print.println("Please Insert Your Password!");
      _print.print("Password: ");
      _print.flush();
      tmpLine = _read.readLine();
      if (tmpLine == null) {
        _print.println("Error.");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
      }
      else if (!validPassword(tmpLine))
      {
        _print.println("Incorrect Password!");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
      }
      else
      {
        _print.println("Password Correct!");
        _print.println("[L2J]");
        _print.print("");
        _print.flush();
        start();
      }
    }
    else
    {
      telnetOutput(1, "Connection attempt from " + client.getInetAddress().getHostAddress() + " rejected.");
      _cSocket.close();
    }
  }

  private boolean validPassword(String password)
  {
    byte[] expectedPass = Base64.decode(_pass);
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] raw = password.getBytes("UTF-8");
      byte[] hash = md.digest(raw);
      for (int i = 0; i < expectedPass.length; i++)
      {
        if (hash[i] != expectedPass[i])
        {
          return false;
        }
      }
      return true;
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
    catch (UnsupportedEncodingException uee)
    {
    }

    return false;
  }

  private boolean validLogin(String login)
  {
    if (!LoginController.getInstance().isGM(login))
      return false;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT password FROM accounts WHERE login=?");
      statement.setString(1, login);
      ResultSet rset = statement.executeQuery();
      if (rset.next())
      {
        _pass = rset.getString("password");
        statement.close();
        con.close();
        int i = 1;
        return i;
      }
      statement.close();
    }
    catch (SQLException e)
    {
      sqle.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return false;
  }

  public void run()
  {
    String _usrCommand = "";
    try
    {
      while ((_usrCommand.compareTo("quit") != 0) && (_usrCommand.compareTo("exit") != 0))
      {
        _usrCommand = _read.readLine();
        if (_usrCommand == null)
        {
          _cSocket.close();
          break;
        }
        if (_usrCommand.equals("help"))
        {
          _print.println("The following is a list of all available commands: ");
          _print.println("help                - shows this help.");
          _print.println("status              - displays basic server statistics.");
          _print.println("unblock <ip>        - removes <ip> from banlist.");
          _print.println("shutdown\t\t\t- shuts down server.");
          _print.println("restart\t\t\t\t- restarts the server.");
          _print.println("RedirectLogger\t\t- Telnet will give you some info about server in real time.");
          _print.println("quit                - closes telnet session.");
          _print.println("");
        }
        else if (_usrCommand.equals("status"))
        {
          _print.println("Registered Server Count: " + GameServerTable.getInstance().getRegisteredGameServers().size());
        }
        else if (_usrCommand.startsWith("unblock"))
        {
          try
          {
            _usrCommand = _usrCommand.substring(8);
            if (LoginController.getInstance().removeBanForAddress(_usrCommand))
            {
              _log.warning("IP removed via TELNET by host: " + _cSocket.getInetAddress().getHostAddress());
              _print.println("The IP " + _usrCommand + " has been removed from the hack protection list!");
            }
            else
            {
              _print.println("IP not found in hack protection list...");
            }
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please Enter the IP to Unblock!");
          }
        }
        else if (_usrCommand.startsWith("shutdown"))
        {
          L2LoginServer.getInstance().shutdown(false);
          _print.println("Bye Bye!");
          _print.flush();
          _cSocket.close();
        }
        else if (_usrCommand.startsWith("restart"))
        {
          L2LoginServer.getInstance().shutdown(true);
          _print.println("Bye Bye!");
          _print.flush();
          _cSocket.close();
        }
        else if (_usrCommand.equals("RedirectLogger")) { _redirectLogger = true;
        } else if ((!_usrCommand.equals("quit")) && 
          (_usrCommand.length() != 0))
        {
          _print.println("Invalid Command");
        }
        _print.print("");
        _print.flush();
      }
      if (!_cSocket.isClosed())
      {
        _print.println("Bye Bye!");
        _print.flush();
        _cSocket.close();
      }
      telnetOutput(1, "Connection from " + _cSocket.getInetAddress().getHostAddress() + " was closed by client.");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void printToTelnet(String msg)
  {
    synchronized (_print)
    {
      _print.println(msg);
      _print.flush();
    }
  }

  public boolean isRedirectLogger()
  {
    return _redirectLogger;
  }
}