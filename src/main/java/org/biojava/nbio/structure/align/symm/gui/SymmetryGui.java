package org.biojava.nbio.structure.align.symm.gui;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.ce.AbstractUserArgumentProcessor;
import org.biojava.nbio.structure.align.gui.AlignmentCalculationRunnable;
import org.biojava.nbio.structure.align.gui.ParameterGUI;
import org.biojava.nbio.structure.align.gui.SelectPDBPanel;
import org.biojava.nbio.structure.align.symm.CeSymm;
import org.biojava.nbio.structure.align.util.ResourceManager;
import org.biojava.nbio.structure.align.webstart.AligUIManager;
import org.biojava.nbio.structure.gui.util.PDBUploadPanel;
import org.biojava.nbio.structure.gui.util.ScopSelectPanel;
import org.biojava.nbio.structure.gui.util.StructurePairSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;

/** A JFrame that allows to trigger a symmetry analysis, either from files in a directory,
 * 	or after manual upload. 
 * 	Adapted from the biojava AlignmentGui class.
 *
 * @author lafita
 *
 */
public class SymmetryGui extends JFrame {

	private final static long serialVersionUID =0l;

	private static final Logger logger = LoggerFactory.getLogger(SymmetryGui.class);

	StructureAlignment algorithm;

	JButton abortB;

	SelectPDBPanel  tab1 ;
	PDBUploadPanel  tab2;
	ScopSelectPanel tab3;

	Thread thread;
	AlignmentCalculationRunnable alicalc;
	JTabbedPane masterPane;
	JTabbedPane tabPane;
	JProgressBar progress;

	public static void main(String[] args){
		
		SymmetryGui.getInstance();

	}

	static final ResourceManager resourceManager = ResourceManager.getResourceManager("ce");

	private static final String MAIN_TITLE = "CE-Symm: Symmetry Analysis Tool - Main - V." + resourceManager.getString("ce.version");;

	private static final SymmetryGui me = new SymmetryGui();

	public static SymmetryGui getInstance(){
				
		AbstractUserArgumentProcessor.printAboutMe();
		
		AligUIManager.setLookAndFeel();

		if (!  me.isVisible())
			me.setVisible(true);

		if ( ! me.isActive())
			me.requestFocus();


		return me;
	}

	public static SymmetryGui getInstanceNoVisibilityChange(){

		return me;
	}


	private SymmetryGui() {
		super();

		thread = null;

		JMenuBar menu = SymmetryMenu.initAlignmentGUIMenu(this);

		this.setJMenuBar(menu);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setTitle(MAIN_TITLE);

		tab1 = new SelectPDBPanel(false);
		tab2 = new PDBUploadPanel(false);
		tab3 = new ScopSelectPanel(false);

		//setup tabPane
		tabPane = new JTabbedPane();

		tabPane.addTab("Select PDB ID", null, tab1, "Select PDB ID to analyze");

		tabPane.addTab("Domain",null, tab3,"Select domain to analyze.");
		
		tabPane.addTab("Custom file",null, tab2,"Analyze your own file.");

		

		Box hBoxAlgo = setupAlgorithm();

		Box vBox = Box.createVerticalBox();


		//vBox.add(hBoxAlgo);

		vBox.add(tabPane);
		vBox.add(Box.createGlue());

		//vBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		masterPane = new JTabbedPane();

		masterPane.addTab("Symmetry Analysis", vBox);

		//JPanel dir = tab1.getPDBDirPanel(pdbDir);

		Box vBoxMain = Box.createVerticalBox();
		vBoxMain.add(hBoxAlgo);

		// pairwise or db search

		vBoxMain.add(masterPane);

		// algorithm selection

		// PDB install config
		//vBoxMain.add(dir);
		// buttons
		vBoxMain.add(initButtons());

		this.getContentPane().add(vBoxMain);

		//SwingUtilities.updateComponentTreeUI( me);

		this.pack();
		this.setVisible(true);


	}

	private Box setupAlgorithm()
	{
		String[] algorithms = {"JCE-symmetry"};
		updateAlgorithm();

		JLabel algoLabel = new JLabel("Symmetry algorithm: ");

		JComboBox algorithmList = new JComboBox(algorithms);
		algorithmList.setSelectedIndex(0);

		Action actionAlgorithm = new AbstractAction("Algorithm") {
			public static final long serialVersionUID = 0l;
			// This method is called when the button is pressed
			@Override
			public void actionPerformed(ActionEvent evt) {
				JComboBox cb = (JComboBox)evt.getSource();
				String algorithmName = (String)cb.getSelectedItem();
				// Perform action...
				//System.out.println("calc structure alignment");
				updateAlgorithm();
			}
		};

		algorithmList.addActionListener(actionAlgorithm);

		Action paramAction = new AbstractAction("Parameters") {
			public static final long serialVersionUID = 0l;
			// This method is called when the button is pressed
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Perform action...
				//System.out.println("calc structure alignment");
				updateAlgorithm();
				configureParameters();
			}
		};

		JButton parameterButton = new JButton(paramAction);

		Box hBoxAlgo = Box.createHorizontalBox();
		hBoxAlgo.add(Box.createGlue());
		hBoxAlgo.add(algoLabel);      
		hBoxAlgo.add(algorithmList);
		hBoxAlgo.add(Box.createGlue());
		hBoxAlgo.add(parameterButton);
		hBoxAlgo.add(Box.createGlue());
		return hBoxAlgo;
	}

	private Box initButtons(){

		//        Box hBox42 = Box.createHorizontalBox();
		progress =new JProgressBar();
		progress.setIndeterminate(false);
		progress.setMaximumSize(new Dimension(10,100));
		progress.setVisible(false);

		//        hBox42.add(Box.createGlue());
		//        hBox42.add(progress);
		//        hBox42.add(Box.createGlue());
		//        vBox.add(hBox42);
		Action action1 = new AbstractAction("Analyze") {
			public static final long serialVersionUID = 0l;
			// This method is called when the button is pressed
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Perform action...
				//System.out.println("calc structure alignment");
				int selectedIndex = masterPane.getSelectedIndex();
				if (selectedIndex == 0)
					calcAlignment();
				else {
					System.err.println("Unknown TAB: " + selectedIndex);
				}
			}
		};

		JButton submitB = new JButton(action1);

		Action action3 = new AbstractAction("Abort") {
			public static final long serialVersionUID = 0l;
			// This method is called when the button is pressed
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Perform action...
				abortCalc();
			}
		};

		abortB = new JButton(action3);

		abortB.setEnabled(false);

		Action action2 = new AbstractAction("Exit") {
			public static final long serialVersionUID = 0l;
			// This method is called when the button is pressed
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Perform action...
				abortCalc();
				dispose();
				System.exit(0);
			}
		};
		
		JButton closeB = new JButton(action2);
		Box hBox = Box.createHorizontalBox();
		hBox.add(closeB);
		hBox.add(Box.createGlue());
		hBox.add(progress);
		hBox.add(abortB);
		//hBox.add(Box.createGlue());
		hBox.add(submitB);

		return hBox;
	}

	protected void configureParameters() {
		StructureAlignment algorithm = getStructureAlignment();
		System.out.println("configure parameters for " + algorithm.getAlgorithmName());

		// show a new config GUI
		new ParameterGUI(algorithm);
	}

	public void cleanUp() {

		if ( alicalc != null) {
			alicalc.cleanup();
		}
	}

	private void calcAlignment() {

		int pos = tabPane.getSelectedIndex();
		StructurePairSelector tab = null;

		if (pos == 0 ){
			tab = tab1;         

		} else if (pos == 1){
			tab = tab3;

		} else if (pos == 2){
			tab = tab2;
		}


		try {
			Structure s1 = tab.getStructure1();

			if ( s1 == null) {
				System.err.println("Please select structure");
				return ;
			}
			Structure s2 = s1.clone();

			String name = "custom";

			if  ( pos == 0){
				name = tab1.getName1();
			} else {
				name = s1.getName();
			}
			
			System.out.println("Analyzing: " + name);


			alicalc = new SymmetryCalc(this,s1,s2,name);


			thread = new Thread(alicalc);
			thread.start();
			abortB.setEnabled(true);
			progress.setIndeterminate(true);
			ProgressThreadDrawer drawer = new ProgressThreadDrawer(progress);
			drawer.start();
		} catch (StructureException e){
			JOptionPane.showMessageDialog(null,"Could not align structures. Exception: " + e.getMessage());
		}

	}

	public void notifyCalcFinished(){
		abortB.setEnabled(false);
		thread = null;
		progress.setIndeterminate(false);
		this.repaint();
	}

	private void abortCalc(){
		System.err.println("Interrupting alignment ...");
		if ( alicalc != null )
			alicalc.interrupt();
		notifyCalcFinished();
	}


	public StructureAlignment getStructureAlignment() {
		return algorithm;
	}

	private void updateAlgorithm() {
		//There is only one algorithm for symmetry
		algorithm = new CeSymm();
	}

}

class ProgressThreadDrawer extends Thread {

	JProgressBar progress;
	static int interval = 300;

	public ProgressThreadDrawer(JProgressBar progress) {
		this.progress = progress;
	}


	@Override
	public void run() {
		progress.setVisible(true);
		boolean finished = false;
		while ( ! finished) {
			try {
				progress.repaint();
				if ( ! progress.isIndeterminate() ){
					finished =false;
					break;
				}

				sleep(interval);
			} catch (InterruptedException e){
			}
			progress.repaint();
		}
		progress.setVisible(false);
		progress = null;
	}
}
