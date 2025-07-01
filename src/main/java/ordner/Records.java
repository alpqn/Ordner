package ordner;

import ordner.utils.SQLRowUpdate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Records
{
	static private final ObservableList<String> m_records = getRecords();
	static public ObservableList<String> records = FXCollections.unmodifiableObservableList(m_records);

	public static boolean createRecord(String recordName, String templateName)
	{
		try { Files.createDirectories(Path.of("records")); } catch(IOException e) { System.err.println("Cannot create records directory " + e); }

		try(Connection con = DriverManager.getConnection("jdbc:sqlite:records/" + recordName + ".db"); Statement statement = con.createStatement();)
		{
			String sql = Files.readString(Paths.get("templates", templateName + ".sql"));
			statement.executeUpdate(sql);
		}
		catch(SQLException e) { System.err.println("Cannot create record " + recordName + " SQL ERROR: " + e); return false; }
		catch(IOException e) { System.err.println("Cannot create record " + recordName + " " + e); return false; }

		m_records.add(recordName);
		return true;
	}

	private static ObservableList<String> getRecords()
	{
		try(var stream = Files.list(Paths.get("records")))
		{
			return FXCollections.observableArrayList(stream.map(path -> path.getFileName().toString())
					.filter(file -> file.endsWith(".db")).map(file -> file.substring(0, file.length() - 3)).toList());
		}
		catch(IOException e) { System.err.println("Couldn't get records " + e); return FXCollections.observableArrayList(); }
	}
 
	public static boolean insertData(Connection con, String tableName, ObservableList<String> data)
	{
		try(Statement statement = con.createStatement())
		{
			var sql = new StringBuilder("INSERT INTO " + tableName + " VALUES(");
			for(var val : data) { sql.append("'" + val + "', "); }

			sql.delete(sql.length() - 2, sql.length()); // Remove the trailing ", "
			statement.execute(sql + ");");
		}
		catch(SQLException e) { System.err.println("Cannot insert data in database SQL ERROR: " + e); return false; }

		return true;
	}

	public static boolean updateData(String recordName, SQLRowUpdate data)
	{
		try(Connection con = DriverManager.getConnection("jdbc:sqlite:records/" + recordName + ".db"))
		{
			updateData(con, data);
		}
		catch(SQLException e) { System.err.println("Couldn't update data SQL ERROR: " + e); return false; }

		return true;
	}

	public static boolean updateData(Connection con, SQLRowUpdate data)
	{
		try(Statement statement = con.createStatement())
		{
			statement.addBatch
			("UPDATE " + data.tableName() + " SET " + data.columnName() + " = '" + data.newValue() +
			"' WHERE rowid IN ( SELECT rowid FROM " + data.tableName() + " LIMIT 1 OFFSET " + data.rowid() + " )");

			statement.executeBatch();
		}
		catch(SQLException e) { System.err.println("Couldn't update data SQL ERROR: " + e); return false; }

		return true;
	}

	public static boolean deleteData(String recordName, String tableName, int rowIndex)
	{
		try(Connection con = DriverManager.getConnection("jdbc:sqlite:records/" + recordName + ".db"))
		{
			deleteData(con, tableName, rowIndex);
		}
		catch(SQLException e) { System.err.println("Couldn't delete row SQL ERROR: " + e); return false; }

		return true;
	}

	public static boolean deleteData(Connection con, String tableName, int rowIndex)
	{
		try(Statement statement = con.createStatement())
		{
			statement.addBatch
			("DELETE FROM " + tableName + " WHERE rowid IN ( SELECT rowid FROM " + tableName + " LIMIT 1 OFFSET " + rowIndex + " )");

			statement.executeBatch();
		}
		catch(SQLException e) { System.err.println("Couldn't delete row SQL ERROR: " + e); return false; }

		return true;
	}

	public static boolean removeRecord(String recordName)
	{
		try
		{
			Files.delete(Path.of("records/"+ recordName + ".db"));
		}
		catch(Exception e) { System.err.println("Couldn't delete record " + recordName + " " + e); return false; }

		m_records.removeIf(name -> name.equals(recordName));
		return true;
	}
}
