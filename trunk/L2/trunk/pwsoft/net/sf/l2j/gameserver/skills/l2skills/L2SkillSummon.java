package net.sf.l2j.gameserver.skills.l2skills;

import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSummon extends L2Skill
{
  private int _npcId;
  private float _expPenalty;
  private boolean _isCubic;

  public L2SkillSummon(StatsSet set)
  {
    super(set);

    _npcId = set.getInteger("npcId", 0);
    _expPenalty = set.getFloat("expPenalty", 0.0F);
    _isCubic = set.getBool("isCubic", false);
  }

  public boolean checkCondition(L2Character activeChar)
  {
    if (activeChar.isPlayer())
    {
      L2PcInstance player = activeChar.getPlayer();

      if (player.noSummon()) {
        player.sendPacket(Static.SUMMON_WRONG_PLACE);
        return false;
      }

      if (_isCubic) {
        if (getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
        {
          return true;
        }
        int mastery = player.getSkillLevel(143);
        if (mastery < 0)
          mastery = 0;
        int count = player.getCubics().size();
        if (count > mastery) {
          player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0431\u043E\u043B\u044C\u0448\u0435 " + count + " \u043A\u0443\u0431\u0438\u043A\u043E\u0432");
          return false;
        }
      } else {
        if (player.inObserverMode())
          return false;
        if (player.getPet() != null)
        {
          player.sendPacket(Static.HAVE_PET);
          return false;
        }
      }
    }
    return super.checkCondition(activeChar, null, false);
  }

  public void useSkill(L2Character caster, FastList<L2Object> targets)
  {
    if ((caster.isAlikeDead()) || (!caster.isPlayer())) {
      return;
    }
    L2PcInstance activeChar = caster.getPlayer();
    if (_npcId == 0)
    {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString("Summon skill " + getId() + " not described yet"));
      return;
    }

    if (_isCubic)
    {
      FastList.Node n;
      if (targets.size() > 1)
      {
        n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
        {
          L2Object obj = (L2Object)n.getValue();

          if (!obj.isPlayer()) {
            continue;
          }
          L2PcInstance player = obj.getPlayer();
          int mastery = player.getSkillLevel(143);
          if (mastery < 0)
            mastery = 0;
          if ((mastery == 0) && (player.getCubics().size() > 0))
          {
            for (L2CubicInstance c : player.getCubics().values())
            {
              c.stopAction();
              c = null;
            }
            player.getCubics().clear();
          }

          if (player.getCubics().size() > mastery) {
            continue;
          }
          if (player.getCubics().containsKey(Integer.valueOf(_npcId))) {
            player.sendPacket(Static.HAVE_CUBIC);
          }
          else {
            player.addCubic(_npcId, getLevel());
            player.broadcastUserInfo();
          }
        }
      }
      else
      {
        int mastery = activeChar.getSkillLevel(143);
        if (mastery < 0)
          mastery = 0;
        if (activeChar.getCubics().size() > mastery)
        {
          activeChar.sendPacket(Static.CUBIC_SUMMONING_FAILED);
          return;
        }
        if (activeChar.getCubics().containsKey(Integer.valueOf(_npcId)))
        {
          activeChar.sendPacket(Static.HAVE_CUBIC);
          return;
        }
        activeChar.addCubic(_npcId, getLevel());
        activeChar.broadcastUserInfo();
      }
      return;
    }

    if ((activeChar.getPet() != null) || (activeChar.isMounted()) || (activeChar.isInsideDismountZone()))
    {
      return;
    }

    L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(_npcId);
    L2SummonInstance summon;
    L2SummonInstance summon;
    if (summonTemplate.type.equalsIgnoreCase("L2SiegeSummon"))
      summon = new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
    else {
      summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
    }
    summon.setName(summonTemplate.name);
    summon.setTitle(activeChar.getName());
    summon.setExpPenalty(_expPenalty);

    if (summon.getLevel() >= Experience.LEVEL.length)
      summon.getStat().setExp(Experience.LEVEL[(Experience.LEVEL.length - 1)]);
    else {
      summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
    }
    summon.setCurrentHp(summon.getMaxHp());
    summon.setCurrentMp(summon.getMaxMp());
    summon.setHeading(activeChar.getHeading());
    summon.setRunning();
    activeChar.setPet(summon);

    L2World.getInstance().storeObject(summon);
    summon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());

    summon.setFollowStatus(true);
    summon.setShowSummonAnimation(false);
  }
}