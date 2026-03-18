package joc.pachet;

import java.io.File;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {
	private GameData g = new GameData();
	private StringProperty state = new SimpleStringProperty();
	// states
	// init - initializat
	// clim - limite modificate
	// flim - fail limite
	// gues - ghicit
	// fail - failed
	// save - salvat
	// svow - salvat dupa over-write
	// ldsf - reload cu succes
	// ldfl - reload fail
	// sphc - save path changed
	// sphf - save path fail
	// sfch - save fail cheater
	// snca - show number cheat activated
	// sncd - show number cheat deactivated
	
	@FXML
	private TextField try_field;
	@FXML
	private Button try_button;
	@FXML 
	private Button save_button;
	@FXML
	private Button reload_button;
	@FXML
	private Button change_button;
    @FXML
    private Label valMax_label;
    @FXML
    private Label valMin_label;
	@FXML
	private Label dim_label;
	@FXML
    private Label thisTryCount_label;
	@FXML
	private Label totalTryCount_label;
	@FXML
    private Label totalGuesses_label;
    @FXML
    private Label winPercent_label;
    @FXML
	private Label dimMax_label;
	@FXML
	private Label lowestTryCount_label;
	@FXML
	//private Label status_label;
	private TextFlow status_textFlow;
	@FXML
	private MenuItem savePath_button;
	@FXML
	private CheckMenuItem showNumber_button;
	@FXML
	private Label showNumber_label;
    
	@FXML
	public void initialize() {
		System.out.println("initializare");
		
		String template_winPercent = "Procent de castig: ";
		String template_lowestTryCount = "Cea mai rapida ghicire: ";
		String template_status = "Status: ";
		String template_cheat = "Cheat activat: ";
		String template_error  = "Eroare: ";
		
		g.setValMin(-10);
		g.setValMax(10);
		g.setTries(0);
		g.setTriesTotal(0);
		g.setGuessesTotal(0);
		g.setDimMax(0);
		g.setTriesLowest(Integer.MAX_VALUE);
		g.setNewNumber();
		this.setState("init");
		
		valMin_label.textProperty().bind(g.getValMinProperty().asString("Valoare minima: %d"));
		valMax_label.textProperty().bind(g.getValMaxProperty().asString("Valoare maxima: %d"));
		dim_label.textProperty().bind(g.getValMaxProperty().subtract(g.getValMinProperty()).add(1).asString("Dimensiune interval: %d"));
		thisTryCount_label.textProperty().bind(g.getTriesProperty().asString("Incercari de la ultimul castig: %d"));
		totalTryCount_label.textProperty().bind(g.getTriesTotalProperty().asString("Numar incercari: %d"));
		totalGuesses_label.textProperty().bind(g.getGuessesTotalProperty().asString("Numar de ghiciri: %d"));
		dimMax_label.textProperty().bind(g.getDimMaxProperty().asString("Cel mai mare interval in care s-a ghicit: %d"));
		
		winPercent_label.textProperty().bind(Bindings.createStringBinding(() -> { 
			if(g.getGuessesTotal() == 0) 
				return template_winPercent + "0.0%";
			return String.format(template_winPercent + "%.2f %%", (double) g.getGuessesTotal() / g.getTriesTotal() * 100);
		}, g.getTriesTotalProperty(), g.getGuessesTotalProperty())); // proprietatile observate
		
		lowestTryCount_label.textProperty().bind(Bindings.createStringBinding(() -> {
			if(g.getTriesLowest() == Integer.MAX_VALUE)
				return template_lowestTryCount + "∞";
			return String.format(template_lowestTryCount + "%d", g.getTriesLowest());
		}, g.getTriesLowestProperty()));
		
		Text prefix = new Text();
		Text sufix = new Text();
		prefix.textProperty().bind(Bindings.createStringBinding(() -> {
			switch(this.getState()) {
				case "init", "clim", "flim", "save", "svow", "ldsf", "gues", "fail", "sphc":
					prefix.setFill(Color.BEIGE);
					return template_status;
				case "ldfl", "sfch", "sphf":
					prefix.setFill(Color.RED);
					return template_error;
				case "snca", "sncd":
					prefix.setFill(Color.AZURE);
					return template_cheat;
			}
			return "ceva nu e bine";
		}, this.state));
		sufix.textProperty().bind(Bindings.createStringBinding(() -> {
			switch(this.getState()) {
				// state-uri
				case "init": return " initializat";
				case "clim": return " limite schimbate";
				case "flim": return " limite eronate";
				case "save": return " progres salvat";
				case "svow": return " progres suprascris";
				case "ldsf": return " progres reluat";
				case "gues": return " ai ghicit";
				case "fail": return " n-ai ghicit";
				case "sphc": return " save path schimbat";
				// erori
				case "ldfl": return " nu s-a putut relua progresul";
				case "sfch": return " nu poti salva deoarece ai folosit coduri";
				case "sphf": return " nu ai selectat un folder valid";
				// cheats
				case "snca": return " acum vei vedea numerele aleatorii";
				case "sncd": return " acum nu vei mai vedea numerele aleatorii";
			}
			return "ceva nu e bine";
		}, this.state));
		
		status_textFlow.getChildren().addAll(prefix, sufix);
		showNumber_label.setText("");
	}
	
	@FXML
	private void handleTryButton() {
		System.out.println(try_button.getText());
		
		int number =  Integer.parseInt(try_field.getText());
		if(number == g.getNumber()) {
			g.guessed();
			this.setState("gues");
			if(g.showNumberState())
				this.showNumber_label.setText("[Cheat] Numar: " + g.getNumber());
		}
		else {
			g.failed();
			if(this.getState() != "fail") // sa nu se actualizeze inutil label-ul
				this.setState("fail");
		}
	}
	
	@FXML
	private void handleSaveButton() {
		System.out.println(save_button.getText());
		if(!g.isCheater()) {
			boolean suprascris = g.save();
			if(suprascris)
				this.setState("svow");
			else
				this.setState("save");
		}
		else
			this.setState("sfch");
	}
	
	@FXML
	private void handleReloadButton() {
		System.out.println(reload_button.getText());
		boolean incarcat = g.load();
		if(incarcat)
			this.setState("ldsf");
		else
			this.setState("ldfl");
	}
	
	@FXML
	private void handleChangeButton() throws RuntimeException {
		System.out.println(change_button.getText());
		
		Dialog<ButtonType> limits_dialog = new Dialog<>();
		limits_dialog.setTitle("Limite");
		limits_dialog.setHeaderText("Introdu mai jos limitele");
		limits_dialog.getDialogPane().getButtonTypes().add(0, ButtonType.OK);
		
		Label valMin_label = new Label("Limita inferioara:");
		Label valMax_label = new Label("Limita superioara:");
		TextField valMin_field = new TextField();
		TextField valMax_field = new TextField();
		
		GridPane afisare = new GridPane();
		afisare.add(valMin_label, 0, 0); // col 0, rand 0
		afisare.add(valMin_field, 1, 0);
		afisare.add(valMax_label, 0, 1);
		afisare.add(valMax_field, 1, 1);
		
		limits_dialog.getDialogPane().setContent(afisare);
		limits_dialog.showAndWait().ifPresent(raspuns -> {
			if(raspuns == ButtonType.OK) {
				try {
					int valMin, valMax;
					if(!valMin_field.getText().isEmpty() && !valMax_field.getText().isEmpty()) {
						valMin = Integer.parseInt(valMin_field.getText());
						valMax = Integer.parseInt(valMax_field.getText());
						if(valMin >= valMax) {
							this.setState("flim");
							throw new RuntimeException("Limita inferioara mai mare sau egala cu cea superioara");
						}
						else {
							g.setValMin(valMin);
							g.setValMax(valMax);
							g.setNewNumber();
							this.setState("clim");
						}
					}
					else if(!valMin_field.getText().isEmpty()) {
						valMin = Integer.parseInt(valMin_field.getText());
						if(valMin > g.getValMax()) {
							this.setState("flim");
							throw new RuntimeException("Limita inferioara mai mare sau egala cu cea superioara");
						}
						else {
							g.setValMin(valMin);
							g.setNewNumber();
							this.setState("clim");
						}
					}
					else if(!valMax_field.getText().isEmpty()) {
						valMax = Integer.parseInt(valMax_field.getText());
						if(valMax < g.getValMin()) {
							this.setState("flim");
							throw new RuntimeException("Limita inferioara mai mare sau egala cu cea superioara");
						}
						else {
							g.setValMax(valMax);
							g.setNewNumber();
							this.setState("clim");
						}
						if(g.showNumberState())
							this.showNumber_label.setText("[Cheat] Numar: " + g.getNumber());
					}
					else
						throw new RuntimeException("Ambele campuri libere in dialog!");
				}
				catch(NumberFormatException ex) {
					System.out.println("NumberFormatException: " + ex.getMessage());
					//ex.printStackTrace();
				}
				catch(RuntimeException ex) {
					System.out.println(ex.getMessage());
					//ex.printStackTrace();
				}
				System.out.println("Dialog OK");
			}
		});
	}
	
	@FXML
	private void handleSavePathButton() {
		DirectoryChooser browser = new DirectoryChooser();
		File director = browser.showDialog(new Stage());
		try {
			g.setSavePath(director.getAbsolutePath() + "/");
		}
		catch(NullPointerException ex) {
			this.setState("sphf");
		}
		this.setState("sphc");
	}
	
	@FXML
	private void handleShowNumberCheat() {
		boolean status = this.showNumber_button.isSelected();
		if(status) {
			if(!g.isCheater()) {
				Alert confirm = new Alert(AlertType.CONFIRMATION, "Esti sigur ca vrei sa activezi cheat-urile? Nu vei mai putea salva");
				confirm.showAndWait().ifPresent(response -> {
					if(response == ButtonType.OK) {
						g.setShowNumberCheat(true);
						g.setCheater();
						Stage stage = (Stage)try_field.getScene().getWindow();
						stage.setTitle("Guess the number [CHEATER]");
					}
				});
			}
			else {
				g.setShowNumberCheat(true);
			}
			this.showNumber_label.setText("[Cheat] Numar: " + g.getNumber());
			this.setState("snca");
		}
		else {
			g.setShowNumberCheat(false);
			this.showNumber_label.setText("");
			this.setState("sncd");
		}
	}
	
	// gettere
	public String getState() {
		return this.state.get();
	}
	//settere
	public void setState(String state) {
		this.state.set(state);
	}
}
