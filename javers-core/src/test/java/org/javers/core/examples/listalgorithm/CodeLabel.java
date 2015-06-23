package org.javers.core.examples.listalgorithm;

import org.javers.core.metamodel.annotation.Id;

public class CodeLabel {

	@Id
	String code;
	String label;
	
	public CodeLabel(String code, String label) {
		this.code = code;
		this.label = label;
	}
	
	@Override
	public String toString() {
		return code+"/"+label;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CodeLabel)) {
			return false;
		}
		CodeLabel other = (CodeLabel) obj;
		return code.equals(other.code) && label.equals(other.label);
	}
	
}
