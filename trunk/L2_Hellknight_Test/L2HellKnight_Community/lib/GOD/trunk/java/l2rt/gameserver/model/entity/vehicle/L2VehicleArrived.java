package l2rt.gameserver.model.entity.vehicle;

class L2VehicleArrived implements Runnable
{
	private final L2Vehicle _vehicle;

	public L2VehicleArrived(L2Vehicle vehicle)
	{
		_vehicle = vehicle;
	}

	public void run()
	{
		_vehicle.updatePeopleInTheBoat(_vehicle.getLoc());
		_vehicle.VehicleArrived();
	}
}