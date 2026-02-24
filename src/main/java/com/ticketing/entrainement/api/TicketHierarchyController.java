package com.ticketing.entrainement.api;

import com.ticketing.entrainement.application.AttachTicketToParentUseCase;
import com.ticketing.entrainement.application.AttachTicketRequest;
import com.ticketing.entrainement.application.DetachTicketFromParentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketHierarchyController {

    private final AttachTicketToParentUseCase attachUseCase;
    private final DetachTicketFromParentUseCase detachUseCase;

    public TicketHierarchyController(AttachTicketToParentUseCase attachUseCase, DetachTicketFromParentUseCase detachUseCase) {
        this.attachUseCase = attachUseCase;
        this.detachUseCase = detachUseCase;
    }

    @PutMapping("/{id}/parent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attachToParent(
            @PathVariable UUID id,
            @RequestBody AttachTicketRequest request
    ) {
        attachUseCase.execute(id, request.parentId());
    }

    @DeleteMapping("/{id}/parent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachFromParent(@PathVariable UUID id) {
        detachUseCase.execute(id);
    }
}