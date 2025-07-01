package ordner.utils;

public enum TemplateMemberType
{
	TEXT("Text"),
	INTEGER("Integer"),
	REAL("Float"),
	DATE("Date (YYYY-MM-DD)"),
	TIME("Time (HH:MM:SS)"),
	TIMESTAMP("Timestamp (YYYY-MM-DD HH:MM:SS)");

	private final String displayType;

	TemplateMemberType(String displayType) { this.displayType = displayType; }

	public static TemplateMemberType fromDisplayType(String displayType)
	{
		for(var val : values()) { if(val.getDisplayType().equals(displayType)) { return val; } }
		return null;
	}

	public String getDisplayType() { return displayType; }
}
