package com.musicPlayer.core.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.musicPlayer.core.MusicType;
import com.musicPlayer.core.entity.Musician;
import com.musicPlayer.core.entity.Song;
import com.musicPlayer.core.repository.MusicianRepository;
import com.musicPlayer.core.repository.SongRepository;

@Controller
public class MusicController {
	
	private static final String PATH_FOLDER = "C:\\MusicPlayer";

	@Autowired
	MusicianRepository musicianRepository;
	@Autowired
	SongRepository songRepository;
	
	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/musician")
	public String musicianList(ModelMap model) {
		List<Musician> musicians = musicianRepository.findAll();
		model.put("musicians", musicians);
		return "musician";
	}
	
	@GetMapping("/song")
	public String songList(ModelMap model) {
		List<Song> songs = songRepository.findAll();
		model.put("songs", songs);
		return "song";
	}

	@GetMapping("/addMusician")
	public String addMusician(ModelMap model) {
		Musician musician = new Musician();
		List<String> musicTypeList = Stream.of(MusicType.values()).map(MusicType::name).collect(Collectors.toList());
		model.put("musicTypeList", musicTypeList);
		model.put("musician", musician);
		return "addMusician";
	}

	@PostMapping("/addMusician")
	public String processAddMusician(Map<String, Object> model, @ModelAttribute Musician musician, BindingResult result,
			RedirectAttributes redirect) {
		musicianRepository.saveAndFlush(musician);
		redirect.addFlashAttribute("alertSuccess", "Musician has been added.");

		return "redirect:musician";
	}

	@GetMapping("/addSong")
	public String addSong(ModelMap model) {
		List<Musician> musicians = musicianRepository.findAll();
		model.put("musicians", musicians);
		return "addSong";
	}
	
	@PostMapping("/addSong") 
	public String singleFileUpload(@RequestParam("file") MultipartFile file, 
			@RequestParam("songName") String songName,
			@RequestParam("musicianId") String musicianId,
			RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
			return "redirect:uploadStatus";
		}
		try {
			String songId = UUID.randomUUID().toString();
			byte[] fileBytes = file.getBytes();
			Path rootFolder = Paths.get(PATH_FOLDER);
			if (!Files.exists(rootFolder)) {
				new File(PATH_FOLDER).mkdirs();
			}
			Path pathUpload = Paths.get(PATH_FOLDER + "\\" + songId + ".mp3");
            Files.write(pathUpload, fileBytes);
			Song song = new Song();
			song.setSongId(songId);
			song.setFileMusic(songId);
			song.setSongName(songName);
			Optional<Musician> musician = musicianRepository.findById(musicianId);
			song.setMusician(musician.get());
			song.setReleaseDate(new Date());
			songRepository.saveAndFlush(song);
			redirectAttributes.addFlashAttribute("alertSuccess", "Song has been added.");
			return "redirect:song";
		}catch (IOException e) {
			redirectAttributes.addFlashAttribute("alertSuccess", "Song add failed.");
			return "redirect:addSong";
		}
	}
	
	@GetMapping("/playMusic")
	public String playMusic(ModelMap model, @RequestParam("songId") String songId) {
		Song song = songRepository.findById(songId).get();
		model.put("song", song);
		return "play";
	}
	
	@GetMapping(value = "/getMusic", produces ="audio/mpeg")
	public @ResponseBody byte[] getImage(@RequestParam("songId") String songId) throws IOException {
		Song song = songRepository.findById(songId).get();
		Path pathUpload = Paths.get(PATH_FOLDER + "\\" + song.getFileMusic() + ".mp3");
	    return Files.readAllBytes(pathUpload);
	}
	
}
