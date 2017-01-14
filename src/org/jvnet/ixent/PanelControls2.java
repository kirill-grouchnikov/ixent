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

import org.jvnet.ixent.algorithms.geometry.delaunay.*;
import org.jvnet.ixent.algorithms.geometry.delaunay.locator.*;
import org.jvnet.ixent.algorithms.graphics.tesselation.*;
import org.jvnet.ixent.algorithms.graphics.turbulence.PerlinTurbulenceGenerator;
import org.jvnet.ixent.algorithms.graphics.turbulence.TurbulenceGenerator;
import org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PanelControls2 extends JPanel {

	private JComboBox comboDisplacementTurbulenceAlgorithm;
	private JComboBox comboTesselationAlgorithm;
	private JComboBox comboTesselationPointLocatorAlgorithm;
	private JComboBox comboTesselationTriangulationAlgorithm;
	private JSpinner spinnerDisplacementMainDirection;
	private JSpinner spinnerDisplacementMaxDisplacement;
	private JSpinner spinnerDisplacementSector;
	private JSpinner spinnerTesselationCellRadius;

	public PanelControls2() {
		this.setLayout(new BorderLayout());

		FormLayout lm = new FormLayout("right:pref, 4dlu, fill:pref:grow", "");
		DefaultFormBuilder formBuilder = new DefaultFormBuilder(lm);

		formBuilder.appendSeparator("Tesselation");
		this.comboTesselationPointLocatorAlgorithm = new JComboBox();
		this.comboTesselationTriangulationAlgorithm = new JComboBox();
		this.spinnerTesselationCellRadius = new JSpinner();
		this.comboTesselationAlgorithm = new JComboBox();
		MapComboBoxModel<String, PointLocator> comboTesselationPointLocatorAlgorithmModel = new MapComboBoxModel<String, PointLocator>();
		comboTesselationPointLocatorAlgorithmModel.addPair("History DAG",
				new PointLocatorHistoryDAG());
		comboTesselationPointLocatorAlgorithmModel.addPair("Local Conflict",
				new PointLocatorLocalConflict());
		comboTesselationPointLocatorAlgorithmModel.addPair("Quad Tree",
				new PointLocatorQuadTree());
		this.comboTesselationPointLocatorAlgorithm
				.setModel(comboTesselationPointLocatorAlgorithmModel);
		this.comboTesselationPointLocatorAlgorithm
				.setRenderer(new MapComboBoxRenderer(
						this.comboTesselationPointLocatorAlgorithm));
		MapComboBoxModel<String, DelaunayManager> comboTesselationTriangulationAlgorithmModel = new MapComboBoxModel<String, DelaunayManager>();
		comboTesselationTriangulationAlgorithmModel.addPair("Edge Flip",
				new DelaunayManagerEdgeFlip());
		comboTesselationTriangulationAlgorithmModel.addPair("Watson",
				new DelaunayManagerWatson());
		this.comboTesselationTriangulationAlgorithm
				.setModel(comboTesselationTriangulationAlgorithmModel);
		this.comboTesselationTriangulationAlgorithm
				.setRenderer(new MapComboBoxRenderer(
						this.comboTesselationTriangulationAlgorithm));
		MapComboBoxModel<String, Tesselator> comboTesselationAlgorithmModel = new MapComboBoxModel<String, Tesselator>();
		comboTesselationAlgorithmModel.addPair("Delaunay",
				new DelaunayTesselator());
		comboTesselationAlgorithmModel
				.addPair("Dither", new DitherTesselator());
		comboTesselationAlgorithmModel.addPair("Voronoi",
				new VoronoiTesselator());
		this.comboTesselationAlgorithm.setModel(comboTesselationAlgorithmModel);
		this.comboTesselationAlgorithm.setRenderer(new MapComboBoxRenderer(
				this.comboTesselationAlgorithm));
		this.comboTesselationAlgorithm.setSelectedIndex(2);
		SpinnerNumberModel tesselationCellRadiusModel = new SpinnerNumberModel();
		tesselationCellRadiusModel.setMinimum(5);
		tesselationCellRadiusModel.setMaximum(30);
		tesselationCellRadiusModel.setValue(10);
		this.spinnerTesselationCellRadius.setModel(tesselationCellRadiusModel);
		formBuilder.append("Algorithm", this.comboTesselationAlgorithm);
		formBuilder.append("Cell radius", this.spinnerTesselationCellRadius);
		formBuilder.append("Triangulation algorithm",
				this.comboTesselationTriangulationAlgorithm);
		formBuilder.append("Point locator algorithm",
				this.comboTesselationPointLocatorAlgorithm);

		formBuilder.appendSeparator("Displacement");
		this.spinnerDisplacementSector = new JSpinner();
		this.spinnerDisplacementMainDirection = new JSpinner();
		this.spinnerDisplacementMaxDisplacement = new JSpinner();
		this.comboDisplacementTurbulenceAlgorithm = new JComboBox();
		MapComboBoxModel<String, TurbulenceGenerator> comboDisplacementTurbulenceAlgorithmModel = new MapComboBoxModel<String, TurbulenceGenerator>();
		comboDisplacementTurbulenceAlgorithmModel.addPair("Perlin",
				new PerlinTurbulenceGenerator());
		this.comboDisplacementTurbulenceAlgorithm
				.setModel(comboDisplacementTurbulenceAlgorithmModel);
		this.comboDisplacementTurbulenceAlgorithm
				.setRenderer(new MapComboBoxRenderer(
						this.comboDisplacementTurbulenceAlgorithm));
		SpinnerNumberModel displacementMaxDisplacementModel = new SpinnerNumberModel();
		displacementMaxDisplacementModel.setMinimum(0);
		displacementMaxDisplacementModel.setMaximum(10);
		displacementMaxDisplacementModel.setValue(1);
		this.spinnerDisplacementMaxDisplacement
				.setModel(displacementMaxDisplacementModel);

		SpinnerNumberModel displacementMainDirectionModel = new SpinnerNumberModel();
		displacementMainDirectionModel.setMinimum(0);
		displacementMainDirectionModel.setMaximum(360);
		displacementMainDirectionModel.setValue(45);
		this.spinnerDisplacementMainDirection
				.setModel(displacementMainDirectionModel);

		SpinnerNumberModel displacementSectorModel = new SpinnerNumberModel();
		displacementSectorModel.setMinimum(0);
		displacementSectorModel.setMaximum(40);
		displacementSectorModel.setValue(10);
		this.spinnerDisplacementSector.setModel(displacementSectorModel);

		formBuilder.append("Turbulence algorithm",
				this.comboDisplacementTurbulenceAlgorithm);
		formBuilder.append("Max displacement",
				this.spinnerDisplacementMaxDisplacement);
		formBuilder.append("Main direction",
				this.spinnerDisplacementMainDirection);
		formBuilder.append("Sector", this.spinnerDisplacementSector);

		this.add(formBuilder.getPanel(), BorderLayout.CENTER);
	}

	public Tesselator getTesselator() {
		MapComboBoxModel<String, Tesselator> model = (MapComboBoxModel<String, Tesselator>) this.comboTesselationAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public PointLocator getTesselationPointLocator() {
		MapComboBoxModel<String, PointLocator> model = (MapComboBoxModel<String, PointLocator>) this.comboTesselationPointLocatorAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public DelaunayManager getTesselationTriangulator() {
		MapComboBoxModel<String, DelaunayManager> model = (MapComboBoxModel<String, DelaunayManager>) this.comboTesselationTriangulationAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public TurbulenceGenerator getTurbulenceGenerator() {
		MapComboBoxModel<String, TurbulenceGenerator> model = (MapComboBoxModel<String, TurbulenceGenerator>) this.comboDisplacementTurbulenceAlgorithm
				.getModel();
		return model.getSelectedItem().value;
	}

	public int getTesselationCellRadius() {
		return (Integer) this.spinnerTesselationCellRadius.getValue();
	}

	public int getDisplacementSector() {
		return (Integer) this.spinnerDisplacementSector.getValue();
	}

	public int getDisplacementMainDirection() {
		return (Integer) this.spinnerDisplacementMainDirection.getValue();
	}

	public int getDisplacementMaxDisplacement() {
		return (Integer) this.spinnerDisplacementMaxDisplacement.getValue();
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(new SubstanceRavenGraphiteLookAndFeel());
		JFrame fr = new JFrame();
		fr.setLayout(new BorderLayout());
		fr.add(new PanelControls2());
		fr.pack();
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
}
