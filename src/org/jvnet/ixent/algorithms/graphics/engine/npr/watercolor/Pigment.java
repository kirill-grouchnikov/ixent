package org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor;

import java.util.*;

/**
 * An enum class for storing information on pigments. The information here is
 * taken from the following sources:
 *
 * <ul>
 *
 * <li><i>Technical Report: Pigment Selection Using Kubelka–Munk Turbid Media
 * Theory and Non-Negative Least Square Technique</i> by Mahnaz Mohammadi, Mahdi
 * Nezamabadi, Lawrence Taplin and Roy Berns (2004) of <a
 * href="http://www.art-si.org/">Art spectral imaging</a></li>
 *
 * <li>Watercolor pigment tables from <a href="http://www.handprint.com/">Bruce
 * MacEvoy site</a></li>
 *
 * </ul>
 *
 * @author Kirill Grouchnikov
 */
public enum Pigment {
//    ultramarine_blue        ("0.2 0.3 0.75	3 1 2 	16 77 237"),
//    prussian_blue		    ("0.08 0.15 0.36	4 2 3	33 75 97"),
    phthalo_blue		    ("0.08 0.2 0.45	4 0 2	24 99 158"),
//    manganese_blue_hue	    ("0.45 0.75 0.9		1 2 1	42 170 232"),
    cobalt_blue		        ("0.25 0.4 0.8 	2 1 2	94 102 244"),
    viridian			    ("0.25 0.46 0.4		2 3 2	94 178 175"),
//    phthalo_green		    ("0.05 0.25 0.13 3 0 2	0 48 16"),
//    chromium_oxide_green	("0.1 0.25 0.16	3 0 2	0 80 0"),
//    cobalt_green		    ("0.26 0.35 0.35	3 2 2	98 144 154"),
    cadmium_red_light		("0.84 0.17 0.17	4 1 1	243 34 40"),
    cadmium_red_medium	    ("0.62 0.05 0.05	4 1 2	255 0 0"),
//    hansa_yellow_medium	    ("0.85 0.82 0.16 		3 0 2	245 228 99"),
//    cadmium_yellow_light	("0.86 0.83 0.16 		3 0 2	247 251 157"),
    cadmium_yellow_medium	("0.84 0.75 0.16 	3 0 2	251 249 1"),
    indian_yellow		    ("0.8 0.41 0.08		2 1 1	255 87 0"),
    quinacridone_red		("0.62 0.18 0.35	3 0 2	248 103 92"),
    dioxazine_purple		("0.18 0.16 0.45 		4 0 3	82 52 91"),
//    cobalt_violet		    ("0.25 0.26 0.6	0 3 1	244 121 230"),
//    venetian_red		    ("0.32 0.1 0.12	4 0 2	211 92 35"),
//    trans_earth_red		    ("0.35 0.1 0.06	 	3 0 1	121 66 94"),
//    indian_red		        ("0.2 0.08 0.08 	3 0 2	174 54 28"),
//    trans_earth_yellow		("0.57 0.37 0.18		3 0 1	174 165 21"),
    yellow_ochre		    ("0.62 0.43 0.24		3 0 3	174 165 21"),
    raw_umber		        ("0.2 0.2 0.2		2 1 1	117 114 62"),
//    burnt_sienna		    ("0.3 0.1 0.1 		3 1 2	211 92 35"),
//    burnt_umber		        ("0.1 0.08 0.08 	4 0 0	89 69 28"),
    raw_sienna		        ("0.4 0.2 0.1		0 0 1	245 228 99"),
    ivory_black		        ("0.2 0.2 0.2		4 1 2	0 0 0"),
//    titanium_white		    ("0.92 0.92 0.92	2 0 2	255 255 255")
    ;

    private final Map<Component, Integer> components;
    private final Map<Component, Double> reflectance;
    private final Map<Component, Double> transmittance;
    private final double density;
    private final double stainingPower;
    private final double granulation;

    public enum Component {
        red, green, blue
    };

    /**
     * Constructor from string. The string must be in the following format
     * (otherwise some exception will be thrown by the scanner):
     *
     * <ul>
     *
     * <li>reflection at wavelength 620nm (red)</li>
     *
     * <li>reflection at wavelength 540nm (green)</li>
     *
     * <li>reflection at wavelength 440nm (blue)</li>
     *
     * <li>staining: 0 (nonstaining) to 4 (heavily staining)</li>
     *
     * <li>granulation: 0 (liquid texture) to 4 (granular)</li>
     *
     * <li>diffusion: 0 (inert) to 4 (very active diffusion)</li>
     *
     * <li>red component of the corresponding Pantone&#174; color (in 0..255
     * range)</li>
     *
     * <li>green component of the corresponding Pantone&#174; color (in 0..255
     * range)</li>
     *
     * <li>blue component of the corresponding Pantone&#174; color (in 0..255
     * range)</li>
     *
     * @param str a string in the above format describing the pigment
     */
    Pigment(String str) {
        Scanner scanner = new Scanner(str);
        double reflR = scanner.nextDouble();
        double reflG = scanner.nextDouble();
        double reflB = scanner.nextDouble();
        int staining = scanner.nextInt();
        int granulation = scanner.nextInt();
        int diffusion = scanner.nextInt();
        int r = scanner.nextInt();
        int g = scanner.nextInt();
        int b = scanner.nextInt();
        this.components = new HashMap<Component, Integer>();
        this.components.put(Component.red, r);
        this.components.put(Component.green, g);
        this.components.put(Component.blue, b);

        this.reflectance = new HashMap<Component, Double>();
        this.transmittance = new HashMap<Component, Double>();

        this.reflectance.put(Component.red, reflR);
        this.transmittance.put(Component.red, 1.0 - reflR);
        this.reflectance.put(Component.green, reflG);
        this.transmittance.put(Component.green, 1.0 - reflG);
        this.reflectance.put(Component.blue, reflB);
        this.transmittance.put(Component.blue, 1.0 - reflB);

        this.density = 0.01 + 0.01 * diffusion;
        this.stainingPower = 1.0 + 2.0 * staining;
        this.granulation = 0.1 + 0.2 * granulation;
    }

    /**
     * Retrieve the specified component strength of this pigment
     *
     * @param component specified component
     * @return its strength (in 0..255 range)
     */
    public int getComponent(Component component) {
        return this.components.get(component);
    }

    /**
     * Retrieve the relative density of this pigment
     *
     * @return relative density of this pigment
     */
    public double getDensity() {
        return this.density;
    }

    /**
     * Retrieve the relative staining power of this pigment
     *
     * @return relative staining power of this pigment
     */
    public double getStainingPower() {
        return this.stainingPower;
    }

    /**
     * Retrieve the relative granulation of this pigment
     *
     * @return relative granulation of this pigment
     */
    public double getGranulation() {
        return this.granulation;
    }

    /**
     * Retrieve the reflectance through a layer of specified thickness at
     * specified wavelength (color component)
     *
     * @param component color component
     * @param thickness layer thickness
     * @return reflectance through this layer
     */
    public double getReflectance(Component component, double thickness) {
        return thickness * this.reflectance.get(component);
    }

    /**
     * Retrieve the transmittance through a layer of specified thickness at
     * specified wavelength (color component)
     *
     * @param component color component
     * @param thickness layer thickness
     * @return transmittance through this layer
     */
    public double getTransmittance(Component component, double thickness) {
        return 1.0 - this.getReflectance(component, thickness);
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Pigment " + this.name());
        sb.append(", rgb (");
        sb.append(this.components.get(Component.red));
        sb.append(", ");
        sb.append(this.components.get(Component.green));
        sb.append(", ");
        sb.append(this.components.get(Component.blue));
        sb.append(")\n");
        sb.append(" unit thickness reflectance (");
        sb.append(this.getReflectance(Component.red, 1.0));
        sb.append(", ");
        sb.append(this.getReflectance(Component.green, 1.0));
        sb.append(", ");
        sb.append(this.getReflectance(Component.blue, 1.0));
        sb.append(")\n");
        sb.append(" unit thickness transmittance (");
        sb.append(this.getTransmittance(Component.red, 1.0));
        sb.append(", ");
        sb.append(this.getTransmittance(Component.green, 1.0));
        sb.append(", ");
        sb.append(this.getTransmittance(Component.blue, 1.0));
        sb.append(")\n");

        return sb.toString();
    }
}
