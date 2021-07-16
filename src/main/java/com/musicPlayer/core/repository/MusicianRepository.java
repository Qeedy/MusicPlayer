package com.musicPlayer.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicPlayer.core.entity.Musician;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, String> {
	
}
