package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.ai.Zaken;
import scripts.skills.ISkillHandler;

public class ZakenTeleports
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.ZAKENTPSELF, L2Skill.SkillType.ZAKENTPPLAYER };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    Location loc = null;
    L2Skill.SkillType type = skill.getSkillType();
    FastList.Node n;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[type.ordinal()])
    {
    case 1:
      loc = getRandomLoc();
      activeChar.teleToLocation(loc.x + Rnd.get(300), loc.y + Rnd.get(300), loc.z, false);
      ((Zaken)activeChar).setTeleported(true);
      break;
    case 2:
      n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
      {
        L2Object obj = (L2Object)n.getValue();

        if ((obj == null) || (!obj.isPlayer())) {
          continue;
        }
        L2PcInstance player = (L2PcInstance)obj;
        if (player.isDead()) {
          continue;
        }
        loc = getRandomLoc();
        player.teleToLocation(loc.x + Rnd.get(300), loc.y + Rnd.get(300), loc.z, false);
      }
    }
  }

  private Location getRandomLoc()
  {
    return CustomServerData.getInstance().getZakenPoint();
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}