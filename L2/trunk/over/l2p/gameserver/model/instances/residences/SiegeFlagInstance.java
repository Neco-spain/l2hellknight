package l2p.gameserver.model.instances.residences;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.npc.NpcTemplate;

public class SiegeFlagInstance extends NpcInstance
{
  private SiegeClanObject _owner;
  private long _lastAnnouncedAttackedTime = 0L;

  public SiegeFlagInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    setHasChatWindow(false);
  }

  public String getName()
  {
    return _owner.getClan().getName();
  }

  public Clan getClan()
  {
    return _owner.getClan();
  }

  public String getTitle()
  {
    return "";
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    Player player = attacker.getPlayer();
    if ((player == null) || (isInvul()))
      return false;
    Clan clan = player.getClan();
    return (clan == null) || (_owner.getClan() != clan);
  }

  public boolean isAttackable(Creature attacker)
  {
    return true;
  }

  protected void onDeath(Creature killer)
  {
    _owner.setFlag(null);
    super.onDeath(killer);
  }

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    if (System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000L)
    {
      _lastAnnouncedAttackedTime = System.currentTimeMillis();
      _owner.getClan().broadcastToOnlineMembers(new IStaticPacket[] { SystemMsg.YOUR_BASE_IS_BEING_ATTACKED });
    }

    super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public boolean isInvul()
  {
    return _isInvul;
  }

  public boolean isFearImmune()
  {
    return true;
  }

  public boolean isParalyzeImmune()
  {
    return true;
  }

  public boolean isLethalImmune()
  {
    return true;
  }

  public boolean isHealBlocked()
  {
    return true;
  }

  public boolean isEffectImmune()
  {
    return true;
  }

  public void setClan(SiegeClanObject owner)
  {
    _owner = owner;
  }
}