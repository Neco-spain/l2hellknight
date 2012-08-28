package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;

public class EtcStatusUpdate extends L2GameServerPacket
{
  private static final String _S__F3_ETCSTATUSUPDATE = "[S] F3 EtcStatusUpdate";
  private L2PcInstance _activeChar;
  private EffectCharge _effect;

  public EtcStatusUpdate(L2PcInstance activeChar)
  {
    _activeChar = activeChar;
    _effect = ((EffectCharge)_activeChar.getFirstEffect(L2Effect.EffectType.CHARGE));
  }

  protected void writeImpl()
  {
    writeC(243);
    if (_effect != null)
      writeD(_effect.getLevel());
    else
      writeD(0);
    writeD(_activeChar.getWeightPenalty());
    writeD((_activeChar.getMessageRefusal()) || (_activeChar.isChatBanned()) ? 1 : 0);
    writeD(_activeChar.isInDangerArea() ? 1 : 0);
    writeD(Math.min(_activeChar.getExpertisePenalty(), 1));
    writeD(_activeChar.getCharmOfCourage() ? 1 : 0);
    writeD(_activeChar.getDeathPenaltyBuffLevel());
  }

  public String getType()
  {
    return "[S] F3 EtcStatusUpdate";
  }
}