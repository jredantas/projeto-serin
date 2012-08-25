package br.unifor.mia.serin.client;

import java.util.List;

import br.unifor.mia.serin.server.URL;
import br.unifor.mia.serin.server.Veiculo;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

//TODO [Remover esse extends Veiculo e analisar se essa classe é realmente necessária, pois ela é muito semelhante a classe SerinClient]
public final class Client extends Veiculo {

	private SerinClient serin;
	
	public Client() {
		m_model.getDocumentManager().addAltEntry(SerinClient.NS, URL.SERIN);
		m_model.read(URL.VEICULO);
		serin = new SerinClient(URL.VEICULO);
	}

	/**
	 * @throws Exception 
	 * @see br.unifor.mia.serin.client.Ontology#post(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Individual post(String about, String marca, String modelo) throws Exception {
		
		Individual individuo = m_model.createIndividual(about, VEICULO);
		individuo.setPropertyValue(MARCA, m_model.createLiteral(marca));
		individuo.setPropertyValue(MODELO, m_model.createLiteral(modelo));
		
		serin.post(individuo);
		
		return individuo;	
	}

	//TODO [Não acessa o serviço REST, arrumar isso]
	public Individual get(String about) {
		
		ExtendedIterator<Individual> itrInd = m_model.listIndividuals();
		
		while (itrInd.hasNext()) {
			Individual ind = itrInd.next();
			if (ind.getURI().equals(about)) {
				return ind;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param ontClass
	 * @return
	 * @throws Exception
	 */
	public List<Individual> list(OntClass ontClass) throws Exception {
		return 	serin.list(ontClass);
	}

	/**
	 * 
	 * @param string
	 * @throws Exception
	 */
	public void delete(String about) throws Exception {
		//TODO [Vr pq o individuo 'logan' não está sendo deletado do servidor]
		serin.delete(get(about));
	}
}
