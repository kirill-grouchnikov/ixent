/*
 * Copyright (c) 2004-2007 Ixent Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Ixent Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.jvnet.ixent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import org.jvnet.flamingo.bcb.*;
import org.jvnet.flamingo.bcb.core.BreadcrumbFileSelector;
import org.jvnet.substance.api.renderers.SubstanceDefaultListCellRenderer;
import org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel;
import org.jvnet.substance.utils.SubstanceCoreUtilities;

public class PanelImage extends JPanel {
	private BreadcrumbFileSelector fileSelector;

	private JList fileList;

	private BufferedImage currImage;

	private JPanel imagePanel;

	public class FileListRenderer extends SubstanceDefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			File file = (File) value;
			FileSystemView fileSystemView = FileSystemView.getFileSystemView();
			Icon origIcon = fileSystemView.getSystemIcon(file);
			Icon themedIcon = SubstanceCoreUtilities.getThemedIcon(list,
					origIcon);
			result.setIcon(themedIcon);
			result.setText(fileSystemView.getSystemDisplayName(file));
			return result;
		}
	}

	public static class FileListModel extends AbstractListModel {
		private ArrayList<File> files = new ArrayList<File>();

		public void add(File file) {
			files.add(file);
		}

		public void sort() {
			Collections.sort(files, new Comparator<File>() {
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && (!o2.isDirectory()))
						return -1;
					if (o2.isDirectory() && (!o1.isDirectory()))
						return 1;
					return o1.getName().toLowerCase().compareTo(
							o2.getName().toLowerCase());
				}
			});
		}

		public Object getElementAt(int index) {
			return files.get(index);
		}

		public int getSize() {
			return files.size();
		}
	}

	public PanelImage() {
		this.setLayout(new BorderLayout());
		this.fileSelector = new BreadcrumbFileSelector(false);
		this.fileSelector.getModel().addPathListener(
				new BreadcrumbPathListener() {
					@Override
					public void breadcrumbPathEvent(BreadcrumbPathEvent event) {
						List<BreadcrumbItem<File>> newPath = fileSelector
								.getModel().getItems();
						if (newPath.size() > 0) {
							BreadcrumbItem<File> lastElem = newPath.get(newPath
									.size() - 1);
							File lastDir = lastElem.getData();
							FileListModel model = new FileListModel();
							if (lastDir.canRead()) {
								for (File child : lastDir.listFiles()) {
									if (child.isHidden())
										continue;
									model.add(child);
								}
							}
							model.sort();
							fileList.setModel(model);
						}
						return;
					}
				});
		this.add(this.fileSelector, BorderLayout.NORTH);

		JSplitPane split = new JSplitPane();
		this.fileList = new JList();
		split.setLeftComponent(new JScrollPane(this.fileList));
		this.fileList.setCellRenderer(new FileListRenderer());
		this.fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.fileList.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						int selectedIndex = fileList.getSelectedIndex();
						if (selectedIndex < 0)
							return;
						final File selected = (File) fileList.getModel()
								.getElementAt(selectedIndex);
						if (selected != null) {
							if (selected.isDirectory()) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										BreadcrumbItem<File> bi = new BreadcrumbItem<File>(
												FileSystemView
														.getFileSystemView()
														.getSystemDisplayName(
																selected),
												selected);
										fileSelector.getModel().addLast(bi);
									}
								});
							} else {
								try {
									currImage = ImageIO.read(selected);
									imagePanel.repaint();
								} catch (Exception exc) {
								}
							}
						}
					}
				});

		this.imagePanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (currImage != null) {
					g.drawImage(currImage, 0, 0, null);
				}
			}
		};
		split.setRightComponent(this.imagePanel);
		split.setDividerLocation(100);

		this.add(split, BorderLayout.CENTER);
	}

	public BufferedImage getCurrentImage() {
		return this.currImage;
	}

	public void setCurrentImage(BufferedImage newImage) {
		this.currImage = newImage;
		this.repaint();
	}

	public JPanel getImagePanel() {
		return imagePanel;
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(new SubstanceRavenGraphiteLookAndFeel());
		JFrame fr = new JFrame();
		fr.setLayout(new BorderLayout());
		fr.add(new PanelImage());
		fr.setSize(400, 400);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}

}
