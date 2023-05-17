package com.goodseats.seatviewreviews.domain.image.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goodseats.seatviewreviews.common.file.FileStorageService;

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
}
