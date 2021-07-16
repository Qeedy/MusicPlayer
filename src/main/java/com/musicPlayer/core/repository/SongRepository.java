package com.musicPlayer.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicPlayer.core.entity.Song;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
	
}

