package xdman.ui.components;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import xdman.Config;
import xdman.DownloadQueue;
import xdman.QueueManager;
import xdman.XDMApp;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.ui.res.ColorResource;
import xdman.ui.res.FontResource;
import xdman.ui.res.ImageResource;
import xdman.ui.res.StringResource;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import static xdman.util.XDMUtils.getScaledInt;

public class BatchClipboardWnd extends JFrame implements ActionListener {

	private static final long serialVersionUID = -8209791156319603983L;
	JList<ClipboardBatchItem> list;
	DefaultListModel<ClipboardBatchItem> model;
	JTextField txtFile;
	DefaultComboBoxModel<DownloadQueue> queueModel;
	JComboBox<DownloadQueue> cmbQueues;
	JCheckBox chkStartQueue;

	public static List<String> getUrls() {
		List<String> urls = new ArrayList<>();
		String text = XDMUtils.getClipBoardText();
		if (!StringUtils.isNullOrEmptyOrBlank(text)) {
			System.out.println(text);
			String[] arr = text.split("\n");
			for (int i = 0; i < arr.length; i++) {
				String url = arr[i];
				try {
					new URL(url);
					urls.add(url);
				} catch (Exception e) {

				}
			}
		}
		return urls;
	}

	public BatchClipboardWnd(List<String> urls) {
		model = new DefaultListModel<>();
		list = new JList<>(model);
		initUI();
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			try {
				String file = XDMUtils.getFileName(url);
				ClipboardBatchItem item = new ClipboardBatchItem();
				item.file = file;
				item.selected = true;
				item.url = url;
				model.addElement(item);
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			String name = ((JComponent) e.getSource()).getName();
			if (name.startsWith("CLOSE")) {
				dispose();
			} else if (name.startsWith("BROWSE_FOLDER")) {
				JFileChooser jfc = XDMFileChooser.getFileChooser(JFileChooser.DIRECTORIES_ONLY,
						new File(Config.getInstance().getDownloadFolder()));
				if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					txtFile.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			} else if (name.equals("DOWNLOAD")) {
				DownloadQueue queue = (DownloadQueue) cmbQueues.getSelectedItem();
				if (queue != null) {
					createDownload(queue);
					if (chkStartQueue.isSelected()) {
						queue.start();
					}
				}
				dispose();
			}
		}

	}

	private void createDownload(DownloadQueue q) {
		String folder = txtFile.getText();
		for (int i = 0; i < model.size(); i++) {
			ClipboardBatchItem item = model.getElementAt(i);
			if (item.selected) {
				String file = item.file;
				String url = item.url;
				HttpMetadata metadata = new HttpMetadata();
				metadata.setUrl(url);
				folder = txtFile.getText();
				XDMApp.getInstance().createDownload(file, folder, metadata, false, q == null ? "" : q.getQueueId(), 0,
						0);
			}
		}
	}

	private void initUI() {
		setUndecorated(true);

		try {
			if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
				if (!Config.getInstance().isNoTransparency())
					setOpacity(0.85f);
			}
		} catch (Exception e) {
			Logger.log(e);
		}

		setTitle(StringResource.get("MENU_BATCH_DOWNLOAD"));
		setIconImage(ImageResource.get("icon.png").getImage());
		setSize(getScaledInt(500), getScaledInt(420));
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		getContentPane().setBackground(ColorResource.getDarkestBgColor());

		JPanel titlePanel = new TitlePanel(null, this);
		titlePanel.setOpaque(false);
		titlePanel.setBounds(0, 0, getWidth(), getScaledInt(50));

		JButton closeBtn = new CustomButton();
		closeBtn.setBounds(getWidth() - getScaledInt(35), getScaledInt(5), getScaledInt(30), getScaledInt(30));
		closeBtn.setBackground(ColorResource.getDarkestBgColor());
		closeBtn.setBorderPainted(false);
		closeBtn.setFocusPainted(false);
		closeBtn.setName("CLOSE");

		closeBtn.setIcon(ImageResource.get("title_close.png"));
		closeBtn.addActionListener(this);
		titlePanel.add(closeBtn);

		JLabel titleLbl = new JLabel(StringResource.get("MENU_BATCH_DOWNLOAD"));
		titleLbl.setFont(FontResource.getBiggerFont());
		titleLbl.setForeground(ColorResource.getSelectionColor());
		titleLbl.setBounds(getScaledInt(25), getScaledInt(15), getScaledInt(200), getScaledInt(30));
		titlePanel.add(titleLbl);

		JLabel lineLbl = new JLabel();
		lineLbl.setBackground(ColorResource.getSelectionColor());
		lineLbl.setBounds(0, getScaledInt(55), getWidth(), 1);
		lineLbl.setOpaque(true);
		add(lineLbl);

		add(titlePanel);

		int y = getScaledInt(55);
		int h = getScaledInt(420) - getScaledInt(100) - getScaledInt(70);

		list.setBorder(null);
		list.setOpaque(false);
		list.setCellRenderer(new SimpleCheckboxRender());

		JScrollPane jsp = new JScrollPane(list);
		jsp.setBounds(0, y, getWidth(), h);
		jsp.getViewport().setOpaque(false);
		jsp.setBorder(null);
		jsp.setOpaque(false);
		DarkScrollBar scrollBar = new DarkScrollBar(JScrollBar.VERTICAL);
		jsp.setVerticalScrollBar(scrollBar);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(jsp);
		y += h;

		JLabel lineLbl2 = new JLabel();
		lineLbl2.setBackground(ColorResource.getDarkBgColor());
		lineLbl2.setBounds(0, y, getWidth(), 1);
		lineLbl2.setOpaque(true);
		add(lineLbl2);

		y += getScaledInt(10);

		JLabel lblFile = new JLabel(StringResource.get("LBL_SAVE_IN"), JLabel.RIGHT);
		lblFile.setFont(FontResource.getNormalFont());
		lblFile.setForeground(Color.WHITE);
		lblFile.setBounds(0, y, getScaledInt(80), getScaledInt(30));
		add(lblFile);

		txtFile = new JTextField();
		txtFile.setBorder(new LineBorder(ColorResource.getSelectionColor(), 1));
		txtFile.setBackground(ColorResource.getDarkestBgColor());
		txtFile.setForeground(Color.WHITE);
		txtFile.setBounds(getScaledInt(90), y + getScaledInt(5), getScaledInt(305) - getScaledInt(15),
				getScaledInt(20));
		txtFile.setCaretColor(ColorResource.getSelectionColor());
		txtFile.setText(Config.getInstance().getDownloadFolder());
		add(txtFile);

		JButton browse = new CustomButton("...");
		browse.setName("BROWSE_FOLDER");
		browse.setMargin(new Insets(0, 0, 0, 0));
		browse.setBounds(getScaledInt(410) - getScaledInt(20), y + getScaledInt(5), getScaledInt(40), getScaledInt(20));
		browse.setFocusPainted(false);
		browse.setBackground(ColorResource.getDarkestBgColor());
		browse.setBorder(new LineBorder(ColorResource.getSelectionColor(), 1));
		browse.setForeground(Color.WHITE);
		browse.addActionListener(this);
		browse.setFont(FontResource.getItemFont());
		add(browse);

		y += getScaledInt(30);

		JLabel lblQueue = new JLabel(StringResource.get("LBL_QUEUE_USE"), JLabel.RIGHT);
		lblQueue.setFont(FontResource.getNormalFont());
		lblQueue.setForeground(Color.WHITE);
		lblQueue.setBounds(0, y, getScaledInt(80), getScaledInt(30));
		add(lblQueue);

		queueModel = new DefaultComboBoxModel<DownloadQueue>();
		ArrayList<DownloadQueue> qlist = QueueManager.getInstance().getQueueList();
		for (int i = 0; i < qlist.size(); i++) {
			queueModel.addElement(qlist.get(i));
		}
		cmbQueues = new JComboBox<>(queueModel);
		cmbQueues.setRenderer(new QueueListRenderer());
		cmbQueues.setBounds(getScaledInt(90), y, getScaledInt(305) - getScaledInt(15) + getScaledInt(50),
				getScaledInt(20));
		add(cmbQueues);

		y += getScaledInt(35);

		chkStartQueue = new JCheckBox(StringResource.get("LBL_START_QUEUE_PROCESSING"));
		chkStartQueue.setBackground(ColorResource.getDarkestBgColor());
		chkStartQueue.setName("START_QUEUE");
		chkStartQueue.setForeground(Color.WHITE);
		chkStartQueue.setFocusPainted(false);

		chkStartQueue.setBounds(getScaledInt(15), y, getScaledInt(200), getScaledInt(20));
		chkStartQueue.setIcon(ImageResource.get("unchecked.png"));
		chkStartQueue.setSelectedIcon(ImageResource.get("checked.png"));
		chkStartQueue.addActionListener(this);

		add(chkStartQueue);

		h = getScaledInt(25);
		JButton btnDwn = createButton("MB_OK");
		btnDwn.setBounds(getWidth() - getScaledInt(15) - getScaledInt(100), y, getScaledInt(100), h);
		btnDwn.setName("DOWNLOAD");
		add(btnDwn);

		JButton btnCn = createButton("ND_CANCEL");
		btnCn.setBounds(getWidth() - getScaledInt(15) - getScaledInt(100) - getScaledInt(110), y, getScaledInt(100), h);
		btnCn.setName("CLOSE");
		add(btnCn);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				int index = list.locationToIndex(event.getPoint());// Get index of item
																	// clicked
				ClipboardBatchItem cItem = model.getElementAt(index);
				cItem.selected = !cItem.selected; // Toggle selected state
				list.repaint(list.getCellBounds(index, index));// Repaint cell
			}
		});

	}

	private JButton createButton(String name) {
		JButton btn = new CustomButton(StringResource.get(name));
		btn.setBackground(ColorResource.getDarkBtnColor());
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setForeground(Color.WHITE);
		btn.setFont(FontResource.getNormalFont());
		btn.addActionListener(this);
		return btn;
	}

}

class ClipboardBatchItem {
	String url;
	String file;
	boolean selected;

	@Override
	public String toString() {
		return file;
	}
}