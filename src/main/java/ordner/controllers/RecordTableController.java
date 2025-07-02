package ordner.controllers;

import ordner.Records;
import ordner.utils.TemplateMemberType;
import ordner.utils.SQLRowUpdate;
import ordner.utils.Utils;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;

public class RecordTableController
{
	@FXML private MainController mainController;
	@FXML private TableView<ObservableList<String>> recordTableView;
	@FXML private HBox addRowHBox;
	@FXML private Label recordNameLabel;
	@FXML private Label rowNumberLabel;
	private String recordName;
	private String templateName;
	private Connection dbConnection;
	private final ObservableList<ObservableList<String>> recordData = FXCollections.observableArrayList();
	private final HashMap<String, TemplateMemberType> columns = new HashMap<>();
	private boolean confirmationFlag = false;

	public void setMainController(MainController controller) { mainController = controller; }

	@FXML private void initialize()
	{
		rowNumberLabel.textProperty().bind(Bindings
				.when(recordTableView.getSelectionModel().selectedIndexProperty().greaterThan(-1)) // If a row is selected
					.then(Bindings.format("%d:%d rows", recordTableView.getSelectionModel().selectedIndexProperty().add(1), Bindings.size(recordData)))
					.otherwise(Bindings
							.when(Bindings.size(recordData).isEqualTo(1))
								.then(Bindings.format("%d row", Bindings.size(recordData)))
								.otherwise(Bindings.format("%d rows", Bindings.size(recordData)))));

		var sortedList = new SortedList<>(recordData);
		sortedList.comparatorProperty().bind(recordTableView.comparatorProperty());
		recordTableView.setItems(sortedList);
	}

	public void update(String recordName)
	{
		if(recordName.equals(this.recordName)) return;

		cleanUp();
		this.recordName = recordName;

		try { dbConnection = DriverManager.getConnection("jdbc:sqlite:records/" + recordName + ".db"); }
		catch(SQLException e) { System.err.println("Cannot get connection to the database SQL ERROR: " + e); }

		try(Statement statement = dbConnection.createStatement())
		{
			var resultSet = dbConnection.getMetaData().getTables(null, null, "%", null);
			String tableName = null;

			while(resultSet.next()) { tableName = resultSet.getString("TABLE_NAME"); }
			if(tableName == null) { throw new SQLException("Table name is null"); }
			templateName = tableName;

			var data = statement.executeQuery("SELECT * FROM " + tableName);
			var columnCount = data.getMetaData().getColumnCount();

			for(int i = 1; i <= columnCount; ++i) // Create columns
			{
				var columnName = data.getMetaData().getColumnName(i);
				columns.put(columnName, TemplateMemberType.valueOf(data.getMetaData().getColumnTypeName(i)));
				var column = getTableColumn(columnName, i - 1);
				recordTableView.getColumns().add(column);

				var textField = new TextField();
				HBox.setHgrow(textField, Priority.ALWAYS);
				textField.setPromptText(columnName);
				addRowHBox.getChildren().add(textField);
			}

			while(data.next())
			{
				ObservableList<String> row = FXCollections.observableArrayList();
				for(int i = 1; i <= columnCount; ++i) { row.add(data.getString(i)); }
				recordData.add(row);
			}
		}
		catch(SQLException e) { System.err.println("Cannot get record" + recordName + " SQL ERROR: " + e); }

		recordNameLabel.setText(recordName + " (" + templateName + ')');
		for(var val : recordTableView.getColumns()) // Make every column have the same width at the start
		{
			val.prefWidthProperty().bind(recordTableView.widthProperty().divide(recordTableView.getColumns().size()));
		}
	}

	private TableColumn<ObservableList<String>, String> getTableColumn(String columnName, int col)
	{
		var column = new TableColumn<ObservableList<String>, String>(columnName);
		column.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(col)));
		column.setCellFactory(TextFieldTableCell.forTableColumn());
		column.setEditable(true);

		if(columns.get(columnName) == TemplateMemberType.INTEGER) { column.setComparator(Comparator.comparingLong(Long::parseLong)); }
		else if(columns.get(columnName) == TemplateMemberType.REAL) { column.setComparator(Comparator.comparingDouble(Double::parseDouble)); }

		column.setOnEditCommit(event ->
		{
			String validatedInput = validateFieldInput(event.getNewValue(), columnName);
			if(validatedInput == null) return;

			event.getRowValue().set(col, validatedInput);
			if(!Records.updateData(dbConnection, new SQLRowUpdate(templateName, columnName, validatedInput, recordData.indexOf(event.getRowValue()))))
			{ MainController.showError("An error occurred while updating the row.", 5000); }
		});

		return column;
	}

	@FXML private void addRowButtonAction()
	{
		ObservableList<String> row = FXCollections.observableArrayList();
		for(var val : addRowHBox.getChildren())
		{
			if(val instanceof TextField textField)
			{
				var validatedInput = validateFieldInput(textField.getText(), textField.getPromptText());
				if(validatedInput == null) return;
				row.add(validatedInput);
			}
		}

		if(!Records.insertData(dbConnection, templateName, row))
		{ MainController.showError("An error occurred while adding the row.", 5000); return; };

		recordData.add(row);
	}

	@FXML private void deleteRowButtonAction()
	{
		if(recordTableView.getSelectionModel().getSelectedItem() == null)
		{
			MainController.showError("No row selected to delete, click on a row to select it.", 5000);
			return;
		}

		int rowIndexUnsorted = recordData.indexOf(recordTableView.getSelectionModel().getSelectedItem());

		if(!Records.deleteData(dbConnection, templateName, rowIndexUnsorted))
		{ MainController.showError("An error occurred while deleting the row.", 5000); return; }

		recordData.remove(rowIndexUnsorted);
	}

	private void cleanUp()
	{
		try{ if(dbConnection != null && !dbConnection.isClosed()){ dbConnection.close(); } }
		catch(SQLException e) { System.err.println("Cannot close database connection SQL ERROR: " + e); }
		recordData.clear();
		recordTableView.getColumns().clear();
		addRowHBox.getChildren().clear();
		columns.clear();
	}

	private String validateFieldInput(String input, String columnName)
	{
		TemplateMemberType columnType = columns.get(columnName);
		String inputSimplified = Utils.simplified(input);

		if(inputSimplified.isEmpty()) { MainController.showError("All cells must contain a value.", 5000); return null; }
		else if(inputSimplified.length() > 256) { MainController.showError("Input too long for \"" + columnName + "\".", 5000); return null; }

		String validatedString = null;
		try
		{
			validatedString = switch(columnType)
			{
				case TEXT -> inputSimplified;
				case REAL ->
				{
					var parsedStr = String.valueOf(Double.parseDouble(inputSimplified));
					parsedStr = (parsedStr.contains("Infinity") ||  parsedStr.equals("NaN")) ? null : parsedStr;
					yield parsedStr;
				}
				case INTEGER -> String.valueOf(Long.parseLong(inputSimplified));
				case DATE -> inputSimplified.equalsIgnoreCase("today") ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
						String.valueOf(LocalDate.parse(inputSimplified, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				case TIME -> inputSimplified.equalsIgnoreCase("now") ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) :
						String.valueOf(LocalDateTime.parse(inputSimplified, DateTimeFormatter.ofPattern("HH:mm:ss")));
				case TIMESTAMP -> inputSimplified.equalsIgnoreCase("now") ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")):
					String.valueOf(LocalDateTime.parse(inputSimplified, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			};
		}
		catch(Exception e) { validatedString = null; }

		if(validatedString == null)
		{ MainController.showError("\"" + input + "\" isn't a valid instance of \"" + columnType.getDisplayType() + "\" (" + columnName + ").", 5000); return null; }

		return validatedString;
	}

	@FXML private void deleteRecordButtonAction()
	{
		if(confirmationFlag)
		{
			if(Records.removeRecord(recordName))
			{
				MainController.showMessage("Record \"" + recordName + "\" has been removed successfully.", 5000, "green");
				mainController.showPage("home");
			}
			else { MainController.showError("An error occurred while removing the record.", 5000); }
		}
		else
		{
			MainController.showMessage("Are you sure you want to delete \"" + recordName + "\" ?", 5000, "white");
			confirmationFlag = true;
			var delay = new PauseTransition(Duration.millis(5000));
			delay.setOnFinished(_ -> confirmationFlag = false);
			delay.play();
		}
	}
}

