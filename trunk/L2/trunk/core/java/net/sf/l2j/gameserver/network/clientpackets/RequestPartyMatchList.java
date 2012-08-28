package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

@SuppressWarnings("unused")
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


	@Override
	protected void readImpl()
	{
		_status = readD();
        //TODO analyse values _unk1-unk5
		/*
        _unk1 = readD();
        _unk2 = readD();
        _unk3 = readD();
        _unk4 = readD();
        _unk5 = readS();
        */
	}

	@Override
	protected void runImpl()
	{
		if (_status == 1)
		{
			// window is open fill the list
			// actually the client should get automatic updates for the list
			// for now we only fill it once

			//Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
			//L2PcInstance[] allPlayers = players.toArray(new L2PcInstance[players.size()]);
			//L2PcInstance[] empty = new L2PcInstance[] { };
			//@SuppressWarnings("unused")
			//PartyMatchList matchList = new PartyMatchList(empty);
			//sendPacket(matchList);
		}
		else if (_status == 3)
		{
			// client does not need any more updates
			if (Config.DEBUG) _log.fine("PartyMatch window was closed.");
		}
		else
		{
			if (Config.DEBUG) _log.fine("party match status: "+_status);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__70_REQUESTPARTYMATCHLIST;
	}
}

/*package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import net.sf.l2j.gameserver.network.serverpackets.PartyRoomInfo;

public class RequestPartyMatchList extends L2GameClientPacket
{
  private static final String _C__70_REQUESTPARTYMATCHLIST = "[C] 70 RequestPartyMatchList";
  private static Logger _log = Logger.getLogger(RequestPartyMatchList.class.getName());
  private int _status;
  private int _MaxPaty;
  private int _LvlPatyMin;
  private int _LvlPartyMax;
  private int _unk4;
  private String _Title;

  protected void readImpl()
  {
    _status = readD();

    _MaxPaty = readD();
    _LvlPatyMin = readD();
    _LvlPartyMax = readD();
    _unk4 = readD();

    _Title = readS();
  }

  protected void runImpl()
  {
    if (_MaxPaty < 0) _MaxPaty = 0;
    if (_MaxPaty > 12) _MaxPaty = 12;
    if (_LvlPatyMin < 1) _LvlPatyMin = 1;
    if (_LvlPatyMin > 80) _LvlPatyMin = 80;
    if (_LvlPartyMax < 1) _LvlPartyMax = 1;
    if (_LvlPartyMax > 80) _LvlPartyMax = 80;
    System.out.println("Status " + _status);
    if (_status >= 0)
    {
      L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
      if (activeChar == null) return;

      int number = activeChar.SavePartyMatch(activeChar, _MaxPaty, _LvlPatyMin, _LvlPartyMax, 1, _Title, 1);
      activeChar.sendPacket(new PartyRoomInfo(activeChar, number, _MaxPaty, _LvlPatyMin, _LvlPartyMax, _Title, activeChar.getTownZone(activeChar)));
      activeChar.sendPacket(new ExAskJoinPartyRoom(activeChar, 1, 1, 1));
      activeChar.SetPartyFind(1);
      activeChar.broadcastUserInfo();
    }
    else if (_status == 3)
    {
      if (Config.DEBUG) _log.fine("PartyMatch window was closed.");

    }
    else if (Config.DEBUG) { _log.fine("party match status: " + _status);
    }
  }

  public String getType()
  {
    return "[C] 70 RequestPartyMatchList";
  }
}*/
