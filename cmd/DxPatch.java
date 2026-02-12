package cmd;
//Bomber D'fiX by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class DxPatch {
	private static final String[] PATCH_NAMES = {
		"Remove Raditz Transformation Tab (06/02/26)",
		"Fix Overlapping Z-Item Names (08/02/26)",
		"Fix Character Roster Bugs (11/02/26)",
		"Fix Invalid Costume for Piccolo in Buu Saga (11/02/26)",
		"Enable Giant-Piercing Ability for GT Goku Dragon Fist (11/02/26)",
		"Correct Wild Sense Cost (12/02/26)",
		"Fix Missing Special Quote Voice Lines (12/02/26)"
	};
	private static final String[] PATCH_TIPS = {
		"Removes any mention of Raditz's Great Ape transformation from his skill list.",
		"Removes excess space from the \"Son of Paragus\" and \"Breakthrough the limit\" Z-Items,"
		+ "<br>specifically for the results screen of Dragon History and the character select in Dragon Tournament.",
		"Rearranges Goku (End) and Goten's positions in the roster so that<br>"
		+ "if Gotenks, Gogeta or Vegito are selected in Dragon Adventure<br>and then Evolution Z (to customize them), "
		+ "the displayed character<br>will no longer point to Vegeta (End) or Goku (Early).<br><br>"
		+ "Master Roshi and Yajirobe have also been rearranged,<br>alongside Mecha Frieza, Cooler, Meta-Cooler and Android #13,<br>"
		+ "in order to prevent issues caused by<br>misplacement of the Random & Password character slots.<br><br>"
		+ "To prevent confusion, the 2nd \"Lower class Saiyan\" Z-Item has<br>been renamed to \"Half-Saiyan Energy\".<br><br>"
		+ "In addition, Vegeta's name is properly set to Vegeta (End)<br>for Ultimate Battle Z and Dragon Tournament.<br><br>"
		+ "This patch affects all gamemodes with character selection/customization.",
		"Instead of the early damaged costume (no. 6), Piccolo will use his 2nd costume (the one with the cape).",
		"Makes GT Goku's Dragon Fist (base form) able to work against giant characters.",
		"Changes the cost of Wild Sense from 3 to 2 for the following characters:<br>"
		+ "* Goku (End) - Super Saiyan<br>"
		+ "* Ultimate Gohan<br>"
		+ "* Gogeta - Super Saiyan 4<br>"
		+ "* Omega Shenron",
		"Restores Krillin and Perfect Cell's missing interaction<br>voice lines against Vegeta (Early) and Future Trunks."
	};
	public static final double VER_NUM = 3.0;
	public static final int NUM_PATCHES = PATCH_NAMES.length;
	private static final int NUM_PATCH_FILES = 11;
	
	public DxPatch(DxIso iso, int patchIdx) {
		try {
			switch (patchIdx) {
				case 0: iso.disableGreatApeForm(); break;
				case 1: iso.fixItemNames(); break;
				case 2: iso.fixCharaRoster(); break;
				case 3: iso.fixPiccoloCostume(); break;
				case 4: iso.enableDragonFistAgainstGiants(); break;
				case 5: iso.rebalanceWildSense(); break;
				case 6: iso.fixSpecialQuotes(); break;
				default: break;
			}
			iso.writeWatermark();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getNumPatchFiles(DxPatchInfo[] infoArray, int patchNum) {
		int cnt = 0;
		for (DxPatchInfo info: infoArray) {
			if (info.getPnum() == patchNum) cnt++;
		}
		return cnt;
	}
	public static DxPatchInfo[] getAllPatchInfo() throws IOException {
		int cnt = 0;
		DxPatchInfo[] infoArray = new DxPatchInfo[NUM_PATCH_FILES];
		//There is no way to access file contents from an input stream without actually writing to a file
		File temp = new File("patch.csv");
		InputStream inputStm = DxIso.class.getResourceAsStream("/patch/patch.csv");
		//COPY_ATTRIBUTES does not work for resources (files stored inside the JAR)
		Files.copy(inputStm, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		Scanner sc = new Scanner(temp);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] lineArray = line.split(",");
			int patchNum = Integer.parseInt(lineArray[0]);
			int patchAddr = Integer.parseInt(lineArray[2]);
			int patchSizeOld = Integer.parseInt(lineArray[3]);
			int patchSizeNew = Integer.parseInt(lineArray[4]);
			infoArray[cnt] = new DxPatchInfo(patchNum, patchAddr, patchSizeOld, patchSizeNew, lineArray[1]);
			cnt++;
		}
		sc.close();
		temp.delete();
		return infoArray;
	}
	public static String getPatchName(int patchIdx) {
		return PATCH_NAMES[patchIdx];
	}
	public static String getPatchTooltip(int patchIdx) {
		return PATCH_TIPS[patchIdx];
	}
}