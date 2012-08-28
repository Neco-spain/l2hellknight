package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestPetGetItem extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2World world = L2World.getInstance();
    L2ItemInstance item = (L2ItemInstance)world.findObject(_objectId);

    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if ((item == null) || (player == null)) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPY() < 100L)
    {
      player.sendActionFailed();
      return;
    }

    player.sCPY();

    if (player.getPet().isSummon())
    {
      player.sendActionFailed();
      return;
    }
    L2PetInstance pet = (L2PetInstance)player.getPet();
    if ((pet == null) || (pet.isDead()) || (pet.isOutOfControl()))
    {
      player.sendActionFailed();
      return;
    }
    pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
  }
}