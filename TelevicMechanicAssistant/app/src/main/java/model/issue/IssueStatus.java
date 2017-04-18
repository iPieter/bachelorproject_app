package model.issue;

public enum IssueStatus
{
	ASSIGNED("Toegewezen"), IN_PROGRESS("In behandeling"), CLOSED("Gesloten");
	
	private String descr;
	
	IssueStatus(String descr)
	{
		this.descr = descr;
	}

	//GETTERS & SETTERS
	public String getDescr()
	{
		return descr;
	}

	public void setDescr(String descr)
	{
		this.descr = descr;
	}
}
