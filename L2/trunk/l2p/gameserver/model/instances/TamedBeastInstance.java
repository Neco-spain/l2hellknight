package l2p.gameserver.model.instances;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.World;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.NpcInfo;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;

public final class TamedBeastInstance extends FeedableBeastInstance
{
  public static final long serialVersionUID = 1L;
  private static final int MAX_DISTANCE_FROM_OWNER = 2000;
  private static final int MAX_DISTANCE_FOR_BUFF = 200;
  private static final int MAX_DURATION = 1200000;
  private static final int DURATION_CHECK_INTERVAL = 60000;
  private static final int DURATION_INCREASE_INTERVAL = 20000;
  private HardReference<Player> _playerRef = HardReferences.emptyRef();
  private int _foodSkillId;
  private int _remainingTime = 1200000;
  private Future<?> _durationCheckTask = null;

  private List<Skill> _skills = new ArrayList();

  private static final Map.Entry<NpcString, int[]>[] TAMED_DATA = new Map.Entry[6];

  public TamedBeastInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    _hasRandomWalk = false;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  public void onAction(Player player, boolean dontMove)
  {
    player.setObjectTarget(this);
  }

  private void onReceiveFood()
  {
    _remainingTime += 20000;
    if (_remainingTime > 1200000)
      _remainingTime = 1200000;
  }

  public int getRemainingTime()
  {
    return _remainingTime;
  }

  public void setRemainingTime(int duration)
  {
    _remainingTime = duration;
  }

  public int getFoodType()
  {
    return _foodSkillId;
  }

  public void setTameType()
  {
    Map.Entry type = TAMED_DATA[l2p.commons.util.Rnd.get(TAMED_DATA.length)];

    setNameNpcString((NpcString)type.getKey());
    setName("#" + getNameNpcStringByNpcId().getId());

    for (int skillId : (int[])type.getValue())
    {
      Skill sk = SkillTable.getInstance().getInfo(skillId, 1);
      if (sk != null)
        _skills.add(sk);
    }
  }

  public NpcString getNameNpcStringByNpcId()
  {
    switch (getNpcId())
    {
    case 18869:
      return NpcString.ALPEN_KOOKABURRA;
    case 18870:
      return NpcString.ALPEN_COUGAR;
    case 18871:
      return NpcString.ALPEN_BUFFALO;
    case 18872:
      return NpcString.ALPEN_GRENDEL;
    }
    return NpcString.NONE;
  }

  public void buffOwner()
  {
    if (!isInRange(getPlayer(), 200L))
    {
      setFollowTarget(getPlayer());
      getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getPlayer(), Integer.valueOf(Config.FOLLOW_RANGE));
      return;
    }

    int delay = 0;
    for (Skill skill : _skills)
    {
      ThreadPoolManager.getInstance().schedule(new Buff(this, getPlayer(), skill), delay);
      delay = delay + skill.getHitTime() + 500;
    }
  }

  public void setFoodType(int foodItemId)
  {
    if (foodItemId > 0)
    {
      _foodSkillId = foodItemId;

      if (_durationCheckTask != null)
        _durationCheckTask.cancel(false);
      _durationCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckDuration(this), 60000L, 60000L);
    }
  }

  protected void onDeath(Creature killer)
  {
    super.onDeath(killer);
    if (_durationCheckTask != null)
    {
      _durationCheckTask.cancel(false);
      _durationCheckTask = null;
    }

    Player owner = getPlayer();
    if (owner != null) {
      owner.removeTrainedBeast(getObjectId());
    }
    _foodSkillId = 0;
    _remainingTime = 0;
  }

  public Player getPlayer()
  {
    return (Player)_playerRef.get();
  }

  public void setOwner(Player owner)
  {
    _playerRef = (owner == null ? HardReferences.emptyRef() : owner.getRef());
    if (owner != null)
    {
      setTitle(owner.getName());
      owner.addTrainedBeast(this);

      for (Player player : World.getAroundPlayers(this)) {
        player.sendPacket(new NpcInfo(this, player));
      }

      setFollowTarget(getPlayer());
      getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Integer.valueOf(Config.FOLLOW_RANGE));
    }
    else {
      doDespawn();
    }
  }

  public void despawnWithDelay(int delay) {
    ThreadPoolManager.getInstance().schedule(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        doDespawn();
      }
    }
    , delay);
  }

  public void doDespawn()
  {
    stopMove();

    if (_durationCheckTask != null)
    {
      _durationCheckTask.cancel(false);
      _durationCheckTask = null;
    }

    Player owner = getPlayer();
    if (owner != null) {
      owner.removeTrainedBeast(getObjectId());
    }
    setTarget(null);
    _foodSkillId = 0;
    _remainingTime = 0;

    onDecay();
  }

  static
  {
    TAMED_DATA[0] = new AbstractMap.SimpleImmutableEntry(NpcString.RECKLESS_S1, new int[] { 6671 });
    TAMED_DATA[1] = new AbstractMap.SimpleImmutableEntry(NpcString.S1_OF_BALANCE, new int[] { 6431, 6666 });
    TAMED_DATA[2] = new AbstractMap.SimpleImmutableEntry(NpcString.SHARP_S1, new int[] { 6432, 6668 });
    TAMED_DATA[3] = new AbstractMap.SimpleImmutableEntry(NpcString.USEFUL_S1, new int[] { 6433, 6670 });
    TAMED_DATA[4] = new AbstractMap.SimpleImmutableEntry(NpcString.S1_OF_BLESSING, new int[] { 6669, 6672 });
    TAMED_DATA[5] = new AbstractMap.SimpleImmutableEntry(NpcString.SWIFT_S1, new int[] { 6434, 6667 });
  }

  private static class CheckDuration extends RunnableImpl
  {
    private TamedBeastInstance _tamedBeast;

    CheckDuration(TamedBeastInstance tamedBeast)
    {
      _tamedBeast = tamedBeast;
    }

    public void runImpl()
      throws Exception
    {
      Player owner = _tamedBeast.getPlayer();

      if ((owner == null) || (!owner.isOnline()))
      {
        _tamedBeast.doDespawn();
        return;
      }

      if (_tamedBeast.getDistance(owner) > 2000.0D)
      {
        _tamedBeast.doDespawn();
        return;
      }

      int foodTypeSkillId = _tamedBeast.getFoodType();
      _tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - 60000);

      ItemInstance item = null;
      int foodItemId = _tamedBeast.getItemIdBySkillId(foodTypeSkillId);
      if (foodItemId > 0) {
        item = owner.getInventory().getItemByItemId(foodItemId);
      }

      if ((item != null) && (item.getCount() >= 1L))
      {
        _tamedBeast.onReceiveFood();
        owner.getInventory().destroyItem(item, 1L);
      }
      else if (_tamedBeast.getRemainingTime() < 900000) {
        _tamedBeast.setRemainingTime(-1);
      }
      if (_tamedBeast.getRemainingTime() <= 0)
        _tamedBeast.doDespawn();
    }
  }

  public static class Buff extends RunnableImpl
  {
    private NpcInstance _actor;
    private Player _owner;
    private Skill _skill;

    public Buff(NpcInstance actor, Player owner, Skill skill)
    {
      _actor = actor;
      _owner = owner;
      _skill = skill;
    }

    public void runImpl()
      throws Exception
    {
      if (_actor != null)
        _actor.doCast(_skill, _owner, true);
    }
  }
}