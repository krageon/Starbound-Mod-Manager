package data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import view.CenteredRegion;
import view.FXDialogueConfirm;
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
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

import common.Configuration;
import common.FileHelper;

public class Mod {
	// I'm pretty sure this needs to go also.
	public ArrayList<String> filesModified = new ArrayList<String>();

	public boolean patched = false;

	// These never change
	public String internalName;
	public String file;
	public String subDirectory;

	private Mod() {
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setName(String name) {
		this.internalName = name;
	}

	// TODO: This is view logic - the variables need an observer pattern and
	// getters and setters so they'll
	// be less retarded to manage and read.
	public void setConflicted(boolean conflicted) {
		hasConflicts = conflicted;
		updateStyles();
	}

	public ArrayList<String> getFilesModified() {
		return filesModified;
	}

	public void setFilesModified(ArrayList<String> filesModified) {
		this.filesModified = filesModified;
	}

	public void uninstall(final ArrayList<Mod> installedMods) {
		patched = false;

		// TODO: Replace with !Directory.Exists
		if (!installed) {
			return;
		}

		// TODO: Remove the need for this (as often as possible - magic strings! D:)
		Configuration.addProperty("mods", file, "false");
		
		// TODO: Ditto. Maybe cache.
		installed = false;

		if (conflictsWithInstalledMods(installedMods)) {

			try {
				createModPatch(installedMods);
			} catch (IOException e) {
				Configuration.printException(e, "Creating mod patch.");
			}

		}

		if (new File(Configuration.modsInstallFolder.getAbsolutePath()
				+ File.separator + internalName).exists()) {

			try {
				FileHelper.deleteFile(Configuration.modsInstallFolder
						.getAbsolutePath()
						+ File.separator
						+ modInfoName.substring(0,
								modInfoName.indexOf(".modinfo")));
			} catch (IOException e) {
				Configuration.printException(e,
						"Deleting installed mod folder when uninstalling.");
			}

		}
		// TODO: View logic again. This is why we need an observer
		updateStyles();

	}

	public void install(final ArrayList<Mod> installedMods) {

		try {

			ZipFile modArchive = new ZipFile(
					Configuration.modsFolder.getAbsolutePath() + File.separator
							+ file);

			modArchive
					.extractAll(Configuration.modsInstallFolder
							.getAbsolutePath()
							+ File.separator
							+ modInfoName.substring(0,
									modInfoName.indexOf(".modinfo")));

		} catch (ZipException e) {
			Configuration.printException(e,
					"Extracting mod folder when installing.");
		}

		if (subDirectory != null) {
			try {
				// TODO: Proper single-argument function for this
				//	NOTE: What does that first part even do? That is weird.
				FileHelper.copyDirectory(
						Configuration.modsInstallFolder.getAbsolutePath()
								+ File.separator
								+ subDirectory
								+ File.separator
								+ modInfoName.substring(0,
										modInfoName.indexOf(".modinfo")),
						Configuration.modsInstallFolder.getAbsolutePath()
								+ File.separator
								+ modInfoName.substring(0,
										modInfoName.indexOf(".modinfo")));
				FileHelper.deleteFile(Configuration.modsInstallFolder
						.getAbsolutePath()
						+ File.separator
						+ subDirectory
						+ File.separator
						+ modInfoName.substring(0,
								modInfoName.indexOf(".modinfo")));
			} catch (IOException e) {
				Configuration
						.printException(e,
								"Copying installed mod subdirectory to main directory.");
			}
		}
		
		// TODO: Redundant or view logic.
		Configuration.addProperty("mods", file, "true");
		installed = true;

		updateStyles();

		if (conflictsWithInstalledMods(installedMods)) {
			try {
				createModPatch(installedMods);
			} catch (IOException e) {
				Configuration.printException(e, "Creating mod patch.");
			}
		}

	}

	private boolean conflictsWithInstalledMods(
			final ArrayList<Mod> installedMods) {
		// TODO: There *must* be a more elegant way to do this.
		//	NOTE: Maybe read filelist, store in sqlite db with .zip file hash as key.
		//	NOTE: Alternatively, shell out to 7z - there's definitely a performance penalty here.
		ArrayList<Mod> tempModList = new ArrayList<Mod>(installedMods);
		ArrayList<String> conflictingFiles = new ArrayList<String>();

		tempModList.remove(this);

		for (Mod mod : tempModList) {
			conflictingFiles.addAll(mod.filesModified);
		}

		conflictingFiles.retainAll(filesModified);

		return (conflictingFiles.size() != 0);

	}

	private void createModPatch(final ArrayList<Mod> installedMods)
			throws IOException {
		// TODO: This is a mess
		if (installedMods.size() <= 1) {

			// No patches needed, delete the patches folder.
			if (Configuration.modsPatchesFolder.exists()) {
				FileHelper.deleteFile(Configuration.modsPatchesFolder);
			}
			// TODO: Investigate possible code duplication
			for (Mod mod : installedMods) {

				try {
					ZipFile modArchive = new ZipFile(
							Configuration.modsFolder.getAbsolutePath()
									+ File.separator + mod.file);
					modArchive.extractAll(Configuration.modsInstallFolder
							.getAbsolutePath()
							+ File.separator
							+ mod.modInfoName.substring(0,
									mod.modInfoName.indexOf(".modinfo")));
				} catch (ZipException e) {
					Configuration
							.printException(e,
									"Last installed mod in patch gets put in its own folder again.");
				}

				if (mod.subDirectory != null) {
					try {
						FileHelper.copyDirectory(
								Configuration.modsInstallFolder
										.getAbsolutePath()
										+ File.separator
										+ mod.subDirectory
										+ File.separator
										+ mod.modInfoName.substring(0,
												mod.modInfoName
														.indexOf(".modinfo")),
								Configuration.modsInstallFolder
										.getAbsolutePath()
										+ File.separator
										+ mod.modInfoName.substring(0,
												mod.modInfoName
														.indexOf(".modinfo")));
						FileHelper.deleteFile(Configuration.modsInstallFolder
								.getAbsolutePath()
								+ File.separator
								+ mod.subDirectory
								+ File.separator
								+ mod.modInfoName.substring(0,
										mod.modInfoName.indexOf(".modinfo")));
					} catch (IOException e) {
						Configuration
								.printException(e,
										"Copying installed mod subdirectory to main directory during patch.");
					}
				}

			}

			return;

		}
		
		// NOTE: Why does this happen
		Collections.reverse(installedMods);

		// TODO: This. Cleaner.
		for (Mod mod : installedMods) {

			try {
				ZipFile modArchive = new ZipFile(
						Configuration.modsFolder.getAbsolutePath()
								+ File.separator + mod.file);
				modArchive.extractAll(Configuration.modsInstallFolder
						.getAbsolutePath()
						+ File.separator
						+ mod.modInfoName.substring(0,
								mod.modInfoName.indexOf(".modinfo")));
			} catch (ZipException e) {
				Configuration.printException(e,
						"Copying temporary folders and files for mod merging.");
			}

			if (mod.subDirectory != null) {
				try {
					FileHelper.copyDirectory(
							Configuration.modsInstallFolder.getAbsolutePath()
									+ File.separator
									+ mod.subDirectory
									+ File.separator
									+ mod.modInfoName
											.substring(0, mod.modInfoName
													.indexOf(".modinfo")),
							Configuration.modsInstallFolder.getAbsolutePath()
									+ File.separator
									+ mod.modInfoName
											.substring(0, mod.modInfoName
													.indexOf(".modinfo")));
					FileHelper.deleteFile(Configuration.modsInstallFolder
							.getAbsolutePath()
							+ File.separator
							+ mod.subDirectory
							+ File.separator
							+ mod.modInfoName.substring(0,
									mod.modInfoName.indexOf(".modinfo")));
				} catch (IOException e) {
					Configuration
							.printException(e,
									"Copying installed mod subdirectory to main directory during patch.");
				}
			}

		}
		
		// NOTE: Didn't we calculate this at a previous point somewhere?
		//	TODO: Investigate and refactor.
		HashSet<Mod> conflictingMods = new HashSet<Mod>();

		// Find all conflicting files in currently installed mods.
		HashMap<String, Integer> fileConflictsTemp = new HashMap<String, Integer>();

		for (Mod mod : installedMods) {
			for (String file : mod.filesModified) {
				if (fileConflictsTemp.containsKey(file)) {
					fileConflictsTemp
							.put(file, fileConflictsTemp.get(file) + 1);
				} else {
					fileConflictsTemp.put(file, 1);
				}
			}
		}

		HashSet<String> fileConflicts = new HashSet<String>();

		for (String s : fileConflictsTemp.keySet()) {
			if (fileConflictsTemp.get(s) > 1) {
				fileConflicts.add(s);
			}
		}

		HashSet<String> toRemove = new HashSet<String>();

		// Purge ignored file extensions.
		for (String s : Configuration.fileTypesToIgnore) {
			for (String file : fileConflicts) {

				if (!new File(Configuration.starboundFolder.getAbsolutePath()
						+ File.separator + "assets" + File.separator + file)
						.exists()) {
					toRemove.add(file);
				}

				if (file.endsWith(s)) {
					toRemove.add(file);
				}

			}
		}

		fileConflicts.removeAll(toRemove);

		if (fileConflicts.isEmpty()) {
			FileHelper.deleteFile(Configuration.modsPatchesFolder
					.getAbsolutePath());
			return;
		}

		// Delete and re-create the patches folder.
		if (Configuration.modsPatchesFolder.exists()) {
			FileHelper.deleteFile(Configuration.modsPatchesFolder);
		}

		Configuration.modsPatchesFolder.mkdir();

		// For each file, get each mod that edits that file.
		// Then get the original file and all mods' files and merge them.
		// Finally, save the merged file in the patched directory.
		ArrayList<Mod> currentMods = new ArrayList<Mod>();

		for (String file : fileConflicts) {

			currentMods.clear();

			for (Mod mod : installedMods) {
				if (mod.filesModified.contains(file)) {
					currentMods.add(mod);
					conflictingMods.add(mod);
				}
			}

			String originalFile = FileHelper
					.fileToString(new File(Configuration.starboundFolder
							.getAbsolutePath()
							+ File.separator
							+ "assets"
							+ File.separator + file));

			diff_match_patch dpm = new diff_match_patch();
			LinkedList<Patch> patchesToApply = new LinkedList<Patch>();

			for (int i = 0; i < currentMods.size(); i++) {

				Mod mod = currentMods.get(i);

				String filePath = "";

				if (mod.assetsPath.isEmpty()) {
					filePath = Configuration.modsInstallFolder
							.getAbsolutePath()
							+ File.separator
							+ mod.internalName
							+ File.separator
							+ file.replace("/", "\\");
				} else {
					filePath = Configuration.modsInstallFolder
							.getAbsolutePath()
							+ File.separator
							+ mod.internalName
							+ File.separator
							+ mod.assetsPath.substring(2)
							+ File.separator
							+ file.replace("/", "\\");
				}

				if (i != currentMods.size() - 1) {
					LinkedList<Diff> diff = dpm.diff_main(originalFile,
							FileHelper.fileToString(new File(filePath)));
					LinkedList<Patch> patches = dpm.patch_make(diff);
					patchesToApply.addAll(patches);
				} else {
					originalFile = (String) dpm.patch_apply(patchesToApply,
							FileHelper.fileToString(new File(filePath)))[0];
				}

			}

			new File(
					new File(Configuration.modsPatchesFolder.getAbsolutePath()
							+ File.separator + "assets" + File.separator + file)
							.getParent()).mkdirs();

			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(
							Configuration.modsPatchesFolder.getAbsolutePath()
									+ File.separator + "assets"
									+ File.separator + file), "UTF-8");

			out.append(originalFile);
			out.flush();
			out.close();

		}

		for (Mod mod : conflictingMods) {

			try {

				FileHelper.deleteFile("tempFolder" + File.separator);

				ZipFile modArchive = new ZipFile(
						Configuration.modsFolder.getAbsolutePath()
								+ File.separator + mod.file);
				modArchive.extractAll(new File("tempFolder" + File.separator)
						.getAbsolutePath());

				ArrayList<File> files = new ArrayList<File>();

				if (mod.assetsPath.isEmpty()) {
					FileHelper.listFiles("tempFolder" + File.separator, files);
				} else {
					FileHelper.listFiles("tempFolder" + File.separator
							+ mod.assetsPath.substring(2), files);
				}

				ArrayList<File> remove = new ArrayList<File>();

				String toFindPath = "";

				if (mod.assetsPath.isEmpty()) {
					toFindPath = new File("tempFolder" + File.separator)
							.getAbsolutePath() + File.separator;
				} else {
					toFindPath = new File("tempFolder" + File.separator
							+ mod.assetsPath.substring(2)).getAbsolutePath()
							+ File.separator;
				}

				for (File file : files) {

					String fileName = file.getAbsolutePath().substring(
							toFindPath.length());
					fileName = fileName.replace("\\", "/");

					if (mod.subDirectory != null) {
						fileName = fileName
								.substring((mod.subDirectory + File.separator)
										.length());
					}

					if (fileConflicts.contains(fileName)) {
						remove.add(file);
					}

					if (fileName.endsWith(".modinfo")) {
						remove.add(file);
					}

				}

				files.removeAll(remove);

				for (File file : files) {

					String fileName = file.getAbsolutePath().substring(
							toFindPath.length());

					if (mod.subDirectory != null) {
						fileName = fileName
								.substring((mod.subDirectory + File.separator)
										.length());
					}

					File outputFile = new File(
							Configuration.modsPatchesFolder.getAbsolutePath()
									+ File.separator + "assets"
									+ File.separator + fileName);
					outputFile.getParentFile().mkdirs();

					FileHelper.copyFile(file, outputFile);

				}

			} catch (ZipException e) {
				Configuration.printException(e,
						"Extracting mod folder when patching.");
			}

			mod.updateStyles();

		}

		for (Mod mod : conflictingMods) {
			FileHelper.deleteFile(Configuration.modsInstallFolder
					.getAbsolutePath() + File.separator + mod.internalName);
		}

		FileHelper.deleteFile("tempFolder" + File.separator);

		FileWriter writer = new FileWriter(new File(
				Configuration.modsPatchesFolder.getAbsolutePath()
						+ File.separator
						+ Configuration.modsPatchesFolder.getName()
						+ ".modinfo"));

		writer.append("{\r\n"
				+ "  \"name\" : \""
				+ Configuration.modsPatchesFolder.getName()
				+ "\",\r\n"
				+ "  \"version\" : \""
				+ Configuration.gameVersionString
				+ "\",\r\n"
				+ "  \"path\" : \"./assets\",\r\n"
				+ "  \"metadata\" : {\r\n"
				+ "    \"name\" : \"A Test Mod\",\r\n"
				+ "    \"author\" : \"KrazyTheFox' Mod Manager\",\r\n"
				+ "    \"description\" : \"This is a patch of all conflicting mods. Do not modify by hand unless you know what you're doing.\",\r\n"
				+ "    \"support_url\" : \"http://community.playstarbound.com/index.php?threads/starbound-mod-manager.51639/\",\r\n"
				+ "    \"version\" : \"1.0\"\r\n" + "  }\r\n" + "}");

		writer.close();

		new FXDialogueConfirm("Patch generated for conflicting mods.").show();

	}

	// TODO: Replace this boolean with a check for folder + modinfo in mods
	// TODO: Flag mods for queueing -> load mods to folders -> check mods for conflicts -> generate patches
	//		in that order - this will save loads of processing time (especially on initial modloader setup).
	// directory
	@SuppressWarnings("rawtypes")
	public static Mod loadMod(String fileName, boolean installed) {

		Mod mod = new Mod();

		mod.file = fileName;
		mod.installed = installed;

		mod.modInfoName = "";

		try {

			ZipFile modArchive = new ZipFile(
					Configuration.modsFolder.getAbsolutePath() + File.separator
							+ mod.file);

			@SuppressWarnings("unchecked")
			List<FileHeader> fileHeaders = modArchive.getFileHeaders();

			for (FileHeader fileHeader : fileHeaders) {
				if (fileHeader.getFileName().endsWith(".modinfo")) {
					modArchive.extractFile(fileHeader,
							new File("").getAbsolutePath());
					mod.modInfoName = fileHeader.getFileName();
					if (mod.modInfoName.contains("/")) {
						mod.subDirectory = mod.modInfoName.substring(0,
								mod.modInfoName.lastIndexOf('/'));
						mod.modInfoName = mod.modInfoName
								.substring(mod.modInfoName.lastIndexOf('/') + 1);
					} else {
						mod.subDirectory = null;
					}
					break;
				}
			}

		} catch (ZipException e) {
			Configuration.printException(e,
					"Searching for mod info in archive: " + mod.file);
		}

		if (mod.modInfoName.isEmpty()) {
			new FXDialogueConfirm(
					"Mod \""
							+ mod.file
							+ "\" is missing a valid .modinfo file.\nPlease contact the creator of this mod for help.")
					.show();
			return null;
		}

		try {

			Map<?, ?> map;

			if (mod.subDirectory == null) {
				map = JsonReader.jsonToMaps(FileHelper.fileToJSON(new File(
						mod.modInfoName)));
			} else {
				map = JsonReader.jsonToMaps(FileHelper.fileToJSON(new File(
						mod.subDirectory + File.separator + mod.modInfoName)));
			}

			for (Object e : map.keySet()) {

				String value = e.toString();

				if (value.equalsIgnoreCase("name")) {

					mod.internalName = map.get(e).toString();

				} else if (value.equalsIgnoreCase("path")) {

					if (map.get(value).equals(".")) {
						mod.assetsPath = "";
					} else {
						mod.assetsPath = map.get(value).toString();
					}

				} else if (value.equalsIgnoreCase("version")) {

					mod.gameVersion = map.get(value).toString();

				} else if (value.equalsIgnoreCase("metadata")) {

					for (Object e2 : ((JsonObject) map.get(value)).entrySet()) {

						String metaValue = e2.toString();

						if (metaValue.split("=").length < 2) {
							continue;
						}

						String key = metaValue.split("=")[0];
						String val = metaValue.split("=")[1];

						if (key.equalsIgnoreCase("author")) {
							mod.author = val;
						} else if (key.equalsIgnoreCase("version")) {
							mod.version = val;
						} else if (key.equalsIgnoreCase("displayname")) {
							mod.displayName = val;
						} else if (key.equalsIgnoreCase("description")) {
							mod.description = val;
						}

					}

				}

			}

		} catch (IOException e) {
			new FXDialogueConfirm(
					"Mod \""
							+ mod.file
							+ "\" is missing a valid .modinfo file.\nPlease contact the creator of this mod for help.")
					.show();
			Configuration.printException(e, "Reading mod info file to JSON: "
					+ mod.modInfoName);
			return null;
		}

		if (!mod.modInfoName.replace(".modinfo", "").toLowerCase()
				.equals(mod.internalName.toLowerCase())) {
			// new FXDialogueConfirm("Mod \"" + mod.file +
			// "\"'s name in its .modinfo file must be the same as the .modinfo file name.\nPlease contact the creator of this mod for help.").show();
			Configuration.printException(new Exception("\"" + mod.modInfoName
					+ "\" does not match \"" + mod.internalName + "\""),
					"Internal name vs. Modinfo name.");
			// return null;
		}

		try {
			if (mod.subDirectory != null) {
				FileHelper.deleteFile(new File(mod.subDirectory));
			} else {
				FileHelper.deleteFile(new File(mod.modInfoName));
			}
		} catch (IOException e) {
			Configuration
					.printException(e, "Deleting temporary .modinfo file.");
		}

		if (mod.displayName == null) {
			mod.displayName = mod.file.substring(0, mod.file.indexOf(".zip"));
		}

		if (mod.version == null) {
			mod.version = "???";
		}

		if (mod.author == null) {
			mod.author = "???";
		}

		try {

			ZipFile modArchive = new ZipFile(
					Configuration.modsFolder.getAbsolutePath() + File.separator
							+ mod.file);

			@SuppressWarnings("unchecked")
			List<FileHeader> fileHeaders = modArchive.getFileHeaders();

			String fileToLookFor = "";

			if (!mod.assetsPath.isEmpty()) {
				fileToLookFor = mod.assetsPath.substring(2) + "/";
			}

			for (FileHeader fileHeader : fileHeaders) {
				if (!fileHeader.isDirectory()
						&& fileHeader.getFileName().contains(fileToLookFor)) {

					String toAdd = fileHeader.getFileName().substring(
							fileHeader.getFileName().indexOf(fileToLookFor)
									+ fileToLookFor.length());

					if (mod.subDirectory != null) {
						toAdd = toAdd
								.substring((mod.subDirectory + File.separator)
										.length());
					}

					boolean isJSONFile = true;
					boolean isModified = true;

					for (String extension : Configuration.fileTypesToIgnore) {
						if (toAdd.endsWith(extension)) {
							isJSONFile = false;
						}
					}

					if (isJSONFile) {

						modArchive.extractFile(fileHeader, new File("temp"
								+ File.separator).getAbsolutePath());

						String fileContents = "";
						String fileLocation = "";

						if (mod.subDirectory != null) {
							fileLocation = "temp" + File.separator
									+ fileToLookFor + mod.subDirectory
									+ File.separator + toAdd;
						} else {
							fileLocation = "temp" + File.separator
									+ fileToLookFor + toAdd;
						}

						fileContents = FileHelper.fileToJSON(new File(
								fileLocation));

						if (fileContents.contains("\"__merge\"")) {
							isModified = false;
							System.out.println("JSON: " + toAdd);
						}

						FileHelper.deleteFile(new File("temp"));

					}

					if (isModified) {
						mod.filesModified.add(toAdd);
					} else {
						System.out.println("Ignored \"" + toAdd
								+ "\", uses new merge system.");
					}

				}
			}

		} catch (ZipException | IOException e) {
			Configuration.printException(e,
					"Locating assets folder in archive.");
		}

		// TODO: This is viewlogic and needs to be moved elsewhere. I'm ... This adds an entry to the list?
		mod.container = new VBox();

		mod.gridPane = new GridPane();
		mod.gridPane.setAlignment(Pos.CENTER);

		mod.gridPane.setMinHeight(46);
		mod.gridPane.setPadding(new Insets(0, 0, 0, 15));

		mod.bottomStroke = new Rectangle(350, 2);
		mod.bottomStroke.widthProperty().bind(mod.container.widthProperty());

		mod.container.getChildren().addAll(mod.gridPane, mod.bottomStroke);

		mod.modName = new Text(mod.displayName);
		mod.modName.getStyleClass().add("modname");
		mod.gridPane.add(mod.modName, 0, 0, 1, 2);

		mod.modVersion = new Text("v" + mod.version);
		mod.modVersion.getStyleClass().add("modversion");
		GridPane.setValignment(mod.modVersion, VPos.BOTTOM);
		mod.gridPane.add(mod.modVersion, 1, 0);

		mod.modAuthor = new Text(mod.author);
		mod.modAuthor.getStyleClass().add("modauthor");
		GridPane.setValignment(mod.modVersion, VPos.TOP);
		mod.gridPane.add(mod.modAuthor, 1, 1);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setFillWidth(true);
		col1.setHgrow(Priority.ALWAYS);
		mod.gridPane.getColumnConstraints().add(col1);

		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.RIGHT);
		mod.gridPane.getColumnConstraints().add(col2);

		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(42);
		col3.setMaxWidth(42);
		col3.setHalignment(HPos.CENTER);
		mod.gridPane.getColumnConstraints().add(col3);

		mod.updateStyles();

		return mod;

	}

}