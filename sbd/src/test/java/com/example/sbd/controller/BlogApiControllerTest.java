package com.example.sbd.controller;

import com.example.sbd.domain.Article;
import com.example.sbd.dto.AddArticleRequest;
import com.example.sbd.dto.UpdateArticleRequest;
import com.example.sbd.repository.BlogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성 및 자동 구성
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스
    // 직렬화 : 객체 -> JSON
    // 역직렬화 : JSON -> 객체


    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach // 테스트 실행 전 실행하는 메서드
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        blogRepository.deleteAll();
    }

    @Test
    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    public void addArticle() throws Exception{

        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);

    }

    @Test
    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    public void findAllArticles() throws Exception{

        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        // builder를 이용해 title과 content를 blogRepository에 Article 형태로 저장한다.
        blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        // ResultActions는 수행된 요청에 대한 결과를 나타내는 객체입니다.
        // mockMvc를 사용해 GET요청을 수행합니다.
        // accept 부분은 요청을 받아들일 수 있는 응답의 미디어 타입을 설정하는 부분이다. JSON 형식의 응답을 받는다.
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk()) // HTTP 상태 코드가 "200 OK" 인지 확인합니다. 즉, 요청이 성공적으로 처리되었는지 확인
                .andExpect(jsonPath("$[0].content").value(content)) // 응답 JSON형식에서 $[0].content 값을 가져와 content 변수의 값과 일치하는지 확인 여기서 $[0]는 첫번째 블로그 글
                .andExpect(jsonPath("$[0].title").value(title));

    }

    @Test
    @DisplayName("findArticle: 블로그 글 조회에 성공한다.")
    public void findArticle() throws Exception{

        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));

    }

    @Test
    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    public void deleteArticle() throws Exception{

        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        mockMvc.perform(delete(url, savedArticle.getId()))
                        .andExpect(status().isOk());

        // then
        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();

    }

    @Test
    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    public void updateArticle() throws Exception{

        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        // when
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);

    }

}