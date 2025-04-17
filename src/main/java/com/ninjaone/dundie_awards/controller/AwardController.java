package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.AwardResponseDTO;
import com.ninjaone.dundie_awards.service.AwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
        log.info("/give-dundie-award/{} called", organizationId);
        return awardService.processAwardRequestByOrganization(organizationId);
    }
}

























