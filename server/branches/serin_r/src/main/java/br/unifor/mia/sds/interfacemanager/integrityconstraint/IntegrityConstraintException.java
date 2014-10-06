package br.unifor.mia.sds.interfacemanager.integrityconstraint;

import br.unifor.mia.sds.interfacemanager.SERINException;

public class IntegrityConstraintException extends SERINException {

	private static final long serialVersionUID = 2477436523117704148L;

	public IntegrityConstraintException() {
		super("<SERIN>Violação de integridade!</SERIN>");
	}

	public IntegrityConstraintException(String msg) {
		super(msg);
	}
	
	public IntegrityConstraintException(Throwable cause) {
		super(cause);
	}
}
