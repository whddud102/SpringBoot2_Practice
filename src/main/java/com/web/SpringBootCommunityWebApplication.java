package com.web;

import com.web.domain.Board;
import com.web.domain.enums.BoardType;
import com.web.domain.User;
import com.web.repository.BoardRepository;
import com.web.repository.UserRepository;
import com.web.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class SpringBootCommunityWebApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCommunityWebApplication.class, args);
	}

	@Autowired
	private UserArgumentResolver userArgumentResolver;

	// UserArgumentResolver를 ArgumentResolver로 등록
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}

	@Bean
	public CommandLineRunner runner(UserRepository userRepository, BoardRepository boardRepository) throws Exception {
		return (args) -> {
			User user = userRepository.save(User.builder()
			.name("havi")
			.password("test")
			.email("havi@gmail.com")
			.createdDate(LocalDateTime.now())
			.build());

			IntStream.rangeClosed(1,200).forEach(index -> boardRepository.save(Board.builder()
			.title("게시글" + index)
			.subTitle("순서" + index)
			.content("콘텐츠")
			.boardType(BoardType.free)
			.createdDate(LocalDateTime.now())
			.updateDate(LocalDateTime.now()
			).user(user).build()));
		};
	}
}
