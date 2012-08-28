package net.sf.l2j.gameserver.instancemanager.clanhallsiege;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.taskmanager.ExclusiveTask;

/*
 * Author: Maxi
 */
public class FortressOfTheDeadManager extends ClanHallSiege
{
        protected static final Logger _log = Logger.getLogger(FortressOfTheDeadManager.class.getName());
        private static FortressOfTheDeadManager _instance;
        private boolean _registrationPeriod = true;
        private int _clanCounter = 0;
        private Map<Integer, clansInfo> _clansInfo = new HashMap<Integer, clansInfo>();
        @SuppressWarnings("unused")
        private L2ClanHallZone zone;
        public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(64);
        @SuppressWarnings("unused")
        private clansInfo _ownerClanInfo = new clansInfo();
        private Map<Integer, DamageInfo> _clansDamageInfo;
        private L2Clan _clan;
        
        private class DamageInfo
        {
                public L2Clan _clan;
                public long _damage;
        }
        
        public static final FortressOfTheDeadManager getInstance()
        {
                if (_instance == null)
                        _instance = new FortressOfTheDeadManager();
                return _instance;
        }
        
        private class clansInfo
        {
                public String _clanName;
                public FastList<String> _clans = new FastList<String>();
        }
        
        private FortressOfTheDeadManager()
        {
                _log.info("Fortress of The Dead");
                long siegeDate = restoreSiegeDate(64);
                Calendar tmpDate = Calendar.getInstance();
                tmpDate.setTimeInMillis(siegeDate);
                setSiegeDate(tmpDate);
                setNewSiegeDate(siegeDate, 64, 22);
                _clansDamageInfo = new HashMap<Integer, DamageInfo>();
                // Schedule siege auto start
                _startSiegeTask.schedule(1000);
        }
        
        public void startSiege()
        {
                
                        setRegistrationPeriod(false);
                        if (_clansInfo.size() == 0)
                        {
                                endSiege(false);
                                return;
                        }
                        if (_clansInfo.size() == 1 && clanhall.getOwnerId() == 0)
                        {
                                endSiege(false);
                                return;
                        }
                        if (_clansInfo.size() == 1 && clanhall.getOwnerId() != 0)
                        {
                                @SuppressWarnings("unused")
                                L2Clan clan = null;
                                for (clansInfo a : _clansInfo.values())
                                        clan = ClanTable.getInstance().getClanByName(a._clanName);
                                setIsInProgress(true);
                                _siegeEndDate = Calendar.getInstance();
                                _siegeEndDate.add(Calendar.MINUTE, 60);
                                _endSiegeTask.schedule(1000);
                                return;
                        }
                        if (!_clansDamageInfo.isEmpty())
                                _clansDamageInfo.clear();
                        setIsInProgress(true);
                        ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(64);
                        if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
                        {
                                ClanTable.getInstance().getClan(clanhall.getOwnerId()).broadcastClanStatus();
                                ClanHallManager.getInstance().setFree(clanhall.getId());
                                clanhall.banishForeigners();
                        }
                        _siegeEndDate = Calendar.getInstance();
                        _siegeEndDate.add(Calendar.MINUTE, 60);
                        _endSiegeTask.schedule(1000);
                
        }
        
        public void endSiege(boolean type)
        {
                setIsInProgress(false);
                if (type = true)
                {
                        L2Clan clanIdMaxDamage = null;
                        long tempMaxDamage = 0;
                        for (DamageInfo damageInfo : _clansDamageInfo.values())
                        {
                                if (damageInfo != null)
                                {
                                        if (damageInfo._damage > tempMaxDamage)
                                        {
                                                tempMaxDamage = damageInfo._damage;
                                                clanIdMaxDamage = damageInfo._clan;
                                        }
                                }
                        }
                        if (clanIdMaxDamage != null)
                        {
                                ClanHall clanhall = null;
                                clanhall = ClanHallManager.getInstance().getClanHallById(64);
                                ClanHallManager.getInstance().setOwner(clanhall.getId(), clanIdMaxDamage);
                                _clansInfo.clear();
                                _clanCounter = 0;
                                _clan.setReputationScore(_clan.getReputationScore()+600, true);;
                        }
                        _log.info("The siege of Fortress of the Dead has finished");
                }
                setNewSiegeDate(getSiegeDate().getTimeInMillis(), 64, 22);
                _startSiegeTask.schedule(1000);
        }
        
        public L2Clan checkHaveWinner()
        {
                @SuppressWarnings("unused")
                L2Clan res = null;
                for (String clanName : getRegisteredClans())
                {
                        @SuppressWarnings("unused")
                        L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
                }
                return null;
        }
        
        public void setRegistrationPeriod(boolean par)
        {
                _registrationPeriod = par;
        }
        
        public boolean isRegistrationPeriod()
        {
                return _registrationPeriod;
        }
        
        public boolean isClanRegister(L2Clan Clan, String clanName)
        {
                if (Clan == null)
                        return false;
                clansInfo regClans = _clansInfo.get(Clan.getClanId());
                if (regClans != null)
                        if (regClans._clans.contains(clanName))
                                return true;
                return false;
        }
        
        public boolean isClanOnSiege(L2Clan Clan)
        {
                if (Clan.getClanId() == clanhall.getOwnerId())
                        return true;
                clansInfo regClans = _clansInfo.get(Clan.getClanId());
                if (regClans == null)
                {
                        return false;
                }
                return true;
        }
        
        public synchronized int registerClanOnSiege(L2PcInstance player, L2Clan Clan)
        {
                if (_clanCounter == 5)
                        return 2;
                {
                        _clanCounter++;
                        clansInfo regClans = _clansInfo.get(Clan.getClanId());
                        if (regClans == null)
                                regClans = new clansInfo();
                        regClans._clanName = Clan.getName();
                        _clansInfo.put(Clan.getClanId(), regClans);
                }
                return 1;
        }
        
        public boolean unRegisterClan(L2Clan Clan)
        {
                if (_clansInfo.remove(Clan.getClanId()) != null)
                {
                        _clanCounter--;
                        return true;
                }
                return false;
        }
        
        public FastList<String> getRegisteredClans()
        {
                FastList<String> clans = new FastList<String>();
                for (clansInfo a : _clansInfo.values())
                {
                        clans.add(a._clanName);
                }
                return clans;
        }
        
        private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
        {
                @Override
                protected void onElapsed()
                {
                        if (getIsInProgress())
                        {
                                cancel();
                                return;
                        }
                        final long timeRemaining = getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
                        if (timeRemaining <= 0)
                        {
                                startSiege();
                                cancel();
                                return;
                        }
                        schedule(timeRemaining);
                }
        };
        private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
        {
                @Override
                protected void onElapsed()
                {
                        if (!getIsInProgress())
                        {
                                cancel();
                                return;
                        }
                        final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
                        if (timeRemaining <= 0)
                        {
                                endSiege(false);
                                cancel();
                                return;
                        }
                        schedule(timeRemaining);
                }
        };
        
        public void addSiegeDamage(L2Clan clan, double damage)
        {
                setIsInProgress(true);
                for (String clanName : getRegisteredClans())
                {
                        clan = ClanTable.getInstance().getClanByName(clanName);
                        DamageInfo clanDamage = _clansDamageInfo.get(clan.getClanId());
                        if (clanDamage != null)
                                clanDamage._damage += damage;
                        else
                        {
                                clanDamage = new DamageInfo();
                                clanDamage._clan = clan;
                                clanDamage._damage += damage;
                                _clansDamageInfo.put(clan.getClanId(), clanDamage);
                        }
                }
        }
        
        public int getClansCount(String Clan)
        {
                for (clansInfo a : _clansInfo.values())
                        if (a._clanName == Clan)
                                return a._clans.size();
                return 0;
        }
}