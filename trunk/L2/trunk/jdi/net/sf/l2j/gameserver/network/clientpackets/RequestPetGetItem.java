package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestPetGetItem extends L2GameClientPacket
{
  private static final String _C__8f_REQUESTPETGETITEM = "[C] 8F RequestPetGetItem";
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2World world = L2World.getInstance();
    L2ItemInstance item = (L2ItemInstance)world.findObject(_objectId);
    if ((item == null) || (((L2GameClient)getClient()).getActiveChar() == null))
      return;
    if ((((L2GameClient)getClient()).getActiveChar().getPet() instanceof L2SummonInstance))
    {
      sendPacket(new ActionFailed());
      return;
    }
    L2PetInstance pet = (L2PetInstance)((L2GameClient)getClient()).getActiveChar().getPet();
    if ((pet == null) || (pet.isDead()) || (pet.isOutOfControl()))
    {
      sendPacket(new ActionFailed());
      return;
    }
    pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
  }

  public String getType()
  {
    return "[C] 8F RequestPetGetItem";
  }
}