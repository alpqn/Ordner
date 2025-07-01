package ordner.controllers;

import ordner.utils.TemplateMemberType;
import ordner.Templates;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TemplateInfoController
{
	@FXML private MainController mainController;
	@FXML private TableView<ObservableList<String>> membersTableView;
	@FXML private TableColumn<ObservableList<String>, String> typeColumn;
	@FXML private TableColumn<ObservableList<String>, String> nameColumn;
	@FXML private Label templateInfoTitle;
	private final ObservableList<ObservableList<String>> templateMembers = FXCollections.observableArrayList(); // TODO: Change this to <TemplateMember>
	boolean confirmationFlag = false;

	public void setMainController(MainController controller) { mainController = controller; }

	@FXML private void initialize()
	{
		typeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get(0)));
		nameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get(1)));
		membersTableView.setItems(templateMembers);
	}

	public void update(String templateName)
	{
		templateMembers.clear();
		templateInfoTitle.setText(templateName);
		try
		{
			var content = Files.readString(Paths.get("templates/" + templateName + ".sql"));
			var members = content.substring(content.indexOf('(') + 1, content.indexOf(')'));
			var pair = members.split(", ");
			for(var val : pair)
			{
				var instance = val.split(" ");
				var name = instance[0].substring(1, instance[0].length() - 1); // Remove the quotes
				var type = TemplateMemberType.valueOf(instance[1]).getDisplayType();
				templateMembers.add(FXCollections.observableArrayList(type,name));
			}
		}
		catch(IOException e) { System.err.println("Couldn't get templates " + e); }
	}

	@FXML private void deleteTemplateButtonAction()
	{
		if(confirmationFlag)
		{
			if(Templates.removeTemplate(templateInfoTitle.getText()))
			{
				MainController.showMessage("Template \"" + templateInfoTitle.getText() + "\" has been removed successfully.", 5000, "green");
				mainController.showPage("home");
			}
			else { MainController.showError("An error occurred while removing the template.", 5000); }
		}
		else
		{
			MainController.showMessage("Are you sure you want to delete \"" + templateInfoTitle.getText() + "\" ?", 5000, "white");
			confirmationFlag = true;
			var delay = new PauseTransition(Duration.millis(5000));
			delay.setOnFinished(_ -> confirmationFlag = false);
			delay.play();
		}
	}
}
