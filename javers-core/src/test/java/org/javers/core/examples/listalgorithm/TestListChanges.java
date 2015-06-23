package org.javers.core.examples.listalgorithm;

import java.util.ArrayList;
import java.util.List;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.container.ListChange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestListChanges {

	List<CodeLabel> bs1 = new ArrayList<CodeLabel>();
	List<CodeLabel> bs2 = new ArrayList<CodeLabel>();

	Javers javers = JaversBuilder.javers().withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE).build();
	
	@Before
	public void setup() {
		bs1.clear();
		bs2.clear();
		// Prepare 2 lists with 2 modifications: 1 addition and 1 value change
		
		// List 1
		bs1.add(new CodeLabel("ID1", "Value1"));
		bs1.add(new CodeLabel("ID2", "Value2"));
		bs1.add(new CodeLabel("ID3", "Value3"));
		// List 2
		bs2.add(new CodeLabel("ID1", "Value1"));
		bs2.add(new CodeLabel("ID2", "Value3"));		
	}
	
	@Test
	public void bothComparison() {
		// 1) Direct list comparison
		Diff diffDirect = javers.compare(bs1, bs2);
		
		// 2) Container comparison
		ListContainer obj1 = new ListContainer(bs1);
		ListContainer obj2 = new ListContainer(bs2);
		Diff diffWithContainer = javers.compare(obj1, obj2);

		displayInfo(diffDirect);
		displayInfo(diffWithContainer);
		// Expect to have same number of differences
		Assert.assertSame(nbRemoved(diffDirect), nbRemoved(diffWithContainer));
		Assert.assertSame(nbAdded(diffDirect), nbAdded(diffWithContainer));
		Assert.assertSame(count(diffDirect), count(diffWithContainer));
	}
	
	private void displayInfo(Diff diff) {
		System.out.println(diff.changesSummary());
		for (Change ch : diff.getChanges()) {
			System.out.println(ch);
		}
		System.out.println(nbRemoved(diff)+" objects removed /"+
						   nbAdded(diff)+" objects added");
	}
	
	private int count(Diff diff) {
		int cnt = 0;
		for (Change ch : diff.getChanges()) {
			if (ch instanceof ListChange) {
				cnt += ((ListChange)ch).getChanges().size();
			} else {
				cnt++;
			}
		}
		return cnt;
	}
	
	private int nbRemoved(Diff diff) {
		return diff.getChangesByType(ObjectRemoved.class).size();		
	}
	
	private int nbAdded(Diff diff) {
		return diff.getChangesByType(NewObject.class).size();		
	}
}
