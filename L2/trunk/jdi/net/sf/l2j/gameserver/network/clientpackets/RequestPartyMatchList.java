package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;

public class RequestPartyMatchList extends L2GameClientPacket
{
  private static final String _C__70_REQUESTPARTYMATCHLIST = "[C] 70 RequestPartyMatchList";
  private static Logger _log = Logger.getLogger(RequestPartyMatchList.class.getName());
  private int _status;
  private int _unk1;
  private int _unk2;
  private int _unk3;
  private int _unk4;
  private String _unk5;

  protected void readImpl()
  {
    _status = readD();
  }

  protected void runImpl()
  {
    if (_status != 1)
    {
      if (_status == 3)
      {
        if (Config.DEBUG) _log.fine("PartyMatch window was closed.");

      }
      else if (Config.DEBUG) _log.fine("party match status: " + _status);
    }
  }

  public String getType()
  {
    return "[C] 70 RequestPartyMatchList";
  }
}