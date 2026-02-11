package cmd;
//Bomber D'fiX by ViveTheJoestar
public class DxPatchInfo {
	private int pnum;
	private int addr;
	private int oldSize;
	private int newSize;
	private String name;
	
	/* FYI, I made this object in the hopes of using it for its own comparator
	   until I realized that the files in patch.csv are already sorted... */
	public DxPatchInfo(int num, int addr, int size1, int size2, String name) {
		pnum = num;
		this.addr = addr;
		oldSize = size1;
		newSize = size2;
		this.name = name;
	}
	public String toString() {
		return pnum + ", " + name + ", " + addr + ", " + oldSize + ", " + newSize;
	}
	
	//I regret nothing... except getters.
	public int getAddr() {
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