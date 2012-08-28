package l2p.gameserver.serverpackets;

import l2p.gameserver.model.DeathPenalty;
import l2p.gameserver.model.Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
  private int IncreasedForce;
  private int WeightPenalty;
  private int MessageRefusal;
  private int DangerArea;
  private int armorExpertisePenalty;
  private int weaponExpertisePenalty;
  private int CharmOfCourage;
  private int DeathPenaltyLevel;
  private int ConsumedSouls;

  public EtcStatusUpdate(Player player)
  {
    IncreasedForce = player.getIncreasedForce();
    WeightPenalty = player.getWeightPenalty();
    MessageRefusal = ((player.getMessageRefusal()) || (player.getNoChannel() != 0L) || (player.isBlockAll()) ? 1 : 0);
    DangerArea = (player.isInDangerArea() ? 1 : 0);
    armorExpertisePenalty = player.getArmorsExpertisePenalty();
    weaponExpertisePenalty = player.getWeaponsExpertisePenalty();
    CharmOfCourage = (player.isCharmOfCourage() ? 1 : 0);
    DeathPenaltyLevel = (player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel());
    ConsumedSouls = player.getConsumedSouls();
  }

  protected final void writeImpl()
  {
    writeC(249);
    writeD(IncreasedForce);
    writeD(WeightPenalty);
    writeD(MessageRefusal);
    writeD(DangerArea);
    writeD(weaponExpertisePenalty);
    writeD(armorExpertisePenalty);
    writeD(CharmOfCourage);
    writeD(DeathPenaltyLevel);
    writeD(ConsumedSouls);
  }
}