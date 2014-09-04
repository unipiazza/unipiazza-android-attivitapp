package com.unipiazza.attivitapp;

public class Prize {

	private int id;
	private String name;
	private int coins;

	public Prize(int id, String name, int coins) {
		this.id = id;
		this.name = name;
		this.coins = coins;
	}

	public Prize() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

}
