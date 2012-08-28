package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinSiege extends L2GameClientPacket
{
  private static final String _C__A4_RequestJoinSiege = "[C] a4 RequestJoinSiege";
  private int _castleId;
  private int _isAttacker;
  private int _isJoining;

  protected void readImpl()
  {
    _castleId = readD();
    _isAttacker = readD();
    _isJoining = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;
    if (!activeChar.isClanLeader()) return;

    Castle castle = CastleManager.getInstance().getCastleById(_castleId);
    Fort fort = FortManager.getInstance().getFortById(_castleId);
    if (castle == null)
    {
      if (fort == null) return;
    }

    if (_isJoining == 1)
    {
      if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
        return;
      }
      if (_isAttacker == 1)
      {
        if (castle != null) castle.getSiege().registerAttacker(activeChar); else {
          fort.getSiege().registerAttacker(activeChar, true);
        }

      }
      else if (castle != null) castle.getSiege().registerDefender(activeChar); else {
        fort.getSiege().registerDefender(activeChar);
      }

    }
    else if (castle != null) {
      castle.getSiege().removeSiegeClan(activeChar); } else {
      fort.getSiege().removeSiegeClan(activeChar);
    }
    if (castle != null) castle.getSiege().listRegisterClan(activeChar); else
      fort.getSiege().listRegisterClan(activeChar);
  }

  public String getType()
  {
    return "[C] a4 RequestJoinSiege";
  }
}