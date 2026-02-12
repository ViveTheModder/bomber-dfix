package gui;
//Bomber D'fiX by ViveTheJoestar
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.DxIso;
import cmd.DxPatch;

public class Program {
	static final String WINDOW_TITLE = "Bomber D'fiX v" + DxPatch.VER_NUM;
	
	static Font getComicSansFont() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String fontName = "Tahoma";
		String[] fonts = ge.getAvailableFontFamilyNames();
		int result = Arrays.binarySearch(fonts, "Comic Sans MS");
		if (result >= 0) fontName = "Comic Sans MS";
		return new Font(fontName, Font.BOLD, 30);
	}
	private static DxIso getIsoFromChooser(File[] lastDir, Toolkit tk) throws IOException {
		DxIso iso = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("DBZ BT2 DX Disc Image (*.ISO)", "iso");
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(filter);
		chooser.setDialogTitle("Open DBZ BT2 DX ISO...");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);
		if (lastDir[0] != null) chooser.setCurrentDirectory(lastDir[0]);
		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File isoDir = chooser.getSelectedFile();
			lastDir[0] = isoDir;
			if (!isoDir.renameTo(isoDir)) {
				String err = "The selected ISO is being used by another process. Close the process and try again!";
				errorBeep(tk);
				JOptionPane.showMessageDialog(chooser, err, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
				return iso;
			}
			iso = new DxIso(isoDir);
			if (!iso.isValid()) {
				String err = "The selected ISO is NOT a valid DBZ BT2 DX ISO!";
				errorBeep(tk);
				JOptionPane.showMessageDialog(chooser, err, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return iso;
	}
	private static void checkDxIso(DxIso iso, ImageIcon[] icons, Image img, JLabel deel, JLabel quote, JFrame f, JPanel p, Toolkit tk)
	throws IOException {
		if (iso != null) {
			if (iso.isValid()) {
				deel.setIcon(icons[1]);
				if (iso.isPatched()) {
					quote.setForeground(Color.MAGENTA);
					quote.setText("is patched (ver. " + iso.getPatchVersion() + ")");
				}
				else {
					quote.setForeground(Color.GREEN);
					quote.setText("is gud");
				}
				quote.setToolTipText(iso.toString());
				String msg = "Should a backup/copy of the ISO be made before applying any patches?";
				int option = JOptionPane.showConfirmDialog(p, msg, WINDOW_TITLE, JOptionPane.YES_NO_CANCEL_OPTION);
				if (option == JOptionPane.YES_OPTION) BackupHandler.start(iso, img, f, tk);
				else if (option == JOptionPane.NO_OPTION) Patcher.start(iso, img, f, tk);
			}
		}
		else {
			deel.setIcon(icons[0]);
			quote.setForeground(Color.RED);
			quote.setText("no gud");
		}
	}
	private static void errorBeep(Toolkit toolkit) {
		Runnable runWinErrorSnd = (Runnable) toolkit.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static void start() {
		final DxIso[] iso = new DxIso[1];
		final File[] lastDir = new File[1];
		String[] imgDirs = { "img/angry.png", "img/blush.png", "img/flush.png" };
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		//set components
		Box clockBox = Box.createHorizontalBox();
		Box quoteBox = Box.createHorizontalBox();
		Image dx = toolkit.getImage(ClassLoader.getSystemResource("img/dx.png"));
		Image dxSmall = dx.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		ImageIcon[] clockIcons = new ImageIcon[imgDirs.length];
		for (int imgCnt = 0; imgCnt < clockIcons.length; imgCnt++) {
			Image img = toolkit.getImage(ClassLoader.getSystemResource(imgDirs[imgCnt]));
			img = img.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
			clockIcons[imgCnt] = new ImageIcon(img);
		}
		JLabel deel = new JLabel(clockIcons[2]);
		JLabel deelQuote = new JLabel("pls click me (or drag & drop) 2 fix iso");
		JFrame frame = new JFrame(WINDOW_TITLE);
		JPanel panel = new JPanel();
		//add drag and drop
		deel.setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferHandler.TransferSupport ts) {
				if (!ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                    return false;
				return true;
			}
			@Override
			@SuppressWarnings("unchecked")
			public boolean importData(TransferHandler.TransferSupport ts) {
		        if (!canImport(ts)) return false;
		        try {
		        	List<File> files = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		        	File firstFile = files.get(0);
			        if (firstFile.getName().toLowerCase().endsWith(".iso")) {
			        	if (firstFile.renameTo(firstFile)) {
			        		iso[0] = new DxIso(firstFile);
			        		if (iso[0].isValid()) checkDxIso(iso[0], clockIcons, dxSmall, deel, deelQuote, frame, panel, toolkit);
			        		else {
								String err = "The selected ISO is NOT a valid DBZ BT2 DX ISO!";
								errorBeep(toolkit);
								JOptionPane.showMessageDialog(frame, err, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
					        }
			        	}
			        	else {
							String err = "The selected ISO is being used by another process. Close the process and try again!";
							errorBeep(toolkit);
							JOptionPane.showMessageDialog(frame, err, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
			        	}
			        }
			        else {
						String err = "The selected ISO is NOT a DBZ BT2 DX ISO!";
						errorBeep(toolkit);
						JOptionPane.showMessageDialog(frame, err, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
			        }
		        } 
		        catch (Exception ex) {
		            return false;
		        }
		        return true;
			}
		});
		//add mouse listener
		deel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent me) {
				try {
					iso[0] = getIsoFromChooser(lastDir, toolkit);
					checkDxIso(iso[0], clockIcons, dxSmall, deel, deelQuote, frame, panel, toolkit);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		//set component properties
		deel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		deelQuote.setFont(getComicSansFont());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//add components
		clockBox.add(Box.createHorizontalGlue());
		clockBox.add(deel);
		clockBox.add(Box.createHorizontalGlue());
		quoteBox.add(Box.createHorizontalGlue());
		quoteBox.add(deelQuote);
		quoteBox.add(Box.createHorizontalGlue());
		panel.add(Box.createVerticalGlue());
		panel.add(quoteBox);
		panel.add(clockBox);
		panel.add(Box.createVerticalGlue());
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(dxSmall);
		frame.setSize(600, 512);
		frame.setMinimumSize(new Dimension(600, 512));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static void launch() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}