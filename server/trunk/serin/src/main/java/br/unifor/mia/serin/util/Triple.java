package br.unifor.mia.serin.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {"subj", "pred", "obj"})
public final class Triple {

	private String subj, pred, obj;

	public Triple() {}

	public Triple(String subj, String pred, String obj) {
		super();
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
	}

	public String getSubj() {
		return subj;
	}
	
	public String getPred() {
		return pred;
	}

	public String getObj() {
		return obj;
	}
	
	public void setSubj(String subj) {
		this.subj = subj;
	}

	public void setPred(String pred) {
		this.pred = pred;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}
}
