package scripts.zone.type;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import scripts.zone.L2ZoneType;

public class L2TvtZone extends L2ZoneType
{
  public L2TvtZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(1, true);

    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      if (player.isGM())
      {
        player.sendAdmResultMessage("You entered TvT Zone.");
        return;
      }

      doBuff(player);

      if ((TvTEvent.isParticipating()) || (TvTEvent.isStarted()))
      {
        if (!TvTEvent.isPlayerParticipant(player.getName()))
          player.teleToLocation(83040, 149094, -3468, true);
      }
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(1, false);
  }

  private void doBuff(L2PcInstance player)
  {
    SkillTable.getInstance().getInfo(1204, 2).getEffects(player, player);
    SkillTable.getInstance().getInfo(1323, 1).getEffects(player, player);

    if (player.isMageClass())
      SkillTable.getInstance().getInfo(1085, 1).getEffects(player, player);
    else
      SkillTable.getInstance().getInfo(1086, 1).getEffects(player, player);
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}