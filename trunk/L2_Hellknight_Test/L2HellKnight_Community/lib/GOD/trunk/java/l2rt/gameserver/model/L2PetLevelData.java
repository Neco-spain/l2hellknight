package l2rt.gameserver.model;


public class L2PetLevelData
{

    public L2PetLevelData()
    {
    }

    public long getPetMaxExp()
    {
        return _petMaxExp;
    }

    public void setPetMaxExp(long pPetMaxExp)
    {
        _petMaxExp = pPetMaxExp;
    }

    public int getOwnerExpTaken()
    {
        return _ownerExpTaken;
    }

    public void setOwnerExpTaken(int pOwnerExpTaken)
    {
        _ownerExpTaken = pOwnerExpTaken;
    }

    public int getPetMaxHP()
    {
        return _petMaxHP;
    }

    public void setPetMaxHP(int pPetMaxHP)
    {
        _petMaxHP = pPetMaxHP;
    }

    public int getPetMaxMP()
    {
        return _petMaxMP;
    }

    public void setPetMaxMP(int pPetMaxMP)
    {
        _petMaxMP = pPetMaxMP;
    }

    public int getPetPAtk()
    {
        return _petPAtk;
    }

    public void setPetPAtk(int pPetPAtk)
    {
        _petPAtk = pPetPAtk;
    }

    public int getPetPDef()
    {
        return _petPDef;
    }

    public void setPetPDef(int pPetPDef)
    {
        _petPDef = pPetPDef;
    }

    public int getPetMAtk()
    {
        return _petMAtk;
    }

    public void setPetMAtk(int pPetMAtk)
    {
        _petMAtk = pPetMAtk;
    }

    public int getPetMDef()
    {
        return _petMDef;
    }

    public void setPetMDef(int pPetMDef)
    {
        _petMDef = pPetMDef;
    }

    public int getPetMaxFeed()
    {
        return _petMaxFeed;
    }

    public void setPetMaxFeed(int pPetMaxFeed)
    {
        _petMaxFeed = pPetMaxFeed;
    }

    public int getPetFeedNormal()
    {
        return _petFeedNormal;
    }

    public void setPetFeedNormal(int pPetFeedNormal)
    {
        _petFeedNormal = pPetFeedNormal;
    }

    public int getPetFeedBattle()
    {
        return _petFeedBattle;
    }

    public void setPetFeedBattle(int pPetFeedBattle)
    {
        _petFeedBattle = pPetFeedBattle;
    }

    public int getPetRegenHP()
    {
        return _petRegenHP;
    }

    public void setPetRegenHP(int pPetRegenHP)
    {
        _petRegenHP = pPetRegenHP;
    }

    public int getPetRegenMP()
    {
        return _petRegenMP;
    }

    public void setPetRegenMP(int pPetRegenMP)
    {
        _petRegenMP = pPetRegenMP;
    }

    public short getPetSoulShot()
    {
        return _petSoulShot;
    }

    public void setPetSoulShot(short soulShot)
    {
        _petSoulShot = soulShot;
    }

    public short getPetSpiritShot()
    {
        return _petSpiritShot;
    }

    public void setPetSpiritShot(short spiritShot)
    {
        _petSpiritShot = spiritShot;
    }

    private int _ownerExpTaken;
    private long _petMaxExp;
    private int _petMaxHP;
    private int _petMaxMP;
    private int _petPAtk;
    private int _petPDef;
    private int _petMAtk;
    private int _petMDef;
    private int _petMaxFeed;
    private int _petFeedBattle;
    private int _petFeedNormal;
    private int _petRegenHP;
    private int _petRegenMP;
    private short _petSoulShot;
    private short _petSpiritShot;
}
