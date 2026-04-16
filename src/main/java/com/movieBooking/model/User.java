package com.movieBooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Marks this class as a database entity.
@Entity
public class User {

	// Primary key for each user record.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private Integer age;
	private String email;
	private String phoneNumber;
	private String marriageStatus;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	// Default constructor required by JPA.
	public User() {
	}

	public User(Long id, String name, String email, String password) {
		this(id, name, null, email, null, null, password);
	}

	public User(Long id, String name, Integer age, String email, String phoneNumber, String password) {
		this(id, name, age, email, phoneNumber, null, password);
	}

	public User(Long id, String name, Integer age, String email, String phoneNumber, String marriageStatus, String password) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.marriageStatus = marriageStatus;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMarriageStatus() {
		return marriageStatus;
	}

	public void setMarriageStatus(String marriageStatus) {
		this.marriageStatus = marriageStatus;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
