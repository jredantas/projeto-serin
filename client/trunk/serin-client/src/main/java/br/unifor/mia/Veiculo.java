package br.unifor.mia;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {"marca", "modelo"})
public class Veiculo {

    private String marca;

    private String modelo;

    public Veiculo() {
		super();
	}
    
	public Veiculo(String marca, String modelo) {
		this();
		this.marca = marca;
		this.modelo = modelo;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}
}
