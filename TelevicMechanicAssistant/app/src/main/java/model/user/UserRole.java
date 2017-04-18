package model.user;


public enum UserRole
{
	ADMIN("Admin"), MECHANIC("Technicus"), OPERATOR("Operator");

	private String descr;
	
	UserRole(String descr)
	{
		this.descr = descr;
	}

	//GETTERS&SETTERS
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
}
