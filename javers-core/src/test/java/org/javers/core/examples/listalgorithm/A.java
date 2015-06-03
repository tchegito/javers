package org.javers.core.examples.listalgorithm;

import java.util.List;

import org.javers.core.metamodel.annotation.Id;


public class A {
	@Id
	public int fieldId;
	
	List<B> anyList;

	public A(int id) {
		fieldId = id;
	}
	
	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public List<B> getAnyList() {
		return anyList;
	}
	
	public void setAnyList(List<B> l) {
		anyList = l;
	}
}
