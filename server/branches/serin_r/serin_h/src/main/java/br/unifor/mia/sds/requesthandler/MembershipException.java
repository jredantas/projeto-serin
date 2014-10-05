package br.unifor.mia.sds.requesthandler;

public class MembershipException extends SDSException {

	private static final long serialVersionUID = 7535529104095779580L;

	public MembershipException() {
		super("<SDS>RESOURCE NOT MEMBERSHIP OF CLASS</SDS>");
	}

}
