package cmd;
//Bomber D'fiX by ViveTheJoestar
public class DxPatchInfo {
	private int pnum;
	private long addr;
	private int oldSize;
	private int newSize;
	private String name;
	
	/* FYI, I made this object in the hopes of using it for its own comparator
	   until I realized that the files in patch.csv are already sorted... */
	public DxPatchInfo(int num, long patchAddr, int size1, int size2, String name) {
		pnum = num;
		this.addr = patchAddr;
		oldSize = size1;
		newSize = size2;
		this.name = name;
	}
	public String toString() {
		return pnum + ", " + name + ", " + addr + ", " + oldSize + ", " + newSize;
	}
	
	//I regret nothing... except getters.
	public long getAddr() {
		return addr;
	}
	public String getName() {
		return name;
	}
	public int getNewSize() {
		return newSize;
	}
	public int getOldSize() {
		return oldSize;
	}
	public int getPnum() {
		return pnum;
	}
}