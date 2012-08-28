package l2m.gameserver.model.entity.residence;

import java.util.Calendar;
import l2p.commons.dao.JdbcEntityState;
import l2m.gameserver.data.dao.DominionDAO;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

public class Dominion extends Residence
{
  private static final long serialVersionUID = 1L;
  private IntSet _flags = new TreeIntSet();
  private Castle _castle;
  private int _lordObjectId;

  public Dominion(StatsSet set)
  {
    super(set);
  }

  public void init()
  {
    initEvent();

    _castle = ((Castle)ResidenceHolder.getInstance().getResidence(Castle.class, getId() - 80));
    _castle.setDominion(this);

    loadData();

    _siegeDate.setTimeInMillis(0L);
    if (getOwner() != null)
    {
      DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
      runnerEvent.registerDominion(this);
    }
  }

  public void rewardSkills()
  {
    Clan owner = getOwner();
    if (owner != null)
    {
      if (!_flags.contains(getId())) {
        return;
      }
      for (int dominionId : _flags.toArray())
      {
        Dominion dominion = (Dominion)ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
        for (Skill skill : dominion.getSkills())
        {
          owner.addSkill(skill, false);
          owner.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill) });
        }
      }
    }
  }

  public void removeSkills()
  {
    Clan owner = getOwner();
    if (owner != null)
    {
      for (int dominionId : _flags.toArray())
      {
        Dominion dominion = (Dominion)ResidenceHolder.getInstance().getResidence(Dominion.class, dominionId);
        for (Skill skill : dominion.getSkills())
          owner.removeSkill(skill.getId());
      }
    }
  }

  public void addFlag(int dominionId)
  {
    _flags.add(dominionId);
  }

  public void removeFlag(int dominionId)
  {
    _flags.remove(dominionId);
  }

  public int[] getFlags()
  {
    return _flags.toArray();
  }

  public ResidenceType getType()
  {
    return ResidenceType.Dominion;
  }

  protected void loadData()
  {
    DominionDAO.getInstance().select(this);
  }

  public void changeOwner(Clan clan)
  {
    int newLordObjectId;
    SystemMessage2 message;
    if (clan == null)
    {
      int newLordObjectId;
      if (_lordObjectId > 0)
        newLordObjectId = 0;
      else
        return;
    }
    else
    {
      newLordObjectId = clan.getLeaderId();

      message = (SystemMessage2)((SystemMessage2)((SystemMessage2)new SystemMessage2(SystemMsg.CLAN_LORD_C2_WHO_LEADS_CLAN_S1_HAS_BEEN_DECLARED_THE_LORD_OF_THE_S3_TERRITORY).addName(clan.getLeader().getPlayer())).addString(clan.getName())).addResidenceName(getCastle());
      for (Player player : GameObjectsStorage.getAllPlayersForIterate()) {
        player.sendPacket(message);
      }
    }
    _lordObjectId = newLordObjectId;

    setJdbcState(JdbcEntityState.UPDATED);
    update();

    for (NpcInstance npc : GameObjectsStorage.getAllNpcsForIterate())
      if (npc.getDominion() == this)
        npc.broadcastCharInfoImpl();
  }

  public int getLordObjectId()
  {
    return _lordObjectId;
  }

  public Clan getOwner()
  {
    return _castle.getOwner();
  }

  public int getOwnerId()
  {
    return _castle.getOwnerId();
  }

  public Castle getCastle()
  {
    return _castle;
  }

  public void update()
  {
    DominionDAO.getInstance().update(this);
  }

  public void setLordObjectId(int lordObjectId)
  {
    _lordObjectId = lordObjectId;
  }
}