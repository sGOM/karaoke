package com.karaoke.karaoke.provider

import com.karaoke.karaoke.SongDto

/**
 * 노래방 브랜드별 검색 기능을 제공하는 Provider의 공통 인터페이스입니다.
 */
interface SearchProvider {
    /**
     * 이 Provider를 식별하는 고유한 이름 (예: "TJ", "KY")
     */
    val providerName: String

    /**
     * 주어진 키워드로 노래를 검색합니다.
     * @param keyword 검색할 키워드
     * @return 검색된 노래 목록
     */
    fun search(keyword: String): List<SongDto>
}
