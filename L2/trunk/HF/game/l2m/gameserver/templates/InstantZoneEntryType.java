package l2m.gameserver.templates;

import l2m.gameserver.data.xml.holder.InstantZoneHolder;
import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.CommandChannel;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.model.quest.QuestState;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.ItemFunctions;

public enum InstantZoneEntryType
{
  SOLO, 

  PARTY, 

  COMMAND_CHANNEL;

  public abstract boolean canEnter(Player paramPlayer, InstantZone paramInstantZone);

  public abstract boolean canReEnter(Player paramPlayer, InstantZone paramInstantZone);

  private static SystemMsg checkPlayer(Player player, InstantZone instancedZone)
  {
    if (player.getActiveReflection() != null) {
      return SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON;
    }
    if ((player.getLevel() < instancedZone.getMinLevel()) || (player.getLevel() > instancedZone.getMaxLevel())) {
      return SystemMsg.C1S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY;
    }
    if ((player.isCursedWeaponEquipped()) || (player.isInFlyingTransform())) {
      return SystemMsg.YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;
    }
    if (InstantZoneHolder.getInstance().getMinutesToNextEntrance(instancedZone.getId(), player) > 0) {
      return SystemMsg.C1_MAY_NOT_REENTER_YET;
    }
    if ((instancedZone.getRemovedItemId() > 0) && (instancedZone.getRemovedItemNecessity()) && (ItemFunctions.getItemCount(player, instancedZone.getRemovedItemId()) < 1L)) {
      return SystemMsg.C1S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED;
    }
    if (instancedZone.getRequiredQuestId() > 0)
    {
      Quest q = QuestManager.getQuest(instancedZone.getRequiredQuestId());
      QuestState qs = player.getQuestState(q.getClass());
      if ((qs == null) || (qs.getState() != 2)) {
        return SystemMsg.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED;
      }
    }
    return null;
  }
}