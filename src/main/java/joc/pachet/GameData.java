package joc.pachet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextInputDialog;

public class GameData {	
	private String path = System.getProperty("user.home") + "/Documents/";
	private int number;
	private IntegerProperty valMin = new SimpleIntegerProperty();
	private IntegerProperty valMax = new SimpleIntegerProperty();
	private IntegerProperty tries = new SimpleIntegerProperty();
	private IntegerProperty triesTotal = new SimpleIntegerProperty();
	private IntegerProperty guessesTotal = new SimpleIntegerProperty();
	private IntegerProperty dimMax = new SimpleIntegerProperty();
	private IntegerProperty triesLowest = new SimpleIntegerProperty();
	private StringProperty language = new SimpleStringProperty();
	private boolean cheater = false;
	private boolean showNumberCheat = false;
	
	public void failed() {
		setTries(getTries() + 1);
		setTriesTotal(getTriesTotal() + 1);
	}
	
	public void guessed() {
		if(getValMax() - getValMin() >= getDimMax() && getTries() <= getTriesLowest()) { // daca intervalul cel mai mare in care s-a ghicit e mai mic decat intervalul actual
			setDimMax(getValMax() - getValMin() + 1);									// + daca numarul de incercari e mai mic decat cele mai putine incercari -> new best
			setTriesLowest(getTries());
		}
		setTries(0);
		setTriesTotal(getTriesTotal() + 1);
		setGuessesTotal(getGuessesTotal() + 1);
		setNewNumber();
	}
	
	public boolean save() {
		boolean suprascris = false;
		TextInputDialog dialog_save = new TextInputDialog();
		dialog_save.setTitle(LanguageManager.get("dialog.save.title"));
		dialog_save.setHeaderText(null);
		dialog_save.setContentText(LanguageManager.get("dialog.save_load.content"));
		Optional<String> rezultat = dialog_save.showAndWait();
		if(rezultat.isPresent()) {
			String name = rezultat.get();
			File fisier = new File(path + name + ".dat");
			if(fisier.exists())
				suprascris = true;
			try(PrintWriter out = new PrintWriter(new FileWriter(fisier))) {
				out.println("name = " + name);
				out.println("valMin = " + this.getValMin());
				out.println("valMax = " + this.getValMax());
				out.println("tries = " + this.getTries());
				out.println("triesTotal = "  + this.getTriesTotal());
				out.println("guessesTotal = " + this.getGuessesTotal());
				out.println("dimMax = " + this.getDimMax());
				out.println("triesLowest = " + this.getTriesLowest());
				out.println("number = " + this.getEncodedNumber(String.valueOf(this.number)));
				out.println("language = " + this.getLanguage());
				return suprascris;
			}
			catch(IOException ex) {
				System.out.println("IOException");
				System.exit(1);
			}
		}
		return false;
	}
	
	public boolean load() {
		TextInputDialog dialog_load = new TextInputDialog();
		Map<String, String> date = new HashMap<>();
		dialog_load.setTitle(LanguageManager.get("dialog.load.title"));
		dialog_load.setHeaderText(null);
		dialog_load.setContentText(LanguageManager.get("dialog.save_load.content"));
		Optional<String> rezultat = dialog_load.showAndWait();
		if(rezultat.isPresent()) {
			String name = rezultat.get();
			File fisier = new File(path + name + ".dat");
			if(fisier.exists()) {
				try(BufferedReader in = new BufferedReader(new FileReader(fisier))) {
					String linie;
					String[] parti;
					while((linie = in.readLine()) != null) {
						linie = linie.trim();
						parti = linie.split("=", 2);
						date.put(parti[0].trim(), parti[1].trim());
					}
	
					this.setValMin(Integer.parseInt(date.get("valMin")));
					this.setValMax(Integer.parseInt(date.get("valMax")));
					this.setTries(Integer.parseInt(date.get("tries")));
					this.setTriesTotal(Integer.parseInt(date.get("triesTotal")));
					this.setGuessesTotal(Integer.parseInt(date.get("guessesTotal")));
					this.setDimMax(Integer.parseInt(date.get("dimMax")));
					this.setTriesLowest(Integer.parseInt(date.get("triesLowest")));
					this.setLanguage(date.get("language"));
					this.number = Integer.parseInt(getDecodedNumber(date.get("number")));
					return true;
				}
				catch(FileNotFoundException ex) {
					System.out.println("Save-ul nu exista");
				}
				catch(IOException ex) {
					System.out.println("IOException");
					System.exit(1);
				}
			}
		}
		return false;
	}
	
	// codare numar actual
	public String getEncodedNumber(String number) {
		return Base64.getEncoder().encodeToString(number.getBytes());
	}
	
	// decodare numar actual
	public String getDecodedNumber(String secret) {
		return new String(Base64.getDecoder().decode(secret));
	}
	
	// gettere valori primitive
	public int getNumber() {
		return this.number;
	}
	public int getValMin() {
		return this.valMin.get();
	}
	public int getValMax() {
		return this.valMax.get();
	}
	public int getTries() {
		return this.tries.get();
	}
	public int getTriesTotal() {
		return this.triesTotal.get();
	}
	public int getGuessesTotal() {
		return this.guessesTotal.get();
	}
	public int getDimMax() {
		return this.dimMax.get();
	}
	public int getTriesLowest() {
		return this.triesLowest.get();
	}
	public String getLanguage() {
		return this.language.get();
	}
	public boolean showNumberState() {
		return this.showNumberCheat;
	}
	public boolean isCheater() {
		return this.cheater;
	}
	
	// settere valori primitive
	public void setValMin(int valMin) {
		this.valMin.set(valMin);
	}
	public void setValMax(int valMax) {
		this.valMax.set(valMax);
	}
	public void setNewNumber() {
		this.number = (int) (valMin.get() + (valMax.get() - valMin.get()) * Math.random());
	}
	public void setTries(int tries)  {
		this.tries.set(tries);
	}
	public void setTriesTotal(int triesTotal) {
		this.triesTotal.set(triesTotal);
	}
	public void setGuessesTotal(int guessesTotal) {
		this.guessesTotal.set(guessesTotal);
	}
	public void setDimMax(int dimMax) {
		this.dimMax.set(dimMax);
	}
	public void setTriesLowest(int triesLowest) {
		this.triesLowest.set(triesLowest);
	}
	public void setSavePath(String path) {
		this.path = path;
	}
	public void setCheater() {
		this.cheater = true;
	}
	public void setShowNumberCheat(boolean showNumberCheat) {
		this.showNumberCheat = showNumberCheat;
	}
	public void setLanguage(String language) {
		this.language.set(language);
	}
	
	// gettere proprietati
	public IntegerProperty getValMinProperty() {
		return this.valMin;
	}
	public IntegerProperty getValMaxProperty() {
		return this.valMax;
	}
	public IntegerProperty getTriesProperty() {
		return this.tries;
	}
	public IntegerProperty getTriesTotalProperty() {
		return this.triesTotal;
	}
	public IntegerProperty getGuessesTotalProperty() {
		return this.guessesTotal;
	}
	public IntegerProperty getDimMaxProperty() {
		return this.dimMax;
	}
	public IntegerProperty getTriesLowestProperty() {
		return this.triesLowest;
	}
	public StringProperty getLanguageProperty() {
		return this.language;
	}
}
