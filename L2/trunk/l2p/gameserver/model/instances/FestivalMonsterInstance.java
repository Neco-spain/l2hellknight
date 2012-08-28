package l2p.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.reward.RewardList;
import l2p.gameserver.model.reward.RewardType;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.ItemFunctions;

public class FestivalMonsterInstance extends MonsterInstance
{
  public static final long serialVersionUID = 1L;
  protected int _bonusMultiplier = 1;

  public FestivalMonsterInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);

    _hasRandomWalk = false;
  }

  public void setOfferingBonus(int bonusMultiplier)
  {
    _bonusMultiplier = bonusMultiplier;
  }

  protected void onSpawn()
  {
    super.onSpawn();

    List pl = World.getAroundPlayers(this);
    if (pl.isEmpty())
      return;
    List alive = new ArrayList(9);
    for (Player p : pl)
      if (!p.isDead())
        alive.add(p);
    if (alive.isEmpty()) {
      return;
    }
    Player target = (Player)alive.get(Rnd.get(alive.size()));
    getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, Integer.valueOf(1));
  }

  public void rollRewards(Map.Entry<RewardType, RewardList> entry, Creature lastAttacker, Creature topDamager)
  {
    super.rollRewards(entry, lastAttacker, topDamager);

    if (entry.getKey() != RewardType.RATED_GROUPED)
      return;
    if (!topDamager.isPlayable()) {
      return;
    }
    Player topDamagerPlayer = topDamager.getPlayer();
    Party associatedParty = topDamagerPlayer.getParty();

    if (associatedParty == null) {
      return;
    }
    Player partyLeader = associatedParty.getPartyLeader();
    if (partyLeader == null) {
      return;
    }
    ItemInstance bloodOfferings = ItemFunctions.createItem(5901);

    bloodOfferings.setCount(_bonusMultiplier);
    partyLeader.getInventory().addItem(bloodOfferings);
    partyLeader.sendPacket(SystemMessage2.obtainItems(5901, _bonusMultiplier, 0));
  }

  public boolean isAggressive()
  {
    return true;
  }

  public int getAggroRange()
  {
    return 1000;
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public boolean canChampion()
  {
    return false;
  }
}