package model.sensordata;

import java.util.Date;

import model.TrainCoach;

public class ProcessedSensorData
{
	private int id;
	private String track;
	private TrainCoach traincoach;
	private Date date;
	private String location;

	public ProcessedSensorData()
	{
	}

	//GETTERS&SETTERS
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public TrainCoach getTraincoach() {
		return traincoach;
	}

	public void setTraincoach(TrainCoach traincoach) {
		this.traincoach = traincoach;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}