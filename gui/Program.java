package gui;
//Bomber D'fiX by ViveTheJoestar
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.DxIso;
import cmd.DxPatch;

public class Program {
	static final Color BG_COLOR = new Color(130, 100, 190), FG_COLOR = new Color(50, 30, 110);
	static final String HTML_START = "<html><div style='text-align: center;'>";
	static final String HTML_END = "</html></div>";
	static final String WINDOW_TITLE = "Bomber D'fiX v" + DxPatch.VER_NUM;
	
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
	//solution taken from Stephen C's answer in stackoverflow (https://stackoverflow.com/a/1402762)
	private static boolean isInternetAvailable(URI uri) {
		try {
			URL url = uri.toURL();
			URLConnection urlc = url.openConnection();
			urlc.connect();
			urlc.getInputStream().close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	private static void checkDxIso(DxIso iso, ImageIcon[] icons, Image img, JLabel clock, JLabel quote, JFrame f, JPanel p, Toolkit tk)
	throws IOException {
		if (iso != null) {
			if (iso.isValid()) {
				clock.setIcon(icons[1]);
				if (iso.isPatched()) {
					quote.setForeground(Color.MAGENTA);
					quote.setText("This ISO is already patched (ver. " + iso.getPatchVersion() + ")!");
				}
				else {
					quote.setForeground(Color.GREEN);
					quote.setText("This ISO is a valid DBZ BT2 DX copy.");
				}
				quote.setToolTipText(iso.toString());
				String msg = "Should a backup/copy of the ISO be made before applying any patches?";
				int option = JOptionPane.showConfirmDialog(p, msg, WINDOW_TITLE, JOptionPane.YES_NO_CANCEL_OPTION);
				if (option == JOptionPane.YES_OPTION) BackupHandler.start(iso, img, f, tk);
				else if (option == JOptionPane.NO_OPTION) Patcher.start(iso, img, f, tk);
			}
		}
		else {
			clock.setIcon(icons[0]);
			quote.setForeground(Color.RED);
			quote.setText("This ISO is invalid (either outdated or entirely different)!");
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
		Font boldFont = new Font("Tahoma", Font.BOLD, 18);
		Image dx = toolkit.getImage(ClassLoader.getSystemResource("img/dx.png"));
		Image dxSmall = dx.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
		ImageIcon[] clockIcons = new ImageIcon[imgDirs.length];
		for (int imgCnt = 0; imgCnt < clockIcons.length; imgCnt++) {
			Image img = toolkit.getImage(ClassLoader.getSystemResource(imgDirs[imgCnt]));
			img = img.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
			clockIcons[imgCnt] = new ImageIcon(img);
		}
		JLabel clock = new JLabel(clockIcons[2]);
		JLabel tip = new JLabel("Click the emoji below (or drag & drop to it) to enter the ISO.");
		JMenuBar navBar = new JMenuBar();
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem update = new JMenuItem("Check for Updates");
		JFrame frame = new JFrame(WINDOW_TITLE);
		JPanel panel = new JPanel();
		//add drag and drop
		clock.setTransferHandler(new TransferHandler() {
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
			        		if (iso[0].isValid()) checkDxIso(iso[0], clockIcons, dxSmall, clock, tip, frame, panel, toolkit);
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
		//add listeners
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Box lblBox = Box.createHorizontalBox();
				String[] text = { "Made by ", "ViveTheJoestar", ", for ", "Deelseton", " and his project ", "DBZ BT2 DX" };
				String[] links = { 
					"https://github.com/ViveTheModder", 
					"https://www.youtube.com/@Deelseton",
					"https://www.youtube.com/watch?v=95p83apY2LI"
				};
				JLabel[] lbls = new JLabel[6];
				for (int lblCnt = 0; lblCnt < lbls.length; lblCnt++) {
					final int index = lblCnt / 2;
					lbls[lblCnt] = new JLabel(text[lblCnt]);
					if (lblCnt % 2 != 0) {
						lbls[lblCnt].setText(HTML_START + "<a href=''>" + text[lblCnt] + "</a>" + HTML_END);
						lbls[lblCnt].addMouseListener(new MouseAdapter() {
							@Override
							public void mousePressed(MouseEvent me) {
								try {
									URI uri = new URI(links[index]);
									if (isInternetAvailable(uri)) Desktop.getDesktop().browse(uri);
									else {
										errorBeep(toolkit);
										String msg = "Cannot open link. Check your Internet connection!";
										JOptionPane.showMessageDialog(frame, msg, WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
									}
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
					lbls[lblCnt].setFont(boldFont);
					lbls[lblCnt].setHorizontalAlignment(JLabel.LEFT);
					lblBox.add(lbls[lblCnt]);
				}
				JOptionPane.showMessageDialog(frame, lblBox, WINDOW_TITLE, JOptionPane.PLAIN_MESSAGE, new ImageIcon(dxSmall));
			}
		});
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					URI link = new URI("https://github.com/ViveTheModder/bomber-dfix/releases");
					if (isInternetAvailable(link)) Desktop.getDesktop().browse(link);
					else {
						errorBeep(toolkit);
						String msg = "Cannot update. Check your Internet connection!";
						JOptionPane.showMessageDialog(frame, msg, WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		clock.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent me) {
				try {
					iso[0] = getIsoFromChooser(lastDir, toolkit);
					checkDxIso(iso[0], clockIcons, dxSmall, clock, tip, frame, panel, toolkit);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		//set component properties
		clock.setCursor(new Cursor(Cursor.HAND_CURSOR));
		tip.setBackground(BG_COLOR);
		tip.setFont(boldFont);
		tip.setForeground(FG_COLOR);
		panel.setBackground(BG_COLOR);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//add components
		clockBox.add(Box.createHorizontalGlue());
		clockBox.add(clock);
		clockBox.add(Box.createHorizontalGlue());
		help.add(update);
		help.add(about);
		navBar.add(help);
		quoteBox.add(Box.createHorizontalGlue());
		quoteBox.add(tip);
		quoteBox.add(Box.createHorizontalGlue());
		panel.add(Box.createVerticalGlue());
		panel.add(quoteBox);
		panel.add(clockBox);
		panel.add(Box.createVerticalGlue());
		frame.add(panel);
		//set frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(dxSmall);
		frame.setJMenuBar(navBar);
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