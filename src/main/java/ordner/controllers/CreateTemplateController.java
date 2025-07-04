package ordner.controllers;

import ordner.utils.TemplateMember;
import ordner.utils.TemplateMemberType;
import ordner.Templates;
import ordner.utils.Utils;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.util.ArrayList;

public class CreateTemplateController
{
	@FXML private MainController mainController;
	@FXML private TableView<ObservableList<String>> membersTableView;
	@FXML private TableColumn<ObservableList<String>, String> typeColumn;
	@FXML private TableColumn<ObservableList<String>, String> nameColumn;
	@FXML private TextField templateNameTextField;
	@FXML private TextField memberNameTextField;
	@FXML private ComboBox<String> typeComboBox;
	private final ObservableList<ObservableList<String>> members = FXCollections.observableArrayList();

	public void setMainController(MainController controller) { mainController = controller; }

	@FXML private void initialize()
	{
		typeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFirst()));
		nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getLast()));

		for(var val: TemplateMemberType.values()) { typeComboBox.getItems().add(val.getDisplayType());}

		membersTableView.setItems(members);
		membersTableView.setPlaceholder(new Label("No members found."));
	}

	@FXML private void addMemberButtonAction()
	{
		var memberName = Utils.simplified(memberNameTextField.getText());
		if(typeComboBox.getValue() == null)
		{ MainController.showError("Please select a type.", 4000); return; }
		else if(memberName.isEmpty())
		{ MainController.showError("Please set a member name.", 4000); return; }
		else if(memberName.length() > 100)
		{ MainController.showError("Member name cannot exceed 100 characters.", 5000); return; }
		else if(!Utils.isAlphanumeric(memberName))
		{ MainController.showError("Member name must only contain alphanumeric characters.", 5000); return; }
		else if(Character.isDigit(memberName.charAt(0)))
		{ MainController.showError("Member name cannot start with a number.", 5000); return; }
		else if(getAllAddedMembersFormatted().stream().anyMatch(member -> member.name().equals(memberName)))
		{ MainController.showError("A member named \"" + memberName + "\" already exists.", 5000); return; }

		members.add(FXCollections.observableArrayList(typeComboBox.getValue(), memberName));
		memberNameTextField.clear();
	}

	@FXML private void createTemplateButtonAction()
	{
		var tempName = Utils.simplified(templateNameTextField.getText());
		if(tempName.isEmpty())
		{ MainController.showError("Please set a template name.", 4000); return; }
		else if(tempName.length() > 100)
		{ MainController.showError("Template name cannot exceed 100 characters.", 5000); return; }
		else if(!Utils.isAlphanumeric(tempName))
		{ MainController.showError("Template name must only contain alphanumeric characters.", 5000); return; }
		else if(Character.isDigit(tempName.charAt(0)))
		{ MainController.showError("Template name cannot start with a number.", 5000); return; }
		else if(Templates.templates.stream().anyMatch(name -> name.equals(tempName)))
		{ MainController.showError("A template named \"" + tempName + "\" already exists.", 5000); return; }
		else if(getAllAddedMembersFormatted().isEmpty())
		{ MainController.showError("The template must have one or more members.", 5000); return; }


		if(Templates.createTemplate(tempName, getAllAddedMembersFormatted()))
		{
			MainController.showMessage("Created \"" + tempName + "\" successfully.", 5000, "green");
			cleanUp();
		}
		else { MainController.showError("An error occurred while creating template", 5000); }
	}

	private ArrayList<TemplateMember> getAllAddedMembersFormatted()
	{
		var values = new ArrayList<TemplateMember>();

		for(var val : members) { values.add(new TemplateMember(val.getLast(), TemplateMemberType.fromDisplayType(val.getFirst()))); }

		return values;
	}

	private void cleanUp()
	{
		typeComboBox.getSelectionModel().clearSelection();
		templateNameTextField.clear();
		memberNameTextField.clear();
		members.clear();
	}
}
