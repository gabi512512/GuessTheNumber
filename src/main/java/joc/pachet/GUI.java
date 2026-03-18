package joc.pachet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		Parent interfataGrafica = FXMLLoader.load(getClass().getResource("/gui/gui.fxml"));
		Scene scena = new Scene(interfataGrafica);
		stage.setTitle("Guess the number");
		stage.setScene(scena);
		stage.show();
	}
}
