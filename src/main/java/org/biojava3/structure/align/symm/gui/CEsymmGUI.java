/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 2010-01-21
 *
 */
package org.biojava3.structure.align.symm.gui;


import javax.swing.JOptionPane;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.StructureAlignmentFactory;
import org.biojava.bio.structure.align.gui.StructureAlignmentDisplay;
import org.biojava.bio.structure.align.gui.jmol.StructureAlignmentJmol;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.align.util.RotationAxis;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava3.structure.align.symm.CeSymm;

/**
 * Prompts the user for a structure, then displays the CE-Symm symmetry.
 * 
 * Further alignments can be made through the menus, but they will not include
 * rotation axis graphics.
 * 
 * @author Spencer Bliven
 *
 */
public class CEsymmGUI {
	private static final long serialVersionUID = -1124973530190871875L;

	public static void main(String[] args) {
		
		//Add CeSymm to the top of the algorithm list
		StructureAlignment[] algorithms = StructureAlignmentFactory.getAllAlgorithms();
		StructureAlignmentFactory.clearAlgorithms();
		StructureAlignmentFactory.addAlgorithm(new CeSymm());
		for(StructureAlignment alg: algorithms) {
			StructureAlignmentFactory.addAlgorithm(alg);
		}

		//Get the pdb name from the user
		String pdb = null;
		AtomCache cache = new AtomCache();
		Atom[] ca1 = null;
		Atom[] ca2 = null;
		Atom[] caInter = null;
		
		final String default_prompt = "Input PDB ID or domain name.\n\nExamples: 3C1G, 3JUT.A, d1bwua_";
		String prompt=default_prompt;
		while(ca1 == null) {
//			pdb = "1VR8";
//			pdb = "3C1G";
//			pdb = "3JUT.A";
//			pdb = "d1bwua_";
//			pdb = "d2biba1"; // screw symmetry
//			pdb = "d1lghb_"; // negative screw symmetry
//			pdb = "d1v3wa_"; // purely translational
//			pdb = "d1yqha1"; // buggy case
//			pdb = "d1fwka2";
//			pdb = "d1ewfa1";

			pdb = (String)JOptionPane.showInputDialog(
					null,
					prompt,
					"CE-Symm",
					JOptionPane.PLAIN_MESSAGE);
			
			if(pdb == null) return; //User cancel
			else if(pdb.length()==0) continue; // Empty
			
			try {
				ca1 = cache.getAtoms(pdb);
				ca2 = cache.getAtoms(pdb);
				caInter = cache.getAtoms(pdb);
			} catch (Exception e) {
				String error = e.getMessage();
				if(error == null) {
					error = "Error";
				}
				prompt = String.format("%s%n%s", error,default_prompt);
				pdb = null;
			}

		}
		
		// Perform the CESymm alignment
		try {
			StructureAlignment cesymm = StructureAlignmentFactory.getAlgorithm(CeSymm.algorithmName);

			AFPChain afp = cesymm.align(ca1, ca2);
			afp.setName1(pdb);
			afp.setName2(pdb);
			
			Matrix mat = afp.getBlockRotationMatrix()[0];
			for( Atom atom:caInter) {
				Calc.rotate(atom, mat);
			}
			
			RotationAxis axis = new RotationAxis(afp);
			StructureAlignmentJmol jmolPanel = StructureAlignmentDisplay.display(afp, ca1, ca2);
			
			String cmd = axis.getJmolScript(ca1);
			jmolPanel.evalString(cmd);
			
			System.out.println("Theta="+axis.getAngle());
		} catch(StructureException e) {
			e.printStackTrace();
		}


	}
	
	public static void showCurrentAlig(AFPChain myAFP, Atom[] ca1, Atom[] ca2)
			throws StructureException {
		AFPChain c = (AFPChain) myAFP.clone();
		StructureAlignmentJmol jmol = StructureAlignmentDisplay.display(c, ca1,
				ca2);

		// draw a line from center of gravity to N terminus

		ResidueNumber res1 = ca1[0].getGroup().getResidueNumber();
		ResidueNumber res2 = ca2[0].getGroup().getResidueNumber();
		String chainId1 = ca1[0].getGroup().getChain().getChainID();
		String chainId2 = ca2[0].getGroup().getChain().getChainID();

		Atom centroid1 = Calc.getCentroid(ca1);
		Atom centroid2 = Calc.getCentroid(ca2);

		String cs1 = "{" + centroid1.getX() + " " + centroid1.getY() + " "
				+ centroid1.getZ() + "}";
		String cs2 = "{" + centroid2.getX() + " " + centroid2.getY() + " "
				+ centroid2.getZ() + "}";

		jmol.evalString("draw l1 line 100 " + cs1 + " (" + res1.getSeqNum()
				+ ":" + chainId1 + ".CA/1) ; draw l2 line 100 " + cs2 + " ("
				+ res2.getSeqNum() + ":" + chainId2 + ".CA/2);");

	}
	
}