package gui;
//Bomber D'fiX by ViveTheJoestar
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import cmd.DxIso;
import cmd.DxPatch;

public class Patcher {
	public static void start(DxIso iso, Image icon, JFrame prevFrame, Toolkit tk) {
		//set components
		Color bgColor = new Color(1, 54, 106);
		Color btnColor = new Color(150, 94, 182);
		Color fgColor = new Color(255, 217, 1);
		Color focusColor = new Color(84, 155, 0);
		Font tahoma = new Font("Tahoma", Font.PLAIN, 15);
		Font tahomaBold = new Font("Tahoma", Font.BOLD, 30);
		JCheckBox[] chkbxs = new JCheckBox[DxPatch.NUM_PATCHES];
		for (int patchCnt = 0; patchCnt < chkbxs.length; patchCnt++) {
			final int index = patchCnt;
			chkbxs[patchCnt] = new JCheckBox(DxPatch.getPatchName(patchCnt));
			chkbxs[patchCnt].setBackground(bgColor);
			chkbxs[patchCnt].setForeground(Color.WHITE);
			chkbxs[patchCnt].setFont(tahoma);
			chkbxs[patchCnt].setToolTipText(DxPatch.getPatchTooltip(patchCnt));
			chkbxs[patchCnt].addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					chkbxs[index].setForeground(focusColor);
				}
				@Override
				public void focusLost(FocusEvent e) {
					chkbxs[index].setForeground(Color.WHITE);
				}
			});
		}
		JButton btn = new JButton("Continue");
		JLabel title = new JLabel("Available Patches:");
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel();
		//add listeners
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				int selPatches = 0;
				long start = System.currentTimeMillis();
				for (int patchCnt = 0; patchCnt < chkbxs.length; patchCnt++) {
					if (chkbxs[patchCnt].isSelected()) {
						selPatches++;
						new DxPatch(iso, patchCnt);
					}
				}
				long end = System.currentTimeMillis();
				double time = (end - start) / 1000.0;
				if (selPatches > 0) {
					String msg = "DBZ BT2 DX has been patched in " + time + " seconds!";
					tk.beep();
					JOptionPane.showMessageDialog(dialog, msg, Program.WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE);
				}
			}	
		});
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				prevFrame.setEnabled(true);
				dialog.dispose();
			}
		});
		//set component properties
		btn.setAlignmentX(JButton.CENTER_ALIGNMENT);
		btn.setBackground(btnColor);
		btn.setContentAreaFilled(false);
		btn.setForeground(Color.WHITE);
		btn.setFont(tahomaBold);
		btn.setHorizontalAlignment(JButton.CENTER);
		btn.setOpaque(true);
		panel.setBackground(bgColor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		prevFrame.setEnabled(false);
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		title.setForeground(fgColor);
		title.setFont(tahomaBold);
		title.setHorizontalAlignment(JLabel.HORIZONTAL);
		//add components
		panel.add(Box.createVerticalGlue());
		panel.add(title);
		panel.add(new JLabel(" "));
		for (int patchCnt = 0; patchCnt < chkbxs.length; patchCnt++) {
			Box patchBox = Box.createHorizontalBox();
			patchBox.add(Box.createHorizontalGlue());
			patchBox.add(chkbxs[patchCnt]);
			patchBox.add(Box.createHorizontalGlue());
			panel.add(patchBox);
		}
		panel.add(new JLabel(" "));
		panel.add(btn);
		panel.add(Box.createVerticalGlue());
		dialog.add(panel);		
		//set dialog properties
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setIconImage(icon);
		dialog.setMinimumSize(new Dimension(384, 512));
		dialog.setSize(384, 512);
		dialog.setLocation(prevFrame.getX() + 512, prevFrame.getY());
		dialog.setTitle(Program.WINDOW_TITLE);
		dialog.setVisible(true);
	}
}