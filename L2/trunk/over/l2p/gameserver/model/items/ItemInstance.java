package l2p.gameserver.model.items;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.dao.JdbcEntity;
import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.Config;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.dao.ItemsDAO;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.attachment.ItemAttachment;
import l2p.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.serverpackets.DropItem;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SpawnItem;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.funcs.Func;
import l2p.gameserver.stats.funcs.FuncTemplate;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.taskmanager.ItemsAutoDestroy;
import l2p.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.templates.item.ItemTemplate.ItemClass;
import l2p.gameserver.templates.item.ItemType;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public final class ItemInstance extends GameObject
  implements JdbcEntity
{
  public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];
  private static final long serialVersionUID = 3162753878915133228L;
  private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
  public static final int CHARGED_NONE = 0;
  public static final int CHARGED_SOULSHOT = 1;
  public static final int CHARGED_SPIRITSHOT = 1;
  public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
  public static final int FLAG_NO_DROP = 1;
  public static final int FLAG_NO_TRADE = 2;
  public static final int FLAG_NO_TRANSFER = 4;
  public static final int FLAG_NO_CRYSTALLIZE = 8;
  public static final int FLAG_NO_ENCHANT = 16;
  public static final int FLAG_NO_DESTROY = 32;
  private int ownerId;
  private int itemId;
  private long count;
  private int enchantLevel = -1;
  private ItemLocation loc;
  private int locData;
  private int customType1;
  private int customType2;
  private int lifeTime;
  private int customFlags;
  private ItemAttributes attrs = new ItemAttributes();

  private int[] _enchantOptions = EMPTY_ENCHANT_OPTIONS;
  private ItemTemplate template;
  private boolean isEquipped;
  private long _dropTime;
  private IntSet _dropPlayers = Containers.EMPTY_INT_SET;
  private long _dropTimeOwner;
  private int _chargedSoulshot = 0;
  private int _chargedSpiritshot = 0;

  private boolean _chargedFishtshot = false;
  private int _augmentationId;
  private int _agathionEnergy;
  private ItemAttachment _attachment;
  private JdbcEntityState _state = JdbcEntityState.CREATED;
  private ScheduledFuture<?> _timerTask;

  public ItemInstance(int objectId)
  {
    super(objectId);
  }

  public ItemInstance(int objectId, int itemId)
  {
    super(objectId);
    setItemId(itemId);
    setLifeTime(getTemplate().isTemporal() ? (int)(System.currentTimeMillis() / 1000L) + getTemplate().getDurability() * 60 : getTemplate().getDurability());
    setAgathionEnergy(getTemplate().getAgathionEnergy());
    setLocData(-1);
    setEnchantLevel(0);
  }

  public int getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(int ownerId)
  {
    this.ownerId = ownerId;
  }

  public int getItemId()
  {
    return itemId;
  }

  public void setItemId(int id)
  {
    itemId = id;
    template = ItemHolder.getInstance().getTemplate(id);
    setCustomFlags(getCustomFlags());
  }

  public long getCount()
  {
    return count;
  }

  public void setCount(long count)
  {
    if (count < 0L) {
      count = 0L;
    }
    if ((!isStackable()) && (count > 1L))
    {
      this.count = 1L;

      return;
    }

    this.count = count;
  }

  public int getEnchantLevel()
  {
    return enchantLevel;
  }

  public void setEnchantLevel(int enchantLevel)
  {
    int old = this.enchantLevel;

    this.enchantLevel = enchantLevel;

    if ((old != this.enchantLevel) && (getTemplate().getEnchantOptions().size() > 0))
    {
      Player player = GameObjectsStorage.getPlayer(ownerId);

      if ((isEquipped()) && (player != null)) {
        ItemEnchantOptionsListener.getInstance().onUnequip(getEquipSlot(), this, player);
      }
      int[] enchantOptions = (int[])getTemplate().getEnchantOptions().get(this.enchantLevel);

      _enchantOptions = (enchantOptions == null ? EMPTY_ENCHANT_OPTIONS : enchantOptions);

      if ((isEquipped()) && (player != null))
        ItemEnchantOptionsListener.getInstance().onEquip(getEquipSlot(), this, player);
    }
  }

  public void setLocName(String loc)
  {
    this.loc = ItemLocation.valueOf(loc);
  }

  public String getLocName()
  {
    return loc.name();
  }

  public void setLocation(ItemLocation loc)
  {
    this.loc = loc;
  }

  public ItemLocation getLocation()
  {
    return loc;
  }

  public void setLocData(int locData)
  {
    this.locData = locData;
  }

  public int getLocData()
  {
    return locData;
  }

  public int getCustomType1()
  {
    return customType1;
  }

  public void setCustomType1(int newtype)
  {
    customType1 = newtype;
  }

  public int getCustomType2()
  {
    return customType2;
  }

  public void setCustomType2(int newtype)
  {
    customType2 = newtype;
  }

  public int getLifeTime()
  {
    return lifeTime;
  }

  public void setLifeTime(int lifeTime)
  {
    this.lifeTime = Math.max(0, lifeTime);
  }

  public int getCustomFlags()
  {
    return customFlags;
  }

  public void setCustomFlags(int flags)
  {
    customFlags = flags;
  }

  public ItemAttributes getAttributes()
  {
    return attrs;
  }

  public void setAttributes(ItemAttributes attrs)
  {
    this.attrs = attrs;
  }

  public int getShadowLifeTime()
  {
    if (!isShadowItem())
      return 0;
    return getLifeTime();
  }

  public int getTemporalLifeTime()
  {
    if (!isTemporalItem())
      return 0;
    return getLifeTime() - (int)(System.currentTimeMillis() / 1000L);
  }

  public void startTimer(Runnable r)
  {
    _timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0L, 60000L);
  }

  public void stopTimer()
  {
    if (_timerTask != null)
    {
      _timerTask.cancel(false);
      _timerTask = null;
    }
  }

  public boolean isEquipable()
  {
    return template.isEquipable();
  }

  public boolean isEquipped()
  {
    return isEquipped;
  }

  public void setEquipped(boolean isEquipped)
  {
    this.isEquipped = isEquipped;
  }

  public int getBodyPart()
  {
    return template.getBodyPart();
  }

  public int getEquipSlot()
  {
    return getLocData();
  }

  public ItemTemplate getTemplate()
  {
    return template;
  }

  public void setDropTime(long time)
  {
    _dropTime = time;
  }

  public long getLastDropTime()
  {
    return _dropTime;
  }

  public long getDropTimeOwner()
  {
    return _dropTimeOwner;
  }

  public ItemType getItemType()
  {
    return template.getItemType();
  }

  public boolean isArmor()
  {
    return template.isArmor();
  }

  public boolean isAccessory()
  {
    return template.isAccessory();
  }

  public boolean isWeapon()
  {
    return template.isWeapon();
  }

  public int getReferencePrice()
  {
    return template.getReferencePrice();
  }

  public boolean isStackable()
  {
    return template.isStackable();
  }

  public void onAction(Player player, boolean shift)
  {
    if (Events.onAction(player, this, shift)) {
      return;
    }
    if ((player.isCursedWeaponEquipped()) && (CursedWeaponsManager.getInstance().isCursed(itemId))) {
      return;
    }
    player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
  }

  public boolean isAugmented()
  {
    return getAugmentationId() != 0;
  }

  public int getAugmentationId()
  {
    return _augmentationId;
  }

  public void setAugmentationId(int val)
  {
    _augmentationId = val;
  }

  public int getChargedSoulshot()
  {
    return _chargedSoulshot;
  }

  public int getChargedSpiritshot()
  {
    return _chargedSpiritshot;
  }

  public boolean getChargedFishshot()
  {
    return _chargedFishtshot;
  }

  public void setChargedSoulshot(int type)
  {
    _chargedSoulshot = type;
  }

  public void setChargedSpiritshot(int type)
  {
    _chargedSpiritshot = type;
  }

  public void setChargedFishshot(boolean type)
  {
    _chargedFishtshot = type;
  }

  public Func[] getStatFuncs()
  {
    Func[] result = Func.EMPTY_FUNC_ARRAY;

    LazyArrayList funcs = LazyArrayList.newInstance();

    if (template.getAttachedFuncs().length > 0) {
      for (FuncTemplate t : template.getAttachedFuncs())
      {
        Func f = t.getFunc(this);
        if (f != null)
          funcs.add(f);
      }
    }
    for (Element e : Element.VALUES)
    {
      if (isWeapon())
        funcs.add(new FuncAttack(e, 64, this));
      if (isArmor()) {
        funcs.add(new FuncDefence(e, 64, this));
      }
    }
    if (!funcs.isEmpty()) {
      result = (Func[])funcs.toArray(new Func[funcs.size()]);
    }
    LazyArrayList.recycle(funcs);

    return result;
  }

  public boolean isHeroWeapon()
  {
    return template.isHeroWeapon();
  }

  public boolean canBeDestroyed(Player player)
  {
    if ((customFlags & 0x20) == 32) {
      return false;
    }
    if (isHeroWeapon()) {
      return false;
    }
    if ((PetDataTable.isPetControlItem(this)) && (player.isMounted())) {
      return false;
    }
    if (player.getPetControlItem() == this) {
      return false;
    }
    if (player.getEnchantScroll() == this) {
      return false;
    }
    if (isCursed()) {
      return false;
    }
    return template.isDestroyable();
  }

  public boolean canBeDropped(Player player, boolean pk)
  {
    if (player.isGM()) {
      return true;
    }
    if ((customFlags & 0x1) == 1) {
      return false;
    }
    if (isShadowItem()) {
      return false;
    }
    if (isTemporalItem()) {
      return false;
    }
    if ((isAugmented()) && ((!pk) || (!Config.DROP_ITEMS_AUGMENTED)) && (!Config.ALT_ALLOW_DROP_AUGMENTED)) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return template.isDropable();
  }

  public boolean canBeTraded(Player player)
  {
    if (isEquipped()) {
      return false;
    }
    if (player.isGM()) {
      return true;
    }
    if ((customFlags & 0x2) == 2) {
      return false;
    }
    if (isShadowItem()) {
      return false;
    }
    if (isTemporalItem()) {
      return false;
    }
    if ((isAugmented()) && (!Config.ALT_ALLOW_DROP_AUGMENTED)) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return template.isTradeable();
  }

  public boolean canBeSold(Player player)
  {
    if ((customFlags & 0x20) == 32) {
      return false;
    }
    if (getItemId() == 57) {
      return false;
    }
    if (template.getReferencePrice() == 0) {
      return false;
    }
    if (isShadowItem()) {
      return false;
    }
    if (isTemporalItem()) {
      return false;
    }
    if ((isAugmented()) && (!Config.ALT_ALLOW_DROP_AUGMENTED)) {
      return false;
    }
    if (isEquipped()) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return template.isSellable();
  }

  public boolean canBeStored(Player player, boolean privatewh)
  {
    if ((customFlags & 0x4) == 4) {
      return false;
    }
    if (!getTemplate().isStoreable()) {
      return false;
    }
    if ((!privatewh) && ((isShadowItem()) || (isTemporalItem()))) {
      return false;
    }
    if ((!privatewh) && (isAugmented()) && (!Config.ALT_ALLOW_DROP_AUGMENTED)) {
      return false;
    }
    if (isEquipped()) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return (privatewh) || (template.isTradeable());
  }

  public boolean canBeCrystallized(Player player)
  {
    if ((customFlags & 0x8) == 8) {
      return false;
    }
    if (isShadowItem()) {
      return false;
    }
    if (isTemporalItem()) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return template.isCrystallizable();
  }

  public boolean canBeEnchanted(boolean gradeCheck)
  {
    if ((customFlags & 0x10) == 16) {
      return false;
    }
    return template.canBeEnchanted(gradeCheck);
  }

  public boolean canBeAugmented(Player player, boolean isAccessoryLifeStone)
  {
    if (!canBeEnchanted(true)) {
      return false;
    }
    if (isAugmented()) {
      return false;
    }
    if (isCommonItem()) {
      return false;
    }
    if (isTerritoryAccessory()) {
      return false;
    }
    if (getTemplate().getItemGrade().ordinal() < ItemTemplate.Grade.C.ordinal()) {
      return false;
    }
    if (!getTemplate().isAugmentable()) {
      return false;
    }
    if (isAccessory()) {
      return isAccessoryLifeStone;
    }
    if (isArmor()) {
      return Config.ALT_ALLOW_AUGMENT_ALL;
    }
    if (isWeapon()) {
      return !isAccessoryLifeStone;
    }
    return true;
  }

  public boolean canBeExchanged(Player player)
  {
    if ((customFlags & 0x20) == 32) {
      return false;
    }
    if (isShadowItem()) {
      return false;
    }
    if (isTemporalItem()) {
      return false;
    }
    if (!ItemFunctions.checkIfCanDiscard(player, this)) {
      return false;
    }
    return template.isDestroyable();
  }

  public boolean isTerritoryAccessory()
  {
    return template.isTerritoryAccessory();
  }

  public boolean isShadowItem()
  {
    return template.isShadowItem();
  }

  public boolean isTemporalItem()
  {
    return template.isTemporal();
  }

  public boolean isCommonItem()
  {
    return template.isCommonItem();
  }

  public boolean isAltSeed()
  {
    return template.isAltSeed();
  }

  public boolean isCursed()
  {
    return template.isCursed();
  }

  public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
  {
    Creature dropper = fromNpc;
    if (dropper == null) {
      dropper = lastAttacker;
    }
    Location pos = Location.findAroundPosition(dropper, 100);

    if (lastAttacker != null)
    {
      _dropPlayers = new HashIntSet(1, 2.0F);
      for (Player $member : lastAttacker.getPlayerGroup()) {
        _dropPlayers.add($member.getObjectId());
      }
      _dropTimeOwner = (System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + ((fromNpc != null) && (fromNpc.isRaid()) ? 285000 : 0));
    }

    dropMe(dropper, pos);

    if (isHerb())
      ItemsAutoDestroy.getInstance().addHerb(this);
    else if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (!isCursed()))
      ItemsAutoDestroy.getInstance().addItem(this);
  }

  public void dropToTheGround(Creature dropper, Location dropPos)
  {
    if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
      dropMe(dropper, dropPos);
    else
      dropMe(dropper, dropper.getLoc());
  }

  public void dropToTheGround(Playable dropper, Location dropPos)
  {
    setLocation(ItemLocation.VOID);
    if (getJdbcState().isPersisted())
    {
      setJdbcState(JdbcEntityState.UPDATED);
      update();
    }

    if (GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
      dropMe(dropper, dropPos);
    else
      dropMe(dropper, dropper.getLoc());
  }

  public void dropMe(Creature dropper, Location loc)
  {
    if (dropper != null) {
      setReflection(dropper.getReflection());
    }
    spawnMe0(loc, dropper);
  }

  public final void pickupMe()
  {
    decayMe();
    setReflection(ReflectionManager.DEFAULT);
  }

  public ItemTemplate.ItemClass getItemClass()
  {
    return template.getItemClass();
  }

  private int getDefence(Element element)
  {
    return isArmor() ? getAttributeElementValue(element, true) : 0;
  }

  public int getDefenceFire()
  {
    return getDefence(Element.FIRE);
  }

  public int getDefenceWater()
  {
    return getDefence(Element.WATER);
  }

  public int getDefenceWind()
  {
    return getDefence(Element.WIND);
  }

  public int getDefenceEarth()
  {
    return getDefence(Element.EARTH);
  }

  public int getDefenceHoly()
  {
    return getDefence(Element.HOLY);
  }

  public int getDefenceUnholy()
  {
    return getDefence(Element.UNHOLY);
  }

  public int getAttributeElementValue(Element element, boolean withBase)
  {
    return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
  }

  public Element getAttributeElement()
  {
    return attrs.getElement();
  }

  public int getAttributeElementValue()
  {
    return attrs.getValue();
  }

  public Element getAttackElement()
  {
    Element element = isWeapon() ? getAttributeElement() : Element.NONE;
    if (element == Element.NONE)
      for (Element e : Element.VALUES)
        if (template.getBaseAttributeValue(e) > 0)
          return e;
    return element;
  }

  public int getAttackElementValue()
  {
    return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
  }

  public void setAttributeElement(Element element, int value)
  {
    attrs.setValue(element, value);
  }

  public boolean isHerb()
  {
    return getTemplate().isHerb();
  }

  public ItemTemplate.Grade getCrystalType()
  {
    return template.getCrystalType();
  }

  public String getName()
  {
    return getTemplate().getName();
  }

  public void save()
  {
    _itemsDAO.save(this);
  }

  public void update()
  {
    _itemsDAO.update(this);
  }

  public void delete()
  {
    _itemsDAO.delete(this);
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    L2GameServerPacket packet = null;
    if (dropper != null)
      packet = new DropItem(this, dropper.getObjectId());
    else {
      packet = new SpawnItem(this);
    }
    return Collections.singletonList(packet);
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(getTemplate().getItemId());
    sb.append(" ");
    if (getEnchantLevel() > 0)
    {
      sb.append("+");
      sb.append(getEnchantLevel());
      sb.append(" ");
    }
    sb.append(getTemplate().getName());
    if (!getTemplate().getAdditionalName().isEmpty())
    {
      sb.append(" ");
      sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
    }
    sb.append(" ");
    sb.append("(");
    sb.append(getCount());
    sb.append(")");
    sb.append("[");
    sb.append(getObjectId());
    sb.append("]");

    return sb.toString();
  }

  public void setJdbcState(JdbcEntityState state)
  {
    _state = state;
  }

  public JdbcEntityState getJdbcState()
  {
    return _state;
  }

  public boolean isItem()
  {
    return true;
  }

  public ItemAttachment getAttachment()
  {
    return _attachment;
  }

  public void setAttachment(ItemAttachment attachment)
  {
    ItemAttachment old = _attachment;
    _attachment = attachment;
    if (_attachment != null)
      _attachment.setItem(this);
    if (old != null)
      old.setItem(null);
  }

  public int getAgathionEnergy()
  {
    return _agathionEnergy;
  }

  public void setAgathionEnergy(int agathionEnergy)
  {
    _agathionEnergy = agathionEnergy;
  }

  public int[] getEnchantOptions()
  {
    return _enchantOptions;
  }

  public IntSet getDropPlayers()
  {
    return _dropPlayers;
  }

  public class FuncDefence extends Func
  {
    private final Element element;

    public FuncDefence(Element element, int order, Object owner)
    {
      super(order, owner);
      this.element = element;
    }

    public void calc(Env env)
    {
      env.value += getAttributeElementValue(element, true);
    }
  }

  public class FuncAttack extends Func
  {
    private final Element element;

    public FuncAttack(Element element, int order, Object owner)
    {
      super(order, owner);
      this.element = element;
    }

    public void calc(Env env)
    {
      env.value += getAttributeElementValue(element, true);
    }
  }

  public static enum ItemLocation
  {
    VOID, 
    INVENTORY, 
    PAPERDOLL, 
    PET_INVENTORY, 
    PET_PAPERDOLL, 
    WAREHOUSE, 
    CLANWH, 
    FREIGHT, 

    LEASE, 
    MAIL;
  }
}