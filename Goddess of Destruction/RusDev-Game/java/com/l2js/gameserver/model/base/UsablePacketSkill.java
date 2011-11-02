package com.l2js.gameserver.model.base;

//Bacek

public final class UsablePacketSkill
{
    private final int _id;
    private final int _level;

    public UsablePacketSkill(int id, int level)
    {
        _id = id;
        _level = level;
    }

    public int id()
    {
        return _id;
    }

    public int level()
    {
        return _level;
    }
}