package com.sunbeam;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GovportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovportalApplication.class, args);
	}
//	@Bean
//	public ModelMapper modelMapper() {
//		ModelMapper mapper = new ModelMapper(); // creating empty model mapper
//		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT)// prop names n data type must match
//																				// between src n dest
//				.setPropertyCondition(Conditions.isNotNull());// DO NOT transfer nulls from src ->dest
//		return mapper;
//	}

}
