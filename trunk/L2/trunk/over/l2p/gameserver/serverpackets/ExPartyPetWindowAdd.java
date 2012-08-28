package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.templates.npc.NpcTemplate;

public class ExPartyPetWindowAdd extends L2GameServerPacket
{
  private final int ownerId;
  private final int npcId;
  private final int type;
  private final int curHp;
  private final int maxHp;
  private final int curMp;
  private final int maxMp;
  private final int level;
  private final int summonId;
  private final String name;

  public ExPartyPetWindowAdd(Summon summon)
  {
    summonId = summon.getObjectId();
    ownerId = summon.getPlayer().getObjectId();
    npcId = (summon.getTemplate().npcId + 1000000);
    type = summon.getSummonType();
    name = summon.getName();
    curHp = (int)summon.getCurrentHp();
    maxHp = summon.getMaxHp();
    curMp = (int)summon.getCurrentMp();
    maxMp = summon.getMaxMp();
    level = summon.getLevel();
  }

  protected final void writeImpl()
  {
    writeEx(24);
    writeD(summonId);
    writeD(npcId);
    writeD(type);
    writeD(ownerId);
    writeS(name);
    writeD(curHp);
    writeD(maxHp);
    writeD(curMp);
    writeD(maxMp);
    writeD(level);
  }
}