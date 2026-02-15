package com.kindergarten.controller;

import com.kindergarten.dto.MessageDto;
import com.kindergarten.dto.SessionDto;
import com.kindergarten.entity.Message;
import com.kindergarten.entity.Session;
import com.kindergarten.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话 API 控制器。
 * 认证未实现前，使用 SessionService.DEFAULT_USER_ID。
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    private long currentUserId() {
        return SessionService.DEFAULT_USER_ID;
    }

    /**
     * 会话列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SessionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var p = sessionService.listSessions(currentUserId(), page, size);
        var dtos = p.map(s -> SessionDto.from(s));
        return ResponseEntity.ok(dtos);
    }

    /**
     * 创建会话
     */
    @PostMapping
    public ResponseEntity<SessionDto> create(@RequestBody CreateSessionRequest req) {
        var s = sessionService.createSession(
                currentUserId(),
                req.title(),
                req.docTypeId()
        );
        return ResponseEntity.ok(SessionDto.from(s));
    }

    /**
     * 会话详情（含消息）
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> get(@PathVariable Long id) {
        var opt = sessionService.getSession(id, currentUserId());
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var s = opt.get();
        var messages = sessionService.getMessages(id, currentUserId()).stream()
                .map(MessageDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(SessionDto.from(s, messages));
    }

    /**
     * 更新会话
     */
    @PutMapping("/{id}")
    public ResponseEntity<SessionDto> update(@PathVariable Long id, @RequestBody UpdateSessionRequest req) {
        var opt = sessionService.updateSession(id, currentUserId(), req.title());
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(SessionDto.from(opt.get()));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!sessionService.deleteSession(id, currentUserId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    public record CreateSessionRequest(
            String title,
            @JsonProperty("doc_type_id") String docTypeId
    ) {}
    public record UpdateSessionRequest(String title) {}
}
