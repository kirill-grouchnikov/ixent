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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jvnet.ixent.algorithms.graphics.engine.npr.MosaicEngine;
import org.jvnet.ixent.algorithms.graphics.engine.npr.WatercolorEngine;
import org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PanelWeights extends javax.swing.JPanel {
	private JSlider sliderStructureSegmentation;
	private JSlider sliderStructureEdgeDetection;
	private JSlider sliderStructureDisplacement;
	private JSlider sliderCorrectionStructureGradient;
	private JSlider sliderCorrectionDisplacement;
	private JSlider sliderEngineEdgeDetection;
	private JSlider sliderEngineSegmentation;
	private JSlider sliderEngineTesselation;
	private JSlider sliderEngineStructureVicinity;
	private JSlider sliderEngineStructureGradient;
	private JSlider sliderEngineDisplacement;

	public PanelWeights() {
		this.setLayout(new BorderLayout());

		FormLayout lm = new FormLayout("right:pref, 4dlu, fill:pref:grow", "");
		DefaultFormBuilder formBuilder = new DefaultFormBuilder(lm);

		this.comboNprEffect = new JComboBox();
		MapComboBoxModel<String, Class> comboNprEffectModel = new MapComboBoxModel<String, Class>();
		comboNprEffectModel.addPair("Mosaic", MosaicEngine.class);
		comboNprEffectModel.addPair("Watercolor", WatercolorEngine.class);
		this.comboNprEffect.setModel(comboNprEffectModel);
		this.comboNprEffect.setRenderer(new MapComboBoxRenderer(this.comboNprEffect));
		this.comboNprEffect.setSelectedIndex(0);
		formBuilder.append("NPR effect", this.comboNprEffect);

		formBuilder.appendSeparator("Weights for structure");
		this.sliderStructureSegmentation = new JSlider(0, 100, 0);
		this.sliderStructureSegmentation
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						sliderStructureEdgeDetection
								.setValue(100 - sliderStructureSegmentation
										.getValue());
					}
				});
		formBuilder.append("Segmentation", this.sliderStructureSegmentation);
		this.sliderStructureEdgeDetection = new JSlider(0, 100, 100);
		this.sliderStructureEdgeDetection
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						sliderStructureEdgeDetection
								.setValue(100 - sliderStructureSegmentation
										.getValue());
					}
				});
		formBuilder.append("Edge detection", this.sliderStructureEdgeDetection);
		this.sliderStructureDisplacement = new JSlider(0, 100, 100);
		formBuilder.append("Displacement", this.sliderStructureDisplacement);

		formBuilder.appendSeparator("Weights for correction");
		this.sliderCorrectionStructureGradient = new JSlider(0, 100, 10);
		formBuilder.append("Structure gradient",
				this.sliderCorrectionStructureGradient);
		this.sliderCorrectionDisplacement = new JSlider(0, 100, 0);
		formBuilder.append("Displacement", this.sliderCorrectionDisplacement);

		formBuilder.appendSeparator("Weights for NPR engine");
		this.sliderEngineEdgeDetection = new JSlider(0, 100, 80);
		formBuilder.append("Edge detection", this.sliderEngineEdgeDetection);
		this.sliderEngineSegmentation = new JSlider(0, 100, 0);
		formBuilder.append("Segmentation", this.sliderEngineSegmentation);
		this.sliderEngineTesselation = new JSlider(0, 100, 100);
		formBuilder.append("Tesselation", this.sliderEngineTesselation);
		this.sliderEngineStructureVicinity = new JSlider(0, 100, 100);
		formBuilder.append("Structure vicinity",
				this.sliderEngineStructureVicinity);
		this.sliderEngineStructureGradient = new JSlider(0, 100, 80);
		formBuilder.append("Structure gradient",
				this.sliderEngineStructureGradient);
		this.sliderEngineDisplacement = new JSlider(0, 100, 100);
		formBuilder.append("Displacement", this.sliderEngineDisplacement);

		this.add(formBuilder.getPanel(), BorderLayout.CENTER);
	}

	public Class<?> getNprEngineClass() {
		MapComboBoxModel<String, Class<?>> model = (MapComboBoxModel<String, Class<?>>) this.comboNprEffect
				.getModel();
		return model.getSelectedItem().value;
	}

	private JComboBox comboNprEffect;

	public double getCorrectionDisplacement() {
		return (double) this.sliderCorrectionDisplacement.getValue()
				/ (double) this.sliderCorrectionDisplacement.getMaximum();
	}

	public double getCorrectionStructureGradient() {
		return (double) this.sliderCorrectionStructureGradient.getValue()
				/ (double) this.sliderCorrectionStructureGradient.getMaximum();
	}

	public double getEngineDisplacement() {
		return (double) this.sliderEngineDisplacement.getValue()
				/ (double) this.sliderEngineDisplacement.getMaximum();
	}

	public double getEngineEdgeDetection() {
		return (double) this.sliderEngineEdgeDetection.getValue()
				/ (double) this.sliderEngineEdgeDetection.getMaximum();
	}

	public double getEngineSegmentation() {
		return (double) this.sliderEngineSegmentation.getValue()
				/ (double) this.sliderEngineSegmentation.getMaximum();
	}

	public double getEngineStructureGradient() {
		return (double) this.sliderEngineStructureGradient.getValue()
				/ (double) this.sliderEngineStructureGradient.getMaximum();
	}

	public double getEngineStructureVicinity() {
		return (double) this.sliderEngineStructureVicinity.getValue()
				/ (double) this.sliderEngineStructureVicinity.getMaximum();
	}

	public double getEngineTesselation() {
		return (double) this.sliderEngineTesselation.getValue()
				/ (double) this.sliderEngineTesselation.getMaximum();
	}

	public double getStructureDisplacement() {
		return (double) this.sliderStructureDisplacement.getValue()
				/ (double) this.sliderStructureDisplacement.getMaximum();
	}

	public double getStructureEdgeDetection() {
		return (double) this.sliderStructureEdgeDetection.getValue()
				/ (double) this.sliderStructureEdgeDetection.getMaximum();
	}

	public double getStructureSegmentation() {
		return (double) this.sliderStructureSegmentation.getValue()
				/ (double) this.sliderStructureSegmentation.getMaximum();
	}
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(new SubstanceRavenGraphiteLookAndFeel());
		JFrame fr = new JFrame();
		fr.setLayout(new BorderLayout());
		fr.add(new PanelWeights());
		fr.pack();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
}
