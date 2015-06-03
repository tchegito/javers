package org.javers.core.examples.listalgorithm;

import java.util.List;

import org.junit.Assert;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BehaviorDifference {

	@Test
	public void compareMethods() {
		A a1 = new A(1);
		A a2 = new A(1);
	
		B b1 = new B(2);
		B b2 = new B(2);
		a1.setAnyList(asList(b1));
		a2.setAnyList(asList(b2));
		
		// 1) create with simple method
		Javers javers = JaversBuilder.javers().build();
		Diff diff1 = javers.compare(a1, a2);
		
		checkNoDiff(diff1);
		
		// 2) create with Levenshtein method
		javers = JaversBuilder.javers().withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE).build();
		Diff diff2 = javers.compare(a1, a2);

		checkNoDiff(diff2);
	}

	public void checkNoDiff(Diff diff) {
		Assert.assertTrue("No changes should have been detected !\n"+diff.changesSummary(), !diff.hasChanges());
	}
	
	@SafeVarargs
	public static <T> List<T> asList(T... objects) {
		List<T> list = Lists.newArrayList();
		for (T t : objects) {
			list.add(t);
		}
		return list;
	}
}
