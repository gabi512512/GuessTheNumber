package joc.pachet;

// idei:
// - adaugare un rand in plus in save care va include versiunea jocului din care s-a facut salvarea pentru a nu aparea exceptii nedorite
// cand adaugam campuri noi la un viitor update

import java.io.File;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
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
	private boolean tester = false;
	
	// butoane
	@FXML
	private Button try_button;
	@FXML 
	private Button save_button;
	@FXML
	private Button reload_button;
	@FXML
	private Button change_button;
	@FXML
	private MenuItem savePath_button;
	@FXML
	private MenuItem language_button;
	@FXML
	private CheckMenuItem showNumber_button;
	
	// text fields
	@FXML
	private TextField try_field;
	
	// labels
	@FXML
	private Label title_label;
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
	// --
		private Text prefix_text = new Text();
		private Text sufix_text = new Text();
	// --
	@FXML
	private TextFlow status_textFlow;
	@FXML
	private Label showNumber_label;
    
	@FXML
	public void initialize() {
		showNumber_label.setText("");
		status_textFlow.getChildren().addAll(prefix_text, sufix_text);
		
		// default language
		LanguageManager.setLanguage("ro");
		this.updateLanguage(true);
		
		// default values
		g.setValMin(-10);
		g.setValMax(10);
		g.setTries(0);
		g.setTriesTotal(0);
		g.setGuessesTotal(0);
		g.setDimMax(0);
		g.setTriesLowest(Integer.MAX_VALUE);
		g.setLanguage("ro");
		g.setNewNumber();
		this.setStatus(Color.DARKORCHID, "status", "init");
		
		// simple labels
		valMin_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("valMin"), g.getValMin()); }, 
			g.getValMinProperty(), g.getLanguageProperty()));
		valMax_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("valMax"), g.getValMax()); }, 
			g.getValMaxProperty(), g.getLanguageProperty()));
		dim_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("dim"), g.getValMax() - g.getValMin() + 1); }, 
			g.getValMinProperty(), g.getValMaxProperty(), g.getLanguageProperty()));
		thisTryCount_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("this_try_count"), g.getTries()); }, 
			g.getTriesProperty(), g.getLanguageProperty()));
		totalTryCount_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("total_try_count"), g.getTriesTotal()); }, 
			g.getTriesTotalProperty(), g.getLanguageProperty()));
		totalGuesses_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("total_guesses_count"), g.getGuessesTotal()); }, 
			g.getGuessesTotalProperty(), g.getLanguageProperty()));
		dimMax_label.textProperty().bind(Bindings.createStringBinding(() -> {
			return String.format(LanguageManager.get("dim_max"), g.getDimMax()); }, 
			g.getDimMaxProperty(), g.getLanguageProperty()));
		
		// advanced labels
		winPercent_label.textProperty().bind(Bindings.createStringBinding(() -> {
			String prefix = LanguageManager.get("win_percent");
			if(g.getGuessesTotal() == 0)
				return prefix + "0.00%";
			return String.format(prefix + "%.2f%%", (float)g.getGuessesTotal()/g.getTriesTotal() * 100.0); },
			g.getTriesTotalProperty(), g.getGuessesTotalProperty(), g.getLanguageProperty()));
		
		lowestTryCount_label.textProperty().bind(Bindings.createStringBinding(() -> {
			String prefix = LanguageManager.get("lowest_try_count");
			if(g.getTriesLowest() == Integer.MAX_VALUE)
				return prefix + "∞";
			return String.format(prefix + "%d", g.getTriesLowest()); },
			g.getTriesLowestProperty(), g.getLanguageProperty()));
	}
	
	@FXML
	private void handleTryButton() {
		try {
			if(try_field.getText().equals("tester")) {
				this.tester = true;
				System.out.println("tester mode");
				return;
			}
			int number =  Integer.parseInt(try_field.getText());
			if(number == g.getNumber()) {
				g.guessed();
				this.setStatus(Color.DARKORCHID, "status", "guessed");
				if(g.showNumberState())
					this.showNumber_label.setText(LanguageManager.get("cheat.number") + g.getNumber());
			}
			else {
				g.failed();
				this.setStatus(Color.DARKORCHID, "status", "failed");
			}
		}
		catch(NumberFormatException ex) {
			this.setStatus(Color.RED, "error", "invalid_number");
		}
	}
	
	@FXML
	private void handleSaveButton() {
		if(!g.isCheater() || this.tester) {
			boolean suprascris = g.save();
			if(suprascris)
				this.setStatus(Color.DARKORCHID, "status", "saved_overwrite");
			else
				this.setStatus(Color.DARKORCHID, "status", "saved");
		}
		else
			this.setStatus(Color.RED, "error", "save_failed_cheats");
	}
	
	@FXML
	private void handleReloadButton() {
		if(!g.isCheater() || this.tester) {
			boolean incarcat = g.load();
			if(incarcat)
				this.setStatus(Color.DARKORCHID, "status", "reload");
			else
				this.setStatus(Color.RED, "error", "reload_failed");
		}
		else
			this.setStatus(Color.RED, "error", "reload_failed_cheats");
	}
	
	@FXML
	private void handleChangeButton() {
		String prefix = "dialog.limits.";
		Dialog<ButtonType> limits_dialog = new Dialog<>();
		limits_dialog.setTitle(LanguageManager.get(prefix + "title"));
		limits_dialog.setHeaderText(LanguageManager.get(prefix + "content"));
		limits_dialog.getDialogPane().getButtonTypes().add(0, ButtonType.OK);
		
		Label valMin_label = new Label(LanguageManager.get(prefix + "inf_limit"));
		Label valMax_label = new Label(LanguageManager.get(prefix + "sup_limit"));
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
							this.setStatus(Color.RED, "error", "failed_boundaries");
						}
						else {
							g.setValMin(valMin);
							g.setValMax(valMax);
							g.setNewNumber();
							this.setStatus(Color.DARKORCHID, "status", "changed_boundaries");
						}
					}
					else if(!valMin_field.getText().isEmpty()) {
						valMin = Integer.parseInt(valMin_field.getText());
						if(valMin > g.getValMax()) {
							this.setStatus(Color.RED, "error", "failed_boundaries");
						}
						else {
							g.setValMin(valMin);
							g.setNewNumber();
							this.setStatus(Color.DARKORCHID, "status", "changed_boundaries");
						}
					}
					else if(!valMax_field.getText().isEmpty()) {
						valMax = Integer.parseInt(valMax_field.getText());
						if(valMax < g.getValMin()) {
							this.setStatus(Color.RED, "error", "failed_boundaries");
						}
						else {
							g.setValMax(valMax);
							g.setNewNumber();
							this.setStatus(Color.DARKORCHID, "status", "changed_boundaries");
						}
						if(g.showNumberState())
							this.showNumber_label.setText(LanguageManager.get("cheat.number") + g.getNumber());
					}
					else
						this.setStatus(Color.RED, "error", "empty_fields");
				}
				catch(NumberFormatException ex) {
					this.setStatus(Color.RED, "error", "invalid_number");
				}
			}
		});
	}
	
	@FXML
	private void handleSavePathButton() {
		DirectoryChooser browser = new DirectoryChooser();
		File director = browser.showDialog(new Stage());
		try {
			g.setSavePath(director.getAbsolutePath() + "/");
			this.setStatus(Color.DARKORCHID, "status", "save_path_changed");
		}
		catch(NullPointerException ex) {
			this.setStatus(Color.RED, "error", "save_path_failed");
		}
	}
	
	@FXML
	private void handleShowNumberCheat() {
		boolean status = this.showNumber_button.isSelected();
		if(status) {
			if(!g.isCheater()) {
				String prefix = "dialog.cheats.";
				ButtonType yes = new ButtonType(LanguageManager.get("dialog.yes"), ButtonBar.ButtonData.YES);
				ButtonType no = new ButtonType(LanguageManager.get("dialog.no"), ButtonBar.ButtonData.NO);
				Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "dialog.confirmation", yes, no);
				dialog.setTitle(LanguageManager.get(prefix + "title"));
				dialog.setHeaderText(null);
				dialog.setContentText(LanguageManager.get(prefix + "content"));
				
				Optional<ButtonType>response = dialog.showAndWait();
				if(response.isPresent() && response.get() == yes) {
					g.setShowNumberCheat(true);
					g.setCheater();
					Stage stage = (Stage)try_field.getScene().getWindow();
					stage.setTitle(LanguageManager.get("cheater.title"));
					this.showNumber_label.setText(LanguageManager.get("cheat.number") + g.getNumber());
					this.setStatus(Color.DARKSEAGREEN, "cheat", "number_shown");
				}
				else
					this.showNumber_button.setSelected(false);
			}
			else {
				g.setShowNumberCheat(true);
				this.showNumber_label.setText(LanguageManager.get("cheat.number") + g.getNumber());
				this.setStatus(Color.DARKSEAGREEN, "cheat", "number_shown");
			}
		}
		else {
			g.setShowNumberCheat(false);
			this.showNumber_label.setText("");
			this.setStatus(Color.DARKSEAGREEN, "cheat", "number_not_shown");
		}
	}
	
	@FXML
	private void handleLanguageButton() {
		String prefix = "dialog.language.";
		ChoiceDialog<String> dialog = new ChoiceDialog<>("English", "English", "Română");
		dialog.setTitle(LanguageManager.get(prefix + "title"));
		dialog.setHeaderText(null);
		dialog.setContentText(LanguageManager.get(prefix + "content"));

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(limba -> {
			switch(limba) {
				case "English": LanguageManager.setLanguage("en");
								this.g.setLanguage("en");
								break;
				case "Română": LanguageManager.setLanguage("ro");
							   this.g.setLanguage("ro");
							   break;
			}
			this.updateLanguage(false);
		});
	}
	
	public void setStatus(Color prefixColor, String prefixKey, String sufixKey) {
		prefix_text.setFill(prefixColor);
		prefix_text.setText(LanguageManager.get(prefixKey));
		sufix_text.setText(LanguageManager.get(prefixKey + "." + sufixKey));
	}
	
	public void updateLanguage(boolean init) {
		String prefix = "button.";
		if(!init) {
			Stage stage = (Stage)title_label.getScene().getWindow();
			if(g.isCheater())
				stage.setTitle(LanguageManager.get("cheater.title"));
			else
				stage.setTitle(LanguageManager.get("title"));
		}
		title_label.setText(LanguageManager.get("title"));
		try_button.setText(LanguageManager.get(prefix + "guess"));
		save_button.setText(LanguageManager.get(prefix + "save"));
		reload_button.setText(LanguageManager.get(prefix + "reload"));
		change_button.setText(LanguageManager.get(prefix + "change_boundaries"));
		savePath_button.getParentMenu().setText(LanguageManager.get(prefix + "settings"));
		savePath_button.setText(LanguageManager.get(prefix + "save_path"));
		language_button.setText(LanguageManager.get(prefix + "language"));
		showNumber_button.getParentMenu().setText(LanguageManager.get(prefix + "cheats"));
		showNumber_button.setText(LanguageManager.get(prefix + "show_number"));
	}
}
