package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2XmassTreeInstance extends L2NpcInstance
{
  private ScheduledFuture _aiTask;

  public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    _aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this), 3000L, 3000L);
  }

  public void deleteMe()
  {
    if (_aiTask != null) _aiTask.cancel(true);

    super.deleteMe();
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    return 900;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  class XmassAI
    implements Runnable
  {
    private L2XmassTreeInstance _caster;

    protected XmassAI(L2XmassTreeInstance caster)
    {
      _caster = caster;
    }

    public void run()
    {
      for (L2PcInstance player : getKnownList().getKnownPlayers().values())
      {
        int i = Rnd.nextInt(3);
        handleCast(player, 4262 + i);
        handleCast(player, 1044);
      }
    }

    private boolean handleCast(L2PcInstance player, int skillId)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

      if (player.getFirstEffect(skill) == null)
      {
        setTarget(player);
        doCast(skill);
        MagicSkillUser msu = new MagicSkillUser(_caster, player, skill.getId(), 1, skill.getHitTime(), 0);
        broadcastPacket(msu);

        return true;
      }

      return false;
    }
  }
}