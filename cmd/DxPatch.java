package cmd;
//Bomber D'fiX by ViveTheJoestar
public class DxPatch {
	public static final int NUM_PATCHES = 1;
	private static final String[] PATCH_NAMES = {
		"Remove Raditz Transformation Tab"	
	};
	private static final String[] PATCH_TIPS = {
		"Removes any mention of Raditz's Great Ape transformation from his skill list."
	};
	
	public DxPatch(DxIso iso, int patchIdx) {
		try {
			switch (patchIdx) {
				case 0: iso.disableGreatApeForm(); break;
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