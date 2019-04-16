package com.apmats.rentflix.controller;

import com.apmats.rentflix.service.RentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RentalController {

	@Autowired
	public RentalController(RentService rentService) {
		this.rentService = rentService;
	}

	private final RentService rentService;

	// Simple route expecting a POSTed list of movie IDs to be rented by the user
	@PostMapping(value = "/customer/{customerId}/rent")
	public ResponseEntity<Map<String, Object>> rent(@PathVariable Long customerId, @RequestBody List<Long> filmIds) {
		Map.Entry<Double, List<Long>> rentRes = rentService.rentFilms(customerId, filmIds);
		Map<String, Object> respMap = new HashMap<>();
		respMap.put("total_cost", rentRes.getKey().toString());
		respMap.put("physical_copy_ids", rentRes.getValue());
		return ResponseEntity.ok(respMap);
	}

	// Simple route expecting a POSTed list of physical copy IDs to be returned by
	// the user
	@PostMapping(value = "/customer/{customerId}/return")
	public ResponseEntity<Map<String, Double>> returnFilms(@PathVariable Long customerId,
			@RequestBody List<Long> physicalMediaIds) {
		Double surcharge = rentService.returnMedia(customerId, physicalMediaIds);
		return ResponseEntity.ok(Collections.singletonMap("total_surcharge", surcharge));
	}

}
