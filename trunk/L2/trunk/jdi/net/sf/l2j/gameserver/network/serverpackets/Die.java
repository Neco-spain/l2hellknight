package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;

public class Die extends L2GameServerPacket
{
  private static final String _S__0B_DIE = "[S] 06 Die";
  private int _charObjId;
  private boolean _fake;
  private boolean _sweepable;
  private int _access;
  private L2Clan _clan;
  private static final int REQUIRED_LEVEL = Config.GM_FIXED;
  L2Character _activeChar;

  public Die(L2Character cha)
  {
    _activeChar = cha;
    if ((cha instanceof L2PcInstance)) {
      L2PcInstance player = (L2PcInstance)cha;
      _access = player.getAccessLevel();
      _clan = player.getClan();
    }

    _charObjId = cha.getObjectId();
    _fake = (!cha.isDead());
    if ((cha instanceof L2Attackable))
      _sweepable = ((L2Attackable)cha).isSweepActive();
  }

  protected final void writeImpl()
  {
    if (_fake) {
      return;
    }
    writeC(6);

    writeD(_charObjId);

    writeD(1);
    if (_clan != null)
    {
      L2SiegeClan siegeClan = null;
      Boolean isInDefense = Boolean.valueOf(false);
      Castle castle = CastleManager.getInstance().getCastle(_activeChar);
      if ((castle != null) && (castle.getSiege().getIsInProgress()))
      {
        siegeClan = castle.getSiege().getAttackerClan(_clan);
        if ((siegeClan == null) && (castle.getSiege().checkIsDefender(_clan))) {
          isInDefense = Boolean.valueOf(true);
        }
      }

      writeD(_clan.getHasHideout() > 0 ? 1 : 0);
      writeD((_clan.getHasCastle() > 0) || (isInDefense.booleanValue()) ? 1 : 0);

      writeD((siegeClan != null) && (!isInDefense.booleanValue()) && (siegeClan.getFlag().size() > 0) ? 1 : 0);
    }
    else
    {
      writeD(0);
      writeD(0);
      writeD(0);
    }

    writeD(_sweepable ? 1 : 0);
    writeD(_access >= REQUIRED_LEVEL ? 1 : 0);
  }

  public String getType()
  {
    return "[S] 06 Die";
  }
}