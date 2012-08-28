package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.FakePlayersTablePlus;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2SummonItem;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.stat.PetStat;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import scripts.items.IItemHandler;

public class SummonItems
  implements IItemHandler
{
  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }

    if (!TvTEvent.onItemSummon(playable.getName())) {
      return;
    }

    L2PcInstance activeChar = (L2PcInstance)playable;

    if (activeChar.isSitting()) {
      activeChar.sendPacket(Static.CANT_MOVE_SITTING);
      return;
    }

    if (activeChar.inObserverMode()) {
      return;
    }

    if (activeChar.isInOlympiadMode()) {
      activeChar.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return;
    }

    L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

    if ((sitem.getType() != 3) && ((activeChar.getPet() != null) || (activeChar.isMounted())) && (sitem.isPetSummon())) {
      activeChar.sendPacket(Static.YOU_ALREADY_HAVE_A_PET);
      return;
    }

    if (activeChar.isAttackingNow()) {
      activeChar.sendPacket(Static.YOU_CANNOT_SUMMON_IN_COMBAT);
      return;
    }

    if ((activeChar.isCursedWeaponEquiped()) && (sitem.isPetSummon())) {
      activeChar.sendPacket(Static.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
      return;
    }

    int npcID = sitem.getNpcId();

    if (npcID == 0) {
      return;
    }

    L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);
    if ((sitem.getType() != 3) && (npcTemplate == null)) {
      return;
    }

    switch (sitem.getType()) {
    case 0:
      try {
        L2Spawn spawn = new L2Spawn(npcTemplate);

        if (spawn == null) {
          return;
        }

        spawn.setId(IdFactory.getInstance().getNextId());
        spawn.setLocx(activeChar.getX());
        spawn.setLocy(activeChar.getY());
        spawn.setLocz(activeChar.getZ());
        L2World.getInstance().storeObject(spawn.spawnOne());
        activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
      }
      catch (Exception e) {
        activeChar.sendMessage("Target is not ingame.");
      }

    case 1:
      L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);

      if (petSummon == null)
      {
        break;
      }
      petSummon.setTitle(activeChar.getName());

      if (!petSummon.isRespawned()) {
        petSummon.setCurrentHp(petSummon.getMaxHp());
        petSummon.setCurrentMp(petSummon.getMaxMp());
        petSummon.getStat().setExp(petSummon.getExpForThisLevel());
        petSummon.setCurrentFed(petSummon.getMaxFed());
      }

      petSummon.setRunning();

      if (!petSummon.isRespawned()) {
        petSummon.store();
      }

      activeChar.setPet(petSummon);

      activeChar.sendPacket(new MagicSkillUser(activeChar, 2046, 1, 1000, 600000));
      activeChar.sendPacket(Static.SUMMON_A_PET);
      L2World.getInstance().storeObject(petSummon);
      petSummon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
      petSummon.startFeed(false);
      item.setEnchantLevel(petSummon.getLevel());

      ThreadPoolManager.getInstance().scheduleAi(new PetSummonFinalizer(activeChar, petSummon), 900L, true);

      if (petSummon.getCurrentFed() <= 0)
        ThreadPoolManager.getInstance().scheduleAi(new PetSummonFeedWait(activeChar, petSummon), 60000L, true);
      else {
        petSummon.startFeed(false);
      }

      break;
    case 2:
      if (!activeChar.disarmWeapons()) {
        return;
      }

      if ((activeChar.getPvpFlag() != 0) || (activeChar.isInPVPArena())) {
        return;
      }

      Ride mount = new Ride(activeChar.getObjectId(), 1, sitem.getNpcId());
      activeChar.broadcastPacket(mount);
      activeChar.setMountType(mount.getMountType());
      activeChar.setMountObjectID(item.getObjectId());
      break;
    case 3:
      if (activeChar.getPartner() != null)
      {
        activeChar.sendPacket(Static.ALREADY_HAVE_PARTNER);
        return;
      }

      L2PcInstance partner = L2PcInstance.restoreFake(IdFactory.getInstance().getNextId(), npcID, true);
      if (partner == null)
      {
        break;
      }
      partner.setName("*" + activeChar.getName() + "*");
      partner.setTitle("");
      partner.setOwner(activeChar);
      partner.setXYZInvisible(activeChar.getX(), activeChar.getY(), activeChar.getZ());
      partner.setFakeLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ());

      switch (npcID)
      {
      case 92:
      case 102:
      case 109:
        partner.setPartnerClass(1);
        FakePlayersTablePlus.getInstance().wearArcher(partner);
        break;
      default:
        FakePlayersTablePlus.getInstance().wearFantom(partner);
      }

      partner.setTitle(activeChar.getName());

      partner.setCurrentHp(partner.getMaxHp());
      partner.setCurrentMp(partner.getMaxMp());

      partner.setRunning();

      activeChar.setPartner(partner);

      activeChar.sendPacket(new MagicSkillUser(activeChar, 2046, 1, 1000, 600000));
      L2World.getInstance().storeObject(partner);
      partner.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());

      item.setEnchantLevel(partner.getLevel());

      ThreadPoolManager.getInstance().scheduleAi(new PartnerSummonFinalizer(activeChar, partner, null), 900L, true);
    }
  }

  public int[] getItemIds()
  {
    return SummonItemsData.getInstance().itemIDs();
  }

  static class PartnerSummonFinalizer
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private L2PcInstance _partner;

    private PartnerSummonFinalizer(L2PcInstance activeChar, L2PcInstance partner)
    {
      _activeChar = activeChar;
      _partner = partner;
    }

    public void run()
    {
      try {
        _activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
        _partner.setFollowStatus(true);
        _partner.setShowSummonAnimation(false);

        _partner.setCurrentHp(_partner.getMaxHp());
        _partner.setCurrentCp(_partner.getMaxCp());
        _partner.setCurrentMp(_partner.getMaxMp());
      }
      catch (Throwable e)
      {
      }
    }
  }

  static class PetSummonFinalizer
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private L2PetInstance _petSummon;

    PetSummonFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
    {
      _activeChar = activeChar;
      _petSummon = petSummon;
    }

    public void run() {
      try {
        _activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
        _petSummon.setFollowStatus(true);
        _petSummon.setShowSummonAnimation(false);
      }
      catch (Throwable e)
      {
      }
    }
  }

  static class PetSummonFeedWait
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private L2PetInstance _petSummon;

    PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
    {
      _activeChar = activeChar;
      _petSummon = petSummon;
    }

    public void run() {
      try {
        if (_petSummon.getCurrentFed() <= 0)
          _petSummon.unSummon(_activeChar);
        else
          _petSummon.startFeed(false);
      }
      catch (Throwable e)
      {
      }
    }
  }
}