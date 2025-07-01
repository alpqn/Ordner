package ordner.controllers;

import ordner.Records;
import ordner.Templates;
import ordner.utils.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CreateRecordController
{
	@FXML private MainController mainController;
	@FXML private ComboBox<String> templateComboBox;
	@FXML private TextField recordNameTextField;

	public void setMainController(MainController controller) { mainController = controller; }

	@FXML private void initialize() { templateComboBox.setItems(Templates.templates); }

	@FXML private void createRecordButtonAction()
	{
		var recordName = Utils.simplified(recordNameTextField.getText());
		if(recordName.isEmpty())
		{ MainController.showError("Please set a record name.", 4000); return; }
		else if(recordName.length() > 100)
		{ MainController.showError("Record name cannot exceed 100 characters.", 5000); return; }
		else if(!Utils.isAlphanumeric(recordName))
		{ MainController.showError("Record name must only contain alphanumeric characters.", 5000); return; }
		else if(Character.isDigit(recordName.charAt(0)))
		{ MainController.showError("Record name cannot start with a number.", 5000); return; }
		else if(templateComboBox.getValue() == null)
		{ MainController.showError("Please select a template.", 4000); return; }
		else if(Records.records.stream().anyMatch(record -> record.equals(recordName)))
		{ MainController.showError("A record named \"" + recordName + "\" already exists.", 5000); return; }

		if(Records.createRecord(recordName, templateComboBox.getValue()))
		{
			MainController.showMessage("Created \"" + recordName + "\" successfully.", 5000, "green");
			recordNameTextField.clear();
		}
		else { MainController.showError("An error occurred while creating the record.", 5000); }
	}
}
