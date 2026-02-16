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
	
	private DxPatchInfo[] info;
	private Path dir;
	private RandomAccessFile raf;
	private String name;
	
	public DxIso(File iso) {
		try {
			info = DxPatch.getAllPatchInfo();
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
	public double backup() throws IOException {
		long start = System.currentTimeMillis();
		Path newDir = dir.getParent().resolve(name.replace(".iso", ".bak"));
		File newIso = newDir.toFile();
		if (newIso.exists()) newIso.delete();
		Files.copy(dir, newDir, StandardCopyOption.COPY_ATTRIBUTES);
		long end = System.currentTimeMillis();
		double time = (end - start) / 1000.0;
		long isoSize = (newIso.length() / 1024) / 1024;
		return isoSize / time;
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
	public void close() throws IOException {
		raf.close();
	}
	
	void disableDbzpChanges() throws Exception {
		int pos = 3324236;
		raf.seek(pos);
		raf.write(-3);
		raf.seek(pos += 20);
		raf.write(-2);
		raf.seek(pos += 4);
		raf.write(-1);
		//By mistake, Deel set Goku (Mid)'s costume to 4 rather than 5, so a new Main Menu PAK is needed
		int i = DxPatch.getNumPatchFiles(info, 1) + DxPatch.getNumPatchFiles(info, 2) + DxPatch.getNumPatchFiles(info, 3);
		writePatchFromPak(info[i].getName(), info[i].getPnum(), info[i].getAddr(), info[i].getOldSize(), info[i].getNewSize());
	}
	void disableGreatApeForm() throws IOException {
		/* As of v1.04, Turles has already had his transformation disabled, so this code is useless
		long[] commonParamAddrs = {3034040384L, 3034939072L, 3035522048L, 3036105728L, 3036689408L, 3037273088L};
		for (long addr: commonParamAddrs) {
			raf.seek(addr);
			raf.writeByte(255); //disable Turles' transformation (now that it points to SS1 Mid Goku)
			raf.seek(addr + 4);
			raf.writeByte(1); //set transformation cost to default (1)
		} */
        long[] sklLstAddrs = { 2410878988L, 2411738572L, 2412604748L, 2413545884L, 2414471404L, 2415355596L };
		byte[] sklLstFooter = { 0x40, 0x00, 0x0A, 0x00 };
		for (long addr: sklLstAddrs) {
			raf.seek(addr);
			//Remove Transformation tab from Raditz's costumes by adding a footer and some padding after it
			raf.write(sklLstFooter);
			raf.write(new byte[192]);
		}
	}
	void enableDragonFistAgainstGiants() throws Exception {
		byte[] dragonFistParams = {-118, 17}; //likely flags (sets of boolean parameters)
		long[] blastParamAddrs = { 2909059877L, 2910006757L, 2910907125L };
		for (long addr: blastParamAddrs) {
			raf.seek(addr);
			raf.write(dragonFistParams);
		}
	}
	void fixBojackUnboundSubs() throws IOException {
		raf.seek(9155905);
		raf.write(59); //change offset to make the GSC param point to 5 instead of 7
		raf.seek(9156400);
		raf.write(7); //replace duplicate 5 in GSDT with 7 (the original value)
	}
	void fixCharaRoster() throws Exception {
		int numFilesOfPrevPatch = DxPatch.getNumPatchFiles(info, 1);
		int numFilesOfCurrPatch = DxPatch.getNumPatchFiles(info, 2);
		for (int i = numFilesOfPrevPatch; i < numFilesOfCurrPatch + numFilesOfPrevPatch; i++)
			writePatchFromPak(info[i].getName(), info[i].getPnum(), info[i].getAddr(), info[i].getOldSize(), info[i].getNewSize());
	}
	void fixItemNames() throws Exception {
		String verNum = getPatchVersion();
		//This patch conflicts with the patch from v1.2, so this check had to be added
		if (Float.parseFloat(verNum) < 1.2) {
			for (int i = 0; i < DxPatch.getNumPatchFiles(info, 1); i++)
				writePatchFromPak(info[i].getName(), info[i].getPnum(), info[i].getAddr(), info[i].getOldSize(), info[i].getNewSize());
		}
		//More of a bonus fix, but this fixes the narrator's text to say Paragus instead of Paragas
		raf.seek(10682586);
		raf.write('u');
	}
	void fixLordSlugSounds() throws Exception {
		int start = DxPatch.getNumPatchFiles(info, 1) + DxPatch.getNumPatchFiles(info, 2);
		int end = start + 2;
		for (int i = start; i < end; i++)
			writePatchFromPak(info[i].getName(), info[i].getPnum(), info[i].getAddr(), info[i].getOldSize(), info[i].getNewSize());
	}
	void fixPiccoloCostume() throws IOException {
		raf.seek(9313732);
		raf.write(8); //Move costume ID by 4 slots ("04 AD" -> "08 AD")
	}
	void fixSpecialQuotes() throws IOException {
		long[] krillinAddrs = { 2385862232L, 2386705784L, 2387616056L, 2388515432L, 2389311960L, 2390190456L };
		long[] trunksAddrs = { 
			2811903576L, 2812789432L, 2813719512L, 2814577704L, 2815523144L, 2816430920L,
			2818707656L, 2819607432L, 2820533768L, 2821349992L, 2822262664L, 2823158920L
		};
		long[] cellAddrs = { 2590651480L, 2591476824L, 2595587160L, 2596407496L };
		long[] synAddrs = { 3011043024L, 3011952336L, 3016700112L, 3017621712L }; 
		byte[] krillinSpeakerParams = { 0x59, 0, 1, 0 };
		byte[] trunksSpeakerParams = { 0x5B, 0, 0, 0};
		byte[] cellSpeakerParams = { 8, 0, 1, 0 }; 
		int totalNumAddrs = krillinAddrs.length + trunksAddrs.length + cellAddrs.length + synAddrs.length;
		int startIndexSyn = krillinAddrs.length + trunksAddrs.length + cellAddrs.length;
		int startIndexCell = krillinAddrs.length + trunksAddrs.length;
		for (int i = 0; i < totalNumAddrs; i++) {
			if (i >= startIndexSyn) {
				raf.seek(synAddrs[i - startIndexSyn]);
				raf.write(new byte[6]);
			}
			else if (i >= startIndexCell) {
				raf.seek(cellAddrs[i - startIndexCell]);
				raf.write(cellSpeakerParams);
				raf.seek(cellAddrs[i - startIndexCell] + 8);
				raf.write(cellSpeakerParams);
			}
			else if (i >= krillinAddrs.length) {
				raf.seek(trunksAddrs[i - krillinAddrs.length] + 8);				
				raf.write(trunksSpeakerParams);
			}
			else {
				raf.seek(krillinAddrs[i]);
				raf.write(krillinSpeakerParams);
			}
		}
	}
	void rebalanceA18() throws IOException {
		long[] blastParamAddrs = { 2562378636L, 2563244204L, 2564158732L, 2564982988L, 2565958412L, 2566812012L };
		for (long addr: blastParamAddrs) {
			raf.seek(addr);
			raf.write(2);
		}
	}
	void rebalanceWildSense() throws IOException {
		long[] sklLstAddrs = {
			//Goku (End) - Super Saiyan
			2332219296L, 2333163456L, 2334091168L, 2335019792L, 2335951360L, 2336905920L,
			//Ultimate Gohan
			2638444576L, 2639358736L, 2640166144L, 2640826528L, 2641668640L, /*2641664736L*/ 2642531872L,
			//Gogeta - Super Saiyan 4
			2787637200L, 2788566992L,
			//Omega Shenron
			3016709312L, 3017630912L
		};
		long[] blastParamAddrs = {
			//Ultimate Gohan
			2638436200L, 2639355992L, 2640163400L, 2640823784L, 2641637416L, 2642529128L,
			//Gogeta - Super Saiyan 4
			2787613720L, 2788543512L,
			//Omega Shenron
			3016701448L, 3017623048L
		};
		for (long addr: sklLstAddrs) {
			raf.seek(addr);
			raf.write('2');
		}
		for (long addr: blastParamAddrs) {
			raf.seek(addr);
			raf.write(2);
		}
	}
	void swapSpeechPortraits() throws IOException {
		int pos = 9432617;
		raf.seek(pos);
		raf.write(121);
		raf.seek(pos += 11);
		raf.write(120);
	}
	void writeWatermark() throws IOException {
		raf.seek(33651);
		String watermark = PATCH_WATERMARK + "" + DxPatch.VER_NUM + "";
		raf.write(watermark.getBytes());
		raf.write(0); //when going from 3.41 to 3.7, the 1 still remains unless this is added
		raf.close();
	}
	
	private void writePatchFromPak(String fileName, int patchIdx, long pakAddr, int oldSize, int newSize) throws Exception {
		byte[] pakBytes = new byte[newSize];
		InputStream stream = DxIso.class.getResourceAsStream("/patch/" + patchIdx + "/" + fileName);
		DataInputStream pakStream = new DataInputStream(stream);
		pakStream.readFully(pakBytes);
		pakStream.close();
		raf.seek(pakAddr);
		raf.write(pakBytes);
		int paddingSize = oldSize - newSize;
		if (paddingSize > 0) raf.write(new byte[paddingSize]);
	}
}