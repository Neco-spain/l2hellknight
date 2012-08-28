package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2SummonItem;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.FloodProtector;

public class SummonItems
  implements IItemHandler
{
  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) {
      return;
    }
    if (!TvTEvent.onItemSummon(playable.getObjectId())) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 3)) return;

    if ((activeChar._inEventCTF) && (CTF._started) && (!Config.CTF_ALLOW_SUMMON))
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isSitting())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
      return;
    }

    if ((activeChar.isAllSkillsDisabled()) || (activeChar.isCastingNow())) {
      return;
    }
    if (activeChar.inObserverMode()) {
      return;
    }
    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return;
    }

    L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

    if (((activeChar.getPet() != null) || (activeChar.isMounted())) && (sitem.isPetSummon()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
      return;
    }

    if (activeChar.isAttackingNow())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
      return;
    }

    if ((activeChar.isCursedWeaponEquiped()) && (sitem.isPetSummon()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
      return;
    }

    int npcID = sitem.getNpcId();

    if (npcID == 0) {
      return;
    }
    L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

    if (npcTemplate == null) {
      return;
    }
    switch (sitem.getType())
    {
    case 0:
      try
      {
        L2Spawn spawn = new L2Spawn(npcTemplate);

        if (spawn == null) {
          return;
        }
        spawn.setId(IdFactory.getInstance().getNextId());
        spawn.setLocx(activeChar.getX());
        spawn.setLocy(activeChar.getY());
        spawn.setLocz(activeChar.getZ());
        L2World.getInstance().storeObject(spawn.spawnOne(true));
        activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
        activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Target is not ingame.");
      }

    case 1:
      L2Skill skill = SkillTable.getInstance().getInfo(2046, 1);
      activeChar.useMagic(skill, false, false);
      activeChar.setLastPetNT(npcTemplate);
      activeChar.setLastPetItem(item);

      activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));

      break;
    case 2:
      if (!activeChar.disarmWeapons()) return;
      Ride mount = new Ride(activeChar.getObjectId(), 1, sitem.getNpcId());
      activeChar.sendPacket(mount);
      activeChar.broadcastPacket(mount);
      activeChar.setMountType(mount.getMountType());
      activeChar.setMountObjectID(item.getObjectId());
    }
  }

  public int[] getItemIds()
  {
    return SummonItemsData.getInstance().itemIDs();
  }
}