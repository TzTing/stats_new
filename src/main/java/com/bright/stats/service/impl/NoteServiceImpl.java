package com.bright.stats.service.impl;

import com.bright.stats.repository.primary.NoteRepository;
import com.bright.stats.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author: Tz
 * @Date: 2023/02/04 16:33
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;


}
