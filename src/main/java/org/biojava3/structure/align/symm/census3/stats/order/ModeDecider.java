package org.biojava3.structure.align.symm.census3.stats.order;

import java.util.Map;

/**
 * A {@link ConsensusDecider} that returns the statistical mode of the orders.
 * @author dmyersturnbull
 */
public class ModeDecider implements ConsensusDecider {

	@Override
	public int decide(Map<Integer,Integer> countsByOrders) {
		int maxCount = 0;
		int maximizingOrder = 0;
		for (int i = 1; i <= 8; i++) {
			Integer count = countsByOrders.get(i);
			if (count == null) count = 0;
			if (count > maxCount) {
				maxCount = count;
				maximizingOrder = i;
			}
		}
		return maximizingOrder;
	}
}
