package ordner.utils;

public record SQLRowUpdate(String tableName, String columnName, String newValue, int rowid)
{
}
