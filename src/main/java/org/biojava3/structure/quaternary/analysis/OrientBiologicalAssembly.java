package org.biojava3.structure.quaternary.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava3.structure.quaternary.core.AxisTransformation;
import org.biojava3.structure.quaternary.core.FindQuarternarySymmetry;
import org.biojava3.structure.quaternary.core.QuatSymmetryParameters;
import org.biojava3.structure.quaternary.core.RotationGroup;
import org.biojava3.structure.quaternary.jmolScript.JmolSymmetryScriptGenerator;

public class OrientBiologicalAssembly {
	public static final int MIN_SEQUENCE_LENGTH = 24;
	public static final double SEQUENCE_IDENTITY_THRESHOLD = 0.30;
	public static final double ALIGNMENT_FRACTION_THRESHOLD = 0.9;
	public static final double RMSD_THRESHOLD = 5.0;

	private String fileName = "";
	private String outputDirectory = "";

	public OrientBiologicalAssembly (String fileName, String outputDirectory) {		
		this.fileName = fileName;
		this.outputDirectory = outputDirectory;
	}

	public static void main(String[] args) {		
		System.out.println("OrientBiologicalAssembly V 0.5: Calculates 4x4 transformation matrix to align structure along highest symmetry axis");
		System.out.println();
		if (args.length != 2) {
			System.out.println("Usage: OrientBiologicalAssembly pdbFile outputDirectory");
			System.exit(-1);
		}
		if (!args[0].contains(".pdb")) {
			System.err.println("Input file must be a .pdb or a pdb.gz file");
			System.exit(-1);
		}
		OrientBiologicalAssembly orienter = new OrientBiologicalAssembly(args[0], args[1]);
		orienter.run();
	}

	public void run() {
		System.out.println("Default parameters:");
		System.out.println("Minimum protein sequence length: " + MIN_SEQUENCE_LENGTH);
		System.out.println("Sequence identity threshold    : " +  Math.round(SEQUENCE_IDENTITY_THRESHOLD*100) + "%");
		System.out.println("Alignment length threshold     : " +  Math.round(ALIGNMENT_FRACTION_THRESHOLD*100) + "%");
		System.out.println("Symmetry RMSD threshold        : " +  RMSD_THRESHOLD);
		System.out.println();

		Structure structure = readStructure();
		System.out.println("Protein chains used for alignment:");
		orient(structure);
	}

	private Structure readStructure() {
		// initialize the PDB_DIR env variable
		AtomCache cache = new AtomCache();
		
		FileParsingParameters p = new FileParsingParameters();
		p.setStoreEmptySeqRes(true);
		p.setLoadChemCompInfo(true);
		p.setAtomCaThreshold(Integer.MAX_VALUE);
		p.setAcceptedAtomNames(new String[]{" CA "});

		PDBFileReader pdbreader = new PDBFileReader();
		pdbreader.setFileParsingParameters(p);

		Structure structure = null;
		try { 
			structure = pdbreader.getStructure(fileName);
			structure.setBiologicalAssembly(isBioassembly());
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		return structure;
	}

	
	public QuatSymmetryParameters getDefaultParameters(){
		QuatSymmetryParameters params = new QuatSymmetryParameters();
		params.setMinimumSequenceLength(MIN_SEQUENCE_LENGTH);
		params.setSequenceIdentityThreshold(SEQUENCE_IDENTITY_THRESHOLD);
		params.setAlignmentFractionThreshold(ALIGNMENT_FRACTION_THRESHOLD);
		params.setRmsdThreshold(RMSD_THRESHOLD);
		
		return params;
	}
	
	private void orient(Structure structure) {	
		
		QuatSymmetryParameters params = getDefaultParameters();
		
		FindQuarternarySymmetry finder = new FindQuarternarySymmetry(structure, params);	

		RotationGroup rotationGroup = new RotationGroup();
		if (finder.getChainCount() > 0) {
			System.out.println();
			rotationGroup = finder.getRotationGroup();
			System.out.println("Results for " + Math.round(SEQUENCE_IDENTITY_THRESHOLD*100) + "% sequence identity threshold:");
			System.out.println("Stoichiometry: " + finder.getCompositionFormula());
			System.out.println("Point group  : " + rotationGroup.getPointGroup());		
			System.out.println("Symmetry RMSD: " + (float) rotationGroup.getAverageTraceRmsd());
		} 
			
			
		System.out.println();
		String prefix = getBaseFileName();
		String bioassemblyId = getBioassemblyId();
		if (! bioassemblyId.isEmpty()) {
		    prefix += "_" + bioassemblyId;
		}
		
		System.out.println("Bioassembly id: " + getBioassemblyId());
		
		AxisTransformation at = new AxisTransformation(finder.getSubunits(), rotationGroup);
		
		String outName = prefix + "_4x4transformation.txt";
		System.out.println("Writing 4x4 transformation to: " + outName);
		writeFile(outName, at.getTransformation().toString());
		
		outName = prefix + "_JmolAnimation.txt";
		System.out.println("Writing Jmol animation to: " + outName);
		writeFile(outName, animate(finder, rotationGroup, at));
		
	}

	private String animate(FindQuarternarySymmetry finder,
			RotationGroup rotationGroup, AxisTransformation at) {

		// use factory method to get point group specific instance of script generator
		JmolSymmetryScriptGenerator script = JmolSymmetryScriptGenerator.getInstance(at, rotationGroup);
		
		// here are some example calls to generate various Jmol scripts
//		System.out.println(script.colorBySubunit());
		System.out.println(script.colorBySequenceCluster());
		System.out.println(script.setDefaultOrientation());
		
		return script.playOrientations();
	}
	
	private String getBioassemblyId() {
		int first = fileName.indexOf(".pdb") + 4;
		int last = fileName.length();
		int index = fileName.indexOf(".gz");
		if (index > 0) {
			last = index;
		}
		return fileName.substring(first, last);
	}
	
	private boolean isBioassembly() {
		return !getBioassemblyId().isEmpty();
	}

	private static void writeFile(String fileName, String text) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		out.println(text);
		out.flush();
		out.close();
	}

	private String getBaseFileName() {
		File f = new File(fileName);
		String name = f.getName();
		name = name.substring(0, name.indexOf('.'));
		Character lastChar = outputDirectory.charAt(outputDirectory.length()-1);
		if (lastChar.equals('/') || lastChar.equals('\\')) {
			return outputDirectory + name;
		} else {
			return outputDirectory + File.separatorChar + name;
		}
	}
}