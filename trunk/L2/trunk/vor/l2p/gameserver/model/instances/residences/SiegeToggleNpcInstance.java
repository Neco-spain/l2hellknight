package l2p.gameserver.model.instances.residences;

import java.util.List;
import java.util.Set;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.templates.npc.NpcTemplate;

public abstract class SiegeToggleNpcInstance extends NpcInstance
{
  private NpcInstance _fakeInstance;
  private int _maxHp;

  public SiegeToggleNpcInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);

    setHasChatWindow(false);
  }

  public void setMaxHp(int maxHp)
  {
    _maxHp = maxHp;
  }

  public void setZoneList(Set<String> set)
  {
  }

  public void register(Spawner spawn)
  {
  }

  public void initFake(int fakeNpcId)
  {
    _fakeInstance = NpcHolder.getInstance().getTemplate(fakeNpcId).getNewInstance();
    _fakeInstance.setCurrentHpMp(1.0D, _fakeInstance.getMaxMp());
    _fakeInstance.setHasChatWindow(false);
  }

  public abstract void onDeathImpl(Creature paramCreature);

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    setCurrentHp(Math.max(getCurrentHp() - damage, 0.0D), false);

    if (getCurrentHp() < 0.5D)
    {
      doDie(attacker);

      onDeathImpl(attacker);

      decayMe();

      _fakeInstance.spawnMe(getLoc());
    }
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    if (attacker == null)
      return false;
    Player player = attacker.getPlayer();
    if (player == null) {
      return false;
    }
    SiegeEvent siegeEvent = (SiegeEvent)getEvent(SiegeEvent.class);
    if ((siegeEvent == null) || (!siegeEvent.isInProgress()))
      return false;
    if (siegeEvent.getSiegeClan("defenders", player.getClan()) != null) {
      return false;
    }
    return !siegeEvent.getObjects("defender_players").contains(Integer.valueOf(player.getObjectId()));
  }

  public boolean isAttackable(Creature attacker)
  {
    return isAutoAttackable(attacker);
  }

  public boolean isInvul()
  {
    return false;
  }

  public boolean hasRandomAnimation()
  {
    return false;
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

  public void decayFake()
  {
    _fakeInstance.decayMe();
  }

  public int getMaxHp()
  {
    return _maxHp;
  }

  protected void onDecay()
  {
    decayMe();

    _spawnAnimation = 2;
  }

  public Clan getClan()
  {
    return null;
  }
}