package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.service.AwardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
public class AwardController {

    private AwardService awardService;

    public AwardController(final AwardService awardService) {
        this.awardService = awardService;
    }

    @PostMapping("/give-dundie-award/{orgId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AwardResponseDTO giveDundieAward(@PathVariable("orgId") Long orgId) {
        return awardService.processAwardRequestByOrganization(orgId);
    }
}

























