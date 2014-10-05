package br.unifor.mia.sds.interfacemanager.integrityconstraint;

public class NotNullException extends IntegrityConstraintException {

	private static final long serialVersionUID = -7335849679958820520L;

	public NotNullException(String propertyName) {
		super("<SERIN>Violção de integridade: campo \'" + propertyName + "\' é obrigatório.</SERIN>");
	}
	
}
