package joc.pachet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextInputDialog;

public class GameData {
	private static final byte[] cheie = "GabiECamSmecher1".getBytes(); // parola 16 octeti
	private static final SecretKeySpec cheie_secreta = new SecretKeySpec(cheie, "AES");
	
	private String path = System.getProperty("user.home") + "/Documents/";
	private int number;
	private IntegerProperty valMin = new SimpleIntegerProperty();
	private IntegerProperty valMax = new SimpleIntegerProperty();
	private IntegerProperty tries = new SimpleIntegerProperty();
	private IntegerProperty triesTotal = new SimpleIntegerProperty();
	private IntegerProperty guessesTotal = new SimpleIntegerProperty();
	private IntegerProperty dimMax = new SimpleIntegerProperty();
	private IntegerProperty triesLowest = new SimpleIntegerProperty();
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
		dialog_save.setTitle("Save");
		dialog_save.setHeaderText("");
		dialog_save.setContentText("Introdu numele: ");
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
				out.println("number = " + this.getEncodedNumber(this.number));
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
		dialog_load.setTitle("Load");
		dialog_load.setHeaderText("");
		dialog_load.setContentText("Introdu numele: ");
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
					this.number = getDecodedNumber(date.get("number"));
					System.out.println("numar decodat: " + this.number);
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
	
	// criptare numar actual
	public String getEncodedNumber(int number) {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(4); // fac un buffer de octeti si aloc 4 octeti
			buffer.putInt(number); // pun numarul in buffer
			byte[] conversie = buffer.array(); // transform continutul buffer-ului intr-un array de octeti
			
			Cipher cifru = Cipher.getInstance("AES");
			cifru.init(Cipher.ENCRYPT_MODE, cheie_secreta);
			byte[] conversieCriptata = cifru.doFinal(conversie);
			String rezultat = Base64.getEncoder().encodeToString(conversieCriptata);
			
			return rezultat;
		}
		catch(NoSuchAlgorithmException ex) {
			System.out.println("Eroare: algortim de generare chei inexistent");
			System.exit(1);
		}
		catch(NoSuchPaddingException ex) {
			System.out.println("Eroare: padding inexistent");
			System.exit(1);
		}
		catch(BadPaddingException ex) {
			System.out.println("Eroare: padding prost");
			System.exit(1);
		}
		catch(IllegalBlockSizeException ex) {
			System.out.println("Eroare: dimensiune bloc ilegala");
			System.exit(1);
		}
		catch(InvalidKeyException ex) {
			System.out.println("Eroare: Cheie invalida");
			System.exit(1);
		}
		return "ceva nu e bine la criptare";
	}
	
	public int getDecodedNumber(String secret) {
		try {
			byte[] conversieCriptata = Base64.getDecoder().decode(secret);
			Cipher cifru = Cipher.getInstance("AES");
			cifru.init(Cipher.DECRYPT_MODE, cheie_secreta);
			byte[] conversie = cifru.doFinal(conversieCriptata);
			
			ByteBuffer buffer = ByteBuffer.wrap(conversie);
			
			return buffer.getInt();
		}
		catch(InvalidKeyException ex) {
			System.out.println("Eroare: cheie invalida");
			System.exit(1);
		}
		catch(NoSuchAlgorithmException ex) {
			System.out.println("Eroare: algortim de generare chei inexistent");
			System.exit(1);
		}
		catch(NoSuchPaddingException ex) {
			System.out.println("Eroare: padding inexistent");
			System.exit(1);
		}
		catch(BadPaddingException ex) {
			System.out.println("Eroare: padding prost");
			System.exit(1);
		}
		catch(IllegalBlockSizeException ex) {
			System.out.println("Eroare: dimensiune bloc ilegala");
			System.exit(1);
		}
		return 0;
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
}
