package br.unifor.mia.sds.interfacemanager.integrityconstraint;

public class ForeignURIException extends IntegrityConstraintException {

	private static final long serialVersionUID = -8213158435584590313L;

	public ForeignURIException(Throwable cause) {
		super(cause);
	}

	public ForeignURIException(String propertyName, String uri) {
		super(
				"<SERIN>Violção de integridade: campo \'"
						+ propertyName
						+ "\' é chave-estrangeira e precisa existir na base de dados. "
						+ "Instância <" + uri + "> não foi localizada na base de dados.</SERIN>");
	}

	public ForeignURIException(String msg) {
		super(msg);
	}
}
