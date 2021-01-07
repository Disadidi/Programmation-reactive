package com.hdconsulting.springboot.reactor.app.models;

public class Usuario {
	
	private String nombre;
	private String apellido;
	
	public Usuario(String nombreComplete) {
		String[] completeCompleteTab = nombreComplete.split(" ");
		this.nombre = completeCompleteTab[0];//.toUpperCase();
		this.apellido = completeCompleteTab[1];//.toUpperCase();
	}
	
	public Usuario(String nombre, String apellido) {
		this.nombre = nombre;
		this.apellido = apellido;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getApellido() {
		return apellido;
	}
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}
	@Override
	public String toString() {
		StringBuilder toString  = new StringBuilder();
		toString.append("Usuario [ nombre = ")
		.append((getNombre() != null)? getNombre(): "")
		.append(", apellido = ")
		.append((getApellido() != null)? getApellido(): "")
		.append("]");
		
		return toString.toString();
	}
	
	
	
	

}
