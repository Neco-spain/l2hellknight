package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.stat.PetStat;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

public class L2PetInstance extends L2Summon
{
  protected static final Logger _logPet = Logger.getLogger(L2PetInstance.class.getName());
  private int _curFed;
  private PetInventory _inventory;
  private final int _controlItemId;
  private boolean _respawned;
  private boolean _mountable;
  private Future _feedTask;
  private int _feedTime;
  protected boolean _feedMode;
  private L2PetData _data;
  private long _expBeforeDeath = 0L;
  private static final int FOOD_ITEM_CONSUME_COUNT = 5;

  public final L2PetData getPetData()
  {
    if (_data == null) {
      _data = L2PetDataTable.getInstance().getPetData(getTemplate().npcId, getStat().getLevel());
    }
    return _data;
  }
  public final void setPetData(L2PetData value) {
    _data = value;
  }

  public static synchronized L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
  {
    if (L2World.getInstance().getPet(owner.getObjectId()) != null) {
      return null;
    }
    L2PetInstance pet = restore(control, template, owner);

    if (pet != null)
    {
      pet.setTitle(owner.getName());
      L2World.getInstance().addPet(owner.getObjectId(), pet);
    }
    return pet;
  }

  public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
  {
    super(objectId, template, owner);
    super.setStat(new PetStat(this));

    _controlItemId = control.getObjectId();

    if (template.npcId == 12564)
      getStat().setLevel((byte)getOwner().getLevel());
    else {
      getStat().setLevel(template.level);
    }
    _inventory = new PetInventory(this);

    int npcId = template.npcId;
    _mountable = L2PetDataTable.isMountable(npcId);
  }

  public PetStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof PetStat)))
      setStat(new PetStat(this));
    return (PetStat)super.getStat();
  }

  public double getLevelMod() {
    return (89.0D + getLevel()) / 100.0D;
  }
  public boolean isRespawned() { return _respawned; }

  public int getSummonType() {
    return 2;
  }

  public void onAction(L2PcInstance player)
  {
    boolean isOwner = player.getObjectId() == getOwner().getObjectId();
    boolean thisIsTarget = (player.getTarget() != null) && (player.getTarget().getObjectId() == getObjectId());

    if ((isOwner) && (thisIsTarget))
    {
      if ((isOwner) && (player != getOwner()))
      {
        updateRefOwner(player);
      }
      player.sendPacket(new PetStatusShow(this));
      player.sendPacket(new ActionFailed());
    }
    else
    {
      if (Config.DEBUG) _logPet.fine("new target selected:" + getObjectId());
      player.setTarget(this);
      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);
    }
  }

  public int getControlItemId() {
    return _controlItemId;
  }

  public L2ItemInstance getControlItem() {
    return getOwner().getInventory().getItemByObjectId(_controlItemId);
  }
  public int getCurrentFed() {
    return _curFed;
  }
  public void setCurrentFed(int num) { _curFed = (num > getMaxFed() ? getMaxFed() : num);
  }

  public void setPkKills(int pkKills)
  {
    _pkKills = pkKills;
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    for (L2ItemInstance item : getInventory().getItems())
      if ((item.getLocation() == L2ItemInstance.ItemLocation.PET_EQUIP) && (item.getItem().getType1() == 0))
      {
        return item;
      }
    return null;
  }

  public L2Weapon getActiveWeaponItem()
  {
    L2ItemInstance weapon = getActiveWeaponInstance();

    if (weapon == null) {
      return null;
    }
    return (L2Weapon)weapon.getItem();
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    return null;
  }

  public PetInventory getInventory()
  {
    return _inventory;
  }

  public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);

    if (item == null)
    {
      if (sendMessage) {
        getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return false;
    }

    PetInventoryUpdate petIU = new PetInventoryUpdate();
    petIU.addItem(item);
    getOwner().sendPacket(petIU);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(count);
      sm.addItemName(item.getItemId());
      getOwner().sendPacket(sm);
    }
    return true;
  }

  public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);

    if (item == null)
    {
      if (sendMessage) getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      return false;
    }

    PetInventoryUpdate petIU = new PetInventoryUpdate();
    petIU.addItem(item);
    getOwner().sendPacket(petIU);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(count);
      sm.addItemName(itemId);
      getOwner().sendPacket(sm);
    }

    return true;
  }

  protected void doPickupItem(L2Object object)
  {
    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());

    if (Config.DEBUG) {
      _logPet.fine("Pet pickup pos: " + object.getX() + " " + object.getY() + " " + object.getZ());
    }
    broadcastPacket(sm);

    if (!(object instanceof L2ItemInstance))
    {
      _logPet.warning("trying to pickup wrong target." + object);
      getOwner().sendPacket(new ActionFailed());
      return;
    }

    L2ItemInstance target = (L2ItemInstance)object;

    if ((target.getItemId() > 8599) && (target.getItemId() < 8615))
    {
      SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
      smsg.addItemName(target.getItemId());
      getOwner().sendPacket(smsg);
      return;
    }

    if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
    {
      SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
      smsg.addItemName(target.getItemId());
      getOwner().sendPacket(smsg);
      return;
    }

    synchronized (target)
    {
      if (!target.isVisible())
      {
        getOwner().sendPacket(new ActionFailed());
        return;
      }

      if ((target.getOwnerId() != 0) && (target.getOwnerId() != getOwner().getObjectId()) && (!getOwner().isInLooterParty(target.getOwnerId())))
      {
        getOwner().sendPacket(new ActionFailed());

        if (target.getItemId() == 57)
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
          smsg.addNumber(target.getCount());
          getOwner().sendPacket(smsg);
        }
        else if (target.getCount() > 1)
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
          smsg.addItemName(target.getItemId());
          smsg.addNumber(target.getCount());
          getOwner().sendPacket(smsg);
        }
        else
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
          smsg.addItemName(target.getItemId());
          getOwner().sendPacket(smsg);
        }

        return;
      }
      if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getOwner().getObjectId()) || (getOwner().isInLooterParty(target.getOwnerId()))))
      {
        target.resetOwnerTimer();
      }
      target.pickupMe(this);

      if (Config.SAVE_DROPPED_ITEM) {
        ItemsOnGroundManager.getInstance().removeObject(target);
      }
    }
    getInventory().addItem("Pickup", target, getOwner(), this);

    PetItemList iu = new PetItemList(this);
    getOwner().sendPacket(iu);

    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    if (getFollowStatus())
      followOwner();
  }

  public void deleteMe(L2PcInstance owner)
  {
    super.deleteMe(owner);
    destroyControlItem(owner);
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer, true))
      return false;
    stopFeed();
    DecayTaskManager.getInstance().addDecayTask(this, 1200000);
    deathPenalty();
    return true;
  }

  public void doRevive(boolean broadcastPacketRevive)
  {
    if (_curFed > getMaxFed() / 10) {
      _curFed = (getMaxFed() / 10);
    }
    getOwner().removeReviving();

    super.doRevive(true);

    DecayTaskManager.getInstance().cancelDecayTask(this);
    startFeed(false);
    setRunning();
  }

  public void doRevive(double revivePower)
  {
    restoreExp(revivePower);
    doRevive(true);
  }

  public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance oldItem = getInventory().getItemByObjectId(objectId);
    L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);

    if (newItem == null) return null;

    PetInventoryUpdate petIU = new PetInventoryUpdate();
    if ((oldItem.getCount() > 0) && (oldItem != newItem)) petIU.addModifiedItem(oldItem); else
      petIU.addRemovedItem(oldItem);
    getOwner().sendPacket(petIU);

    if ((target instanceof PcInventory))
    {
      L2PcInstance targetPlayer = ((PcInventory)target).getOwner();
      InventoryUpdate playerUI = new InventoryUpdate();
      if (newItem.getCount() > count) playerUI.addModifiedItem(newItem); else
        playerUI.addNewItem(newItem);
      targetPlayer.sendPacket(playerUI);

      StatusUpdate playerSU = new StatusUpdate(targetPlayer.getObjectId());
      playerSU.addAttribute(14, targetPlayer.getCurrentLoad());
      targetPlayer.sendPacket(playerSU);
    }
    else if ((target instanceof PetInventory))
    {
      petIU = new PetInventoryUpdate();
      if (newItem.getCount() > count) petIU.addRemovedItem(newItem); else
        petIU.addNewItem(newItem);
      ((PetInventory)target).getOwner().getOwner().sendPacket(petIU);
    }
    return newItem;
  }

  public void giveAllToOwner()
  {
    try
    {
      Inventory petInventory = getInventory();
      L2ItemInstance[] items = petInventory.getItems();
      for (int i = 0; i < items.length; i++)
      {
        L2ItemInstance giveit = items[i];
        if (giveit.getItem().getWeight() * giveit.getCount() + getOwner().getInventory().getTotalWeight() < getOwner().getMaxLoad())
        {
          giveItemToOwner(giveit);
        }
        else
        {
          dropItemHere(giveit);
        }
      }
    }
    catch (Exception e)
    {
      _logPet.warning("Give all items error " + e);
    }
  }

  public void giveItemToOwner(L2ItemInstance item)
  {
    try
    {
      getInventory().transferItem("PetTransfer", item.getObjectId(), item.getCount(), getOwner().getInventory(), getOwner(), this);
      PetInventoryUpdate petiu = new PetInventoryUpdate();
      ItemList PlayerUI = new ItemList(getOwner(), false);
      petiu.addRemovedItem(item);
      getOwner().sendPacket(petiu);
      getOwner().sendPacket(PlayerUI);
    }
    catch (Exception e)
    {
      _logPet.warning("Error while giving item to owner: " + e);
    }
  }

  public void destroyControlItem(L2PcInstance owner)
  {
    L2World.getInstance().removePet(owner.getObjectId());
    try
    {
      L2ItemInstance removedItem = owner.getInventory().destroyItem("PetDestroy", getControlItemId(), 1, getOwner(), this);

      InventoryUpdate iu = new InventoryUpdate();
      iu.addRemovedItem(removedItem);

      owner.sendPacket(iu);

      StatusUpdate su = new StatusUpdate(owner.getObjectId());
      su.addAttribute(14, owner.getCurrentLoad());
      owner.sendPacket(su);

      owner.broadcastUserInfo();

      L2World world = L2World.getInstance();
      world.removeObject(removedItem);
    }
    catch (Exception e) {
      _logPet.warning("Error while destroying control item: " + e);
    }

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
      statement.setInt(1, getControlItemId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _logPet.warning("could not delete pet:" + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void dropAllItems() {
    try {
      L2ItemInstance[] items = getInventory().getItems();
      for (int i = 0; i < items.length; i++)
      {
        dropItemHere(items[i]);
      }
    }
    catch (Exception e)
    {
      _logPet.warning("Pet Drop Error: " + e);
    }
  }

  public void dropItemHere(L2ItemInstance dropit)
  {
    dropit = getInventory().dropItem("Drop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);

    if (dropit != null)
    {
      _logPet.finer("Item id to drop: " + dropit.getItemId() + " amount: " + dropit.getCount());
      dropit.dropMe(this, getX(), getY(), getZ() + 100);
    }
  }

  public boolean isMountable()
  {
    return _mountable;
  }

  private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner) {
    Connection con = null;
    try
    {
      L2PetInstance pet;
      L2PetInstance pet;
      if (template.type.compareToIgnoreCase("L2BabyPet") == 0)
        pet = new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
      else {
        pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
      }
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, karma, pkkills, fed FROM pets WHERE item_obj_id=?");
      statement.setInt(1, control.getObjectId());
      ResultSet rset = statement.executeQuery();
      if (!rset.next())
      {
        rset.close();
        statement.close();
        localL2PetInstance1 = pet;
        return localL2PetInstance1;
      }
      pet._respawned = true;
      pet.setName(rset.getString("name"));

      pet.getStat().setLevel(rset.getByte("level"));
      pet.getStat().setExp(rset.getLong("exp"));
      pet.getStat().setSp(rset.getInt("sp"));

      pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
      pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
      pet.getStatus().setCurrentCp(pet.getMaxCp());

      pet.setKarma(rset.getInt("karma"));
      pet.setPkKills(rset.getInt("pkkills"));
      pet.setCurrentFed(rset.getInt("fed"));

      rset.close();
      statement.close();
      L2PetInstance localL2PetInstance1 = pet;
      return localL2PetInstance1;
    }
    catch (Exception e)
    {
      _logPet.warning("could not restore pet data: " + e);
      PreparedStatement statement = null;
      return statement; } finally { try { con.close(); } catch (Exception e) {
      } } throw localObject;
  }

  public void store()
  {
    if (getControlItemId() == 0)
    {
      return;
    }
    String req;
    String req;
    if (!isRespawned()) {
      req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,karma,pkkills,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
    }
    else {
      req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,karma=?,pkkills=?,fed=? WHERE item_obj_id = ?";
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement(req);
      statement.setString(1, getName());
      statement.setInt(2, getStat().getLevel());
      statement.setDouble(3, getStatus().getCurrentHp());
      statement.setDouble(4, getStatus().getCurrentMp());
      statement.setLong(5, getStat().getExp());
      statement.setInt(6, getStat().getSp());
      statement.setInt(7, getKarma());
      statement.setInt(8, getPkKills());
      statement.setInt(9, getCurrentFed());
      statement.setInt(10, getControlItemId());
      statement.executeUpdate();
      statement.close();
      _respawned = true;
    } catch (Exception e) {
      _logPet.warning("could not store pet data: " + e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    L2ItemInstance itemInst = getControlItem();
    if ((itemInst != null) && (itemInst.getEnchantLevel() != getStat().getLevel()))
    {
      itemInst.setEnchantLevel(getStat().getLevel());
      itemInst.updateDatabase();
    }
  }

  public synchronized void stopFeed()
  {
    if (_feedTask != null)
    {
      _feedTask.cancel(false);
      _feedTask = null;
      if (Config.DEBUG) _logPet.fine("Pet [#" + getObjectId() + "] feed task stop");
    }
  }

  public synchronized void startFeed(boolean battleFeed)
  {
    stopFeed();
    if (!isDead())
    {
      if (battleFeed)
      {
        _feedMode = true;
        _feedTime = _data.getPetFeedBattle();
      }
      else
      {
        _feedMode = false;
        _feedTime = _data.getPetFeedNormal();
      }

      if (_feedTime <= 0) _feedTime = 1;

      _feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 60000 / _feedTime, 60000 / _feedTime);
    }
  }

  public synchronized void unSummon(L2PcInstance owner)
  {
    stopFeed();
    stopHpMpRegeneration();
    super.unSummon(owner);

    if (!isDead())
      L2World.getInstance().removePet(owner.getObjectId());
  }

  public void restoreExp(double restorePercent)
  {
    if (_expBeforeDeath > 0L)
    {
      getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100.0D));
      _expBeforeDeath = 0L;
    }
  }

  private void deathPenalty()
  {
    int lvl = getStat().getLevel();
    double percentLost = -0.07000000000000001D * lvl + 6.5D;

    long lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100.0D);

    _expBeforeDeath = getStat().getExp();

    getStat().addExp(-lostExp);
  }

  public void addExpAndSp(long addToExp, int addToSp)
  {
    if (getNpcId() == 12564)
      getStat().addExpAndSp(Math.round((float)addToExp * Config.SINEATER_XP_RATE), addToSp);
    else
      getStat().addExpAndSp(Math.round((float)addToExp * Config.PET_XP_RATE), addToSp);
  }

  public long getExpForThisLevel() {
    return getStat().getExpForLevel(getLevel());
  }
  public long getExpForNextLevel() { return getStat().getExpForLevel(getLevel() + 1); } 
  public final int getLevel() {
    return getStat().getLevel(); } 
  public int getMaxFed() { return getStat().getMaxFeed(); } 
  public int getAccuracy() {
    return getStat().getAccuracy();
  }
  public int getCriticalHit(L2Character target, L2Skill skill) { return getStat().getCriticalHit(target, skill); } 
  public int getEvasionRate(L2Character target) {
    return getStat().getEvasionRate(target);
  }
  public int getRunSpeed() { return getStat().getRunSpeed(); } 
  public int getPAtkSpd() {
    return getStat().getPAtkSpd();
  }
  public int getMAtkSpd() { return getStat().getMAtkSpd(); } 
  public int getMAtk(L2Character target, L2Skill skill) {
    return getStat().getMAtk(target, skill);
  }
  public int getMDef(L2Character target, L2Skill skill) { return getStat().getMDef(target, skill); } 
  public int getPAtk(L2Character target) {
    return getStat().getPAtk(target);
  }
  public int getPDef(L2Character target) { return getStat().getPDef(target);
  }

  public final int getSkillLevel(int skillId)
  {
    if ((_skills == null) || (_skills.get(Integer.valueOf(skillId)) == null)) return -1;
    int lvl = getLevel();
    return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
  }

  public void updateRefOwner(L2PcInstance owner)
  {
    int oldOwnerId = getOwner().getObjectId();

    setOwner(owner);
    L2World.getInstance().removePet(oldOwnerId);
    L2World.getInstance().addPet(oldOwnerId, this);
  }

  public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
  {
    if (miss) return;

    if (target.getObjectId() != getOwner().getObjectId())
    {
      if ((pcrit) || (mcrit)) {
        getOwner().sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_BY_PET));
      }
      SystemMessage sm = new SystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE);
      sm.addNumber(damage);
      getOwner().sendPacket(sm);
    }
  }

  class FeedTask
    implements Runnable
  {
    FeedTask()
    {
    }

    public void run()
    {
      try
      {
        if (isAttackingNow())
        {
          if (!_feedMode) {
            startFeed(true);
          }
          else if (_feedMode)
            startFeed(false);
        }
        if (getCurrentFed() > 5)
        {
          setCurrentFed(getCurrentFed() - 5);
        }
        else
        {
          setCurrentFed(0);
          stopFeed();
          unSummon(getOwner());
          getOwner().sendMessage("Your pet is too hungry to stay summoned.");
        }

        int foodId = L2PetDataTable.getFoodItemId(getTemplate().npcId);
        if (foodId == 0) return;

        L2ItemInstance food = null;
        food = getInventory().getItemByItemId(foodId);

        if ((food != null) && (getCurrentFed() < 0.55D * getMaxFed()))
        {
          if (destroyItem("Feed", food.getObjectId(), 1, null, false))
          {
            setCurrentFed(getCurrentFed() + 100);
            if (getOwner() != null)
            {
              SystemMessage sm = new SystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
              sm.addItemName(foodId);
              getOwner().sendPacket(sm);
            }
          }
        }

        broadcastStatusUpdate();
      }
      catch (Throwable e)
      {
        if (Config.DEBUG)
          L2PetInstance._logPet.warning("Pet [#" + getObjectId() + "] a feed task error has occurred: " + e);
      }
    }
  }
}