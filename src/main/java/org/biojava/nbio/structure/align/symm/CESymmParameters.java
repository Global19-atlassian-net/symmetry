package org.biojava.nbio.structure.align.symm;

import java.util.List;

import org.biojava.nbio.structure.align.ce.CeParameters;

/**
 * Provides parameters to {@link CeSymm}
 * 
 * @author Spencer Bliven
 * @author Aleix Lafita
 *
 */
public class CESymmParameters extends CeParameters {

	private int maxSymmOrder; //Renamed, old variable maxNrAlternatives (now means max nr. of iterations/order of symmetry)
	private SymmetryType symmetryType;
	private OrderDetectorMethod orderDetectorMethod;
	private RefineMethod refineMethod;
	private boolean optimization;  //true means that optimization is performed after refinement
	private int seed;             //random number generator seed in the Monte Carlo optimization, for reproducibility of results
	
	public static enum OrderDetectorMethod {
		SEQUENCE_FUNCTION;
		public static OrderDetectorMethod DEFAULT = SEQUENCE_FUNCTION;
	}
	
	public static enum RefineMethod {
		NOT_REFINED,
		SINGLE,
		MULTIPLE;
		public static RefineMethod DEFAULT = NOT_REFINED;
	}
	
	/**
	 * The internal symmetry detection can be divided into two types: 
	 * CLOSED: includes the circular and dihedral symmetries, and
	 * NON_CLOSED: includes the helical and protein repeats symmetries.
	 * All internal symmetry cases share one property: all the subunits have the same 3D transformation.
	 * 
	 * AUTO option automatically identifies the type. The criteria is that the closed symmetry generates
	 * CeSymm alignments with circular permutations (2 blocks in AFPChain), whereas the non-closed symmetry
	 * generates alignments without a CP (only one block in AFPChain).
	 */
	public enum SymmetryType {
		CLOSED,
		NON_CLOSED,
		AUTO;
		public static SymmetryType DEFAULT = AUTO;
	}
	
	public CESymmParameters() {
		super();
		maxSymmOrder = 8;
		symmetryType = SymmetryType.DEFAULT;
		refineMethod = RefineMethod.DEFAULT;
		orderDetectorMethod = OrderDetectorMethod.DEFAULT;
		optimization = false;
		seed = 0;
	}

	@Override
	public String toString() {
		return "CESymmParameters [maxSymmOrder=" + maxSymmOrder + ", symmetryType="
				+ symmetryType + ", orderDetectorMethod=" + orderDetectorMethod
				+ ", refineMethod=" + refineMethod + ", optimization="
				+ optimization + ", seed=" + seed + ", winSize=" + winSize
				+ ", rmsdThr=" + rmsdThr + ", rmsdThrJoin=" + rmsdThrJoin
				+ ", maxOptRMSD=" + maxOptRMSD + ", scoringStrategy="
				+ scoringStrategy + ", maxGapSize=" + maxGapSize
				+ ", showAFPRanges=" + showAFPRanges
				+ ", sideChainScoringType=" + sideChainScoringType
				+ ", gapOpen=" + gapOpen + ", gapExtension=" + gapExtension
				+ ", distanceIncrement=" + distanceIncrement + ", oRmsdThr="
				+ oRmsdThr + ", maxNrIterationsForOptimization="
				+ maxNrIterationsForOptimization + ", substitutionMatrix="
				+ substitutionMatrix + ", seqWeight=" + seqWeight + "]";
	}


	@Override
	public void reset(){
		super.reset();
		maxSymmOrder = 8;
		symmetryType = SymmetryType.DEFAULT;
		orderDetectorMethod = OrderDetectorMethod.DEFAULT;
		refineMethod = RefineMethod.DEFAULT;
		optimization = true;
		seed = 0;
	}


	@Override
	public List<String> getUserConfigHelp() {
		List<String> params = super.getUserConfigHelp();
		
		//maxSymmOrder help explanation
		params.add("Sets the maximum order of symmetry of the protein.");
		
		StringBuilder symmTypes = new StringBuilder("Type of Symmetry: ");
		SymmetryType[] vals = SymmetryType.values();
		if(vals.length == 1) {
			symmTypes.append(vals[0].name());
		} else if(vals.length > 1 ) {
			for(int i=0;i<vals.length-1;i++) {
				symmTypes.append(vals[i].name());
				symmTypes.append(", ");
			}
			symmTypes.append("or ");
			symmTypes.append(vals[vals.length-1].name());
		}
		params.add(symmTypes.toString());
		
		StringBuilder orderTypes = new StringBuilder("Order Detection Method: ");
		OrderDetectorMethod[] vals2 = OrderDetectorMethod.values();
		if(vals2.length == 1) {
			orderTypes.append(vals2[0].name());
		} else if(vals2.length > 1 ) {
			for(int i=0;i<vals2.length-1;i++) {
				orderTypes.append(vals2[i].name());
				orderTypes.append(", ");
			}
			orderTypes.append("or ");
			orderTypes.append(vals[vals.length-1].name());
		}
		params.add(orderTypes.toString());
		
		StringBuilder refineTypes = new StringBuilder("Refinement Method: ");
		RefineMethod[] values = RefineMethod.values();
		if(values.length == 1) {
			refineTypes.append(values[0].name());
		} else if(values.length > 1 ) {
			for(int i=0;i<values.length-1;i++) {
				refineTypes.append(values[i].name());
				refineTypes.append(", ");
			}
			refineTypes.append("or ");
			refineTypes.append(values[values.length-1].name());
		}
		params.add(refineTypes.toString());
		
		//optimization help explanation
		params.add("Optimize the refined alignment (true) or do not optimize (false).");
		
		//seed help explanation
		params.add("Random seed for the Monte Carlo optimization, for reproducibility of results.");
		
		return params;
	}

	@Override
	public List<String> getUserConfigParameters() {
		List<String> params = super.getUserConfigParameters();
		params.add("MaxSymmOrder");
		params.add("SymmetryType");
		params.add("OrderDetectorMethod");
		params.add("RefineMethod");
		params.add("Optimization");
		params.add("Seed");
		return params;
	}

	@Override
	public List<String> getUserConfigParameterNames(){
		List<String> params = super.getUserConfigParameterNames();
		params.add("Maximum Order of Symmetry");
		params.add("Type of Symmetry");
		params.add("Order Detection Method");
		params.add("Refinement Method");
		params.add("Optimization");
		params.add("Random Seed");
		return params;
	}

	@SuppressWarnings("rawtypes")
	public List<Class> getUserConfigTypes() {
		List<Class> params = super.getUserConfigTypes();
		params.add(Integer.class);
		params.add(SymmetryType.class);
		params.add(OrderDetectorMethod.class);
		params.add(RefineMethod.class);
		params.add(Boolean.class);
		params.add(Integer.class);
		return params;
	}

	public RefineMethod getRefineMethod() {
		return refineMethod;
	}

	public void setRefineMethod(RefineMethod refineMethod) {
		this.refineMethod = refineMethod;
	}
	
	@Deprecated
	public void setRefineResult(boolean doRefine) {
		if (!doRefine){
			refineMethod = RefineMethod.NOT_REFINED;
		}
		else{
			refineMethod = RefineMethod.DEFAULT;
		}
	}

	public OrderDetectorMethod getOrderDetectorMethod() {
		return orderDetectorMethod;
	}

	public void setOrderDetectorMethod(OrderDetectorMethod orderDetectorMethod) {
		this.orderDetectorMethod = orderDetectorMethod;
	}
	
	public void setMaxSymmOrder(Integer maxSymmOrder) {
		this.maxSymmOrder = maxSymmOrder;
	}

	public int getMaxSymmOrder() {
		return maxSymmOrder;
	}

	public SymmetryType getSymmetryType() {
		return symmetryType;
	}

	public void setSymmetryType(SymmetryType type) {
		this.symmetryType = type;
	}

	public boolean getOptimization() {
		return optimization;
	}

	public void setOptimization(Boolean optimization) {
		this.optimization = optimization;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}
	
}
