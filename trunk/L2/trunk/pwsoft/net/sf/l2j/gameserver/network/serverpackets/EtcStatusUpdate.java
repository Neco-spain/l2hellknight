package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;

public class EtcStatusUpdate extends L2GameServerPacket
{
  private int IncreasedForce;
  private int WeightPenalty;
  private int MessageRefusal;
  private int DangerArea;
  private int _expertisePenalty;
  private int CharmOfCourage;
  private int DeathPenaltyLevel;
  private boolean can_writeImpl = false;
  private L2PcInstance _player;
  private EffectCharge _effect;

  public EtcStatusUpdate(L2PcInstance player)
  {
    if (player == null)
      return;
    _player = player;
  }

  public final void runImpl()
  {
    IncreasedForce = _player.getCharges();
    WeightPenalty = _player.getWeightPenalty();
    MessageRefusal = ((_player.getMessageRefusal()) || (_player.isChatBanned()) ? 1 : 0);
    DangerArea = (_player.isInDangerArea() ? 1 : 0);
    _expertisePenalty = _player.getExpertisePenalty();
    CharmOfCourage = (_player.getCharmOfCourage() ? 1 : 0);
    DeathPenaltyLevel = _player.getDeathPenaltyBuffLevel();
    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    writeC(243);
    writeD(IncreasedForce);
    writeD(WeightPenalty);
    writeD(MessageRefusal);
    writeD(DangerArea);
    writeD(_expertisePenalty);
    writeD(CharmOfCourage);
    writeD(DeathPenaltyLevel);
  }
}