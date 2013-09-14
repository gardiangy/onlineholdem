package hu.onlineholdem.entity;

import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
public class Message {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="value")
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	

}
