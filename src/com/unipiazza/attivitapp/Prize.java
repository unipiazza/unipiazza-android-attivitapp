package com.unipiazza.attivitapp;

public class Prize {

	private int id;
	private String name;
	private int coins;
	private String title;

	public Prize(int id, String title, String name, int coins) {
		this.id = id;
		this.name = name;
		this.coins = coins;
		this.title = title;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
