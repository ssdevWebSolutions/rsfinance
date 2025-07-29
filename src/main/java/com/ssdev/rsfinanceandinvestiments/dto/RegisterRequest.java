package com.ssdev.rsfinanceandinvestiments.dto;


public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    
    
    
    
    
	public RegisterRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RegisterRequest(String name, String email, String password) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
	}
	@Override
	public String toString() {
		return "RegisterRequest [name=" + name + ", email=" + email + ", password=" + password + "]";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}

