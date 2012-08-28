package net.sf.l2j.gameserver.geoeditorcon;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import org.mmocore.network.MMOConnection;

public class GeoEditorThread extends Thread
{
  private static Logger _log = Logger.getLogger(GeoEditorThread.class.getName());

  private boolean _working = false;

  private int _mode = 0;

  private int _sendDelay = 1000;
  private Socket _geSocket;
  private OutputStream _out;
  private FastList<L2PcInstance> _gms;

  public GeoEditorThread(Socket ge)
  {
    _geSocket = ge;
    _working = true;
    _gms = new FastList();
  }

  public void interrupt()
  {
    try
    {
      _geSocket.close();
    }
    catch (Exception e) {
    }
    super.interrupt();
  }

  public void run()
  {
    try
    {
      _out = _geSocket.getOutputStream();
      int timer = 0;

      while (_working)
      {
        if (!isConnected()) {
          _working = false;
        }
        if ((_mode == 2) && (timer > _sendDelay))
        {
          for (L2PcInstance gm : _gms)
            if (!gm.getClient().getConnection().isClosed())
              sendGmPosition(gm);
            else
              _gms.remove(gm);
          timer = 0;
        }

        try
        {
          sleep(100L);
          if (_mode == 2)
            timer += 100;
        }
        catch (Exception e) {
        }
      }
    }
    catch (SocketException e) {
      _log.warning("GeoEditor disconnected. " + e.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally
    {
      try {
        _geSocket.close();
      }
      catch (Exception e) {
      }
      _working = false;
    }
  }

  public void sendGmPosition(int gx, int gy, short z)
  {
    if (!isConnected())
      return;
    try
    {
      synchronized (_out)
      {
        writeC(11);
        writeC(1);
        writeD(gx);
        writeD(gy);
        writeH(z);
        _out.flush();
      }
    }
    catch (SocketException e) {
      _log.warning("GeoEditor disconnected. " + e.getMessage());
      _working = false;
    }
    catch (Exception e) {
      e.printStackTrace();
      try
      {
        _geSocket.close();
      }
      catch (Exception ex) {
      }
      _working = false;
    }
  }

  public void sendGmPosition(L2PcInstance _gm)
  {
    sendGmPosition(_gm.getX(), _gm.getY(), (short)_gm.getZ());
  }

  public void sendPing()
  {
    if (!isConnected())
      return;
    try
    {
      synchronized (_out)
      {
        writeC(1);
        writeC(2);

        _out.flush();
      }
    }
    catch (SocketException e) {
      _log.warning("GeoEditor disconnected. " + e.getMessage());
      _working = false;
    }
    catch (Exception e) {
      e.printStackTrace();
      try
      {
        _geSocket.close();
      }
      catch (Exception ex) {
      }
      _working = false;
    }
  }

  private void writeD(int value) throws IOException
  {
    _out.write(value & 0xFF);
    _out.write(value >> 8 & 0xFF);
    _out.write(value >> 16 & 0xFF);
    _out.write(value >> 24 & 0xFF);
  }

  private void writeH(int value) throws IOException
  {
    _out.write(value & 0xFF);
    _out.write(value >> 8 & 0xFF);
  }

  private void writeC(int value) throws IOException
  {
    _out.write(value & 0xFF);
  }

  public void setMode(int value)
  {
    _mode = value;
  }

  public void setTimer(int value)
  {
    if (value < 500)
      _sendDelay = 500;
    else if (value > 60000)
      _sendDelay = 60000;
    else
      _sendDelay = value;
  }

  public void addGM(L2PcInstance gm)
  {
    if (!_gms.contains(gm))
      _gms.add(gm);
  }

  public void removeGM(L2PcInstance gm)
  {
    if (_gms.contains(gm))
      _gms.remove(gm);
  }

  public boolean isSend(L2PcInstance gm)
  {
    return (_mode == 1) && (_gms.contains(gm));
  }

  private boolean isConnected()
  {
    return (_geSocket.isConnected()) && (!_geSocket.isClosed());
  }

  public boolean isWorking()
  {
    sendPing();
    return _working;
  }

  public int getMode()
  {
    return _mode;
  }
}