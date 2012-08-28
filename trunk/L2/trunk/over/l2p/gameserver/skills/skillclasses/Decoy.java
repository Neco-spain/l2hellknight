package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.instances.DecoyInstance;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public class Decoy extends Skill
{
  private final int _npcId;
  private final int _lifeTime;

  public Decoy(StatsSet set)
  {
    super(set);

    _npcId = set.getInteger("npcId", 0);
    _lifeTime = (set.getInteger("lifeTime", 1200) * 1000);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((activeChar.isAlikeDead()) || (!activeChar.isPlayer()) || (activeChar != target)) {
      return false;
    }
    if (_npcId <= 0) {
      return false;
    }
    if (activeChar.isInObserverMode()) {
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature caster, List<Creature> targets)
  {
    Player activeChar = caster.getPlayer();

    NpcTemplate DecoyTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
    DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, _lifeTime);

    decoy.setCurrentHp(decoy.getMaxHp(), false);
    decoy.setCurrentMp(decoy.getMaxMp());
    decoy.setHeading(activeChar.getHeading());
    decoy.setReflection(activeChar.getReflection());

    activeChar.setDecoy(decoy);

    decoy.spawnMe(Location.findAroundPosition(activeChar, 50, 70));
  }
}