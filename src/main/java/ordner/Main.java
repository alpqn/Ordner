package ordner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application
{
	@Override public void start(Stage stage) throws IOException
	{
		var scene = new Scene(FXMLLoader.load(Main.class.getResource("fxml/App.fxml")));
		stage.setTitle("Ordner");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args){ launch(); }
}