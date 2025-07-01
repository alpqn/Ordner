package ordner;

import ordner.utils.TemplateMember;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Templates
{
	static private final ObservableList<String> m_templates = getTemplates();
	static public ObservableList<String> templates = FXCollections.unmodifiableObservableList(m_templates);

	public static boolean createTemplate(String tempName, List<TemplateMember> members)
	{
		var sql = new StringBuilder("CREATE TABLE \"" + tempName + "\" (");

		for(var member : members) { sql.append("\"" + member.name() + "\" " + member.type() + ", "); }

		sql.delete(sql.length() - 2, sql.length()); // Remove the trailing ", "

		try
		{
			var file = Paths.get("templates/" + tempName + ".sql");
			Files.createDirectories(Path.of("templates"));
			Files.writeString(file, sql + ");\n");
		}
		catch(IOException e) { System.err.println("Couldn't create template " + tempName + " " + e); return false; }

		m_templates.add(tempName);
		return true;
	}

	private static ObservableList<String> getTemplates()
	{
		try(var stream = Files.list(Paths.get("templates")))
		{
			return FXCollections.observableArrayList(stream.map(file -> file.getFileName().toString())
					.filter(file -> file.endsWith(".sql")).map(file -> file.substring(0, file.length() - 4)).toList());
		}
		catch(IOException e) { System.err.println("Couldn't get templates " + e); return FXCollections.observableArrayList(); }
	}

	public static boolean removeTemplate(String tempName)
	{
		try
		{
			Files.delete(Path.of("templates/"+ tempName + ".sql"));
		}
		catch(Exception e) { System.err.println("Couldn't delete template " + tempName + " " + e); return false; }

		m_templates.removeIf(name -> name.equals(tempName));
		return true;
	}
}
