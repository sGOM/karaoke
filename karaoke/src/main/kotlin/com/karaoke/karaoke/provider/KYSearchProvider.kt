package com.karaoke.karaoke.provider

import com.karaoke.karaoke.SongDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KYSearchProvider(
    @Qualifier("kyRestClient") private val restClient: RestClient,
    private val logger: Logger,
) : SearchProvider {

    override val providerName: String = "KY"

    override fun search(keyword: String): List<SongDto> {
        return try {
            val responseBody = restClient.get()
                .uri { builder ->
                    builder.queryParam("category", 7) // 1:곡제목, 2:가수, 7:통합검색
                        .queryParam("keyword", keyword)
                        .queryParam("s_page", 1)
                        .build()
                }
                .retrieve()
                .body(String::class.java)

            if (responseBody.isNullOrBlank()) {
                logger.warn("금영(KY) 응답 본문이 비어있습니다.")
                return emptyList()
            }

            parse(responseBody)
        } catch (e: Exception) {
            logger.error("금영(KY) 노래 검색 중 오류 발생: 키워드='{}', 원인: {}", keyword, e.message, e)
            emptyList()
        }
    }

    private fun parse(htmlBody: String): List<SongDto> {
        if (htmlBody.isBlank()) {
            logger.warn("파싱할 HTML 본문이 비어있습니다.")
            return emptyList()
        }
        val doc: Document = Jsoup.parse(htmlBody)

        // [수정됨] "검색 결과가 없습니다" 메시지 확인 (두 가지 케이스 모두 처리)
        if (doc.selectFirst("div.search_result_empty") != null || doc.selectFirst("p.no_results") != null) {
            logger.info("금영(KY) 검색 결과 없음 메시지 확인됨.")
            return emptyList()
        }

        val songListItems =
            doc.select("div.search_daily_chart_wrap > ul.search_chart_list:has(input[name=\"songseq[]\"])")

        if (songListItems.isEmpty()) {
            logger.info("금영(KY) 노래 목록을 찾지 못했습니다. HTML 구조가 변경되었거나, 다른 형태의 '결과 없음' 페이지일 수 있습니다.")
            return emptyList()
        }

        return songListItems.mapNotNull { songItem ->
            val songId = songItem.selectFirst("li.search_chart_num")?.text()?.trim()
            val title = songItem.selectFirst("li.search_chart_tit > span.tit")?.text()?.trim()

            if (songId.isNullOrBlank() || title.isNullOrBlank()) {
                var logMsg = "금영(KY) 파싱 데이터 중 필수 정보 누락:"
                if (songId.isNullOrBlank()) logMsg += " songId 비어있음."
                if (title.isNullOrBlank()) logMsg += " title 비어있음."
                logger.warn(logMsg + " 해당 항목 건너뜀. HTML 부분: ${songItem.html().take(150)}...")
                null
            } else {
                SongDto(
                    songId = songId,
                    title = title,
                    singer = songItem.selectFirst("li.search_chart_sng")?.text()?.trim() ?: "",
                    composer = songItem.selectFirst("li.search_chart_cmp")?.text()?.trim() ?: "",
                    lyricist = songItem.selectFirst("li.search_chart_wrt")?.text()?.trim() ?: ""
                )
            }
        }
    }
}
