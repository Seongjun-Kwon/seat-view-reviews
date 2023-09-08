package com.goodseats.seatviewreviews.domain.image.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goodseats.seatviewreviews.domain.image.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

	private final FileStorageService fileStorageService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void rollbackUpload(RollbackUploadEvent rollbackUploadEvent) {
		fileStorageService.delete(
				rollbackUploadEvent.getImage().getImageType().getSubPath(),
				rollbackUploadEvent.getImage().getSavedName()
		);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void rollbackUpload(ImageDeleteEvent imageDeleteEvent) {
		fileStorageService.delete(
				imageDeleteEvent.getImage().getImageType().getSubPath(),
				imageDeleteEvent.getImage().getSavedName()
		);
	}
}
