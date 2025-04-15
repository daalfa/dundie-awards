package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.service.AwardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
public class AwardController {

    private final AwardService awardService;

    public AwardController(AwardService awardService) {
        this.awardService = awardService;
    }

    @PostMapping("/give-dundie-award/{organizationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AwardResponseDTO giveDundieAward(@PathVariable("organizationId") Long organizationId) {
        return awardService.processAwardRequestByOrganization(organizationId);
    }
}

























