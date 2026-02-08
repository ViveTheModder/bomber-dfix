package cmd;
//Bomber D'fiX by ViveTheJoestar
public class DxPatch {
	private static final String[] PATCH_NAMES = {
		"Remove Raditz Transformation Tab",
		"Fix Overlapping Z-Item Names"
	};
	private static final String[] PATCH_TIPS = {
		"Removes any mention of Raditz's Great Ape transformation from his skill list.",
		"Removes excess space from the \"Son of Paragus\" and \"Breakthrough the limit\" Z-Items,"
		+ "<br>specifically for the results screen of Dragon History and the character select in Dragon Tournament."
	};
	public static final double VER_NUM = 1.1;
	public static final int NUM_PATCHES = PATCH_NAMES.length;
	
	public DxPatch(DxIso iso, int patchIdx) {
		try {
			switch (patchIdx) {
				case 0: iso.disableGreatApeForm(); break;
				case 1: iso.fixItemNames(); break;
				default: break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getPatchName(int patchIdx) {
		return PATCH_NAMES[patchIdx];
	}
	public static String getPatchTooltip(int patchIdx) {
		return PATCH_TIPS[patchIdx];
	}
}