package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchList;

public final class RequestPartyMatchConfig extends L2GameClientPacket
{
  @SuppressWarnings("unused")
private static final String _C__6F_REQUESTPARTYMATCHCONFIG = "[C] 6F RequestPartyMatchConfig";
  private int _automaticRegistration;
  private int _sortZone;
  private int _sortLvl;

  protected void readImpl()
  {
    this._automaticRegistration = readD();
    this._sortZone = readD();
    this._sortLvl = readD();
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null) {
      return;
    }

    if (this._automaticRegistration == 1)
    {
      PartyMatchList matchList = new PartyMatchList(this._sortZone, this._sortLvl);
      sendPacket(matchList);
    }
  }

  public String getType()
  {
    return "[C] 6F RequestPartyMatchConfig";
  }
}