package com.l2js.gameserver.model.base;

/**
 * Created by IntelliJ IDEA.
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 0:10:14
 * To change this template use File | Settings | File Templates.
 */
public final class UsablePacketItem
{
    private final int _itemId;
    private final int _count;

    public UsablePacketItem(int itemId, int count)
    {
        _itemId = itemId;
        _count = count;
    }

    public int itemId()
    {
        return _itemId;
    }

    public int count()
    {
        return _count;
    }
}
