package l2p.gameserver.serverpackets;

public class ExChangeAttributeInfo extends L2GameServerPacket {
    private int objid, _id;

    public ExChangeAttributeInfo(int obj, int id) {
        objid = obj;
        _id = id;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x118);
        writeD(33502);//??unk?? Не уверен, но наверно ИД кристалла, который меняет атрибут
        writeD(_id); //Доступные атрибуты (маска. Огонь 1, Вода 2, Ветер 4, Земля 8, Святость 16, Тьма 32. Если вам например нужно сделать чтобы можно было выбрать огонь, воду, землю, святость, тьму = (1 | 2 | 8 | 16 | 32))
        writeD(objid);//??unk?? Не уверен, но наверно ОбджИД итема, в котором меняете атрибут
    }
}