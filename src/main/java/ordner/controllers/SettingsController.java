package ordner.controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class SettingsController
{
	@FXML private MainController mainController;
	@FXML private ColorPicker oddRowColorPicker;
	@FXML private ColorPicker evenRowColorPicker;
	@FXML private ColorPicker selectedRowColorPicker;
	@FXML private ColorPicker cellEditFieldColorPicker;
	@FXML private ColorPicker cellTextColorPicker;
	@FXML private ComboBox<Pos> cellTextAlignmentComboBox;
	@FXML private ComboBox<String> fontComboBox;
	@FXML private Spinner<Integer> fontSpinner;
	private final String cssLocation = "src/main/resources/ordner/stylesheets/userPrefs.css";
	private boolean confirmationFlag = false;

	public void setMainController(MainController controller) { mainController = controller; }

	@FXML private void initialize()
	{
		cellTextAlignmentComboBox.setItems(FXCollections.observableArrayList(Pos.values()));
		fontComboBox.setItems(FXCollections.observableArrayList(Font.getFamilies()));
		fontSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,96));

		var filePath = Path.of(cssLocation);
		if(Files.exists(filePath))
		{
			try
			{
				Function<String, Color> parseHex = (str) -> Color.valueOf(str.substring(str.indexOf('#'), str.indexOf(';')));
				String content = Files.readString(filePath);
				for(var line : content.split("\n"))
				{
					if(line.startsWith(".table-view .table-row-cell:odd {")) { oddRowColorPicker.setValue(parseHex.apply(line)); }
					else if(line.startsWith(".table-view .table-row-cell:even {")) { evenRowColorPicker.setValue(parseHex.apply(line)); }
					else if(line.startsWith(".table-view .table-row-cell:selected:focused {")) { selectedRowColorPicker.setValue(parseHex.apply(line)); }
					else if(line.startsWith(".table-view .text-field")) { cellEditFieldColorPicker.setValue(parseHex.apply(line)); }
					else if(line.startsWith(".table-view .cell { "))
					{
						fontComboBox.setValue(line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"')));
						fontSpinner.getValueFactory().setValue(Integer.valueOf(line.substring(line.lastIndexOf(':') + 1, line.lastIndexOf(';')).trim()));
					}
					else if(line.startsWith(".table-view .table-cell {"))
					{
						cellTextColorPicker.setValue(parseHex.apply(line));
						cellTextAlignmentComboBox.setValue(Pos.valueOf(line.substring(line.lastIndexOf(':') + 1, line.lastIndexOf(';')).trim()));
					}
				}
			}
			catch(IOException e) { System.err.println("Cannot read userPrefs.css " + e); }
		}
	}

	@FXML private void saveButtonAction()
	{
		String cssContent = String.format("""
			.table-view .table-row-cell:odd { -fx-background-color: #%s; }
			.table-view .table-row-cell:even { -fx-background-color: #%s; }
			.table-view .table-row-cell:selected:focused { -fx-background-color: #%s; }
			.table-view .text-field { -fx-background-color: #%s; }
			.table-view .cell { -fx-font-family: "%s"; -fx-font-size: %s; }
			.table-view .table-cell { -fx-text-fill: #%s; -fx-alignment: %s; }
			"""
			,oddRowColorPicker.getValue().toString().substring(2, 8)
			,evenRowColorPicker.getValue().toString().substring(2, 8)
			,selectedRowColorPicker.getValue().toString().substring(2, 8)
			,cellEditFieldColorPicker.getValue().toString().substring(2, 8)
			,fontComboBox.getValue()
			,fontSpinner.getValue()
			,cellTextColorPicker.getValue().toString().substring(2, 8)
			,cellTextAlignmentComboBox.getValue().toString());

		try { Files.writeString(Path.of(cssLocation), cssContent); }
		catch(IOException e)
		{
			System.err.println("Cannot create " + cssLocation + " " + e);
			MainController.showError("An error occurred while updating settings.", 5000);
			return;
		}

		MainController.showMessage("Successfully saved settings. Restart the application to apply the changes.", 5000, "green");
	}

	@FXML private void resetToDefaultsButtonAction()
	{
		if(confirmationFlag)
		{
			try { Files.deleteIfExists(Path.of(cssLocation)); }
			catch(IOException e)
			{
				System.err.println("Couldn't delete " + cssLocation + " " + e);
				MainController.showError("An error occurred while resetting to defaults.", 5000);
				return;
			}

			MainController.showMessage("Successfully reset to defaults. Restart the application to apply the changes.", 5000, "green");
		}
		else
		{
			MainController.showMessage("Are you sure you want to reset settings to defaults?", 5000, "white");
			confirmationFlag = true;
			var delay = new PauseTransition(Duration.millis(5000));
			delay.setOnFinished(_ -> confirmationFlag = false);
			delay.play();
		}
	}
}
