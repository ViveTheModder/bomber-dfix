package cmd;
//Bomber D'fiX by ViveTheJoestar
import java.io.File;
import gui.Program;

public class Main {
	public static void main(String[] args) throws Exception {
		String helpText = "USAGE: java -jar bomber-dfix-v3.0.jar [path-to-iso] [arg]\n"
		+ "Replace [arg] with either -a (to apply all patches) or one of the numbers below:\n";
		String patchText = "";
		for (int patchCnt = 0; patchCnt < DxPatch.NUM_PATCHES; patchCnt++) {
			String patchName = DxPatch.getPatchTooltip(patchCnt).replace("<br>", "\n");
			patchText += patchCnt + ": " + DxPatch.getPatchName(patchCnt) + "\n" + patchName + "\n\n";
		}
		if (args.length > 0 && args.length <= 2) {
			if (args[0].equals("-h")) {
				System.out.println(helpText + "\n" + patchText);
				System.exit(0);
			}
			File tmp = new File(args[0]);
			if (tmp.isFile() && tmp.getName().toLowerCase().endsWith(".iso")) {
				DxIso iso = new DxIso(tmp);
				if (iso.isValid()) {
					if (iso.isPatched())
						System.out.println("INFO: This ISO has already been patched (ver. " + DxPatch.VER_NUM + ")!");
					long start = System.currentTimeMillis();
					if (args[1].matches("\\d")) {
						int patchNum = Integer.parseInt(args[0]);
						if (patchNum < DxPatch.NUM_PATCHES) {
							String patchName = DxPatch.getPatchName(patchNum);
							patchName = patchName.substring(0, patchName.length() - 11); //remove patch date
							System.out.println("Applying patch #" + patchNum + " (" + patchName + ") to DBZ BT2 DX...");
							new DxPatch(new DxIso(iso.getPath().toFile()), patchNum);
						}
						else System.out.println("ERROR: Invalid patch number!");
					}
					else if (args[1].equals("-a")) {
						for (int patchCnt = 0; patchCnt < DxPatch.NUM_PATCHES; patchCnt++) {
							String patchName = DxPatch.getPatchName(patchCnt);
							patchName = patchName.substring(0, patchName.length() - 11); //remove patch date
							System.out.println("Applying patch #" + patchCnt + " (" + patchName + ") to DBZ BT2 DX...");
							new DxPatch(new DxIso(iso.getPath().toFile()), patchCnt);
						}
					}
					long end = System.currentTimeMillis();
					System.out.println("TIME: " + (end - start) / 1000.0 + " s");
				}
				else System.out.println("ERROR: Provided ISO is NOT a valid DBZ BT2 DX ISO!");
			}
			else System.out.println("ERROR: Path does NOT point to a ISO file!");
		}
		else Program.launch();
	}
}