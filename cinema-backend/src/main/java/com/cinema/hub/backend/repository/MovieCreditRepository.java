package com.cinema.hub.backend.repository;

import com.cinema.hub.backend.entity.MovieCredit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieCreditRepository extends JpaRepository<MovieCredit, Integer> {
}
