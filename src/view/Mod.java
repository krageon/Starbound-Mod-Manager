package view;

import common.Configuration;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Mod {
	private static final String installedImage = Mod.class.getResource("installed.png").toExternalForm();
	private static final String notInstalledImage = Mod.class.getResource("notinstalled.png").toExternalForm();
	private static final String conflictImage = Mod.class.getResource("conflict.png").toExternalForm();
	private static final String outdatedImage = Mod.class.getResource("outdated.png").toExternalForm();
	
	// These never change
	public String internalName;
	
	public String modInfoName;
	public String displayName;
	public String author;
	public String version;
	public String assetsPath;
	public String gameVersion;
	public String description;
	
	// This one might?
	public String conflicts;
	
	// These might be useful as cache or something. This'd rightly need an observer pattern
	//	so that it won't make my eyes water to look at it.
	public boolean installed = false;
	public boolean selected = false;
	public boolean hasConflicts = false;
	public boolean patched = false;
	
	// Interface variables
	public VBox container;
	public GridPane gridPane;
	public Rectangle bottomStroke;
	public Text modName;
	public Text modAuthor;
	public Text modVersion;
	public CenteredRegion modStatus;
	
	public int order = 0;
	
	public void update() {
		gridPane.getStyleClass().removeAll("not-installed-mod-fill", "installed-mod-fill", "conflicted-mod-fill");
		bottomStroke.getStyleClass().removeAll("not-installed-mod-stroke", "installed-mod-stroke", "conflicted-mod-stroke");
		modName.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		modVersion.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		modAuthor.getStyleClass().removeAll("not-installed-font-color", "installed-font-color", "conflicted-font-color");
		gridPane.getChildren().remove(modStatus);
		
		if (!Configuration.gameVersionString.equals(gameVersion)) {
			gridPane.getStyleClass().add("outdated-mod-fill");
			bottomStroke.getStyleClass().add("outdated-mod-stroke");
			modName.getStyleClass().add("outdated-font-color");
			modVersion.getStyleClass().add("outdated-font-color");
			modAuthor.getStyleClass().add("outdated-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(outdatedImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (hasConflicts && !installed && !patched) {
			gridPane.getStyleClass().add("conflicted-mod-fill");
			bottomStroke.getStyleClass().add("conflicted-mod-stroke");
			modName.getStyleClass().add("conflicted-font-color");
			modVersion.getStyleClass().add("conflicted-font-color");
			modAuthor.getStyleClass().add("conflicted-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(conflictImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (installed || patched) {
			gridPane.getStyleClass().add("installed-mod-fill");
			bottomStroke.getStyleClass().add("installed-mod-stroke");
			modName.getStyleClass().add("installed-font-color");
			modVersion.getStyleClass().add("installed-font-color");
			modAuthor.getStyleClass().add("installed-font-color");
			if (hasConflicts) {
				modStatus = new CenteredRegion(new ImageView(new Image(conflictImage)));
			} else {
				modStatus = new CenteredRegion(new ImageView(new Image(installedImage)));
			}
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else {
			gridPane.getStyleClass().add("not-installed-mod-fill");
			bottomStroke.getStyleClass().add("not-installed-mod-stroke");
			modName.getStyleClass().add("not-installed-font-color");
			modVersion.getStyleClass().add("not-installed-font-color");
			modAuthor.getStyleClass().add("not-installed-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(notInstalledImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		}
	}
}
