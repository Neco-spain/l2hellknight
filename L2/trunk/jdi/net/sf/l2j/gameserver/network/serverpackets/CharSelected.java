package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;

public class CharSelected extends L2GameServerPacket
{
  private static final String _S__21_CHARSELECTED = "[S] 15 CharSelected";
  private L2PcInstance _activeChar;
  private int _sessionId;

  public CharSelected(L2PcInstance cha, int sessionId)
  {
    _activeChar = cha;
    _sessionId = sessionId;
  }

  protected final void writeImpl()
  {
    writeC(21);

    writeS(_activeChar.getName());
    writeD(_activeChar.getCharId());
    writeS(_activeChar.getTitle());
    writeD(_sessionId);
    writeD(_activeChar.getClanId());
    writeD(0);
    writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
    writeD(_activeChar.getRace().ordinal());
    writeD(_activeChar.getClassId().getId());
    writeD(1);
    writeD(_activeChar.getX());
    writeD(_activeChar.getY());
    writeD(_activeChar.getZ());

    writeF(_activeChar.getCurrentHp());
    writeF(_activeChar.getCurrentMp());
    writeD(_activeChar.getSp());
    writeQ(_activeChar.getExp());
    writeD(_activeChar.getLevel());
    writeD(_activeChar.getKarma());
    writeD(0);
    writeD(_activeChar.getINT());
    writeD(_activeChar.getSTR());
    writeD(_activeChar.getCON());
    writeD(_activeChar.getMEN());
    writeD(_activeChar.getDEX());
    writeD(_activeChar.getWIT());
    for (int i = 0; i < 30; i++)
    {
      writeD(0);
    }

    writeD(0);
    writeD(0);

    writeD(GameTimeController.getInstance().getGameTime());

    writeD(0);

    writeD(0);

    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);

    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
  }

  public String getType()
  {
    return "[S] 15 CharSelected";
  }
}