package gui;
//Bomber D'fiX by ViveTheJoestar
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import cmd.DxIso;

public class BackupHandler {
	public static void start(DxIso iso, Image icon, JFrame prevFrame, Toolkit tk) {
		final long[] seconds = new long[1];
		String initText = "Copying ISO, please wait... ";
		//set components
		Box labelBox = Box.createHorizontalBox();
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel();
		JLabel label = new JLabel(initText);
		Timer timer = new Timer(1000, e -> {
			seconds[0]++;
			if (seconds[0] >= 3600) {
				long hours = seconds[0] / 3600;
				long minutes = (seconds[0] / 60) % 60;
				long remainder = seconds[0] % 3600;
				label.setText(initText + "(time: " + hours + "h" + minutes + "m" + remainder + "s)");
			}
			else if (seconds[0] >= 60) {
				long minutes = seconds[0] / 60;
				long remainder = seconds[0] % 60;
				label.setText(initText + "(time: " + minutes + "m" + remainder + "s)");
			}
			else label.setText(initText + "(time: " + seconds[0] + "s)");
		});
		timer.start();
		//add window listener
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				String msg = "Aborting the ISO backup process. Is this OK?";
				int option = JOptionPane.showConfirmDialog(dialog, msg, Program.WINDOW_TITLE, JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					prevFrame.setEnabled(true);
					dialog.dispose();
					System.exit(1);
				}
			}
		});
		//set component properties
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setBackground(Program.BG_COLOR);
		label.setForeground(Program.FG_COLOR);
		label.setFont(new Font("Tahoma", Font.BOLD, 30));
		label.setHorizontalAlignment(JLabel.CENTER);
		panel.setBackground(Program.BG_COLOR);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		prevFrame.setEnabled(false);
		//add components
		labelBox.add(Box.createHorizontalGlue());
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		panel.add(Box.createVerticalGlue());
		panel.add(labelBox);
		panel.add(Box.createVerticalGlue());
		dialog.add(panel);
		//set dialog properties
		dialog.setAlwaysOnTop(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setIconImage(icon);
		dialog.setMinimumSize(new Dimension(576, 384));
		dialog.setSize(576, 384);
		dialog.setLocation(prevFrame.getX() + 384, prevFrame.getY());
		dialog.setTitle(Program.WINDOW_TITLE);
		dialog.setVisible(true);
		//set worker
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				double avgSpeed = iso.backup();
				timer.stop();
				tk.beep();
				String msg = "Backup completed! (Average Speed: " + String.format("%.3f", avgSpeed) + " MiB/s)";
				JOptionPane.showMessageDialog(dialog, msg, Program.WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
				dialog.dispose();
				Patcher.start(iso, icon, prevFrame, tk);
				return null;
			}		
		};
		worker.execute();
	}
}