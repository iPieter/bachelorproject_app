package model;

import java.util.ArrayList;
import java.util.List;

import model.user.User;

public class Workplace
{
	private int id;
	private String name;
	private List<TrainCoach> traincoaches = new ArrayList<TrainCoach>();
	private List<User> mechanics;

	public Workplace()
	{
	}

	// GETTERS & SETTTERS
	public int getId()
	{
		return this.id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public List<TrainCoach> getTraincoaches()
	{
		return this.traincoaches;
	}

	public void setTraincoaches( List<TrainCoach> traincoaches )
	{
		this.traincoaches = traincoaches;
	}

	/**
	 * @author Pieter Delobelle
	 * @version 1.0.0
	 * @return the mechanics
	 */
	public List<User> getMechanics()
	{
		return mechanics;
	}

	/**
	 * @author Pieter Delobelle
	 * @version 1.0.0
	 * @param mechanics the mechanics to set
	 */
	public void setMechanics(List<User> mechanics)
	{
		this.mechanics = mechanics;
	}
}