package com.karaoke.karaoke.provider

import com.karaoke.karaoke.SongDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TJSearchProvider(
    @Qualifier("tjRestClient") private val restClient: RestClient,
    private val logger: Logger,
) : SearchProvider {

    override val providerName: String = "TJ"

    override fun search(keyword: String): List<SongDto> {
        return try {
            val responseBody = restClient.get()
                .uri { builder ->
                    builder.queryParam("pageNo", 1)
                        .queryParam("pageRowCnt", 1000)
                        .queryParam("strSotrGubun", "ASC")
                        .queryParam("strSortType", "")
                        .queryParam("nationType", "KOR")
                        .queryParam("strType", 2)
                        .queryParam("searchTxt", keyword)
                        .build()
                }
                .retrieve()
                .body(String::class.java)

            if (responseBody.isNullOrBlank()) {
                logger.warn("TJ미디어 응답 본문이 비어있습니다.")
                return emptyList()
            }

            parse(responseBody)
        } catch (e: Exception) {
            logger.error("TJ미디어 노래 검색 중 오류 발생: 키워드='{}', 원인: {}", keyword, e.message, e)
            emptyList()
        }
    }

    private fun parse(htmlBody: String): List<SongDto> {
        if (htmlBody.isBlank()) {
            logger.warn("파싱할 HTML 본문이 비어있습니다.")
            return emptyList()
        }
        val doc: Document = Jsoup.parse(htmlBody)

        if (doc.selectFirst("div.music-search-list > p.nodata") != null || doc.selectFirst("p.no-date") != null) {
            logger.info("TJ미디어 검색 결과 없음 메시지 확인됨.")
            return emptyList()
        }

        val songDetailUls = doc.select("ul.chart-list-area.music.type-a.type-b > li > ul.grid-container.list.ico")

        if (songDetailUls.isEmpty()) {
            logger.info("TJ미디어 노래 상세 정보 UL을 찾지 못했습니다. HTML 구조가 변경되었거나, 다른 형태의 '결과 없음' 페이지일 수 있습니다.")
            return emptyList()
        }

        return songDetailUls.mapNotNull { songDetailUl ->
            val songId = songDetailUl.selectFirst("li.grid-item.center.pos-type span.num2")?.text()?.trim()
            val title = songDetailUl.selectFirst("li.grid-item.title3 div.flex-box > p > span")?.text()?.trim()

            if (songId.isNullOrBlank() || title.isNullOrBlank()) {
                var logMsg = "TJ미디어 파싱 데이터 중 필수 정보 누락:"
                if (songId.isNullOrBlank()) logMsg += " songId 비어있음."
                if (title.isNullOrBlank()) logMsg += " title 비어있음."
                logger.warn(logMsg + " 해당 항목 건너뜀. HTML 부분: ${songDetailUl.html().take(100)}...")
                null
            } else {
                SongDto(
                    songId = songId,
                    title = title,
                    singer = songDetailUl.selectFirst("li.grid-item.title4.singer p > span")?.text()?.trim() ?: "",
                    composer = songDetailUl.selectFirst("li.grid-item.title6 p > span")?.text()?.trim() ?: "",
                    lyricist = songDetailUl.selectFirst("li.grid-item.title5 p > span")?.text()?.trim() ?: ""
                )
            }
        }
    }
}
