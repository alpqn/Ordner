module ordner {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires org.xerial.sqlitejdbc;


	opens ordner to javafx.fxml;
	exports ordner;
	exports ordner.controllers;
	opens ordner.controllers to javafx.fxml;
	exports ordner.utils;
	opens ordner.utils to javafx.fxml;
}