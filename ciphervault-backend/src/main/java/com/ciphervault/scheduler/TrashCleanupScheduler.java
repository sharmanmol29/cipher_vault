package com.ciphervault.scheduler;

import com.ciphervault.service.VaultFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrashCleanupScheduler {

	private final VaultFileService vaultFileService;

	public TrashCleanupScheduler(VaultFileService vaultFileService) {
		this.vaultFileService = vaultFileService;
	}

	@Scheduled(cron = "${app.scheduler.trash-cleanup-cron:0 0 3 * * *}")
	public void purgeOldTrash() {
		log.info("Running scheduled trash purge");
		vaultFileService.purgeExpiredTrash();
	}
}
