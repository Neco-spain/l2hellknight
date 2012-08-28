package l2m.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import l2p.commons.net.nio.impl.SendablePacket;
import l2p.commons.versioning.Version;
import l2m.gameserver.GameServer;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.Element;
import l2m.gameserver.model.base.MultiSellIngredient;
import l2m.gameserver.model.items.ItemAttributes;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2GameServerPacket extends SendablePacket<GameClient>
  implements IStaticPacket
{
  private static final Logger _log = LoggerFactory.getLogger(L2GameServerPacket.class);

  public final boolean write()
  {
    try
    {
      writeImpl();
      return true;
    }
    catch (Exception e)
    {
      _log.error("Client: " + getClient() + " - Failed writing: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
    }
    return false;
  }

  protected abstract void writeImpl();

  protected void writeEx(int value) {
    writeC(254);
    writeH(value);
  }

  protected void writeD(boolean b)
  {
    writeD(b ? 1 : 0);
  }

  protected void writeDD(int[] values, boolean sendCount)
  {
    if (sendCount)
      getByteBuffer().putInt(values.length);
    for (int value : values)
      getByteBuffer().putInt(value);
  }

  protected void writeDD(int[] values)
  {
    writeDD(values, false);
  }

  protected void writeItemInfo(ItemInstance item)
  {
    writeItemInfo(item, item.getCount());
  }

  protected void writeItemInfo(ItemInstance item, long count)
  {
    writeD(item.getObjectId());
    writeD(item.getItemId());
    writeD(item.getEquipSlot());
    writeQ(count);
    writeH(item.getTemplate().getType2ForPackets());
    writeH(item.getCustomType1());
    writeH(item.isEquipped() ? 1 : 0);
    writeD(item.getBodyPart());
    writeH(item.getEnchantLevel());
    writeH(item.getCustomType2());
    writeD(item.getAugmentationId());
    writeD(item.getShadowLifeTime());
    writeD(item.getTemporalLifeTime());
    writeH(item.getAttackElement().getId());
    writeH(item.getAttackElementValue());
    writeH(item.getDefenceFire());
    writeH(item.getDefenceWater());
    writeH(item.getDefenceWind());
    writeH(item.getDefenceEarth());
    writeH(item.getDefenceHoly());
    writeH(item.getDefenceUnholy());
    writeH(item.getEnchantOptions()[0]);
    writeH(item.getEnchantOptions()[1]);
    writeH(item.getEnchantOptions()[2]);
  }

  protected void writeItemInfo(ItemInfo item)
  {
    writeItemInfo(item, item.getCount());
  }

  protected void writeItemInfo(ItemInfo item, long count)
  {
    writeD(item.getObjectId());
    writeD(item.getItemId());
    writeD(item.getEquipSlot());
    writeQ(count);
    writeH(item.getItem().getType2ForPackets());
    writeH(item.getCustomType1());
    writeH(item.isEquipped() ? 1 : 0);
    writeD(item.getItem().getBodyPart());
    writeH(item.getEnchantLevel());
    writeH(item.getCustomType2());
    writeD(item.getAugmentationId());
    writeD(item.getShadowLifeTime());
    writeD(item.getTemporalLifeTime());
    writeH(item.getAttackElement());
    writeH(item.getAttackElementValue());
    writeH(item.getDefenceFire());
    writeH(item.getDefenceWater());
    writeH(item.getDefenceWind());
    writeH(item.getDefenceEarth());
    writeH(item.getDefenceHoly());
    writeH(item.getDefenceUnholy());
    writeH(item.getEnchantOptions()[0]);
    writeH(item.getEnchantOptions()[1]);
    writeH(item.getEnchantOptions()[2]);
  }

  protected void writeItemElements(MultiSellIngredient item)
  {
    if (item.getItemId() <= 0)
    {
      writeItemElements();
      return;
    }
    ItemTemplate i = ItemHolder.getInstance().getTemplate(item.getItemId());
    if (item.getItemAttributes().getValue() > 0)
    {
      if (i.isWeapon())
      {
        Element e = item.getItemAttributes().getElement();
        writeH(e.getId());
        writeH(item.getItemAttributes().getValue(e) + i.getBaseAttributeValue(e));
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
      }
      else if (i.isArmor())
      {
        writeH(-1);
        writeH(0);
        for (Element e : Element.VALUES)
          writeH(item.getItemAttributes().getValue(e) + i.getBaseAttributeValue(e));
      }
      else {
        writeItemElements();
      }
    }
    else writeItemElements();
  }

  protected void writeItemElements()
  {
    writeH(-1);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
  }

  public String getType()
  {
    return "[S] " + getClass().getSimpleName();
  }

  public L2GameServerPacket packet(Player player)
  {
    return this;
  }
}