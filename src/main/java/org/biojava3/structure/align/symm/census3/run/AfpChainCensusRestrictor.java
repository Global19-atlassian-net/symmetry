package org.biojava3.structure.align.symm.census3.run;

import org.biojava.bio.structure.align.model.AFPChain;

/**
 * A method to determine whether CE-Symm should do additional analysis, such as order-detection.
 * @author dmyersturnbull
 */
public interface AfpChainCensusRestrictor {

	boolean isPossiblySignificant(AFPChain afpChain);
	
}
