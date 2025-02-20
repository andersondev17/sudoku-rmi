// src/model/Player.java
package com.sudoku.model;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private boolean isActive;

    public Player(int id) {
        this.id = id;
        this.isActive = false;
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}