package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
  private static final String _C__71_REQUESTPARTYMATCHDETAIL = "[C] 71 RequestPartyMatchDetail";
  private int _objectId;
  private int _number;
  private int _unk1;
  private int _unk2;
  private int _unk3;

  protected void readImpl()
  {
    _number = readD();
    _unk1 = readD();
    _unk1 = readD();
    _unk1 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    PartyMatchDetail details = new PartyMatchDetail(_number);
    sendPacket(details);
  }

  public String getType()
  {
    return "[C] 71 RequestPartyMatchDetail";
  }
}