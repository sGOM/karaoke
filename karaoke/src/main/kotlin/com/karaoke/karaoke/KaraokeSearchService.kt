package com.karaoke.karaoke

import com.karaoke.karaoke.provider.SearchProvider
import org.springframework.stereotype.Service

@Service
class KaraokeSearchService(
    searchProviders: List<SearchProvider>
) {
    private val providerMap: Map<String, SearchProvider> = searchProviders.associateBy { it.providerName.uppercase() }

    /**
     * 지정된 제공업체(provider)를 통해 키워드로 노래를 검색합니다.
     *
     * @param providerName "TJ", "KY" 등 검색할 제공업체 이름
     * @param keyword 검색어
     * @return 검색된 노래 목록
     * @throws IllegalArgumentException 지원하지 않는 제공업체일 경우
     */
    fun search(providerName: String, keyword: String): List<SongDto> {
        val provider = providerMap[providerName.uppercase()]
            ?: throw IllegalArgumentException("지원하지 않는 노래방 브랜드입니다: $providerName")

        return provider.search(keyword)
    }
}
