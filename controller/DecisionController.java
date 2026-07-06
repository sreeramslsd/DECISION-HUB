package com.decisionhub.controller;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller exposing APIs for creating, viewing, searching, and managing decision boards.
 */
@RestController
@RequestMapping("/decisions")
@RequiredArgsConstructor
@Tag(name = "Decision", description = "Decision Board Management Endpoints")
@Slf4j
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping
    @Operation(summary = "Create a new decision board", description = "Creates a decision board aggregate containing options and comparison factors in DRAFT state", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody DecisionRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to create decision board: {}", request.title());
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.createDecision(request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve decision board details", description = "Gets active decision board details by its unique ID (respects public/private visibility)")
    public ResponseEntity<DecisionResponse> getDecisionById(@PathVariable UUID id) {
        log.debug("REST request to get decision board by ID: {}", id);
        DecisionResponse response = decisionService.getDecisionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get list of visible decision boards", description = "Retrieves a paginated list of all active decision boards authorized for the requester")
    public ResponseEntity<PageResponse<DecisionResponse>> getDecisions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        log.debug("REST request to get active decisions list");
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DecisionResponse> response = decisionService.getDecisions(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update decision board metadata", description = "Updates metadata of an existing decision board (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> updateDecision(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update decision board: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.updateDecision(id, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete decision board", description = "Soft-deletes a decision board workspace (requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteDecision(
            @PathVariable UUID id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete decision board: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        decisionService.deleteDecision(id, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Transition decision lifecycle status", description = "Transitions decision lifecycle state (DRAFT -> ACTIVE -> CLOSED, requires creator/owner)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DecisionResponse> transitionStatus(
            @PathVariable UUID id,
            @RequestParam DecisionStatus status,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to transition decision board status: id={}, status={}", id, status);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        DecisionResponse response = decisionService.transitionStatus(id, status, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search visible decision boards", description = "Performs full-text indexing queries matching query string against title, description, and tags")
    public ResponseEntity<PageResponse<DecisionResponse>> searchDecisions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("REST request to search decisions with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DecisionResponse> response = decisionService.searchDecisions(query, pageable);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
