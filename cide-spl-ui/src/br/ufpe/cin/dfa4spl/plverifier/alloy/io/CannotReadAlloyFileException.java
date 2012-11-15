package br.ufpe.cin.dfa4spl.plverifier.alloy.io;

import java.io.IOException;

import edu.mit.csail.sdg.alloy4.Err;

public class CannotReadAlloyFileException extends IOException {

	private static final long serialVersionUID = 1L;

	public CannotReadAlloyFileException(String message, Err e) {
		super(message, e);
		e.printStackTrace();
	}

}