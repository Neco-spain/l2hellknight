package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CabaleBufferInstance extends L2NpcInstance
{
  private ScheduledFuture<?> _aiTask;

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }

    player.sendPacket(new ActionFailed());
  }

  public L2CabaleBufferInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);

    if (_aiTask != null) {
      _aiTask.cancel(true);
    }
    _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CabalaAI(this), 3000L, 3000L);
  }

  public void deleteMe()
  {
    if (_aiTask != null)
    {
      _aiTask.cancel(true);
      _aiTask = null;
    }

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

  private class CabalaAI
    implements Runnable
  {
    private L2CabaleBufferInstance _caster;

    protected CabalaAI(L2CabaleBufferInstance caster)
    {
      _caster = caster;
    }

    public void run()
    {
      boolean isBuffAWinner = false;
      boolean isBuffALoser = false;

      int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
      int losingCabal = 0;

      if (winningCabal == 2)
        losingCabal = 1;
      else if (winningCabal == 1) {
        losingCabal = 2;
      }

      for (L2PcInstance player : getKnownList().getKnownPlayers().values())
      {
        int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);

        if ((playerCabal == winningCabal) && (playerCabal != 0) && (_caster.getNpcId() == 31094))
        {
          if (!player.isMageClass())
          {
            if (handleCast(player, 4364))
            {
              isBuffAWinner = true;
              continue;
            }

          }
          else if (handleCast(player, 4365))
          {
            isBuffAWinner = true;
            continue;
          }

        }
        else if ((playerCabal == losingCabal) && (playerCabal != 0) && (_caster.getNpcId() == 31093))
        {
          if (!player.isMageClass())
          {
            if (handleCast(player, 4361))
            {
              isBuffALoser = true;
              continue;
            }

          }
          else if (handleCast(player, 4362))
          {
            isBuffALoser = true;
            continue;
          }

        }

        if ((isBuffAWinner) && (isBuffALoser))
          break;
      }
    }

    private boolean handleCast(L2PcInstance player, int skillId)
    {
      int skillLevel = player.getLevel() > 40 ? 1 : 2;

      if ((player.isDead()) || (!player.isVisible()) || (!isInsideRadius(player, getDistanceToWatchObject(player), false, false))) {
        return false;
      }
      L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
      if (player.getFirstEffect(skill) == null)
      {
        skill.getEffects(_caster, player);
        broadcastPacket(new MagicSkillUser(_caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
        sm.addSkillName(skillId);
        player.sendPacket(sm);
        return true;
      }

      return false;
    }
  }
}