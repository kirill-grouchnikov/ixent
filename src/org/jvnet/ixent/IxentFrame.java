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
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.LayerUI;
import org.jdesktop.jxlayer.plaf.ext.SpotLightUI;
import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManager;
import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManagerFactory;
import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocator;
import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocatorFactory;
import org.jvnet.ixent.algorithms.graphics.colorreduction.ColorReductor;
import org.jvnet.ixent.algorithms.graphics.colorreduction.ColorReductorFactory;
import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetector;
import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetectorFactory;
import org.jvnet.ixent.algorithms.graphics.engine.FrameworkEngine;
import org.jvnet.ixent.algorithms.graphics.engine.FrameworkEngine.WeightKind;
import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.*;
import org.jvnet.ixent.algorithms.graphics.segmentation.Segmentator;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentatorFactory;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.Coarsener;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.CoarsenerFactory;
import org.jvnet.ixent.algorithms.graphics.tesselation.Tesselator;
import org.jvnet.ixent.algorithms.graphics.tesselation.TesselatorFactory;
import org.jvnet.ixent.algorithms.graphics.turbulence.TurbulenceGenerator;
import org.jvnet.ixent.algorithms.graphics.turbulence.TurbulenceGeneratorFactory;
import org.jvnet.ixent.util.ImageCreator;
import org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel;

public class IxentFrame extends JFrame implements ImageCallback {

	private PanelControls1 panelControls1;
	private PanelControls2 panelControls2;
	private PanelImage panelImage;
	private PanelWeights panelWeights;
	private JButton startButton;

	/**
	 * {@link JXLayer} painter.
	 */
	private SpotLightUI blurPainter;
	private JPanel allPanel;

	public IxentFrame() {
		super("Ixent 1.0");
		this.setIconImage(null);
		this.setLayout(new BorderLayout());

		this.allPanel = new JPanel(new BorderLayout());
		JXLayer<JPanel> layer = new JXLayer<JPanel>(allPanel);

		JPanel mainPanel = new JPanel(new BorderLayout());

		this.panelImage = new PanelImage();
		JPanel controlsPanel = new JPanel(new GridLayout(1, 2, 0, 5));
		this.panelControls1 = new PanelControls1();
		this.panelControls1.setBorder(new EmptyBorder(2, 2, 2, 2));
		this.panelControls2 = new PanelControls2();
		this.panelControls2.setBorder(new EmptyBorder(2, 2, 2, 2));
		controlsPanel.add(this.panelControls1);
		controlsPanel.add(this.panelControls2);
		controlsPanel.setBorder(new TitledBorder("General settings"));
		mainPanel.add(controlsPanel, BorderLayout.SOUTH);
		mainPanel.add(this.panelImage, BorderLayout.CENTER);
		allPanel.add(mainPanel, BorderLayout.CENTER);

		blurPainter = new SpotLightUI(2);
		layer.setUI(blurPainter);
		
		allPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (blurPainter.isShadowEnabled()) {
					blurPainter.reset();
					Rectangle bounds = panelImage.getImagePanel().getBounds();
					Point posMe = allPanel.getLocationOnScreen();
					Point posC = panelImage.getImagePanel()
							.getLocationOnScreen();
					Rectangle rect = new Rectangle(posC.x - posMe.x, posC.y
							- posMe.y, bounds.width, bounds.height);
					blurPainter.addShape(rect);
				}
			}
		});

		JPanel controlsPanel2 = new JPanel(new BorderLayout());
		this.panelWeights = new PanelWeights();
		this.panelWeights.setBorder(new CompoundBorder(new TitledBorder(
				"Weights"), new EmptyBorder(2, 2, 2, 2)));
		controlsPanel2.add(this.panelWeights, BorderLayout.CENTER);
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.startButton = new JButton("Start");
		this.startButton.addActionListener(new StartAction());
		buttonsPanel.add(this.startButton);
		controlsPanel2.add(buttonsPanel, BorderLayout.SOUTH);
		allPanel.add(controlsPanel2, BorderLayout.EAST);
		this.add(layer, BorderLayout.CENTER);
		this.setSize(900, 700);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public class StartAction implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			new Thread() {
				public void run() {
					blurPainter.setShadowEnabled(true);
					blurPainter.reset();
					//blurPainter.setEnabled(true);
					Rectangle bounds = panelImage.getImagePanel().getBounds();
					Point posMe = allPanel.getLocationOnScreen();
					Point posC = panelImage.getImagePanel()
							.getLocationOnScreen();
					Rectangle rect = new Rectangle(posC.x - posMe.x, posC.y
							- posMe.y, bounds.width, bounds.height);
					// bounds.y += (posC.y - posMe.y);
					blurPainter.addShape(rect);
					// IxentFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					EdgeDetector edgeDetector = panelControls1
							.getEdgeDetector();
					EdgeDetector.EdgeFuzzyness edgeDetectorFuzzyness = panelControls1
							.getEdgeDetectionFuzzyness();
					EdgeDetector.EdgeStrength edgeDetectorStrength = panelControls1
							.getEdgeDetectionStrength();
					Segmentator segmentator = panelControls1.getSegmentator();
					Coarsener segmentatorCoarsener = panelControls1
							.getSegmentationCoarsener();
					int segmentatorMaxSegments = panelControls1
							.getSegmentationMaxSegments();
					ColorReductor colorReductor = panelControls1
							.getColorReductor();

					ColorReductorFactory.instance = colorReductor;
					EdgeDetectorFactory.setInstance(edgeDetector);
					SegmentatorFactory.instance = segmentator;
					CoarsenerFactory.instance = segmentatorCoarsener;
					EdgeDetectionLinkInfo edgeDetectionLinkInfo = new EdgeDetectionLinkInfo(
							edgeDetectorFuzzyness, edgeDetectorStrength);
					SegmentationLinkInfo segmentationLinkInfo = new SegmentationLinkInfo(
							segmentatorMaxSegments);

					Tesselator tesselator = panelControls2.getTesselator();
					int tesselationCellRadius = panelControls2
							.getTesselationCellRadius();
					DelaunayManager tesselatorTriangulator = panelControls2
							.getTesselationTriangulator();
					PointLocator tesselatorPointLocator = panelControls2
							.getTesselationPointLocator();
					TurbulenceGenerator turbulenceGenerator = panelControls2
							.getTurbulenceGenerator();
					int displacementMaxDisplacement = panelControls2
							.getDisplacementMaxDisplacement();
					int displacementMainDirection = panelControls2
							.getDisplacementMainDirection();
					int displacementSector = panelControls2
							.getDisplacementSector();

					PointLocatorFactory.instance = tesselatorPointLocator;
					DelaunayManagerFactory.instance = tesselatorTriangulator;
					TesselatorFactory.instance = tesselator;
					TurbulenceGeneratorFactory.instance = turbulenceGenerator;

					DisplacementLinkInfo displacementLinkInfo = new DisplacementLinkInfo(
							displacementMaxDisplacement,
							displacementMainDirection, displacementSector);
					TesselationLinkInfo tesselationLinkInfo = new TesselationLinkInfo(
							tesselationCellRadius);

					Class<?> nprEngineClass = panelWeights.getNprEngineClass();

					BufferedImage selectedImage = panelImage.getCurrentImage();
					FrameworkEngine frameworkEngine = new FrameworkEngine(
							selectedImage);
					frameworkEngine.setParameters(segmentationLinkInfo,
							edgeDetectionLinkInfo, displacementLinkInfo,
							tesselationLinkInfo,
							new NprLinkInfo(nprEngineClass));

					frameworkEngine.setWeight(
							WeightKind.weightSegmentationForStructure,
							panelWeights.getStructureSegmentation());
					frameworkEngine.setWeight(
							WeightKind.weightEdgeDetectionForStructure,
							panelWeights.getStructureEdgeDetection());
					frameworkEngine.setWeight(
							WeightKind.weightEdgeDetectionForNPR, panelWeights
									.getEngineEdgeDetection());
					frameworkEngine.setWeight(
							WeightKind.weightDisplacementForStructure,
							panelWeights.getStructureDisplacement());
					frameworkEngine.setWeight(
							WeightKind.weightStructureGradientForCorrection,
							panelWeights.getCorrectionStructureGradient());
					frameworkEngine.setWeight(
							WeightKind.weightDisplacementForCorrection,
							panelWeights.getCorrectionDisplacement());
					frameworkEngine.setWeight(
							WeightKind.weightSegmentationForNPR, panelWeights
									.getEngineSegmentation());
					frameworkEngine.setWeight(
							WeightKind.weightTesselationForNPR, panelWeights
									.getEngineTesselation());
					frameworkEngine.setWeight(
							WeightKind.weightStructureVicinityForNPR,
							panelWeights.getEngineStructureVicinity());
					frameworkEngine.setWeight(
							WeightKind.weightStructureGradientForNPR,
							panelWeights.getEngineStructureGradient());
					frameworkEngine.setWeight(
							WeightKind.weightDisplacementForNPR, panelWeights
									.getEngineDisplacement());

					ImageCreator.originalImage = selectedImage;
					ImageCreator.ratio = 1.0;
					ImageCreator.imageCallback = IxentFrame.this;

					try {
						frameworkEngine.init();
						frameworkEngine.process();
					} catch (Exception e) {
						e.printStackTrace();
						MessageListDialog mld = MessageListDialog
								.showMessageDialog(IxentFrame.this,
										"Exception caught", e);
						mld.setToExitOnDispose(false);
						mld.setLocationRelativeTo(IxentFrame.this);
						mld.setVisible(true);
					}
					// IxentFrame.this.setCursor(Cursor.getDefaultCursor());
					blurPainter.reset();
					blurPainter.setShadowEnabled(false);
				}
			}.start();
		}
	}

	public void imageUpdated(BufferedImage currImage) {
		this.panelImage.setCurrentImage(currImage);
	}

	public static void main(String args[]) throws Exception {
		JFrame.setDefaultLookAndFeelDecorated(true);
		UIManager.setLookAndFeel(new SubstanceRavenGraphiteLookAndFeel());
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new IxentFrame().setVisible(true);
			}
		});
	}
}
