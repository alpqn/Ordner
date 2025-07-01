module ordner {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	requires java.compiler;
	requires java.sql;
	requires jdk.jshell;


	opens ordner to javafx.fxml;
	exports ordner;
	exports ordner.controllers;
	opens ordner.controllers to javafx.fxml;
	exports ordner.utils;
	opens ordner.utils to javafx.fxml;
}