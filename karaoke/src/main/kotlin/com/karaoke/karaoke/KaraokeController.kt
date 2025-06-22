package com.karaoke.karaoke

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KaraokeController(private val karaokeSearchService: KaraokeSearchService) {

    // 예시: /api/search/tj?keyword=아이유
    @GetMapping("/api/search/{provider}")
    fun search(
        @PathVariable provider: String,
        @RequestParam keyword: String
    ): List<SongDto> {
        return karaokeSearchService.search(provider, keyword)
    }
}
