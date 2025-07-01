package ordner.controllers;

import ordner.Records;
import ordner.Templates;
import ordner.utils.Utils;

import javafx.scene.layout.Priority;
import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class MainController
{
	@FXML private CreateTemplateController createTemplateController;
	@FXML private CreateRecordController createRecordController;
	@FXML private TemplateInfoController templateInfoController;
	@FXML private RecordTableController recordTableController;
	@FXML private SettingsController settingsController;
	@FXML private StackPane mainStack;
	@FXML private HBox toolbar;
	@FXML private VBox templatesVBox;
	@FXML private VBox recordsVBox;
	@FXML private VBox recentRecordsVBox;
	@FXML private Label noTemplateLabel;
	@FXML private Label noRecordLabel;
	@FXML private Label noRecentRecordsLabel;
	@FXML static private final Label toolbarMsg = new Label("");

	@FXML private void initialize()
	{
		createRecordController.setMainController(this);
		createTemplateController.setMainController(this);
		templateInfoController.setMainController(this);
		recordTableController.setMainController(this);
		settingsController.setMainController(this);

		populateSidebar();

		populateRecentlyOpenedRecords();

		toolbarMsg.setFont(new Font(18));
		toolbarMsg.setMaxWidth(1400);
		toolbarMsg.setTextOverrun(OverrunStyle.LEADING_WORD_ELLIPSIS);
		toolbar.getChildren().add(toolbarMsg);
	}

	private void populateSidebar()
	{
		Function<String, Button> createTemplateButton = (name) ->
		{
			var button = new Button(name);
			button.setMnemonicParsing(false); // Don't parse underscores
			button.setOnMouseClicked(event ->
			{
				templateInfoController.update(((Button) event.getSource()).getText());
				showPage("templateInfo");
			});
			return button;
		};

		Function<String, Button> createRecordButton = (name) ->
		{
			var button = new Button(name);
			button.setMnemonicParsing(false); // Don't parse underscores
			button.setOnMouseClicked(event ->
			{
				recordTableController.update(((Button) event.getSource()).getText());
				showPage("recordTable");
			});
			return button;
		};

		Templates.templates.addListener((ListChangeListener<String>) c ->
		{
			while(c.next())
			{
				if(c.wasAdded())
				{
					Utils.changeVisibility(noTemplateLabel, false);
					templatesVBox.getChildren().add(createTemplateButton.apply(c.getAddedSubList().getFirst()));
				}
				else if(c.wasRemoved())
				{
					if(Templates.templates.isEmpty()) { Utils.changeVisibility(noTemplateLabel, true); }
					templatesVBox.getChildren().removeIf(b -> b instanceof Button button && button.getText().equals(c.getRemoved().getFirst()));
				}
			}
		});

		Records.records.addListener((ListChangeListener<String>) c ->
		{
			while(c.next())
			{
				if(c.wasAdded())
				{
					Utils.changeVisibility(noRecordLabel, false);
					recordsVBox.getChildren().add(createRecordButton.apply(c.getAddedSubList().getFirst()));
				}
				else if(c.wasRemoved())
				{
					if(Records.records.isEmpty()) { Utils.changeVisibility(noRecordLabel, true); Utils.changeVisibility(noRecentRecordsLabel, true);}
					recordsVBox.getChildren().removeIf(b -> b instanceof Button button && button.getText().equals(c.getRemoved().getFirst()));
				}
			}
		});

		for(var tempName : Templates.templates) { templatesVBox.getChildren().add(createTemplateButton.apply(tempName)); }
		for(var recordName : Records.records) {  recordsVBox.getChildren().add(createRecordButton.apply(recordName)); }

		if(!Templates.templates.isEmpty()) { Utils.changeVisibility(noTemplateLabel, false); }
		if(!Records.records.isEmpty()) { Utils.changeVisibility(noRecordLabel, false); Utils.changeVisibility(noRecentRecordsLabel, false);}
	}

	private void populateRecentlyOpenedRecords()
	{
		try(var stream = Files.find(Paths.get("records/"), 1, (_, atr) -> atr.isRegularFile()))
		{
			var fileDateMap = new TreeMap<FileTime, String>(Comparator.reverseOrder());

			for(var val : stream.toList())
			{
				var fileName = val.getFileName().toString();
				if(fileName.endsWith(".db"))
				{
					fileDateMap.put(Files.getLastModifiedTime(val), fileName.substring(0, fileName.length() - 3)); // Remove ".db"
				}
			}

			int recordNumber = 0;
			for(Map.Entry<FileTime, String> val: fileDateMap.entrySet())
			{
				if(recordNumber == 5) break; // Most recent 5 records

				var fileName = val.getValue();
				var date = val.getKey().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss MMMM d"));
				int numberOfRows = 0;

				try(Connection con = DriverManager.getConnection("jdbc:sqlite:records/" + fileName + ".db"); Statement statement = con.createStatement();)
				{
					String tableName = null;
					var resultSet = con.getMetaData().getTables(null, null, "%", null);

					while(resultSet.next()) { tableName = resultSet.getString("TABLE_NAME"); }
					if(tableName == null) { throw new SQLException("Table name is null"); }

					var rowsData = statement.executeQuery("SELECT COUNT() AS rows FROM " + tableName);
					while(rowsData.next()) { numberOfRows = rowsData.getInt("rows"); }

				}
				catch(SQLException e) { System.err.println("Couldn't get recent record " + fileName + " SQL ERROR: " + e); }

				var recordButton = new Button(fileName + " \nlast edited in " + date + "\n (");

				if(numberOfRows == 1) { recordButton.setText(recordButton.getText() + numberOfRows + " row)"); }
				else { recordButton.setText(recordButton.getText() + numberOfRows + " rows)"); }

				var hb = new HBox();
				recentRecordsVBox.getChildren().add(hb);
				hb.getChildren().add(recordButton);
				hb.setMaxWidth(500);
				HBox.setHgrow(recordButton, Priority.ALWAYS);
				HBox.setMargin(recordButton, new Insets(10, 0, 0, 0));
				recordButton.setMaxWidth(Double.MAX_VALUE);
				recordButton.setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);
				recordButton.setMnemonicParsing(false);
				recordButton.setOnMouseClicked(event ->
				{
					var buttonText =  ((Button) event.getSource()).getText();
					recordTableController.update(buttonText.substring(0, buttonText.indexOf('\n') - 1));
					showPage("recordTable");
				});

				Records.records.addListener((ListChangeListener<String>) c ->
				{
					while(c.next())
					{
						if(c.wasRemoved())
						{

							System.out.println(c.getRemoved().getFirst() + recordButton.getText().substring(0, recordButton.getText().indexOf('\n')));
							if(c.getRemoved().getFirst().equals(recordButton.getText().substring(0, recordButton.getText().indexOf('\n') - 1)))
							{ Utils.changeVisibility(hb, false); System.out.println("ja;p");}
						}
					}
				});

				++recordNumber;
			}
			if(recordNumber == 0) { Utils.changeVisibility(noRecentRecordsLabel, true); }
		}
		catch(IOException e) { System.err.println("Couldn't get records " + e); Utils.changeVisibility(noRecentRecordsLabel, true);}
	}

	public static void showMessage(String msg, int ms, String color)
	{
		toolbarMsg.setText(msg);
		toolbarMsg.setTextFill(Paint.valueOf(color));

		if (toolbarMsg.getUserData() instanceof PauseTransition delayEvent) { delayEvent.stop(); } // If there is already a delay (already showing a message), cancel it

		var delay = new PauseTransition(Duration.millis(ms));
		delay.setOnFinished(_ -> toolbarMsg.setText(""));
		delay.play();

		toolbarMsg.setUserData(delay);
	}

	public static void showError(String msg, int ms) { showMessage(msg, ms, "red"); }
	public void showPage(String pageName) { mainStack.getChildren().filtered(page -> page.getId().equals(pageName)).getFirst().toFront(); }

	@FXML private void onKeyPressed(KeyEvent event) { if(event.getCode() == KeyCode.ESCAPE) { showPage("home"); } }
	@FXML private void mainMenuButtonAction() { showPage("home"); }
	@FXML private void newTemplateButtonAction() { showPage("createTemplate"); }
	@FXML private void newRecordButtonAction() { showPage("createRecord"); }
	@FXML private void editSettingsButtonAction() { showPage("settingsPage"); }
}