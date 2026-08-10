package com.paybank.hexagonal.DTO;

import java.util.UUID;

public record DemandePaiementDto(UUID compteClientId, int montantCentimes, String cleIdempotence, String tokenCarte) {

	 public int getMontantCentimes() {
		 return montantCentimes;
	 }
     
	 public String getCleIdempotence() {
		 return cleIdempotence;
	 }
	 
	 
     public String getTokenCarteMokbank() {
    	 return tokenCarte;
     }
	
}
