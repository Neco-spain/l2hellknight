package com.l2js.gameserver.network.clientpackets;

import com.l2js.gameserver.datatables.CharNameTable;
import com.l2js.gameserver.network.serverpackets.ExIsCharNameCreatable;

public class RequestCharacterNameCreatable extends L2GameClientPacket 
{ 
    protected String _name; 
     
    @Override 
    protected void readImpl() 
    { 
        _name = readS(); 
    } 

    @Override 
    protected void runImpl() 
    { 
        sendPacket(new ExIsCharNameCreatable(CharNameTable.getInstance().doesCharNameExist(_name) ? 0 : 1)); 
    } 

    @Override 
    public String getType() 
    { 
        return "[C] B0 RequestCharacterNameCreatable"; 
    } 
}