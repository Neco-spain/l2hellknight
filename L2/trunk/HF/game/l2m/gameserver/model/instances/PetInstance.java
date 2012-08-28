package l2m.gameserver.model.instances;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.Future;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.handler.items.IItemHandler;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.PetData;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.BaseStats;
import l2m.gameserver.model.base.Experience;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.PetInventory;
import l2m.gameserver.model.items.attachment.FlagItemAttachment;
import l2m.gameserver.network.serverpackets.InventoryUpdate;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SocialAction;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.data.tables.PetDataTable;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.WeaponTemplate;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetInstance extends Summon
{
  public static final long serialVersionUID = 1L;
  private static final Logger _log = LoggerFactory.getLogger(PetInstance.class);
  private static final int DELUXE_FOOD_FOR_STRIDER = 5169;
  private final int _controlItemObjId;
  private int _curFed;
  protected PetData _data;
  private Future<?> _feedTask;
  protected PetInventory _inventory;
  private int _level;
  private boolean _respawned;
  private int lostExp;

  public static final PetInstance restore(ItemInstance control, NpcTemplate template, Player owner)
  {
    PetInstance pet = null;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT objId, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
      statement.setInt(1, control.getObjectId());
      rset = statement.executeQuery();

      if (!rset.next())
      {
        if ((PetDataTable.isBabyPet(template.getNpcId())) || (PetDataTable.isImprovedBabyPet(template.getNpcId())))
          pet = new PetBabyInstance(IdFactory.getInstance().getNextId(), template, owner, control);
        else
          pet = new PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
        PetInstance localPetInstance1 = pet;
        return localPetInstance1;
      }
      if ((PetDataTable.isBabyPet(template.getNpcId())) || (PetDataTable.isImprovedBabyPet(template.getNpcId())))
        pet = new PetBabyInstance(rset.getInt("objId"), template, owner, control, rset.getInt("level"), rset.getLong("exp"));
      else {
        pet = new PetInstance(rset.getInt("objId"), template, owner, control, rset.getInt("level"), rset.getLong("exp"));
      }
      pet.setRespawned(true);

      String name = rset.getString("name");
      pet.setName((name == null) || (name.isEmpty()) ? template.name : name);
      pet.setCurrentHpMp(rset.getDouble("curHp"), rset.getInt("curMp"), true);
      pet.setCurrentCp(pet.getMaxCp());
      pet.setSp(rset.getInt("sp"));
      pet.setCurrentFed(rset.getInt("fed"));
    }
    catch (Exception e)
    {
      _log.error("Could not restore Pet data from item: " + control + "!", e);
      Object localObject1 = null;
      return localObject1; } finally { DbUtils.closeQuietly(con, statement, rset);
    }

    return pet;
  }

  public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
  {
    this(objectId, template, owner, control, 0, 0L);
  }

  public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, int currentLevel, long exp)
  {
    super(objectId, template, owner);

    _controlItemObjId = control.getObjectId();
    _exp = exp;
    _level = control.getEnchantLevel();

    if (_level <= 0)
    {
      if (template.npcId == 12564)
        _level = owner.getLevel();
      else
        _level = template.level;
      _exp = getExpForThisLevel();
    }

    int minLevel = PetDataTable.getMinLevel(template.npcId);
    if (_level < minLevel) {
      _level = minLevel;
    }
    if (_exp < getExpForThisLevel()) {
      _exp = getExpForThisLevel();
    }
    while ((_exp >= getExpForNextLevel()) && (_level < Experience.getMaxLevel())) {
      _level += 1;
    }
    while ((_exp < getExpForThisLevel()) && (_level > minLevel)) {
      _level -= 1;
    }
    if (PetDataTable.isVitaminPet(template.npcId))
    {
      _level = owner.getLevel();
      _exp = getExpForNextLevel();
    }

    _data = PetDataTable.getInstance().getInfo(template.npcId, _level);
    _inventory = new PetInventory(this);
  }

  protected void onSpawn()
  {
    super.onSpawn();

    startFeed(false);
  }

  protected void onDespawn()
  {
    super.onSpawn();
    store();
    stopFeed();
  }

  public boolean tryFeedItem(ItemInstance item)
  {
    if (item == null) {
      return false;
    }
    boolean deluxFood = (PetDataTable.isStrider(getNpcId())) && (item.getItemId() == 5169);
    if ((getFoodId() != item.getItemId()) && (!deluxFood)) {
      return false;
    }
    int newFed = Math.min(getMaxFed(), getCurrentFed() + Math.max(getMaxFed() * getAddFed() * (deluxFood ? 2 : 1) / 100, 1));
    if ((getCurrentFed() != newFed) && 
      (getInventory().destroyItem(item, 1L)))
    {
      getPlayer().sendPacket(new SystemMessage(1527).addItemName(item.getItemId()));
      setCurrentFed(newFed);
      sendStatusUpdate();
    }
    return true;
  }

  public boolean tryFeed()
  {
    ItemInstance food = getInventory().getItemByItemId(getFoodId());
    if ((food == null) && (PetDataTable.isStrider(getNpcId())))
      food = getInventory().getItemByItemId(5169);
    return tryFeedItem(food);
  }

  public void addExpAndSp(long addToExp, long addToSp)
  {
    Player owner = getPlayer();

    if (PetDataTable.isVitaminPet(getNpcId())) {
      return;
    }
    _exp += addToExp;
    _sp = (int)(_sp + addToSp);

    if (_exp > getMaxExp()) {
      _exp = getMaxExp();
    }
    if ((addToExp > 0L) || (addToSp > 0L)) {
      owner.sendPacket(new SystemMessage(1014).addNumber(addToExp));
    }
    int old_level = _level;

    while ((_exp >= getExpForNextLevel()) && (_level < Experience.getMaxLevel())) {
      _level += 1;
    }
    while ((_exp < getExpForThisLevel()) && (_level > getMinLevel())) {
      _level -= 1;
    }
    if (old_level < _level)
    {
      owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.PetLevelUp", owner, new Object[0]).addNumber(_level));
      broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 2122) });
      setCurrentHpMp(getMaxHp(), getMaxMp());
    }

    if (old_level != _level)
    {
      updateControlItem();
      updateData();
    }

    if ((addToExp > 0L) || (addToSp > 0L))
      sendStatusUpdate();
  }

  public boolean consumeItem(int itemConsumeId, long itemCount)
  {
    return getInventory().destroyItemByItemId(itemConsumeId, itemCount);
  }

  private void deathPenalty()
  {
    if (isInZoneBattle())
      return;
    int lvl = getLevel();
    double percentLost = -0.07000000000000001D * lvl + 6.5D;

    lostExp = (int)Math.round((getExpForNextLevel() - getExpForThisLevel()) * percentLost / 100.0D);
    addExpAndSp(-lostExp, 0L);
  }

  private void destroyControlItem()
  {
    Player owner = getPlayer();
    if (getControlItemObjId() == 0)
      return;
    if (!owner.getInventory().destroyItemByObjectId(getControlItemObjId(), 1L)) {
      return;
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
      statement.setInt(1, getControlItemObjId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("could not delete pet:" + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  protected void onDeath(Creature killer)
  {
    super.onDeath(killer);

    Player owner = getPlayer();

    owner.sendPacket(Msg.THE_PET_HAS_BEEN_KILLED_IF_YOU_DO_NOT_RESURRECT_IT_WITHIN_24_HOURS_THE_PETS_BODY_WILL_DISAPPEAR_ALONG_WITH_ALL_THE_PETS_ITEMS);
    startDecay(86400000L);

    if (PetDataTable.isVitaminPet(getNpcId())) {
      return;
    }
    stopFeed();
    deathPenalty();
  }

  public void doPickupItem(GameObject object)
  {
    Player owner = getPlayer();

    stopMove();

    if (!object.isItem()) {
      return;
    }
    ItemInstance item = (ItemInstance)object;

    if (item.isCursed())
    {
      owner.sendPacket(new SystemMessage(56).addItemName(item.getItemId()));
      return;
    }

    synchronized (item)
    {
      if (!item.isVisible()) {
        return;
      }
      if (item.isHerb())
      {
        Skill[] skills = item.getTemplate().getAttachedSkills();
        if (skills.length > 0)
          for (Skill skill : skills)
            altUseSkill(skill, this);
        item.deleteMe();
        return;
      }

      if (!getInventory().validateWeight(item))
      {
        sendPacket(Msg.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT);
        return;
      }

      if (!getInventory().validateCapacity(item))
      {
        sendPacket(Msg.DUE_TO_THE_VOLUME_LIMIT_OF_THE_PETS_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE);
        return;
      }

      if (!item.getTemplate().getHandler().pickupItem(this, item)) {
        return;
      }
      FlagItemAttachment attachment = (item.getAttachment() instanceof FlagItemAttachment) ? (FlagItemAttachment)item.getAttachment() : null;
      if (attachment != null) {
        return;
      }
      item.pickupMe();
    }

    if ((owner.getParty() == null) || (owner.getParty().getLootDistribution() == 0))
    {
      getInventory().addItem(item);
      sendChanges();
    }
    else {
      owner.getParty().distributeItem(owner, item, null);
    }broadcastPickUpMsg(item);
  }

  public void doRevive(double percent)
  {
    restoreExp(percent);
    doRevive();
  }

  public void doRevive()
  {
    stopDecay();
    super.doRevive();
    startFeed(false);
    setRunning();
  }

  public int getAccuracy()
  {
    return (int)calcStat(Stats.ACCURACY_COMBAT, _data.getAccuracy(), null, null);
  }

  public ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getActiveWeaponItem()
  {
    return null;
  }

  public ItemInstance getControlItem()
  {
    Player owner = getPlayer();
    if (owner == null)
      return null;
    int item_obj_id = getControlItemObjId();
    if (item_obj_id == 0)
      return null;
    return owner.getInventory().getItemByObjectId(item_obj_id);
  }

  public int getControlItemObjId()
  {
    return _controlItemObjId;
  }

  public int getCriticalHit(Creature target, Skill skill)
  {
    return (int)calcStat(Stats.CRITICAL_BASE, _data.getCritical(), target, skill);
  }

  public int getCurrentFed()
  {
    return _curFed;
  }

  public int getEvasionRate(Creature target)
  {
    return (int)calcStat(Stats.EVASION_RATE, _data.getEvasion(), target, null);
  }

  public long getExpForNextLevel()
  {
    return PetDataTable.getInstance().getInfo(getNpcId(), _level + 1).getExp();
  }

  public long getExpForThisLevel()
  {
    return PetDataTable.getInstance().getInfo(getNpcId(), _level).getExp();
  }

  public int getFoodId()
  {
    return _data.getFoodId();
  }

  public int getAddFed()
  {
    return _data.getAddFed();
  }

  public PetInventory getInventory()
  {
    return _inventory;
  }

  public long getWearedMask()
  {
    return _inventory.getWearedMask();
  }

  public final int getLevel()
  {
    return _level;
  }

  public void setLevel(int level)
  {
    _level = level;
  }

  public double getLevelMod()
  {
    return (89.0D + getLevel()) / 100.0D;
  }

  public int getMinLevel()
  {
    return _data.getMinLevel();
  }

  public long getMaxExp()
  {
    return PetDataTable.getInstance().getInfo(getNpcId(), Experience.getMaxLevel() + 1).getExp();
  }

  public int getMaxFed()
  {
    return _data.getFeedMax();
  }

  public int getMaxLoad()
  {
    return (int)calcStat(Stats.MAX_LOAD, _data.getMaxLoad(), null, null);
  }

  public int getInventoryLimit()
  {
    return Config.ALT_PET_INVENTORY_LIMIT;
  }

  public int getMaxHp()
  {
    return (int)calcStat(Stats.MAX_HP, _data.getHP(), null, null);
  }

  public int getMaxMp()
  {
    return (int)calcStat(Stats.MAX_MP, _data.getMP(), null, null);
  }

  public int getPAtk(Creature target)
  {
    double mod = BaseStats.STR.calcBonus(this) * getLevelMod();
    return (int)calcStat(Stats.POWER_ATTACK, _data.getPAtk() / mod, target, null);
  }

  public int getPDef(Creature target)
  {
    double mod = getLevelMod();
    return (int)calcStat(Stats.POWER_DEFENCE, _data.getPDef() / mod, target, null);
  }

  public int getMAtk(Creature target, Skill skill)
  {
    double ib = BaseStats.INT.calcBonus(this);
    double lvlb = getLevelMod();
    double mod = lvlb * lvlb * ib * ib;
    return (int)calcStat(Stats.MAGIC_ATTACK, _data.getMAtk() / mod, target, skill);
  }

  public int getMDef(Creature target, Skill skill)
  {
    double mod = BaseStats.MEN.calcBonus(this) * getLevelMod();
    return (int)calcStat(Stats.MAGIC_DEFENCE, _data.getMDef() / mod, target, skill);
  }

  public int getPAtkSpd()
  {
    return (int)calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _data.getAtkSpeed(), null, null), null, null);
  }

  public int getMAtkSpd()
  {
    return (int)calcStat(Stats.MAGIC_ATTACK_SPEED, _data.getCastSpeed(), null, null);
  }

  public int getRunSpeed()
  {
    return getSpeed(_data.getSpeed());
  }

  public int getSoulshotConsumeCount()
  {
    return PetDataTable.getSoulshots(getNpcId());
  }

  public int getSpiritshotConsumeCount()
  {
    return PetDataTable.getSpiritshots(getNpcId());
  }

  public ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getSecondaryWeaponItem()
  {
    return null;
  }

  public int getSkillLevel(int skillId)
  {
    if ((_skills == null) || (_skills.get(Integer.valueOf(skillId)) == null))
      return -1;
    int lvl = getLevel();
    return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
  }

  public int getSummonType()
  {
    return 2;
  }

  public NpcTemplate getTemplate()
  {
    return (NpcTemplate)_template;
  }

  public boolean isMountable()
  {
    return _data.isMountable();
  }

  public boolean isRespawned()
  {
    return _respawned;
  }

  public void restoreExp(double percent)
  {
    if (lostExp != 0)
    {
      addExpAndSp(()(lostExp * percent / 100.0D), 0L);
      lostExp = 0;
    }
  }

  public void setCurrentFed(int num)
  {
    _curFed = Math.min(getMaxFed(), Math.max(0, num));
  }

  public void setRespawned(boolean respawned)
  {
    _respawned = respawned;
  }

  public void setSp(int sp)
  {
    _sp = sp;
  }

  public void startFeed(boolean battleFeed)
  {
    boolean first = _feedTask == null;
    stopFeed();
    if (!isDead())
    {
      int feedTime;
      int feedTime;
      if (PetDataTable.isVitaminPet(getNpcId()))
        feedTime = 10000;
      else
        feedTime = Math.max(first ? 15000 : 1000, 60000 / (battleFeed ? _data.getFeedBattle() : _data.getFeedNormal()));
      _feedTask = ThreadPoolManager.getInstance().schedule(new FeedTask(), feedTime);
    }
  }

  private void stopFeed()
  {
    if (_feedTask != null)
    {
      _feedTask.cancel(false);
      _feedTask = null;
    }
  }

  public void store()
  {
    if ((getControlItemObjId() == 0) || (_exp == 0L)) {
      return;
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      String req;
      String req;
      if (!isRespawned())
        req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,objId,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?)";
      else
        req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,objId=? WHERE item_obj_id = ?";
      statement = con.prepareStatement(req);
      statement.setString(1, getName().equalsIgnoreCase(getTemplate().name) ? "" : getName());
      statement.setInt(2, _level);
      statement.setDouble(3, getCurrentHp());
      statement.setDouble(4, getCurrentMp());
      statement.setLong(5, _exp);
      statement.setLong(6, _sp);
      statement.setInt(7, _curFed);
      statement.setInt(8, getObjectId());
      statement.setInt(9, _controlItemObjId);
      statement.executeUpdate();
    }
    catch (Exception e)
    {
      _log.error("Could not store pet data!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    _respawned = true;
  }

  protected void onDecay()
  {
    giveAllToOwner();
    destroyControlItem();

    super.onDecay();
  }

  public void unSummon()
  {
    stopFeed();
    giveAllToOwner();

    super.unSummon();
    store();
    getInventory().store();
  }

  private synchronized void giveAllToOwner()
  {
    synchronized (getInventory())
    {
      for (ItemInstance i : getInventory().getItems())
      {
        ItemInstance item = getInventory().removeItem(i, i.getCount());
        if (getPlayer() != null)
          getPlayer().getInventory().addItem(item);
        else
          item.dropMe(this, getLoc().changeZ(25));
      }
      getInventory().clear();
    }
  }

  public void updateControlItem()
  {
    ItemInstance controlItem = getControlItem();
    if (controlItem == null)
      return;
    controlItem.setEnchantLevel(_level);
    controlItem.setCustomType2(isDefaultName() ? 0 : 1);
    controlItem.setJdbcState(JdbcEntityState.UPDATED);
    controlItem.update();
    Player owner = getPlayer();
    owner.sendPacket(new InventoryUpdate().addModifiedItem(controlItem));
  }

  private void updateData()
  {
    _data = PetDataTable.getInstance().getInfo(getTemplate().npcId, _level);
  }

  public double getExpPenalty()
  {
    return PetDataTable.getExpPenalty(getTemplate().npcId);
  }

  public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
  {
    Player owner = getPlayer();
    if (crit)
      owner.sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
    if (miss)
      owner.sendPacket(new SystemMessage(2265).addName(this));
    else
      owner.sendPacket(new SystemMessage(1015).addNumber(damage));
  }

  public void displayReceiveDamageMessage(Creature attacker, int damage)
  {
    Player owner = getPlayer();

    if (!isDead())
    {
      SystemMessage sm = new SystemMessage(1016);
      if (attacker.isNpc())
        sm.addNpcName(((NpcInstance)attacker).getTemplate().npcId);
      else
        sm.addString(attacker.getName());
      sm.addNumber(damage);
      owner.sendPacket(sm);
    }
  }

  public int getFormId()
  {
    switch (getNpcId())
    {
    case 16025:
    case 16037:
    case 16041:
    case 16042:
      if (getLevel() >= 70)
        return 3;
      if (getLevel() >= 65)
        return 2;
      if (getLevel() < 60) break;
      return 1;
    }

    return 0;
  }

  public boolean isPet()
  {
    return true;
  }

  public boolean isDefaultName()
  {
    return (StringUtils.isEmpty(_name)) || (getName().equalsIgnoreCase(getTemplate().name));
  }

  public int getEffectIdentifier()
  {
    return 0;
  }

  class FeedTask extends RunnableImpl
  {
    FeedTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      Player owner = getPlayer();

      while ((getCurrentFed() <= 0.55D * getMaxFed()) && (tryFeed()));
      if ((PetDataTable.isVitaminPet(getNpcId())) && (getCurrentFed() <= 0)) {
        deleteMe();
      } else if (getCurrentFed() <= 0.1D * getMaxFed())
      {
        owner.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2PetInstance.UnSummonHungryPet", owner, new Object[0]));
        unSummon();
        return;
      }

      setCurrentFed(getCurrentFed() - 5);

      sendStatusUpdate();
      startFeed(isInCombat());
    }
  }
}