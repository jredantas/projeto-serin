package br.unifor.mia.serin.client.util;

import java.io.IOException;
import java.util.Collection;

import br.unifor.mia.Veiculo;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

/**
 * Classe que auxilia a conversão das classes de ontologia para objetos que
 * podem ser utilizados pelo serviço web
 * 
 * @author bruno muniz (brunoamuniz@gmail.com)
 * @since 06/06/2009
 */
public class OntologyConverter {

	static final String VEICULO_URI = "http://www.unifor.br/veiculo.owl";
	
	/**
	 * Método que converte um objeto em uma instancia de uma ontologia....
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Individual toOntology(Collection<Veiculo> veiculos, OntModel model, Individual individual)
			throws IOException {

		if (veiculos != null && !veiculos.isEmpty()) {
			// verificando se existe mais de um objeto no XML..

			String marca =
					individual.getPropertyValue(
							individual.getOntModel().getProperty(VEICULO_URI + "#marca")).toString();

			for (Veiculo item : veiculos) {
				if (marca.equals(item.getMarca())) {
					individual.setPropertyValue(
							model.getProperty(VEICULO_URI + "#marca"),
							model.createLiteral(item.getMarca()));
					individual.setPropertyValue(
							model.getProperty(VEICULO_URI + "#modelo"),
							model.createLiteral(item.getModelo()));
				}
			}

			return individual;
		}
		return null;
	}

	/**
	 * Returns the Representation of an item.
	 * 
	 * @param item
	 *            the item.
	 * 
	 * @return The Representation of the item.
	 */
	public static Veiculo getRepresentation(Individual individual) {

		OntModel model = individual.getOntModel();
		
		return new Veiculo(individual.getPropertyValue(
				model.getProperty(VEICULO_URI + "#marca")).toString(),
				individual.getPropertyValue(model.getProperty(VEICULO_URI + "#modelo")).toString());
	}
}