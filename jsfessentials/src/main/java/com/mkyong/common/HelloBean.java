package com.mkyong.common;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import java.io.Serializable;

@ManagedBean
@SessionScoped
public class HelloBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = "Dennis";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSayWelcome() {
		// check if null?
		if ("".equals(name) || name == null) {
			return "";
		} else {
			return "Ajax message : Welcome " + name;
		}
	}
	
	public void doHello(){
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Hello "+name));
	}
	
	
}