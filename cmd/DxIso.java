package cmd;
//Bomber D'fiX by ViveTheJoestar
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DxIso {
	//20260202215408 was the previous build date (for v1.02)
	private static final String ISO_BUILD_DATE = "20260205011839";
	private static final String PATCH_WATERMARK = "BOMBER D'FIX PATCH VER. ";
	
	private Path dir;
	private RandomAccessFile raf;
	private String name;
	
	public DxIso(File iso) {
		try {
			dir = iso.toPath();
			raf = new RandomAccessFile(iso, "rw");
			name = iso.getName();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isPatched() throws IOException {
		raf.seek(33651);
		byte[] watermarkBytes = new byte[PATCH_WATERMARK.length()];
		raf.read(watermarkBytes);
		String watermark = new String(watermarkBytes);
		return watermark.equals(PATCH_WATERMARK);
	}
	public boolean isValid() throws IOException {
		raf.seek(33581);
		byte[] buildDateBytes = new byte[ISO_BUILD_DATE.length()];
		raf.read(buildDateBytes);
		String buildDate = new String(buildDateBytes);
		return buildDate.equals(ISO_BUILD_DATE);
	}
	public Path getPath() {
		return dir;
	}
	public String getPatchVersion() throws IOException {
		raf.seek(33675);
		byte[] versionBytes = new byte[3];
		raf.read(versionBytes);
		return new String(versionBytes);
	}
	public String toString() {
		return name;
	}
	public void backup() throws IOException {
		Path newDir = dir.getParent().resolve(name.replace(".iso", ".bak"));
		File newIso = newDir.toFile();
		if (newIso.exists()) newIso.delete();
		Files.copy(dir, newDir, StandardCopyOption.COPY_ATTRIBUTES);
	}
	public void disableGreatApeForm() throws IOException {
		/* As of v1.04, Turles has already had his transformation disabled, so this code is useless
		long[] commonParamAddrs = {3034040384L, 3034939072L, 3035522048L, 3036105728L, 3036689408L, 3037273088L};
		for (long addr: commonParamAddrs) {
			raf.seek(addr);
			raf.writeByte(255); //disable Turles' transformation (now that it points to SS1 Mid Goku)
			raf.seek(addr + 4);
			raf.writeByte(1); //set transformation cost to default (1)
		} */
        long[] sklLstAddrs = {2410878988L, 2411738572L, 2412604748L, 2413545884L, 2414471404L, 2415355596L};
		byte[] sklLstFooter = {0x40, 0x00, 0x0A, 0x00};
		for (long addr: sklLstAddrs) {
			raf.seek(addr);
			//Remove Transformation tab from Raditz's costumes by adding a footer and some padding after it
			raf.write(sklLstFooter);
			raf.write(new byte[192]);
		}
		writeWatermark();
	}
	public void fixItemNames() throws IOException {
		int[] pakAddrs = {16539648, 38950912}, ogSizes = {1551632, 765072}, newSizes = {1551584, 765024};
		String[] fileNames = { "0_DragonTournamentMenu.pak", "1_DragonAdventureResults.cpak" };
		for (int pakCnt = 0; pakCnt < pakAddrs.length; pakCnt++) {
			byte[] pakBytes = new byte[newSizes[pakCnt]];
			InputStream stream = DxIso.class.getResourceAsStream("/patch/1/"+fileNames[pakCnt]);
			DataInputStream pakStream = new DataInputStream(stream);
			pakStream.readFully(pakBytes);
			pakStream.close();
			raf.seek(pakAddrs[pakCnt]);
			raf.write(pakBytes);
			raf.write(new byte[ogSizes[pakCnt] - newSizes[pakCnt]]);
		}
		//More of a bonus fix, but this fixes the narrator's text to say Paragus instead of Paragas
		raf.seek(10682586);
		raf.write('u');
		writeWatermark();
	}
	private void writeWatermark() throws IOException {
		raf.seek(33651);
		String watermark = PATCH_WATERMARK + "" + DxPatch.VER_NUM + "";
		raf.write(watermark.getBytes());
		raf.close();
	}
}