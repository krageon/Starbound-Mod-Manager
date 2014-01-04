package modmanager.view;

import modmanager.common.Configuration;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Mod {
	private static final String installedImage = Mod.class.getResource(
			"installed.png").toExternalForm();
	private static final String notInstalledImage = Mod.class.getResource(
			"notinstalled.png").toExternalForm();
	private static final String conflictImage = Mod.class.getResource(
			"conflict.png").toExternalForm();
	private static final String outdatedImage = Mod.class.getResource(
			"outdated.png").toExternalForm();

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

	// These might be useful as cache or something. This'd rightly need an
	// observer pattern
	// so that it won't make my eyes water to look at it.
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

	public Mod() {
		// This adds an entry to the list?
		container = new VBox();

		gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);

		gridPane.setMinHeight(46);
		gridPane.setPadding(new Insets(0, 0, 0, 15));

		bottomStroke = new Rectangle(350, 2);
		bottomStroke.widthProperty().bind(container.widthProperty());

		container.getChildren().addAll(gridPane, bottomStroke);

		modName = new Text(displayName);
		modName.getStyleClass().add("modname");
		gridPane.add(modName, 0, 0, 1, 2);

		modVersion = new Text("v" + version);
		modVersion.getStyleClass().add("modversion");
		GridPane.setValignment(modVersion, VPos.BOTTOM);
		gridPane.add(modVersion, 1, 0);

		modAuthor = new Text(author);
		modAuthor.getStyleClass().add("modauthor");
		GridPane.setValignment(modVersion, VPos.TOP);
		gridPane.add(modAuthor, 1, 1);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setFillWidth(true);
		col1.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().add(col1);

		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.RIGHT);
		gridPane.getColumnConstraints().add(col2);

		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(42);
		col3.setMaxWidth(42);
		col3.setHalignment(HPos.CENTER);
		gridPane.getColumnConstraints().add(col3);

		update();
	}

	public void update() {
		gridPane.getStyleClass().removeAll("not-installed-mod-fill",
				"installed-mod-fill", "conflicted-mod-fill");
		bottomStroke.getStyleClass().removeAll("not-installed-mod-stroke",
				"installed-mod-stroke", "conflicted-mod-stroke");
		modName.getStyleClass().removeAll("not-installed-font-color",
				"installed-font-color", "conflicted-font-color");
		modVersion.getStyleClass().removeAll("not-installed-font-color",
				"installed-font-color", "conflicted-font-color");
		modAuthor.getStyleClass().removeAll("not-installed-font-color",
				"installed-font-color", "conflicted-font-color");
		gridPane.getChildren().remove(modStatus);

		if (!Configuration.gameVersionString.equals(gameVersion)) {
			gridPane.getStyleClass().add("outdated-mod-fill");
			bottomStroke.getStyleClass().add("outdated-mod-stroke");
			modName.getStyleClass().add("outdated-font-color");
			modVersion.getStyleClass().add("outdated-font-color");
			modAuthor.getStyleClass().add("outdated-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(
					outdatedImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (hasConflicts && !installed && !patched) {
			gridPane.getStyleClass().add("conflicted-mod-fill");
			bottomStroke.getStyleClass().add("conflicted-mod-stroke");
			modName.getStyleClass().add("conflicted-font-color");
			modVersion.getStyleClass().add("conflicted-font-color");
			modAuthor.getStyleClass().add("conflicted-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(
					conflictImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else if (installed || patched) {
			gridPane.getStyleClass().add("installed-mod-fill");
			bottomStroke.getStyleClass().add("installed-mod-stroke");
			modName.getStyleClass().add("installed-font-color");
			modVersion.getStyleClass().add("installed-font-color");
			modAuthor.getStyleClass().add("installed-font-color");
			if (hasConflicts) {
				modStatus = new CenteredRegion(new ImageView(new Image(
						conflictImage)));
			} else {
				modStatus = new CenteredRegion(new ImageView(new Image(
						installedImage)));
			}
			gridPane.add(modStatus, 2, 0, 1, 2);
		} else {
			gridPane.getStyleClass().add("not-installed-mod-fill");
			bottomStroke.getStyleClass().add("not-installed-mod-stroke");
			modName.getStyleClass().add("not-installed-font-color");
			modVersion.getStyleClass().add("not-installed-font-color");
			modAuthor.getStyleClass().add("not-installed-font-color");
			modStatus = new CenteredRegion(new ImageView(new Image(
					notInstalledImage)));
			gridPane.add(modStatus, 2, 0, 1, 2);
		}
	}
}
