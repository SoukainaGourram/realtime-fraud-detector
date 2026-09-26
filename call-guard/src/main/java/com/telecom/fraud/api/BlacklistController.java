package com.telecom.fraud.api;

import com.telecom.fraud.model.Blacklist;
import com.telecom.fraud.service.BlacklistService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @GetMapping
    public ResponseEntity<List<Blacklist>> getAll() {
        return ResponseEntity.ok(blacklistService.getAllBlockedNumbers());
    }

    @PostMapping
    public ResponseEntity<Blacklist> blockNumber(@RequestBody BlockRequest req) {
        // En vrai projet, on récupérerait le user via SecurityContext
        String admin = "admin_dashboard"; 
        Blacklist bl = blacklistService.blockNumber(req.getMsisdn(), req.getReason(), admin);
        return ResponseEntity.ok(bl);
    }

    @DeleteMapping("/{msisdn}")
    public ResponseEntity<Void> unblockNumber(@PathVariable String msisdn) {
        blacklistService.unblockNumber(msisdn);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class BlockRequest {
        private String msisdn;
        private String reason;
    }
}
