package com.vizu.identidade.cadastro;

import com.vizu.identidade.onboarding.dto.*;
import com.vizu.identidade.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @PostMapping("/contratantes")
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse create(@Valid @RequestBody OnboardingRequest request) {
        return service.create(request);
    }
}
