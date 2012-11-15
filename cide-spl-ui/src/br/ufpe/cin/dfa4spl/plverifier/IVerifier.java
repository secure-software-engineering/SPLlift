package br.ufpe.cin.dfa4spl.plverifier;

import java.util.Set;

public interface IVerifier {

	boolean isValid(Set<String> configuration);
	
	Set<Set<String>> getValidProducts();

}