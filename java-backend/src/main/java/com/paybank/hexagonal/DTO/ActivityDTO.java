package com.paybank.hexagonal.DTO;

import java.math.BigDecimal;

public record ActivityDTO(
	    String id,
	    String description,
	    BigDecimal amount,
	    String status
	) {}