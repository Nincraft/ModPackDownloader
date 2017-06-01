package com.nincraft.modpackdownloader.summary;

import com.nincraft.modpackdownloader.container.Mod;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class UpdateCheckSummarizer {

	@Getter
	private static UpdateCheckSummarizer instance = new UpdateCheckSummarizer();

	@Getter
	private List<Mod> modList = new ArrayList<>();

	public void summarize() {
		log.info("Number of updates found: {}", modList.size());
		if (!modList.isEmpty()) {
			log.info("Updatable mod list: ");
			for (Mod mod : modList) {
				log.info(mod.getName());
			}
		}
	}
}
