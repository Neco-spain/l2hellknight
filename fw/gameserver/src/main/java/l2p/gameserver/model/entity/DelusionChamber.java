package l2p.gameserver.model.entity;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.instancemanager.DelusionChamberManager;
import l2p.gameserver.instancemanager.DelusionChamberManager.DelusionChamberRoom;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class DelusionChamber extends Reflection {
    private Future<?> killChamberTask;

    protected static final int MILLISECONDS_IN_MINUTE = 60000;
    protected int _roomType;
    protected int _choosenRoom = -1;
    protected boolean isBossRoom = false;

    protected List<Integer> _completedRooms = new ArrayList<Integer>();

    protected int jumps_current = 0;
    protected boolean _hasJumped = false;

    private Future<?> teleporterTask;
    private Future<?> spawnTask;

    public DelusionChamber(Party party, int type, int room) {
        super();
        onCreate();
        startCollapseTimer(7200000); // 120 минут таймер, для защиты от утечек памяти
        InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(type + 120); // Для равенства типа комнаты и ИД инстанса
        setInstancedZone(iz);
        setName(iz.getName());
        _roomType = type;
        setParty(party);
        party.setReflection(this);
        _choosenRoom = room;
        checkBossRoom(_choosenRoom);

        Location coords = getRoomCoord(_choosenRoom);

        setReturnLoc(party.getPartyLeader().getLoc());
        setTeleportLoc(coords);
        for (Player p : party.getPartyMembers()) {
            p.setVar("backCoords", getReturnLoc().toXYZString(), -1);
            DelusionChamberManager.teleToLocation(p, Location.findPointToStay(coords, 50, 100, getGeoIndex()), this);
            p.setReflection(this);
        }

        createSpawnTimer(_choosenRoom);
        createTeleporterTimer();
    }

    public void createSpawnTimer(int room) {
        if (spawnTask != null) {
            spawnTask.cancel(false);
            spawnTask = null;
        }

        final DelusionChamberRoom riftRoom = DelusionChamberManager.getInstance().getRoom(_roomType, room);

        spawnTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                for (SimpleSpawner s : riftRoom.getSpawns()) {
                    SimpleSpawner sp = s.clone();
                    sp.setReflection(DelusionChamber.this);
                    addSpawn(sp);
                    if (!isBossRoom)
                        sp.startRespawn();
                    for (int i = 0; i < sp.getAmount(); i++)
                        sp.doSpawn(true);
                }
                DelusionChamber.this.addSpawnWithoutRespawn(getManagerId(), riftRoom.getTeleportCoords(), 0);
            }
        }, 10000);
    }

    protected void createTeleporterTimer() {
        if (teleporterTask != null) {
            teleporterTask.cancel(false);
            teleporterTask = null;
        }

        teleporterTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                if (jumps_current < 4 && getPlayersInside(true) > 0) {
                    jumps_current++;
                    teleportToNextRoom();
                    createTeleporterTimer();
                } else
                    createNewKillChamberTimer();
            }
        }, calcTimeToNextJump()); //Teleporter task, 8-10 minutes
    }

    protected long calcTimeToNextJump() {
        if (isBossRoom)
            return 60 * MILLISECONDS_IN_MINUTE;
        return 8 * MILLISECONDS_IN_MINUTE + Rnd.get(120000);
    }

    public Location getRoomCoord(int room) {
        return DelusionChamberManager.getInstance().getRoom(_roomType, room).getTeleportCoords();
    }

    public synchronized void createNewKillChamberTimer() {
        if (killChamberTask != null) {
            killChamberTask.cancel(false);
            killChamberTask = null;
        }

        killChamberTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                if (getParty() != null && !getParty().getPartyMembers().isEmpty())
                    for (Player p : getParty().getPartyMembers())
                        if (p.getReflection() == DelusionChamber.this) {
                            String var = p.getVar("backCoords");
                            if (var == null || var.equals(""))
                                continue;
                            p.teleToLocation(Location.parseLoc(var), ReflectionManager.DEFAULT);
                            p.unsetVar("backCoords");
                        }
                collapse();
            }
        }, 100L);
    }

    public int getType() {
        return _roomType;
    }

    protected void teleportToNextRoom() {
        _completedRooms.add(_choosenRoom);

        for (Spawner s : getSpawns())
            s.deleteAll();

        int size = DelusionChamberManager.getInstance().getRooms(_roomType).size();
        /*
          if(jumps_current < getMaxJumps())
              size--; // комната босса может быть только последней
           */

        if (getType() >= 11 && jumps_current == 4)
            _choosenRoom = 9; // В DC последние 2 печати всегда кончаются рейдом
        else { // выбираем комнату, где еще не были
            List<Integer> notCompletedRooms = new ArrayList<Integer>();
            for (int i = 1; i <= size; i++)
                if (!_completedRooms.contains(i))
                    notCompletedRooms.add(i);
            _choosenRoom = notCompletedRooms.get(Rnd.get(notCompletedRooms.size()));
        }

        checkBossRoom(_choosenRoom);
        setTeleportLoc(getRoomCoord(_choosenRoom));

        for (Player p : getParty().getPartyMembers())
            if (p.getReflection() == this)
                DelusionChamberManager.teleToLocation(p, Location.findPointToStay(getRoomCoord(_choosenRoom), 50, 100, DelusionChamber.this.getGeoIndex()), this);

        createSpawnTimer(_choosenRoom);
    }

    public void partyMemberExited(Player player) {
        if (getPlayersInside(false) < 2 || getPlayersInside(true) == 0) {
            createNewKillChamberTimer();
            return;
        }
    }

    protected int getPlayersInside(boolean alive) {
        if (_playerCount == 0)
            return 0;

        int sum = 0;

        for (Player p : getPlayers())
            if (!alive || !p.isDead())
                sum++;

        return sum;
    }

    public void manualExitChamber(Player player, NpcInstance npc) {
        if (!player.isInParty() || player.getParty().getReflection() != this)
            return;

        if (!player.getParty().isLeader(player)) {
            DelusionChamberManager.getInstance().showHtmlFile(player, "delusionchamber/NotPartyLeader.htm", npc);
            return;
        }

        createNewKillChamberTimer();
    }

    public String getName() {
        InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(_roomType + 120);
        return iz.getName();
    }

    protected int getManagerId() {
        return 32664;
    }

    public void checkBossRoom(int room) {
        isBossRoom = DelusionChamberManager.getInstance().getRoom(_roomType, room).isBossRoom();
    }

    public void manualTeleport(Player player, NpcInstance npc) {
        if (!player.isInParty() || !player.getParty().isInReflection() || !(player.getParty().getReflection() instanceof DelusionChamber))
            return;

        if (!player.getParty().isLeader(player)) {
            DelusionChamberManager.getInstance().showHtmlFile(player, "delusionchamber/NotPartyLeader.htm", npc);
            return;
        }

        if (!isBossRoom) {
            if (_hasJumped) {
                DelusionChamberManager.getInstance().showHtmlFile(player, "delusionchamber/AlreadyTeleported.htm", npc);
                return;
            }
            _hasJumped = true;
        } else {
            manualExitChamber(player, npc);
            return;
        }

        teleportToNextRoom();
    }
}