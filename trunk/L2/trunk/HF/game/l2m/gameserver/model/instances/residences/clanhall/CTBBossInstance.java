package l2m.gameserver.model.instances.residences.clanhall;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import l2m.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2m.gameserver.model.entity.events.objects.CTBTeamObject;
import l2m.gameserver.model.instances.MonsterInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.npc.NpcTemplate;

public abstract class CTBBossInstance extends MonsterInstance
{
  private static final long serialVersionUID = 1L;
  public static final Skill SKILL = SkillTable.getInstance().getInfo(5456, 1);
  private CTBTeamObject _matchTeamObject;

  public CTBBossInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    setHasChatWindow(false);
  }

  public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
  {
    if ((attacker.getLevel() > getLevel() + 8) && (attacker.getEffectList().getEffectsCountForSkill(SKILL.getId()) == 0))
    {
      doCast(SKILL, attacker, false);
      return;
    }

    super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
  }

  public boolean isAttackable(Creature attacker)
  {
    CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
    if ((clan != null) && (attacker.isPlayable()))
    {
      Player player = attacker.getPlayer();
      if (player.getClan() == clan.getClan())
        return false;
    }
    return true;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return isAttackable(attacker);
  }

  public void onDeath(Creature killer)
  {
    ClanHallTeamBattleEvent event = (ClanHallTeamBattleEvent)getEvent(ClanHallTeamBattleEvent.class);
    event.processStep(_matchTeamObject);

    super.onDeath(killer);
  }

  public String getTitle()
  {
    CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
    return clan == null ? "" : clan.getClan().getName();
  }

  public void setMatchTeamObject(CTBTeamObject matchTeamObject)
  {
    _matchTeamObject = matchTeamObject;
  }
}