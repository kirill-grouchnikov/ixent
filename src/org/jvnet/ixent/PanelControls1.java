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

import java.awt.BorderLayout;

import javax.swing.*;

import org.jvnet.ixent.algorithms.graphics.colorreduction.ColorReductor;
import org.jvnet.ixent.algorithms.graphics.colorreduction.MedianCutColorReductor;
import org.jvnet.ixent.algorithms.graphics.edgedetection.CannyEdgeDetector;
import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetector;
import org.jvnet.ixent.algorithms.graphics.segmentation.Segmentator;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.*;
import org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PanelControls1 extends JPanel {
	private JComboBox comboEdgeDetectionAlgorithm;
	private JComboBox comboEdgeDetectionFuzzyness;
	private JComboBox comboEdgeDetectionStrength;
	private JComboBox comboSegmentationAlgorithm;
	private JComboBox comboSegmentationCoarsenerAlgorithm;
	private JComboBox comboColorReductionAlgorithm;
	private JSpinner spinnerSegmentationMaxSegments;

	public PanelControls1() {
		this.setLayout(new BorderLayout());

		FormLayout lm = new FormLayout("right:pref, 4dlu, fill:pref:grow", "");
		DefaultFormBuilder formBuilder = new DefaultFormBuilder(lm);

		formBuilder.appendSeparator("Edge Detection");
		this.comboEdgeDetectionAlgorithm = new JComboBox();
		this.comboEdgeDetectionFuzzyness = new JComboBox();
		this.comboEdgeDetectionStrength = new JComboBox();
		MapComboBoxModel<String, EdgeDetector> comboEdgeDetectionAlgorithmModel = new MapComboBoxModel<String, EdgeDetector>();
		comboEdgeDetectionAlgorithmModel.addPair("Canny",
				new CannyEdgeDetector());
		this.comboEdgeDetectionAlgorithm
				.setModel(comboEdgeDetectionAlgorithmModel);
		this.comboEdgeDetectionAlgorithm.setRenderer(new MapComboBoxRenderer(
				this.comboEdgeDetectionAlgorithm));
		MapComboBoxModel<String, EdgeDetector.EdgeFuzzyness> comboEdgeDetectionFuzzynessModel = new MapComboBoxModel<String, EdgeDetector.EdgeFuzzyness>();
		comboEdgeDetectionFuzzynessModel.addPair("Fuzzy",
				EdgeDetector.EdgeFuzzyness.fuzzy);
		comboEdgeDetectionFuzzynessModel.addPair("Exact",
				EdgeDetector.EdgeFuzzyness.exact);
		this.comboEdgeDetectionFuzzyness
				.setModel(comboEdgeDetectionFuzzynessModel);
		this.comboEdgeDetectionFuzzyness.setRenderer(new MapComboBoxRenderer(
				this.comboEdgeDetectionFuzzyness));
		MapComboBoxModel<String, EdgeDetector.EdgeStrength> comboEdgeDetectionStrengthModel = new MapComboBoxModel<String, EdgeDetector.EdgeStrength>();
		comboEdgeDetectionStrengthModel.addPair("Soft",
				EdgeDetector.EdgeStrength.soft);
		comboEdgeDetectionStrengthModel.addPair("Medium",
				EdgeDetector.EdgeStrength.medium);
		comboEdgeDetectionStrengthModel.addPair("Strong",
				EdgeDetector.EdgeStrength.strong);
		comboEdgeDetectionStrengthModel.addPair("Very strong",
				EdgeDetector.EdgeStrength.veryStrong);
		this.comboEdgeDetectionStrength
				.setModel(comboEdgeDetectionStrengthModel);
		this.comboEdgeDetectionStrength.setRenderer(new MapComboBoxRenderer(
				this.comboEdgeDetectionStrength));
		this.comboEdgeDetectionStrength.setSelectedIndex(1);

		formBuilder.append("Algorithm", this.comboEdgeDetectionAlgorithm);
		formBuilder.append("Fuzzyness", this.comboEdgeDetectionFuzzyness);
		formBuilder.append("Strength", this.comboEdgeDetectionStrength);

		formBuilder.appendSeparator("Segmentation");
		this.comboSegmentationAlgorithm = new JComboBox();
		this.spinnerSegmentationMaxSegments = new JSpinner();
		this.comboSegmentationCoarsenerAlgorithm = new JComboBox();
		formBuilder.append("Algorithm", this.comboSegmentationAlgorithm);
		formBuilder.append("Max segments", this.spinnerSegmentationMaxSegments);
		formBuilder.append("Coarsener Algorithm",
				this.comboSegmentationCoarsenerAlgorithm);
		MapComboBoxModel<String, Segmentator> comboSegmentationAlgorithmModel = new MapComboBoxModel<String, Segmentator>();
		comboSegmentationAlgorithmModel.addPair("AMG", new AMGSegmentator());
		this.comboSegmentationAlgorithm
				.setModel(comboSegmentationAlgorithmModel);
		this.comboSegmentationAlgorithm.setRenderer(new MapComboBoxRenderer(
				this.comboSegmentationAlgorithm));
		MapComboBoxModel<String, Coarsener> comboSegmentationCoarsenerAlgorithmModel = new MapComboBoxModel<String, Coarsener>();
		comboSegmentationCoarsenerAlgorithmModel.addPair("Simple",
				new SimpleCoarsener());
		comboSegmentationCoarsenerAlgorithmModel.addPair("Brightness",
				new BrightnessCoarsener());
		comboSegmentationCoarsenerAlgorithmModel.addPair("Texture",
				new TextureCoarsener());
		comboSegmentationCoarsenerAlgorithmModel.addPair("Aggregate",
				new AggregateCoarsener());
		this.comboSegmentationCoarsenerAlgorithm
				.setModel(comboSegmentationCoarsenerAlgorithmModel);
		this.comboSegmentationCoarsenerAlgorithm
				.setRenderer(new MapComboBoxRenderer(
						this.comboSegmentationCoarsenerAlgorithm));
		comboSegmentationCoarsenerAlgorithm.setSelectedIndex(2);
		SpinnerNumberModel segmentationMaxSegmentsModel = new SpinnerNumberModel();
		segmentationMaxSegmentsModel.setMinimum(3);
		segmentationMaxSegmentsModel.setMaximum(100);
		segmentationMaxSegmentsModel.setValue(5);
		this.spinnerSegmentationMaxSegments
				.setModel(segmentationMaxSegmentsModel);

		formBuilder.appendSeparator("Color Reduction");
		this.comboColorReductionAlgorithm = new JComboBox();
		formBuilder.append("Algorithm", this.comboColorReductionAlgorithm);
		MapComboBoxModel<String, ColorReductor> comboColorReductionAlgorithmModel = new MapComboBoxModel<String, ColorReductor>();
		comboColorReductionAlgorithmModel.addPair("Median Cut",
				new MedianCutColorReductor());
		this.comboColorReductionAlgorithm
				.setModel(comboColorReductionAlgorithmModel);
		this.comboColorReductionAlgorithm.setRenderer(new MapComboBoxRenderer(
				this.comboColorReductionAlgorithm));

		this.add(formBuilder.getPanel(), BorderLayout.CENTER);
	}

	public EdgeDetector getEdgeDetector() {
		MapComboBoxModel<String, EdgeDetector> model = (MapComboBoxModel<String, EdgeDetector>) this.comboEdgeDetectionAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public EdgeDetector.EdgeFuzzyness getEdgeDetectionFuzzyness() {
		MapComboBoxModel<String, EdgeDetector.EdgeFuzzyness> model = (MapComboBoxModel<String, EdgeDetector.EdgeFuzzyness>) this.comboEdgeDetectionFuzzyness
				.getModel();
		return model.getSelectedItem().value;
	}

	public EdgeDetector.EdgeStrength getEdgeDetectionStrength() {
		MapComboBoxModel<String, EdgeDetector.EdgeStrength> model = (MapComboBoxModel<String, EdgeDetector.EdgeStrength>) this.comboEdgeDetectionStrength
				.getModel();
		return model.getSelectedItem().value;
	}

	public Segmentator getSegmentator() {
		MapComboBoxModel<String, Segmentator> model = (MapComboBoxModel<String, Segmentator>) this.comboSegmentationAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public Coarsener getSegmentationCoarsener() {
		MapComboBoxModel<String, Coarsener> model = (MapComboBoxModel<String, Coarsener>) this.comboSegmentationCoarsenerAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public int getSegmentationMaxSegments() {
		return (Integer) this.spinnerSegmentationMaxSegments.getValue();
	}

	public ColorReductor getColorReductor() {
		MapComboBoxModel<String, ColorReductor> model = (MapComboBoxModel<String, ColorReductor>) this.comboColorReductionAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(new SubstanceRavenGraphiteLookAndFeel());
		JFrame fr = new JFrame();
		fr.setLayout(new BorderLayout());
		fr.add(new PanelControls1());
		fr.pack();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
}
