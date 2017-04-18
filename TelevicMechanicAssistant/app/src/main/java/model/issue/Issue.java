package model.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.sensordata.ProcessedSensorData;
import model.user.User;

public class Issue implements Serializable
{
	//private int id;
	private String descr;
	private IssueStatus status;
	//private List<IssueAsset> assets = new ArrayList<IssueAsset>();
	//private User mechanic;
	private User operator;
	private ProcessedSensorData data;
	private Date assignedTime;
	private Date inProgressTime;
	private Date closedTime;

	//private double gpsLat;
	//private double gpsLon;
	
	public Issue()
	{
		assignedTime = new Date();
		inProgressTime = new Date();
		closedTime = new Date();
		gpsLat = 0.0;
		gpsLon = 0.0;
	}

	// GETTERS & SETTERS
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
	}

	public List<IssueAsset> getAssets() {
		return assets;
	}

	public void setAssets(List<IssueAsset> assets) {
		this.assets = assets;
	}

	public User getMechanic() {
		return mechanic;
	}

	public void setMechanic(User mechanic) {
		this.mechanic = mechanic;
	}

	public User getOperator() {
		return operator;
	}

	public void setOperator(User operator) {
		this.operator = operator;
	}

	public ProcessedSensorData getData() {
		return data;
	}

	public void setData(ProcessedSensorData data) {
		this.data = data;
	}

	public Date getAssignedTime() {
		return assignedTime;
	}

	public void setAssignedTime(Date assignedTime) {
		this.assignedTime = assignedTime;
	}

	public Date getInProgressTime() {
		return inProgressTime;
	}

	public void setInProgressTime(Date inProgressTime) {
		this.inProgressTime = inProgressTime;
	}

	public Date getClosedTime() {
		return closedTime;
	}

	public void setClosedTime(Date closedTime) {
		this.closedTime = closedTime;
	}

	public double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public double getGpsLon() {
		return gpsLon;
	}

	public void setGpsLon(double gpsLon) {
		this.gpsLon = gpsLon;
	}
}