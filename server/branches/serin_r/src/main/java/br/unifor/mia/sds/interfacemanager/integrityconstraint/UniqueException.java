package br.unifor.mia.sds.interfacemanager.integrityconstraint;

public class UniqueException extends IntegrityConstraintException {

	private static final long serialVersionUID = -9079504002411135630L;

	public UniqueException(String propertyName) {
		super("<SERIN>Violção de integridade: campo \'" + propertyName + "\' é precisa ser único.</SERIN>");
	}
}
