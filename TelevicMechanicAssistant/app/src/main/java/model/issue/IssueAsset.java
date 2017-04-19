package model.issue;

import java.util.Date;

import model.user.User;

public class IssueAsset
{
	private int id;
	private String descr;
	private Date time;
	private String location;
	private User user;
	
	public IssueAsset()
	{
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId( int id )
	{
		this.id = id;
	}

	/**
	 * @return the descr
	 */
	public String getDescr()
	{
		return descr;
	}

	/**
	 * @param descr the descr to set
	 */
	public void setDescr( String descr )
	{
		this.descr = descr;
	}

	/**
	 * @return the location
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation( String location )
	{
		this.location = location;
	}

	/**
	 * @return the time
	 */
	public Date getTime()
	{
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime( Date time )
	{
		this.time = time;
	}

	/**
	 * @return the user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser( User user )
	{
		this.user = user;
	}

	/**
	 * 	Returns a String representation of this object
	 *  @return The String representation	 
	 * */
	public String toString()
	{
		return descr + ":" + user.getName() + ":" + time.toString() ;
	}
}