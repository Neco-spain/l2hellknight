package net.sf.l2j.gameserver.model;

import java.util.Set;
import javolution.util.FastSet;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class BlockList
{
  private final Set<String> _blockSet;
  private boolean _blockAll;

  public BlockList()
  {
    _blockSet = new FastSet();
    _blockAll = false;
  }

  private void addToBlockList(L2PcInstance character)
  {
    if (character != null)
    {
      _blockSet.add(character.getName());
    }
  }

  private void removeFromBlockList(L2PcInstance character)
  {
    if (character != null)
    {
      _blockSet.remove(character.getName());
    }
  }

  private boolean isInBlockList(L2PcInstance character)
  {
    return _blockSet.contains(character.getName());
  }

  private boolean isBlockAll()
  {
    return _blockAll;
  }

  public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance character)
  {
    BlockList blockList = listOwner.getBlockList();
    return (blockList.isBlockAll()) || (blockList.isInBlockList(character));
  }

  private void setBlockAll(boolean state)
  {
    _blockAll = state;
  }

  private Set<String> getBlockList()
  {
    return _blockSet;
  }

  public static void addToBlockList(L2PcInstance listOwner, L2PcInstance character)
  {
    listOwner.getBlockList().addToBlockList(character);
    character.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addString(listOwner.getName()));
    listOwner.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addString(character.getName()));
  }

  public static void removeFromBlockList(L2PcInstance listOwner, L2PcInstance character)
  {
    listOwner.getBlockList().removeFromBlockList(character);
    listOwner.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(character.getName()));
  }

  public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance character)
  {
    return listOwner.getBlockList().isInBlockList(character);
  }

  public static boolean isBlockAll(L2PcInstance listOwner)
  {
    return listOwner.getBlockList().isBlockAll();
  }

  public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
  {
    listOwner.getBlockList().setBlockAll(newValue);
  }

  public static void sendListToOwner(L2PcInstance listOwner)
  {
    for (String playerName : listOwner.getBlockList().getBlockList())
    {
      listOwner.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString(playerName));
    }
  }
}