package com.cinema.hub.backend.service;

import com.cinema.hub.backend.dto.MovieDetailDto;
import com.cinema.hub.backend.entity.Movie;
import com.cinema.hub.backend.entity.MovieCredit;
import com.cinema.hub.backend.entity.MovieGenre;
import com.cinema.hub.backend.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final String STATUS_NOW_SHOWING = "NowShowing";
    private final MovieRepository movieRepository;

    public List<MovieDetailDto> getNowShowingMovies() {
        return movieRepository.findAllByStatusWithGenres(STATUS_NOW_SHOWING)
                .stream()
                .map(this::mapToSummaryDto)
                .toList();
    }

    public MovieDetailDto getMovieDetailById(int movieId) {
        Movie movie = movieRepository.findMovieDetailById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));
        return mapToDetailDto(movie);
    }

    private MovieDetailDto mapToSummaryDto(Movie movie) {
        return MovieDetailDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .ageRating(movie.getAgeRating())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .imdbUrl(movie.getImdbUrl())
                .releaseDate(movie.getReleaseDate())
                .genres(extractGenres(movie))
                .actors(List.of())
                .directors(List.of())
                .build();
    }

    private MovieDetailDto mapToDetailDto(Movie movie) {
        return MovieDetailDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .ageRating(movie.getAgeRating())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .imdbUrl(movie.getImdbUrl())
                .releaseDate(movie.getReleaseDate())
                .genres(extractGenres(movie))
                .actors(extractCredits(movie, "Actor"))
                .directors(extractCredits(movie, "Director"))
                .build();
    }

    private List<String> extractGenres(Movie movie) {
        List<MovieGenre> movieGenres = movie.getMovieGenres();
        if (movieGenres == null) {
            return List.of();
        }

        return movieGenres.stream()
                .map(MovieGenre::getGenre)
                .filter(Objects::nonNull)
                .map(genre -> genre.getName())
                .distinct()
                .toList();
    }

    private List<String> extractCredits(Movie movie, String creditType) {
        List<MovieCredit> credits = movie.getCredits();
        if (credits == null) {
            return List.of();
        }

        Comparator<MovieCredit> comparator = Comparator.comparing(
                MovieCredit::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)
        );

        return credits.stream()
                .filter(credit -> creditType.equalsIgnoreCase(credit.getCreditType()))
                .sorted(comparator)
                .map(MovieCredit::getPerson)
                .filter(Objects::nonNull)
                .map(person -> person.getFullName())
                .distinct()
                .toList();
    }
}
