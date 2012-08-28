package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.knownlist.NullKnownList;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowTownMap;

public class L2StaticObjectInstance extends L2Object
{
  private static Logger _log = Logger.getLogger(L2StaticObjectInstance.class.getName());
  public static final int INTERACTION_DISTANCE = 150;
  private int _staticObjectId;
  private int _type = -1;
  private int _x;
  private int _y;
  private String _texture;

  public int getStaticObjectId()
  {
    return _staticObjectId;
  }

  public void setStaticObjectId(int StaticObjectId)
  {
    _staticObjectId = StaticObjectId;
  }

  public L2StaticObjectInstance(int objectId)
  {
    super(objectId);
    setKnownList(new NullKnownList(this));
  }

  public int getType()
  {
    return _type;
  }

  public void setType(int type)
  {
    _type = type;
  }

  public void setMap(String texture, int x, int y)
  {
    _texture = ("town_map." + texture);
    _x = x;
    _y = y;
  }

  private int getMapX()
  {
    return _x;
  }

  private int getMapY()
  {
    return _y;
  }

  public void onAction(L2PcInstance player)
  {
    if (_type < 0) _log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + getStaticObjectId());

    if (this != player.getTarget())
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
    }
    else
    {
      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      if (!player.isInsideRadius(this, 150, false, false))
      {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);

        player.sendPacket(new ActionFailed());
      } else {
        if (_type == 2) {
          String filename = "data/html/signboard.htm";
          String content = HtmCache.getInstance().getHtm(filename);
          NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

          if (content == null) html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>"); else {
            html.setHtml(content);
          }
          player.sendPacket(html);
          player.sendPacket(new ActionFailed());
        } else if (_type == 0) { player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
        }
        player.sendPacket(new ActionFailed());
      }
    }
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }
}