package br.unifor.mia.sds.interfacemanager.integrityconstraint;

public class IdException extends IntegrityConstraintException {

	private static final long serialVersionUID = 4348220789504045012L;

	public IdException(Throwable cause) {
		super(cause);
	}
	
	public IdException(String propertyName) {
		super("<SERIN>Violção de integridade: campo \'" + propertyName + "\' é chave-primária (obrigatório e único).</SERIN>");
	}
}
