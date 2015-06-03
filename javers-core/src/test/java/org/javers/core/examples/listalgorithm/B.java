package org.javers.core.examples.listalgorithm;

import org.javers.core.metamodel.annotation.Id;

public class B {
	@Id
	public int fieldId;
	
	public B(int id) {
		this.fieldId = id;
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}
}
